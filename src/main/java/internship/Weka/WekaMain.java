/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka;

import ChiSquare.Chi;
import LogReg.FilteredLogRegClassifier;
import LogReg.Logistic;
import LogReg.TTest;
import ChiSquare.ChiSquaredAttributeEval;

import internship.Weka.WrapperAndClassifier.BehemothWrapper;
import internship.Weka.WrapperAndClassifier.EnsembleClassifierWeka;
import internship.Weka.WrapperAndClassifier.BehemothClassifier;
import internship.Weka.SplittingAndCombining.FillInUnknown;
import internship.Weka.SplittingAndCombining.MakeDoubles;
import internship.Weka.SplittingAndCombining.RemoveUnwanted;
import internship.Weka.SplittingAndCombining.Splitting;
import internship.Weka.SplittingAndCombining.Steps;
import internship.Weka.WrapperAndClassifier.EnsembleWrapper;
import internship.Weka.WrapperAndClassifier.GeneticSearch;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import org.rosuda.JRI.Rengine;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;

import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Main module doing most of the work. Fast majority of things are settings for
 * the different classifiers & such
 *
 * @author Florian
 */
public class WekaMain implements Serializable {

    public ArrayList<ArrayList<String>> otherFollow;
    public String[][] deathList;
    final static Charset ENCODING = StandardCharsets.UTF_8;
    private String basePath;
    private String followPath;
    private String bioPath;
    private String targetLocation;
    private ArrayList<String> unwanted;
    public ArrayList<Data> sets;
    public ArrayList<Data> testSets;
    public int folds = 10;
    public boolean pruned = false;
    public String classifier = "";
    public String numAtt = "";
    public String minInLeave = "";
    public int numAttributes;
    public int minLeavesRandom;
    public int numTree;
    public int numForestAtt;
    public int numNeighbours;
    public double ridgeFactor;
    private transient Steps steps;
    private boolean ignoreLowAUC = false;
    private boolean ignore = true;
    private boolean all_false = false;
    private boolean proportional_chance = false;
    public FilteringRecombination filterRecombination = FilteringRecombination.intersection;
    public int selected = 20;
    public String hospitalPath;
    public boolean hospitalization;
    public FilterType baseFilter = FilterType.univariate;

    public enum VoteType {

        majority, maxProb, minProb, veto, averageProb;
    }

    public enum FilteringRecombination {

        none, filter, union, random, intersection;
    }

    public enum FilterType {

        univariate, multivariate, indiWrapper, ensembleWrap, averageWrap;
    }
    public VoteType voteType;

    public WekaMain(String basePath, String followPath, String bioPath, String targetLocation, ArrayList<String> unwanted, boolean pruned, String classifier, String numAtt, String minInLeaves, String hospitalPath) {
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
        this.unwanted = unwanted;
        this.pruned = pruned;
        this.classifier = classifier;
        this.numAtt = numAtt;
        this.minInLeave = minInLeaves;
        this.hospitalPath = hospitalPath;
    }

    public WekaMain(String basePath, String followPath, String bioPath, String targetLocation, ArrayList<String> unwanted, String hospitalPath) {
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
        this.unwanted = unwanted;
        this.hospitalPath = hospitalPath;
    }

