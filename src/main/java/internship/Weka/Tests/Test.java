/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.Tests;

import internship.Weka.Data;
import internship.Weka.Result;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import internship.Weka.Settings;
import internship.Weka.WekaMain;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A number of scripts to run tests. Basicly doesn't do much more than do a number of runs with different settings.
 * Not bothering with commenting the specifics as it isn't too interesting for future work anyway.
 * @author Florian
 */
public class Test {

    final static Charset ENCODING = StandardCharsets.UTF_8;
    public WekaMain weka;
    public ArrayList<String> classType;
    public ArrayList<Integer> periods;
    public String filters;
    public String path;

    public Test(String baseLinePath, String followUpPath, String bioMarkersPath, String targetLocation, ArrayList<String> unwanted, String hospitalPath) throws FileNotFoundException, IOException, Exception {
        weka = new WekaMain(baseLinePath, followUpPath, bioMarkersPath, targetLocation, unwanted, hospitalPath);
        weka.filterRecombination = weka.filterRecombination.intersection;
        filters = weka.filterRecombination.toString();
        path = "C:\\Temp\\" + filters + "\\filtered_logReg_" + filters + ".csv";


        weka.run();
        classType = new ArrayList<String>();
        classType.add("normal");
        classType.add("unknownData");
        classType.add("unknownTime");
        periods = new ArrayList<Integer>();
        periods.add(0);
        periods.add(1);
        periods.add(2);
        periods.add(3);
        periods.add(4);
        periods.add(5);
        weka.run();
    }
    
    public void compareIndividuals(String classifier, String path) throws Exception {
        int NN = 5;
        int numTrees = 50;
        int numAtt = 30;
        double ridge = 0.1;
        int minLeaves = 10;
        int numForestAtt = 30;
        weka.numNeighbours = NN;
        weka.classifier = classifier;
        weka.minInLeave = "" + minLeaves;
        weka.numAtt = "" + numAtt;
        weka.numTree = numTrees;
        weka.numForestAtt = numForestAtt;
        weka.ridgeFactor = ridge;
        weka.pruned = false;

        weka.buildClassifier();
        weka.voteType = weka.voteType.averageProb;
        ArrayList<ArrayList<String>> text = new ArrayList<ArrayList<String>>();
        ArrayList<String> current = new ArrayList<String>();
        ArrayList<String> enter = new ArrayList<String>();
        current.add("Settings: ");
        current.add("#NN " + NN);
        current.add("#Trees " + numTrees);
        current.add("#Att " + numAtt);
        current.add("Ridge " + ridge);
        current.add("#min inst/leave " + minLeaves);
        current.add("#ForestAtt " + numForestAtt);
        text.add(current);
        current = new ArrayList<String>();
        current.add("Alpha:");
        current.add("25%");
        current.add("20%");
        current.add("15%");
        current.add("10%");
        current.add("5%");
        current.add("2.5%");
        current.add("1%");
        current.add("0.5%");
        current.add("0.25");
        current.add("0.1%");
        current.add("0.05%");
        text.add(current);
        current = new ArrayList<String>();
        current.add("Tvalue:");
        current.add("0.674");
        current.add("0.842");
        current.add("1.036");
        current.add("1.282");
        current.add("1.645");
        current.add("1.960");
        current.add("2.326");
        current.add("2.576");
        current.add("2.807");
        current.add("3.090");
        current.add("3.291");
        enter.add(" ");
        text.add(current);
        current = new ArrayList<String>();
        ArrayList<Integer[]> period = new ArrayList<Integer[]>();
        for (int i = 0; i < periods.size() - 1; i++) {
            for (int j = i + 1; j < periods.size(); j++) {
                period.add(new Integer[2]);
                period.get(period.size() - 1)[0] = periods.get(i);
                period.get(period.size() - 1)[1] = periods.get(j);
            }
        }
        current.add(" ");
        for (int i = 0; i < period.size(); i++) {
            current.add("period " + period.get(i)[0] + " - " + period.get(i)[1]);
        }
        text.add(enter);
        text.add(enter);
        text.add(current);
        current = new ArrayList<String>();
        for (int i = 0; i < period.size(); i++) {
            current.add("period " + period.get(i)[0] + " - " + period.get(i)[1]);
            for (int j = 0; j < period.size(); j++) {
                if (j <= i || period.get(i)[1] != period.get(j)[1]) {
                    current.add("N.A.");
                } else {
                    current.add("" + weka.compareIndiv(period.get(i)[0], period.get(i)[1], period.get(j)[0], period.get(j)[1]).tvalue);
                }
            }
            text.add(current);
            current = new ArrayList<String>();
        }

        writeExcelFile(path, text);

    }

    public void runTests() throws Exception {
        ArrayList<String> classifiers = new ArrayList<String>();
        ArrayList<ArrayList<String>> text = new ArrayList<ArrayList<String>>();

        int NN = 5;
        int numTrees = 10;
        int numAtt = 30;
        double maxIt = 0.1;
        int minLeaves = 10;
        int numForestAtt = 30;

        text = listComparison("J48", NN, numTrees, numAtt, maxIt, minLeaves, numForestAtt, true);
        ArrayList<ArrayList<String>> text2 = listComparison("randomTree", NN, numTrees, numAtt, maxIt, minLeaves, numForestAtt, false);
//        for (int i = 0; i < text2.size(); i++) {
//            text.get(i).addAll(text2.get(i));
//        }
//
//        for (int j = 10; j <= 70; j += 10) {
//            text2 = listComparison("randomForest", NN, j, numAtt, maxIt, minLeaves, numForestAtt, false);
//            for (int i = 0; i < text2.size(); i++) {
//                text.get(i).addAll(text2.get(i));
//            }
//        }
//        text2 = listComparison("NN", NN, numTrees, numAtt, maxIt, minLeaves, numForestAtt, false);
//        for (int i = 0; i < text2.size(); i++) {
//            text.get(i).addAll(text2.get(i));
//        }
        for (double j = 0.0; j <= 20.0; j++) {
            text2 = listComparison("logReg", NN, numTrees, numAtt, (maxIt * j), minLeaves, numForestAtt, false);
            System.out.println(j);
            for (int i = 0; i < text2.size(); i++) {
                text.get(i).addAll(text2.get(i));
            }
        }
//        }
//        text2 = listComparison("naiveBayes", NN, numTrees, numAtt, maxIt, minLeaves, numForestAtt, false);
//        for (int i = 0; i < text2.size(); i++) {
//            text.get(i).addAll(text2.get(i));
//        }
//        text2 = listComparison("SVM", NN, numTrees, numAtt, maxIt, minLeaves, numForestAtt, false);
//        for (int i = 0; i < text2.size(); i++) {
//            text.get(i).addAll(text2.get(i));
//        }
//        text2 = listComparison("AdaBooster", NN, numTrees, numAtt, maxIt, minLeaves, numForestAtt, false);
//        for (int i = 0; i < text2.size(); i++) {
//            text.get(i).addAll(text2.get(i));
//        }
        ArrayList<String> current = new ArrayList<String>();
        ArrayList<String> enter = new ArrayList<String>();
        enter.add(" ");
        current.add("Settings: ");
        current.add("#NN " + NN);
        current.add("#Trees " + numTrees);
        current.add("#Att " + numAtt);
        current.add("Ridge " + maxIt);
        current.add("#min inst/leave " + minLeaves);
        current.add("#ForestAtt " + numForestAtt);

        text.add(0, enter);
        text.add(0, current);
        writeExcelFile(path, text);

    }