    //determines the relevant sets for a given ensemble
    public ArrayList<Data> relevantSets(int start, int end) {
        ArrayList<Data> relevantSets = new ArrayList<Data>();
        ensemble = true;
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).start >= start && sets.get(i).end == end) {
                relevantSets.add(sets.get(i));
            }
        }
        return relevantSets;
    }

    /**
     * tests an ensemble using crossvalidation
     *
     * @param start
     * @param end
     * @param veto
     * @param maxFU
     * @param local
     * @return
     * @throws Exception
     */
    public Result testEnsemble(int start, int end, int veto, int maxFU, boolean local) throws Exception {
        ArrayList<Data> relevantSets = new ArrayList<Data>();
        ensemble = true;
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).start >= start && sets.get(i).end == end) {
                relevantSets.add(sets.get(i));
            }
        }
        if (local) {
            makeTrueClassesEnsemble(relevantSets.get(0));
        }
        filterEnsemble(relevantSets);
        if (relevantSets.size() == 1) {
            relevantSets.get(0).crossValidate();
            Result a = new Result(relevantSets.get(0).cM, maxFU, veto, 0);
            for (int i = 0; i < relevantSets.get(0).res.votes.size(); i++) {
                a.votes.add(relevantSets.get(0).res.votes.get(i));
            }
            makeTrueClasses();
            ensemble = false;
            return a;
        } else {

            Result a = kFoldCrossValidation(relevantSets, maxFU, veto, new ArrayList<Data>());
            makeTrueClasses();
            ensemble = false;
            return a;
        }

    }

    /**
     * does crossvalidation on an ensemble
     *
     * @param sets
     * @param maxFU
     * @param veto
     * @param testSets
     * @return
     * @throws Exception
     */
    public Result kFoldCrossValidation(ArrayList<Data> sets, int maxFU, int veto, ArrayList<Data> testSets) throws Exception {
        folds = 10;
        int[][] cM = new int[2][2];
        Result result = new Result();
        long seed = 100;
        Random rand = new Random(seed);
        for (int i = 0; i < folds; i++) {
            ArrayList<Data> trainSets = new ArrayList<Data>();
            testSets = new ArrayList<Data>();
            Instances randData = new Instances(sets.get(0).data);
            randData.randomize(rand);
            Instances train = randData.trainCV(folds, i);
            Instances test = randData.testCV(folds, i);

            trainSets.add(new Data(train, sets.get(0).measurement, sets.get(0).start, sets.get(0).end));
            testSets.add(new Data(test, sets.get(0).measurement, sets.get(0).start, sets.get(0).end));
            for (int j = 1; j < sets.size(); j++) {
                Instances train2 = new Instances(sets.get(j).data);
                Instances test2 = new Instances(sets.get(j).data);

                for (int l = 0; l < train2.numInstances(); l++) {
                    boolean contained = false;
                    for (int k = 0; k < train.numInstances(); k++) {
                        if (train.instance(k).stringValue(0).equals(train2.instance(l).stringValue(0))) {
                            contained = true;
                        }
                    }
                    if (!contained) {
                        train2.delete(l);
                        l = 0;
                    }
                }
                for (int l = 0; l < test2.numInstances(); l++) {
                    boolean contained = false;
                    for (int k = 0; k < test.numInstances(); k++) {
                        if (test.instance(k).stringValue(0).equals(test2.instance(l).stringValue(0))) {
                            contained = true;
                        }
                    }
                    if (!contained) {
                        test2.delete(l);
                        l = 0;
                    }
                }

                trainSets.add(new Data(train2, sets.get(j).measurement, sets.get(j).start, sets.get(j).end));
                testSets.add(new Data(test2, sets.get(j).measurement, sets.get(j).start, sets.get(j).end));
            }

            if (classifier.equals("J48")) {
                buildJ48Classifiers(trainSets);
            } else if (classifier.equals("randomTree")) {
                buildRandomTreeClassifiers(trainSets);
            } else if (classifier.equals("randomForest")) {
                buildRandomForestClassifiers(trainSets);
            } else if (classifier.equals("NN")) {
                buildNNClassifiers(trainSets);
            } else if (classifier.equals("logReg")) {
                buildLogRegClassifiers(trainSets);
            } else if (classifier.equals("naiveBayes")) {
                buildNaiveBayesClassifiers(trainSets);
            } else if (classifier.equals("SVM")) {
                buildSVMClassifiers(trainSets);
            } else if (classifier.equals("AdaBooster")) {
                buildAdaBoosterClassifiers(trainSets);
            }

            int[][] res = testClassifiers(trainSets, veto, maxFU, testSets, result);
            cM[0][0] += res[0][0];
            cM[0][1] += res[0][1];
            cM[1][0] += res[1][0];
            cM[1][1] += res[1][1];

        }
        double[][] cMatrix = new double[2][2];
        cMatrix[0][0] = cM[0][0];
        cMatrix[0][1] = cM[0][1];
        cMatrix[1][0] = cM[1][0];
        cMatrix[1][1] = cM[1][1];
        result.cMatrix = cMatrix;
        result.veto = veto;
        result.measurement = maxFU;
        return result;
    }

    public Result classifyTestSets(ArrayList<Data> sets, int maxFU, int veto, ArrayList<Data> testSets) throws Exception {
        int seed = 100;
        int folds = 10;
        int[][] cM = new int[2][2];

        for (int i = 0; i < folds; i++) {
            ArrayList<Data> trainSets = new ArrayList<Data>();
            testSets = new ArrayList<Data>();
            Instances randData = new Instances(sets.get(0).data);
            Instances train = randData.trainCV(folds, i);
            Instances test = randData.testCV(folds, i);

            trainSets.add(new Data(train, sets.get(0).measurement, sets.get(0).start, sets.get(0).end));
            testSets.add(new Data(test, sets.get(0).measurement, sets.get(0).start, sets.get(0).end));
            for (int j = 1; j < sets.size(); j++) {
                Instances train2 = new Instances(sets.get(j).data);
                Instances test2 = new Instances(sets.get(j).data);

                for (int l = 0; l < train2.numInstances(); l++) {
                    boolean contained = false;
                    for (int k = 0; k < train.numInstances(); k++) {
                        if (train.instance(k).stringValue(0).equals(train2.instance(l).stringValue(0))) {
                            contained = true;
                        }
                    }
                    if (!contained) {
                        train2.delete(l);
                        l = 0;
                    }
                }
                for (int l = 0; l < test2.numInstances(); l++) {
                    boolean contained = false;
                    for (int k = 0; k < test.numInstances(); k++) {
                        if (test.instance(k).stringValue(0).equals(test2.instance(l).stringValue(0))) {
                            contained = true;
                        }
                    }
                    if (!contained) {
                        test2.delete(l);
                        l = 0;
                    }
                }

                trainSets.add(new Data(train2, sets.get(j).measurement, sets.get(j).start, sets.get(j).end));
                testSets.add(new Data(test2, sets.get(j).measurement, sets.get(j).start, sets.get(j).end));
            }

            if (classifier.equals("J48")) {
                buildJ48Classifiers(trainSets);
            } else if (classifier.equals("randomTree")) {
                buildRandomTreeClassifiers(trainSets);
            } else if (classifier.equals("randomForest")) {
                buildRandomForestClassifiers(trainSets);
            } else if (classifier.equals("NN")) {
                buildNNClassifiers(trainSets);
            } else if (classifier.equals("logReg")) {
                buildLogRegClassifiers(trainSets);
            } else if (classifier.equals("naiveBayes")) {
                buildNaiveBayesClassifiers(trainSets);
            } else if (classifier.equals("SVM")) {
                buildSVMClassifiers(trainSets);
            } else if (classifier.equals("AdaBooster")) {
                buildAdaBoosterClassifiers(trainSets);
            }

            int[][] res = testClassifiers(trainSets, veto, maxFU, testSets, new Result());
            cM[0][0] += res[0][0];
            cM[0][1] += res[0][1];
            cM[1][0] += res[1][0];
            cM[1][1] += res[1][1];

        }
        return new Result(cM, veto, maxFU, 0);
    }

    /**
     * classifies an individual within the relevant ensemble
     *
     * @param in
     * @param start of ensemble
     * @param end of ensemble
     * @return
     * @throws Exception
     */
    public double[] classifyIndividual(Instance in, int start, int end) throws Exception {
        int IDindex = -1;
        for (int k = 0; k < in.numAttributes(); k++) {
            if (in.attribute(k).name().contains("PatientID")) {
                IDindex = k;
            }
        }
        String name = in.stringValue(IDindex);

        ArrayList<Data> relevantSets = new ArrayList<Data>();
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).start >= start && sets.get(i).end == end) {
                relevantSets.add(sets.get(i));
            }
        }
        if (relevantSets.size() == 1) {
            int index = -1;
            IDindex = -1;
            for (int k = 0; k < in.numAttributes(); k++) {
                if (in.attribute(k).name().contains("PatientID")) {
                    IDindex = k;
                }
            }

            for (int j = 0; j < relevantSets.get(0).data.numInstances(); j++) {
                if (relevantSets.get(0).data.instance(j).stringValue(IDindex).equals(name)) {
                    index = j;
                }
            }
            if (index == -1) {
                //this ID is no longer present in this set so this set so stop classifying
            } else if (index >= relevantSets.get(0).data.numInstances()) {
                for (int o = 0; o < relevantSets.get(0).data.numInstances(); o++) {
                    if (relevantSets.get(0).data.instance(o).stringValue(IDindex).equals(name)) {
                        index = o;
                        break;
                    }
                }
            }
            return relevantSets.get(0).classifier.distributionForInstance(relevantSets.get(0).data.instance(index));
        }
        ArrayList<Double> votes = new ArrayList<Double>();
        for (int i = 0; i < relevantSets.size(); i++) {
            int index = -1;
            IDindex = -1;
            for (int k = 0; k < in.numAttributes(); k++) {
                if (in.attribute(k).name().contains("PatientID")) {
                    IDindex = k;
                }
            }
            for (int j = 0; j < relevantSets.get(i).data.numInstances(); j++) {
                if (relevantSets.get(i).data.instance(j).stringValue(IDindex).equals(name)) {
                    index = j;
                }
            }
            if (index == -1) {
                //this ID is no longer present in this set so this set so stop classifying
                continue;
            } else if (index >= relevantSets.get(i).data.numInstances()) {
                for (int o = 0; o < relevantSets.get(i).data.numInstances(); o++) {
                    if (relevantSets.get(i).data.instance(o).stringValue(IDindex).equals(name)) {
                        index = o;
                        break;
                    }
                }
            }
            votes.add(relevantSets.get(i).classifier.distributionForInstance(relevantSets.get(i).data.instance(index))[1]);
        }
        Double f = Double.parseDouble(voting(votes, 0));
        double[] d = new double[2];
        d[0] = 1 - f;
        d[1] = f;
        return d;

    }

    public void run() throws FileNotFoundException, IOException, Exception {
        RemoveUnwanted removal = new RemoveUnwanted(basePath, followPath, bioPath, targetLocation);
        removal.doWork(unwanted);
        FillInUnknown fill = new FillInUnknown(basePath, followPath, bioPath, targetLocation);
        fill.doWork();
        Splitting split = new Splitting(basePath, followPath, bioPath, targetLocation);
        split.work();
        steps = new Steps(basePath, followPath, bioPath, targetLocation, hospitalPath, hospitalization);
        steps.doWork();

        MakeDoubles doubles = new MakeDoubles(basePath, followPath, bioPath, targetLocation);
        doubles.doWork();

        sets = new ArrayList<Data>();

        otherFollow = readLargerTextFile(followPath);
        removeSpaces();

        makeTrueClasses();

        sets.add(new Data(readCSVData(targetLocation + "\\base1_weka.csv"), 1, 0, 1));
        sets.add(new Data(readCSVData(targetLocation + "\\base3_weka.csv"), 2, 0, 2));
        sets.add(new Data(readCSVData(targetLocation + "\\base6_weka.csv"), 3, 0, 3));
        sets.add(new Data(readCSVData(targetLocation + "\\base12_weka.csv"), 4, 0, 4));
        sets.add(new Data(readCSVData(targetLocation + "\\base18_weka.csv"), 5, 0, 5));
        sets.add(new Data(readCSVData(targetLocation + "\\one3_weka.csv"), 2, 1, 2));
        sets.add(new Data(readCSVData(targetLocation + "\\one6_weka.csv"), 3, 1, 3));
        sets.add(new Data(readCSVData(targetLocation + "\\one12_weka.csv"), 4, 1, 4));
        sets.add(new Data(readCSVData(targetLocation + "\\one18_weka.csv"), 5, 1, 5));
        sets.add(new Data(readCSVData(targetLocation + "\\three6_weka.csv"), 3, 2, 3));
        sets.add(new Data(readCSVData(targetLocation + "\\three12_weka.csv"), 4, 2, 4));
        sets.add(new Data(readCSVData(targetLocation + "\\three18_weka.csv"), 5, 2, 5));
        sets.add(new Data(readCSVData(targetLocation + "\\six12_weka.csv"), 4, 3, 4));
        sets.add(new Data(readCSVData(targetLocation + "\\six18_weka.csv"), 5, 3, 5));
        sets.add(new Data(readCSVData(targetLocation + "\\twelve18_weka.csv"), 5, 4, 5));
    }

    public void setClassifierAtt(boolean prune, int numAttributes, int minLeavesRandom, int numTree, int numForestAtt, int numNeighbours, double ridgeFactor) {
        this.pruned = prune;
        this.numAttributes = numAttributes;
        this.minLeavesRandom = minLeavesRandom;
        this.numTree = numTree;
        this.numForestAtt = numForestAtt;
        this.numNeighbours = numNeighbours;
        this.ridgeFactor = ridgeFactor;
    }

    public void buildClassifier() throws Exception {
        if (baseFilter == FilterType.univariate || baseFilter == FilterType.multivariate || baseFilter == FilterType.indiWrapper) {
            for (int i = 0; i < sets.size(); i++) {
                sets.get(i).filtered = filter(sets.get(i).data);
                sets.get(i).original = sets.get(i).data;
            }
        } else if (baseFilter == FilterType.averageWrap) {
            //attributes are derived by  behemothWrapper which needs to be run before building the classifier to save time

        } else if (baseFilter == FilterType.ensembleWrap) {
            //attributes are derived by  ensembleWrapper which needs to be run before building the classifier to save time
        }
        
        if (classifier.equals("J48")) {
            buildJ48Classifiers(sets);
        } else if (classifier.equals("randomTree")) {
            buildRandomTreeClassifiers(sets);
        } else if (classifier.equals("randomForest")) {
            buildRandomForestClassifiers(sets);
        } else if (classifier.equals("NN")) {
            buildNNClassifiers(sets);
        } else if (classifier.equals("logReg")) {
            buildLogRegClassifiers(sets);
        } else if (classifier.equals("naiveBayes")) {
            buildNaiveBayesClassifiers(sets);
        } else if (classifier.equals("SVM")) {
            buildSVMClassifiers(sets);
        } else if (classifier.equals("AdaBooster")) {
            buildAdaBoosterClassifiers(sets);
        }
    }

    /**
     * build classifier based on a subset of data
     *
     * @param start relevant ensemble start
     * @param end relevant ensemble end
     * @param ID list of IDs of instances which form the subset
     * @throws Exception
     */
    public void buildClassifierSubset(int start, int end, ArrayList<String> ID) throws Exception {
        ArrayList<Data> subSet = new ArrayList<Data>();
        //the list of IDs allows to train on a subset of data.
        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).start >= start && sets.get(i).end == end && sets.get(i).start < end) {
                subSet.add(sets.get(i));
                Instances d = subSet.get(subSet.size() - 1).data;
                while (ID.size() > d.numInstances()) {
                    boolean contained = false;
                    for (int j = 0; j < d.numInstances(); j++) {
                        for (String s : ID) {
                            if (d.instance(j).stringValue(0).equals(s)) {
                                contained = true;
                                break;
                            }
                        }
                        if (!contained) {
                            d.remove(j);
                            break;
                        }
                    }
                }
            }
        }
        if (classifier.equals("J48")) {
            buildJ48Classifiers(subSet);
        } else if (classifier.equals(
                "randomTree")) {
            buildRandomTreeClassifiers(subSet);
        } else if (classifier.equals(
                "randomForest")) {
            buildRandomForestClassifiers(subSet);
        } else if (classifier.equals(
                "NN")) {
            buildNNClassifiers(subSet);
        } else if (classifier.equals(
                "logReg")) {
            buildLogRegClassifiers(subSet);
        } else if (classifier.equals(
                "naiveBayes")) {
            buildNaiveBayesClassifiers(subSet);
        } else if (classifier.equals(
                "SVM")) {
            buildSVMClassifiers(subSet);
        } else if (classifier.equals(
                "AdaBooster")) {
            buildAdaBoosterClassifiers(subSet);
        }
    }
    //build the classifiers based on a subset of the data conveyed by their ID's

    public void buildClassifierDataSubset(ArrayList<String> ID) throws Exception {

        //the list of IDs allows to train on a subset of data.
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).data = sets.get(i).original;
            Instances d = sets.get(i).data;

            while (ID.size() < d.numInstances()) {

                boolean contained = false;
                for (int j = 0; j < d.numInstances(); j++) {
                    for (String s : ID) {
                        if (d.instance(j).stringValue(0).equals(s)) {
                            contained = true;
                            break;
                        }
                    }
                    if (!contained) {
                        d.remove(j);
                        break;
                    }
                }
            }
        }

        if (classifier.equals(
                "J48")) {
            buildJ48Classifiers(sets);
        } else if (classifier.equals(
                "randomTree")) {
            buildRandomTreeClassifiers(sets);
        } else if (classifier.equals(
                "randomForest")) {
            buildRandomForestClassifiers(sets);
        } else if (classifier.equals(
                "NN")) {
            buildNNClassifiers(sets);
        } else if (classifier.equals(
                "logReg")) {
            buildLogRegClassifiers(sets);
        } else if (classifier.equals(
                "naiveBayes")) {
            buildNaiveBayesClassifiers(sets);
        } else if (classifier.equals(
                "SVM")) {
            buildSVMClassifiers(sets);
        } else if (classifier.equals(
                "AdaBooster")) {
            buildAdaBoosterClassifiers(sets);
        }
        for (Data d : sets) {
            d.data = d.original;
        }
    }

    public void buildTestData(String baseLinePathTest, String followUpPathTest, String bioMarkersPathTest, String targetLocationTest) throws FileNotFoundException, IOException, Exception {

        RemoveUnwanted removal = new RemoveUnwanted(baseLinePathTest, followUpPathTest, bioMarkersPathTest, targetLocationTest);
        removal.doWork(unwanted);
        FillInUnknown fill = new FillInUnknown(baseLinePathTest, followUpPathTest, bioMarkersPathTest, targetLocationTest);
        fill.doWork();
        Splitting split = new Splitting(baseLinePathTest, followUpPathTest, bioMarkersPathTest, targetLocationTest);
        split.work();
        Steps steps = new Steps(baseLinePathTest, followUpPathTest, bioMarkersPathTest, targetLocationTest, hospitalPath, hospitalization);
        steps.doWork();
        MakeDoubles doubles = new MakeDoubles(baseLinePathTest, followUpPathTest, bioMarkersPathTest, targetLocationTest);
        doubles.doWork();

        testSets = new ArrayList<Data>();

        otherFollow = readLargerTextFile(followUpPathTest);
        removeSpaces();
        makeTrueClasses();

        testSets.add(new Data(readCSVData(targetLocationTest + "\\base1_weka.csv"), 1, 0, 1));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\base3_weka.csv"), 2, 0, 2));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\base6_weka.csv"), 3, 0, 3));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\base12_weka.csv"), 4, 0, 4));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\base18_weka.csv"), 5, 0, 5));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\one3_weka.csv"), 2, 1, 2));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\one6_weka.csv"), 3, 1, 3));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\one12_weka.csv"), 4, 1, 4));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\one18_weka.csv"), 5, 1, 5));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\three6_weka.csv"), 3, 2, 3));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\three12_weka.csv"), 4, 2, 4));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\three18_weka.csv"), 5, 2, 5));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\six12_weka.csv"), 4, 3, 4));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\six18_weka.csv"), 5, 3, 5));
        testSets.add(new Data(readCSVData(targetLocationTest + "\\twelve18_weka.csv"), 5, 4, 5));

    }

    public void removeSpaces() {
        for (int i = 0; i < otherFollow.size(); i++) {
            String s = otherFollow.get(i).get(0);
            s.replace(" ", "");
            otherFollow.get(i).set(0, s);
        }
    }

    public void buildJ48Classifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildJ48Classifier(sets.get(i).data);
        }
    }

    public void buildAdaBoosterClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildAdaBoostClassifier(sets.get(i).data);
        }
    }

    public void buildRandomTreeClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildRandomTreeClassifier(sets.get(i).data);
        }
    }

    public void buildRandomForestClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildRandomForestClassifier(sets.get(i).data);
        }
    }

    public void buildSVMClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildSVMClassifier(sets.get(i).data);
        }
    }

    public void buildNNClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildNNClassifier(sets.get(i).data);
        }
    }

    public void buildNaiveBayesClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildNaiveBayesClassifier(sets.get(i).data);
        }
    }

    public void buildLogRegClassifiers(ArrayList<Data> sets) throws Exception {
        for (int i = 0; i < sets.size(); i++) {
            sets.get(i).classifier = buildLogRegClassifier(sets.get(i).data);
        }
    }

    public void makeTrueClasses() {
        int index = 0;
        deathList = new String[otherFollow.size() - 1][2];
        for (int i = 0; i < otherFollow.get(0).size(); i++) {
            if (otherFollow.get(0).get(i).equals("Death")) {
                index = i;
            }
        }
        for (int i = 1; i < otherFollow.size(); i++) {
            String s = "";
            if (otherFollow.get(i).get(index).equals("1")) {
                s = "True";
            } else {
                s = "False";
            }
            deathList[i - 1][1] = s;
            deathList[i - 1][0] = otherFollow.get(i - 1).get(0);
        }

    }
    public boolean ensemble = false;

    /**
     * sets the true class value for a relevant ensemble to the death list used
     * for comparing the voting and such.
     *
     * @param data
     */
    public void makeTrueClassesEnsemble(Data data) {
        int index = 0;
        deathList = new String[data.data.numInstances()][2];
        index = data.data.classIndex();
        for (int i = 0; i < data.data.numInstances(); i++) {
            deathList[i][1] = data.data.instance(i).stringValue(index);
            deathList[i][0] = data.data.instance(i).stringValue(0);
        }
        int count = 0;
        for (int i = 0; i < deathList.length; i++) {
            if (deathList[i][1].equals("True")) {
                count++;
            }
        }
        ensemble = true;
    }

    /**
     * only using the data available until a certain measurement also ignores
     * the following classifiers
     *
     * @param sets
     * @param veto
     * @param measurements
     * @param testSets
     * @param res
     * @return
     * @throws Exception
     */
    public int[][] testClassifiers(ArrayList<Data> sets, int veto, int measurements, ArrayList<Data> testSets, Result res) throws Exception {
        Instances base1 = testSets.get(0).data;
        String[][] classification = new String[base1.numInstances()][3];
        for (int i = 0; i < base1.numInstances(); i++) {
            String ID = base1.instance(i).stringValue((base1.attribute(0)));
            ArrayList<Double> classes = new ArrayList<Double>();
            for (int j = 0; j < testSets.size(); j++) {
                if (testSets.get(j).measurement > measurements) {
                    continue;
                }
                int index = -1;

                for (int k = 0; k < testSets.get(j).data.numInstances(); k++) {
                    if (testSets.get(j).data.instance(k).stringValue(0).equals(ID)) {
                        index = k;
                        break;
                    }
                }
                if (index == -1) {
                    //this ID is no longer present in this set so this set so stop classifying
                    continue;
                }

                Instance in = testSets.get(j).data.instance(index);
                classes.add(sets.get(j).classifier.distributionForInstance(in)[1]);
            }
            classification[i][0] = ID;
            classification[i][1] = voting(classes, veto);

        }
        int[][] classified = new int[2][2];
        for (int i = 0; i < classification.length; i++) {
            for (int j = 0; j < deathList.length; j++) {
                if (deathList[j][0].equals(classification[i][0])) {

                    if (deathList[j][1].equals("True")) {
                        res.addIndividual(classification[i][0], Double.parseDouble(classification[i][1]), true);
                        if (Double.parseDouble(classification[i][1]) >= 0.5) {
                            classified[0][0]++;
                        } else {
                            classified[1][0]++;
                        }
                    } else {
                        res.addIndividual(classification[i][0], Double.parseDouble(classification[i][1]), false);
                        if (Double.parseDouble(classification[i][1]) >= 0.5) {
                            classified[0][1]++;
                        } else {
                            classified[1][1]++;
                        }
                    }
                }
            }
        }
        return classified;

    }

    /**
     * votes for an individual's classification based on the votes cast by the
     * classifiers in an ensemble Following schemese: Veto: Average vote, if
     * Veto is reached they may veto the majority Average probability Minimum
     * probability Maximum probability majority vote assuming 0.5 is the cut of
     * point for true/false
     *
     * @param votes
     * @param veto
     * @return
     */
    public String voting(ArrayList<Double> votes, int veto) {
        Double vote = -1.0;
        if (voteType == VoteType.veto) {
            int c = 0;
            vote = 0.0;
            for (int i = 0; i < votes.size(); i++) {
                vote += votes.get(i);
                if (votes.get(i) >= 0.5) {
                    c++;
                }
            }
            if (c >= veto) {
                vote = 1.0;
            } else {
                vote /= (double) votes.size();
            }
        } else if (voteType == VoteType.maxProb) {
            for (int i = 0; i < votes.size(); i++) {
                if (votes.get(i) >= vote) {
                    vote = votes.get(i);
                }
            }
        } else if (voteType == VoteType.minProb) {
            vote = 2.0;
            for (int i = 0; i < votes.size(); i++) {
                if (votes.get(i) <= vote) {
                    vote = votes.get(i);
                }
            }
        } else if (voteType == VoteType.averageProb) {
            vote = 0.0;
            for (int i = 0; i < votes.size(); i++) {
                vote += votes.get(i);
            }
            vote /= (double) votes.size();
        } else if (voteType == VoteType.majority) {
            int pos = 0;
            int neg = 0;
            for (int i = 0; i < votes.size(); i++) {
                if (votes.get(i) >= 0.5) {
                    pos++;
                } else {
                    neg++;
                }
            }
            if (pos >= neg) {
                vote = 1.0;
            } else {
                vote = 0.0;
            }
        }
        return vote.toString();
    }

    public Classifier buildJ48Classifier(Instances in) throws Exception {
        J48 tree = new J48();
        String[] options = new String[1];
        if (!pruned) {
            options[0] = "-U";  //unpruned
        } else {
            options[0] = "-P";
        }
        tree.setOptions(options);
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(tree);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Classifier buildRandomTreeClassifier(Instances in) throws Exception {
        RandomTree tree = new RandomTree();
        tree.setKValue(Integer.parseInt(numAtt));
        tree.setMinNum(Integer.parseInt(minInLeave));
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(tree);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);

        f.buildClassifier(in);
        return f;
    }

    public Classifier buildRandomForestClassifier(Instances in) throws Exception {
        RandomForest forest = new RandomForest();
        forest.setNumTrees(numTree);
        forest.setNumFeatures(numForestAtt);
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(forest);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Classifier buildNNClassifier(Instances in) throws Exception {
        IBk nn = new IBk();
        nn.setKNN(numNeighbours);
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(nn);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Classifier buildNaiveBayesClassifier(Instances in) throws Exception {
        NaiveBayes naive = new NaiveBayes();
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(naive);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Classifier buildLogRegClassifier(Instances in) throws Exception {
        Logistic log = new Logistic();
        log.setRidge(ridgeFactor);
        //FilteredLogRegClassifier f = new FilteredLogRegClassifier();
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(log);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Classifier buildSVMClassifier(Instances in) throws Exception {
        SMO svm = new SMO();
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(svm);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Classifier buildAdaBoostClassifier(Instances in) throws Exception {
        AdaBoostM1 ada = new AdaBoostM1();
        FilteredClassifier f = new FilteredClassifier();
        f.setClassifier(ada);
        Remove rm = new Remove();
        rm.setAttributeIndices("1");
        f.setFilter(rm);
        f.buildClassifier(in);
        return f;
    }

    public Instances readArrfData(String path) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        Instances data = new Instances(reader);
        reader.close();
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    public Instances readCSVData(String path) throws FileNotFoundException, IOException, Exception {
        DataSource source = new DataSource(path);
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    public ArrayList<ArrayList<String>> readLargerTextFile(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        ArrayList<ArrayList<String>> text = new ArrayList<ArrayList<String>>();
        int row = -1;
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                row++;
                String s = scanner.nextLine();
                text.add(new ArrayList<String>());
                String s2 = "";
                int collumn = 0;
                for (int i = 1; i <= s.length(); i++) {
                    if (s.substring(i - 1, i).equals(";")) {
                        collumn++;
                        text.get(row).add(s2);
                        s2 = "";
                    } else {
                        s2 += s.substring(i - 1, i);
                    }

                }
                text.get(row).add(s2);
            }
        }
        return text;
    }

    public Comparison compareIndiv(int start1, int end1, int start2, int end2) throws Exception {
        Data s1 = new Data();
        Data s2 = new Data();

        for (int i = 0; i < sets.size(); i++) {
            if (sets.get(i).start == start1 && sets.get(i).end == end1) {
                s1 = sets.get(i);
            }
            if (sets.get(i).start == start2 && sets.get(i).end == end2) {
                s2 = sets.get(i);
            }
        }
        //make sure are the same size
        if (s1.data.numInstances() != s2.data.numInstances()) {
            return new Comparison(0, 0, 0, false, 0);
        }
        ArrayList<Double> diff = new ArrayList<Double>();
        for (int i = 0; i < s1.data.numInstances(); i++) {
            String ID = s1.data.instance(i).stringValue(0);
            for (int j = 0; j < s2.data.numInstances(); j++) {
                if (s2.data.instance(j).stringValue(0).equals(ID)) {
                    double v1 = s1.classifier.classifyInstance(s1.data.instance(i));
                    double v2 = s2.classifier.classifyInstance(s2.data.instance(j));
                    if (v1 > v2) {
                        diff.add(v1 - v2);
                    } else {
                        diff.add(v2 - v1);
                    }
                    break;
                }
            }
        }
        double mean = 0.0;
        double sDev = 0.0;
        for (int i = 0; i < diff.size(); i++) {
            mean += diff.get(i);
        }
        mean /= diff.size();
        for (int i = 0; i < diff.size(); i++) {
            sDev += (diff.get(i) - mean) * (diff.get(i) - mean);
        }
        sDev /= diff.size();
        sDev = Math.sqrt(sDev);
        double tValue = mean / (sDev) * Math.sqrt(diff.size());
        if (sDev == 0) {
            tValue = Double.POSITIVE_INFINITY;
        }
        return new Comparison(mean, sDev, tValue, true, diff.size());

    }

    /**
     * attribute selection on an individual set of data based on individual
     * Filter
     *
     * @param ins
     * @return
     * @throws Exception
     */
    public Instances filter(Instances ins) throws Exception {
        Instances in = new Instances(ins);
        Instances in2 = new Instances(ins);
        in.deleteAttributeAt(0);

        AttributeSelection attFilter = new AttributeSelection();  // package weka.filters.supervised.attribute!
        System.out.println(baseFilter);
        if (baseFilter == FilterType.univariate) {
            //rank attributes
            GainRatioAttributeEval eval = new GainRatioAttributeEval();
            Ranker search = new Ranker();
            attFilter.setEvaluator(eval);
            attFilter.setSearch(search);
            attFilter.setFolds(10);
            attFilter.SelectAttributes(in);

            attFilter.setXval(true);
            attFilter.selectAttributesCVSplit(in);

            double[][] d = attFilter.rankedAttributes();

            ArrayList<String> sel = new ArrayList<String>();
            sel.add(in2.attribute(0).name());
            sel.add(in2.attribute(in2.numAttributes() - 1).name());
            for (int i = 0; i < selected; i++) {
                sel.add(in.attribute((int) d[i][0]).name());
            }
            int i = 0;
            while (i < in2.numAttributes()) {
                boolean contained = false;
                for (int j = 0; j < sel.size(); j++) {
                    if (in2.attribute(i).name().equals(sel.get(j))) {
                        contained = true;
                    }
                }
                if (!contained) {
                    in2.deleteAttributeAt(i);
                    i = 0;
                } else {
                    i++;
                }
            }
            return in2;
        } else if (baseFilter == FilterType.indiWrapper) {
            WrapperSubsetEval eval = new WrapperSubsetEval();

            //convoluted way og getting the wrapper to use the correct evaluation function. 6 corresponds to AUC
            //classifier is set correctly below this.
            //this also sets the # of folds for K-fold (10) and some threshold
            String[] WrapperEvalOpts_nbc = {"-B", "weka.classifiers.bayes.NaiveBayes", "-F", "2", "-T", "0.01", "-E", "auc", "-R", "6"};// "" + WrapperSubsetEval.EVAL_AUC};
            eval.setOptions(WrapperEvalOpts_nbc);
            //get the correct classifier.
            if (classifier.equals("J48")) {
                eval.setClassifier(new J48());
            } else if (classifier.equals("randomTree")) {
                eval.setClassifier(new RandomTree());
            } else if (classifier.equals("randomForest")) {
                eval.setClassifier(new RandomForest());
            } else if (classifier.equals("NN")) {
                eval.setClassifier(new IBk());
            } else if (classifier.equals("logReg")) {
                eval.setClassifier(new Logistic());
            } else if (classifier.equals("naiveBayes")) {
                eval.setClassifier(new NaiveBayes());
            } else if (classifier.equals("SVM")) {
                eval.setClassifier(new SMO());
            } else if (classifier.equals("AdaBooster")) {
                eval.setClassifier(new AdaBoostM1());
            }

            GeneticSearch search = new GeneticSearch();
            search.setPopulationSize(30);
            search.setMaxGenerations(100);
            attFilter.setEvaluator(eval);
            attFilter.setSearch(search);
            attFilter.setFolds(10);
            attFilter.setXval(true);
            attFilter.SelectAttributes(in);
            attFilter.selectAttributesCVSplit(in);

            int[] select = attFilter.selectedAttributes();
            for (int i = 0; i < select.length; i++) {
                System.out.println(select[i]);
            }
            ArrayList<String> sel = new ArrayList<String>();
            sel.add(in2.attribute(0).name());
            sel.add(in2.attribute(in2.numAttributes() - 1).name());
            for (int i = 0; i < selected && i < select.length; i++) {
                sel.add(in.attribute((int) select[i]).name());
            }
            int i = 0;
            while (i < in2.numAttributes()) {
                boolean contained = false;
                for (int j = 0; j < sel.size(); j++) {
                    if (in2.attribute(i).name().equals(sel.get(j))) {
                        contained = true;
                    }
                }
                if (!contained) {
                    in2.deleteAttributeAt(i);
                    i = 0;
                } else {
                    i++;
                }
            }
            return in2;
        } else if (baseFilter == FilterType.multivariate) {
            CfsSubsetEval eval = new CfsSubsetEval();
            GeneticSearch search = new GeneticSearch();
            search.setPopulationSize(30);
            search.setMaxGenerations(100);
            attFilter.setEvaluator(eval);
            attFilter.setSearch(search);
            attFilter.setFolds(10);
            attFilter.setXval(true);
            attFilter.SelectAttributes(in);
            attFilter.selectAttributesCVSplit(in);

            int[] select = attFilter.selectedAttributes();
            for (int i = 0; i < select.length; i++) {
                System.out.println(select[i]);
            }
            ArrayList<String> sel = new ArrayList<String>();
            sel.add(in2.attribute(0).name());
            sel.add(in2.attribute(in2.numAttributes() - 1).name());
            for (int i = 0; i < selected && i < select.length; i++) {
                sel.add(in.attribute((int) select[i]).name());
            }
            int i = 0;
            while (i < in2.numAttributes()) {
                boolean contained = false;
                for (int j = 0; j < sel.size(); j++) {
                    if (in2.attribute(i).name().equals(sel.get(j))) {
                        contained = true;
                    }
                }
                if (!contained) {
                    in2.deleteAttributeAt(i);
                    i = 0;
                } else {
                    i++;
                }
            }
            return in2;
        } else {
            return in2;
        }

    }

    /**
     * does attribute selection on an ensemble using the filtered data from the
     * individual classifiers According to a number of schemes none: no filter
     * filter: Every classifier keeps its own filtering union: union of the
     * selected filters intersection: intersection of the selected filters
     * Random: random combination of said filters
     *
     * @param sets
     * @throws Exception
     */
    public void filterEnsemble(ArrayList<Data> sets) throws Exception {
        System.out.println(filterRecombination);
        if (baseFilter == FilterType.averageWrap) {
            for (Data d : sets) {
                d.data = d.filtered;
            }
        } else if (baseFilter == FilterType.ensembleWrap) {
            int start = 10;
            int end = 0;
            for (Data d : sets) {
                if (d.start <= start) {
                    start = d.start;
                }
                end = d.end;
            }
            setAttributes(ensembleWrapper(start, end));
            for (Data d : sets) {
                d.data = d.filtered;
            }
        } //last possibility is individual filteringm which is done elsewhere, only need to recombine
        else {
            if (filterRecombination == filterRecombination.none) {
                //no filtering needed;
            } else if (filterRecombination == filterRecombination.filter) {
                for (Data d : sets) {
                    d.data = d.filtered;
                }
            } else if (filterRecombination == filterRecombination.union) {
                ArrayList<String> att = new ArrayList<String>();
                for (Data d : sets) {
                    for (int i = 0; i < d.filtered.numAttributes(); i++) {
                        att.add(d.filtered.attribute(i).name());
                    }
                }
                for (Data d : sets) {
                    int i = 1;
                    d.data = new Instances(d.original);
                    while (i < d.data.numAttributes()) {
                        boolean remains = false;
                        for (int j = 0; j < att.size(); j++) {
                            if (d.data.attribute(i).name().equals(att.get(j))) {
                                remains = true;
                                break;
                            }
                        }
                        if (remains) {
                            i++;
                        } else {
                            d.data.deleteAttributeAt(i);
                            i = 1;
                        }
                    }
                    if (d.data.numAttributes() <= 2) {
                        d.data = new Instances(d.original);
                    }
                }
            } else if (filterRecombination == filterRecombination.intersection) {
                ArrayList<String> att = new ArrayList<String>();
                for (int j = 0; j < sets.get(0).filtered.numAttributes(); j++) {
                    att.add(sets.get(0).filtered.attribute(j).name());
                }
                for (int i = 1; i < sets.size(); i++) {
                    ArrayList<String> att1 = new ArrayList<String>();
                    for (int j = 0; j < sets.get(i).filtered.numAttributes(); j++) {
                        att1.add(sets.get(i).filtered.attribute(j).name());
                    }
                    ArrayList<String> att2 = new ArrayList<String>();
                    att2.addAll(att);
                    att2.removeAll(att1);
                    att.removeAll(att2);
                    System.out.println(att1.size() + " " + att2.size() + " " + att.size());
                }
                for (Data d : sets) {
                    int i = 1;
                    d.data = new Instances(d.original);
                    while (i < d.data.numAttributes()) {
                        boolean remains = false;
                        for (int j = 0; j < att.size(); j++) {
                            if (d.data.attribute(i).name().equals(att.get(j))) {
                                remains = true;
                                break;
                            }
                        }
                        if (remains) {
                            i++;
                        } else {
                            d.data.deleteAttributeAt(i);
                            i = 1;
                        }
                    }
                    if (d.data.numAttributes() <= 2) {
                        d.data = new Instances(d.original);
                    }
                }
            } else if (filterRecombination == filterRecombination.random) {
                ArrayList<String> names = new ArrayList<String>();
                for (Data d : sets) {
                    //att 0 & the last one are class & ID, not relevant here
                    for (int i = 1; i < d.data.numAttributes() - 1; i++) {
                        names.add(d.data.attribute(i).name());
                    }
                }
                ArrayList<String> remain = new ArrayList<String>();
                Random r = new Random();
                for (int i = 0; i < selected; i++) {
                    remain.add(names.get(r.nextInt(names.size())));
                }
                remain.add(sets.get(0).data.attribute(0).name());
                remain.add(sets.get(0).data.attribute(sets.get(0).data.numAttributes() - 1).name());
                for (Data d : sets) {
                    int i = 1;
                    d.data = new Instances(d.original);
                    while (i < d.data.numAttributes()) {
                        boolean remains = false;
                        for (int j = 0; j < remain.size(); j++) {
                            if (d.data.attribute(i).name().equals(remain.get(j))) {
                                remains = true;
                                break;
                            }
                        }
                        if (remains) {
                            i++;
                        } else {
                            d.data.deleteAttributeAt(i);
                            i = 1;
                        }
                    }
                    if (d.data.numAttributes() <= 2) {
                        d.data = new Instances(d.original);
                    }
                }
            }
        }

        if (classifier.equals("J48")) {
            buildJ48Classifiers(sets);
        } else if (classifier.equals("randomTree")) {
            buildRandomTreeClassifiers(sets);
        } else if (classifier.equals("randomForest")) {
            buildRandomForestClassifiers(sets);
        } else if (classifier.equals("NN")) {
            buildNNClassifiers(sets);
        } else if (classifier.equals("logReg")) {
            buildLogRegClassifiers(sets);
        } else if (classifier.equals("naiveBayes")) {
            buildNaiveBayesClassifiers(sets);
        } else if (classifier.equals("SVM")) {
            buildSVMClassifiers(sets);
        } else if (classifier.equals("AdaBooster")) {
            buildAdaBoosterClassifiers(sets);
        }
    }

    /**
     * basicly a script to run different tests. Will be removed eventually.
     *
     * @throws Exception
     */
    public void Test123() throws Exception {
        classifier = "logReg";
        long time = System.currentTimeMillis();

        buildClassifier();
        System.out.println(hospitalization);
        ArrayList<Double> ridge = new ArrayList<Double>();
        ridgeFactor = 100;


        filterRecombination = filterRecombination.filter;
        baseFilter = FilterType.univariate;
        buildClassifier();
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 6; j++) {
                System.out.println(testEnsemble(i, j, 5, 5, true).calcAUC());
                printResult(i, j, relevantSets(i,j), "none_ridge_100");

            }
        }
        baseFilter = FilterType.univariate;

        buildClassifier();
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 6; j++) {
                System.out.println(testEnsemble(i, j, 5, 5, true).calcAUC());
                printResult(i, j, relevantSets(i, j), "univariate_ridge_0_01");

            }
        }
        baseFilter = FilterType.multivariate;

        buildClassifier();
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 6; j++) {
                System.out.println(testEnsemble(i, j, 5, 5, true).calcAUC());
                printResult(i, j, relevantSets(i, j), "multivariate_ridge_0_01");

            }
        }
        baseFilter = FilterType.indiWrapper;
        buildClassifier();
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 6; j++) {
                System.out.println(testEnsemble(i, j, 5, 5, true).calcAUC());
                printResult(i, j, relevantSets(i,j), "indiwrapper_ridge_0_01");

            }
        }