    public ArrayList<ArrayList<String>> listComparison(String classifier, int NN, int numTrees, int numAtt, double Ridge, int minLeaves, int numForestAtt, boolean first) throws Exception {
        ArrayList<ArrayList<String>> text = new ArrayList<ArrayList<String>>();
        ArrayList<String> current = new ArrayList<String>();
        weka.numNeighbours = NN;
        weka.classifier = classifier;
        weka.minInLeave = "" + minLeaves;
        weka.numAtt = "" + numAtt;
        weka.numTree = numTrees;
        weka.numForestAtt = numForestAtt;
        weka.ridgeFactor = Ridge;
        weka.pruned = false;
        weka.buildClassifier();
        weka.voteType = weka.voteType.averageProb;
        weka.buildClassifier();
        ArrayList<String> enter = new ArrayList<String>();
        enter.add(" ");
        if (first) {
            current.add("");
        }

        text.add(current);
        text.add(enter);
        current = new ArrayList<String>();
        for (int start = 0; start < periods.size() - 1; start++) {

            for (int end = start + 1; end < periods.size(); end++) {
                enter.add(" ");
                if (first) {
                    current.add("");
                }
                text.add(enter);
                text.add(enter);
                if (classifier.equals("randomForest")) {
                    current.add(classifier + numTrees);
                } else if (classifier.equals("logReg")) {
                    current.add(classifier + Ridge);
                }else {
                    current.add(classifier);
                }
                
                text.add(current);

                current = new ArrayList<String>();
                if (first) {
                    current.add("Classifiers for the period: " + periods.get(start) + "-" + periods.get(end));
                    text.add(current);
                    current = new ArrayList<String>();

                }
                current = new ArrayList<String>();
                if (first) {
                    current.add("Classifiers");
                    current.add("AUC ");
                    text.add(current);
                } else {
                    current.add("AUC");
                    text.add(enter);
                    text.add(current);

                }
                current = new ArrayList<String>();
                for (int k = 0; k < weka.sets.size(); k++) {
                    if (weka.sets.get(k).start >= periods.get(start) && weka.sets.get(k).end == periods.get(end)) {
                        double roc = 0.0;
                        weka.sets.get(k).crossValidate();
                        roc = weka.sets.get(k).getAUC();
                        if (first) {
                            current.add("Classifier from " + weka.sets.get(k).start + "-" + periods.get(end));
                        }
                        current.add("" + roc);
                        text.add(current);
                        current = new ArrayList<String>();
                    }
                }
                Result res = weka.testEnsemble(periods.get(start), periods.get(end), 0, 5, true);
                for (int i = 0; i < weka.sets.size(); i++) {
                    if (weka.sets.get(i).start >= periods.get(start) && weka.sets.get(i).end == periods.get(end)) {
                        //writeAttributeFile("C:\\Temp\\" + filters + "\\" + classifier + "\\attributes_" + classifier + "_" + weka.sets.get(i).start + "-" + weka.sets.get(i).end + "_" + filters + "_ensemble_" + periods.get(start) + "-" + periods.get(end) + ".txt", weka.sets.get(i));
                    }
                }
                if (first) {
                    current.add("Ensemble Classifier from " + periods.get(start) + "-" + periods.get(end));
                }
                current.add("" + res.calcAUC());
                text.add(current);
                current = new ArrayList<String>();
                System.out.println("done another " + start + " " + end);
            }
        }
        return text;


    }

    public void writeAttributeFile(String aFileName, Data d) throws IOException {
        Path path = Paths.get(aFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {
            writer.write("There are a total of " + d.data.numAttributes() + " attributes with the following filter" + filters);
            writer.newLine();
            for (int i = 0; i < d.data.numAttributes(); i++) {
                writer.write(d.data.attribute(i).name());
                writer.newLine();
            }


        }
    }

    public void writeExcelFile(String aFileName, ArrayList<ArrayList<String>> text) throws IOException {
        Path path = Paths.get(aFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {

            for (int i = 0; i < text.size(); i++) {
                String s = "";
                for (int j = 0; j < text.get(i).size(); j++) {
                    if (j == 0) {
                        s += text.get(i).get(j);
                    } else {
                        s += ";" + text.get(i).get(j);
                    }
                }
                writer.write(s);
                writer.newLine();
            }
        }
    }
}