//        baseFilter = FilterType.ensembleWrap;
//        buildClassifier();

//        for (int i = 3; i < 4; i++) {
//
//            for (int j = 5; j < 6; j++) {
//                setAttributes(ensembleWrapper(i, j));
//                buildClassifier();
//                printResult(i, j, relevantSets(i, j), "ensemble_ridge_100");
//
//            }
//
//        }
//        baseFilter = FilterType.averageWrap;
//        buildClassifier();
//        //       setAttributes(behemothWrapper());
//        ArrayList<Boolean> b = makelist();
//        boolean[] a = new boolean[b.size()];
//        for(int i = 0; i < b.size(); i++){
//            a[i] = b.get(i);
//        }
//        setAttributes(a);
//        for (int i = 0; i < 5; i++) {
//            for (int j = i + 1; j < 6; j++) {
//                if (i == 0){
//                    j = 5;
//                }
//                printResult(i, j, relevantSets(i, j), "Behemoth_ridge_100");
//
//            }
//        }
//
//
//
        System.out.println("done");
    }

    private ArrayList<Boolean> makelist() {
        ArrayList<Boolean> list = new ArrayList<Boolean>();
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(false);
        list.add(false);
        list.add(false);
        list.add(true);
        list.add(true);

        return list;
    }

    private void printAUC(ArrayList<ArrayList<String>> auc, String s) throws IOException {
        String pathName = targetLocation + "selectedEnsemble_" + s + ".csv";
        Path path = Paths.get(pathName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
            for (int i = 0; i < auc.get(0).size(); i++) {
                String a = "";
                for (int j = 0; j < auc.size(); j++) {
                    a += auc.get(j).get(i) + ";";
                }
                writer.write(a);
                writer.newLine();
            }
        }
    }

    private void printEnsembleResults(boolean[] selected, int start, int end, ArrayList<Data> sets, String add) throws IOException, Exception {
        String pathName = targetLocation + "selectedEnsemble_" + start + "_" + end + add + ".txt";
        Path path = Paths.get(pathName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
            String s = "Selected Features for ensemble: " + start + "-" + end;

            writer.write("Achieved AUC: " + testEnsemble(start, end, 5, 5, true).calcAUC());
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.write(s);
            writer.newLine();

            for (int i = 0; i < selected.length; i++) {
                s = "";
                if (selected[i]) {
                    s += sets.get(0).original.attribute(i).name();
                    writer.write(s);
                    writer.newLine();
                }
            }

            for (int i = 0; i < sets.size(); i++) {
                writer.newLine();
                writer.newLine();
                writer.newLine();
                writer.write("Classifier " + sets.get(i).start + " " + sets.get(i).end + "has the following pvalues & tvalues & " + (sets.get(i).data.numInstances() - sets.get(i).data.numAttributes() - 1) + "degrees of freedom");
                double[][] pvalue = TTest(sets.get(i));
                writer.newLine();
                for (int j = 0; j < pvalue.length; j++) {
                    s = "";
                    //do take into account that the set still has the ID attribute, but Pvalues dont

                    s += "Attribute: " + sets.get(i).data.attribute(j + 1).name() + " Pvalue: " + pvalue[j][1] + " Tvalue: " + pvalue[j][0];
                    writer.write(s);
                    writer.newLine();
                }
            }

        }
    }

    private void printResult(int start, int end, ArrayList<Data> sets, String type) throws IOException, Exception {
        String pathName = targetLocation + "classifierEnsemble" + type + "" + start + "" + end + ".csv";
        Path path = Paths.get(pathName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
            String st = "" + start;
            String en = "" + end;
            if (start == 0) {
                st = "0";
            } else if (start == 2) {
                st = "3";
            } else if (start == 3) {
                st = "6";
            } else if (start == 4) {
                st = "12";
            }
            if (end == 5) {
                en = "18";
            } else if (end == 2) {
                en = "3";
            } else if (end == 3) {
                en = "6";
            } else if (end == 4) {
                en = "12";
            }
            String s = "Selected Features for ensemble: " + st + "-" + en;
            writer.write("Achieved AUC: " + testEnsemble(start, end, 5, 5, true).calcAUC());
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.write(s);
            writer.newLine();

            int count = 0;
            s = "";
            for (int j = 0; j < sets.size(); j++) {
                writer.write("classifier " + sets.get(j).start + "-"+ sets.get(j).end);
                writer.newLine();
                for (int i = 0; i < sets.get(j).data.numAttributes(); i++) {
                    s += sets.get(j).data.attribute(i).name() + ";";
                    count++;
                    if (count == 1) {
                        writer.write(s);
                        writer.newLine();
                        s = "";
                        count = 0;
                    }
                }
            }
            writer.write(s);
            writer.newLine();
            writer.newLine();
            writer.newLine();
            s = "";
            count = 0;
            ArrayList<double[][]> list = new ArrayList<double[][]>();
            for (int i = 0; i < sets.size(); i++) {
                list.add(TTest(sets.get(i)));
            }
            count = 0;
            writer.write("P values for each attribute except for the ID attribute");
            writer.newLine();
            Chi chi = new Chi();
            for(Data d: sets){
                double[] pValues = chi.PValues(d.data);

                double[] chiValues = chi.ChiValues(d.data);
                writer.write("Ensemble:" + d.start + "-" + d.end);
                for(int i = 0; i<pValues.length; i++){
                    writer.write(d.data.attribute(i+1).name() + " pvalue: " + pValues[i] + " chiValue: " + chiValues[i]);

                }
            }
            
//            writer.write("P values for each attribute except for the ID attribute");
//            writer.newLine();
//            ArrayList<Double> pvalueMax = new ArrayList<Double>();
//            ArrayList<Double> pvalueMin = new ArrayList<Double>();
//            ArrayList<String> name = new ArrayList<String>();
//            for(int j = 0; j < list.size(); j++){
//                for(int i = 1; i < sets.get(j).data.numAttributes()-1; i++){
//                    String attName = sets.get(j).data.attribute(i).name();
//                    if(name.contains(attName)){
//                        pvalueMax.set(name.indexOf(attName), Math.max(pvalueMax.get(name.indexOf((attName))), list.get(j)[i-1][1]));
//                        pvalueMin.set(name.indexOf(attName), Math.min(pvalueMin.get(name.indexOf((attName))), list.get(j)[i-1][1]));
//                    }else{
//                        name.add(attName);
//                        pvalueMax.add(list.get(j)[i-1][1]);
//                        pvalueMin.add(list.get(j)[i-1][1]);
//                    }
//                }
//            }
//            for (int i = 0; i < sets.get(0).data.numAttributes()-1; i++) {
//                //do take into account that the set still has the ID attribute, but Pvalues dont
//                double min = 10;
//                double max = -10;
//                if (i != 0) {
//                    for (int j = 0; j < list.size(); j++) {
//                        if (j == 0) {
//                            min = list.get(j)[i - 1][1];
//                            max = min = list.get(j)[i - 1][1];
//                        }
//                        if (list.get(j)[i - 1][1] < min) {
//                            min = list.get(j)[i - 1][1];
//                        } else if (list.get(j)[i - 1][1] > max) {
//                            max = list.get(j)[i - 1][1];
//                        }
//                    }
//
//                    s += sets.get(0).data.attribute(i).name() + " Min: Pvalue: " + min + " Max value: " + max + " ; ";
//                }
//                count++;
//                if (count == 1) {
//                    writer.write(s);
//                    writer.newLine();
//                    s = "";
//                    count = 0;
//                }
//            }
//            writer.write(s);
//            writer.newLine();
//            s = "";
//            count = 0;

        }
    }

    /**
     * Runs a wrapper for feature selection on the entire ensemble, using the
     * average AUC as a measurement of goodness
     *
     * @return selected features in the form of a list of booleans
     * @throws Exception
     */
    public boolean[] ensembleWrapper(int start, int end) throws Exception {

        EnsembleClassifierWeka w = new EnsembleClassifierWeka(this, start, end);
        Instances in = null;
        //pick the latest set as the most relevant.
        for (Data d : sets) {
            if (d.end == end && d.start == end - 1) {
                in = new Instances(d.original);
            }
        }

        AttributeSelection attFilter = new AttributeSelection();  // package weka.filters.supervised.attribute!
        WrapperSubsetEval eval = new WrapperSubsetEval();
        //convoluted way of getting the wrapper to use the correct evaluation function. 6 corresponds to AUC
        //classifier is set correctly below this.
        //this also sets the # of folds for K-fold (10) and some threshold
        String[] WrapperEvalOpts_nbc = {"-B", "weka.classifiers.bayes.NaiveBayes", "-F", "5", "-T", "0.01", "-R", "6", "-E", "auc"};
        eval.setOptions(WrapperEvalOpts_nbc);
        //get the correct classifier.
        eval.setClassifier(w);
        in.setClass(in.attribute(in.numAttributes() - 1));
        long time = System.currentTimeMillis();
        //BestFirst search = new BestFirst();
        GeneticSearch search = new GeneticSearch();
        search.setPopulationSize(30);
        search.setMaxGenerations(100);
        search.setReportFrequency(10);
        System.out.println(search.getMaxGenerations() + " " + search.getPopulationSize() + " " + search.getMutationProb());

        //search.setStartSet("1,2"); //include the ID and a random other attribute. ID is needed for the wrapper approach, other attribute is needed as otherwise
        //not needed when using genetic search
        //it starts with just the ID and the classattribute which can't be used for training... given that any is good enough
        // I've picked the first attribute after ID
        eval.buildEvaluator(in);
        attFilter.setEvaluator(eval);
        attFilter.setSearch(search);

        attFilter.setXval(true);
        attFilter.SelectAttributes(in);
        System.out.println((System.currentTimeMillis() - time) / 1000);
        int[] select = attFilter.selectedAttributes();
        boolean[] selected = new boolean[sets.get(0).original.numAttributes()];
        System.out.println(eval);
        for (int i = 0; i < select.length; i++) {
            selected[select[i]] = true;
        }
        System.out.println("");
        System.out.println("selected attributes:");
        for (int i = 0; i < selected.length; i++) {
            System.out.println(" Attribute " + i + " " + selected[i]);
        }
        return selected;

    }

    /**
     * Runs a wrapper for feature selection on the entire thing, using the
     * average AUC of ensembles as a measurement of goodness
     *
     * @return selected features in the form of a list of booleans
     * @throws Exception
     */
    public boolean[] behemothWrapper() throws Exception {
        //pick a random data set to use as source for the attributes. Really doesn't matter but weka wants a list of attributes...
        Instances in = sets.get(0).original;

        AttributeSelection attFilter = new AttributeSelection();  // package weka.filters.supervised.attribute!
        BehemothWrapper eval = new BehemothWrapper();

        //get the correct classifier.
        eval.setClassifier(new BehemothClassifier(this));
        in.setClass(in.attribute(in.numAttributes() - 1));
        long time = System.currentTimeMillis();
        //BestFirst search = new BestFirst();
        GeneticSearch search = new GeneticSearch();
        search.setPopulationSize(30);
        search.setMaxGenerations(100);
        //search.setStartSet("1,2"); //include the ID and a random other attribute. ID is needed for the wrapper approach, other attribute is needed as otherwise
        //it starts with just the ID and the classattribute which can't be used for training... given that any is good enough
        // I've picked the first attribute after ID
        eval.buildEvaluator(in);
        attFilter.setEvaluator(eval);
        attFilter.setSearch(search);
        attFilter.setFolds(2);
        attFilter.setXval(false); //settings
        attFilter.SelectAttributes(in);
        System.out.println((System.currentTimeMillis() - time) / 1000);
        int[] select = attFilter.selectedAttributes();
        boolean[] selected = new boolean[sets.get(0).original.numAttributes()];
        System.out.println(eval);
        for (int i = 0; i < select.length; i++) {
            selected[select[i]] = true;
        }
        System.out.println("");
        System.out.println("selected attributes:");
        for (int i = 0; i < selected.length; i++) {
            System.out.println(" Attribute " + i + " " + selected[i]);
        }
        return selected;
    }

    public void setAttributes(boolean[] sel) {
        ArrayList<String> namesNotSelected = new ArrayList<String>();
        for (int i = 0; i < sel.length; i++) {
            if (!sel[i]) {
                namesNotSelected.add(sets.get(0).original.attribute(i).name());
            }
        }
        for (Data d : sets) {
            ArrayList<String> temp = new ArrayList<String>();
            temp.addAll(namesNotSelected);
            d.filtered = new Instances(d.original);
            while (!temp.isEmpty()) {
                for (int i = 0; i < d.filtered.numAttributes(); i++) {
                    if (temp.contains(d.filtered.attribute(i).name())) {
                        temp.remove(d.filtered.attribute(i).name());
                        d.filtered.deleteAttributeAt(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * only works with log regression. Returns p and t values. Do take into
     * account it doesn't include the ID attribute so it's shorter than the
     * normal set of attributes.
     *
     * @param d
     * @return d[i][0] = t , d[i][1] = p
     * @throws Exception
     */
    public double[][] TTest(Data d) throws Exception {
        double sse = 0;
        Instances ins = new Instances(d.data);
        ins.deleteAttributeAt(0);
        int classIndex = ins.classIndex();
        Logistic log = new Logistic();
        for (int i = 0; i < folds; i++) {
            Instances randData = new Instances(ins);
            Instances train = randData.trainCV(folds, i);
            Instances test = randData.testCV(folds, i);

            log.buildClassifier(train);
            for (Instance in : test) {
                if (in.stringValue(classIndex).equals("False")) {
                    sse += Math.pow(0 - log.distributionForInstance(in)[1], 2);
                } else {
                    sse += Math.pow(1 - log.distributionForInstance(in)[1], 2);
                }
            }
        }
        sse /= (d.data.numInstances() - d.data.numAttributes() - 1);
        TTest t = new TTest(sse);

        double[] std = log.getSTD();
        double[][] coef = log.coefficients();
        double[] tValue = new double[std.length];
        for (int i = 0; i < std.length; i++) {
            tValue[i] = t.tValue(coef[i][0], std[i]);
        }

        double[][] pValue = new double[tValue.length][2];
        for (int i = 0; i < tValue.length; i++) {
            //freedom is going to be massive anyway due to the high amount of individuals vs 
            //attributes after selection so not bothereing to actually calculate them. and just picking the
            //the highest in my table in the book: Probability and Statisics for Engineers and Scientists by  Walpole et all, 8th edition
            pValue[i][1] = t.pValue(tValue[i]);
            pValue[i][0] = tValue[i];
        }
        return pValue;
    }
    
    

}
