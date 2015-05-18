/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.WrapperAndClassifier;

import java.util.BitSet;
import java.util.Enumeration;
import java.util.Random;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.SubsetEvaluator;
import weka.attributeSelection.WrapperSubsetEval;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author Florian
 */
public class BehemothWrapper extends ASEvaluation implements SubsetEvaluator, OptionHandler, TechnicalInformationHandler {

    private Instances trainInstances;
    private int classIndex;
    private int numAtt;
    private int numInstances;
    private int folds = 10;
    private int seed;
    private BehemothClassifier classifier;

    @Override
    public void buildEvaluator(Instances data) throws Exception {
        getCapabilities().testWithFail(data);
        trainInstances = data;
        classIndex = trainInstances.classIndex();
        numAtt = trainInstances.numAttributes();
        numInstances = trainInstances.numInstances();


    }

    @Override
    public double evaluateSubset(BitSet subset) throws Exception {
        int numAttributes = 0;
        int i = 0;
        int j =0;
        Random Rnd = new Random(seed);
        Remove delTransform = new Remove();
        delTransform.setInvertSelection(true);
        // copy the instances
        Instances trainCopy = new Instances(trainInstances);
        // count attributes set in the BitSet
        for (i = 0; i < numAtt; i++) {
            if (subset.get(i)) {
                numAttributes++;
            }
        }
        // set up an array of attribute indexes for the filter (+1 for the class)
        int[] featArray = new int[numAttributes + 1];
        for (i = 0, j = 0; i < numAtt; i++) {
            if (subset.get(i)) {
                featArray[j++] = i;
            }
        }
        featArray[j] = classIndex;
        delTransform.setAttributeIndicesArray(featArray);
        delTransform.setInputFormat(trainCopy);
        trainCopy = Filter.useFilter(trainCopy, delTransform);
        System.out.println(trainCopy.numAttributes());
        classifier.buildClassifier(trainCopy);            
        return classifier.getAverageAUC(folds);

    }

    public Capabilities getCapabilities() {
        Capabilities result;
        if (getClassifier() == null) {

            result = super.getCapabilities();
            result.disableAll();

        } else {

            result = getClassifier().getCapabilities();

        }

        for (Capability cap : Capability.values()) {
            result.enableDependency(cap);
        }
        result.setMinimumNumberInstances(getFolds());
        return result;

    }

    public void setClassifier(BehemothClassifier c) {
        this.classifier = c;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    @Override
    public Enumeration listOptions() {
        return null;

    }

    public void setFolds(int f) {
        this.folds = f;
    }

    public int getFolds() {
        return folds;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getSeed() {
        return seed;
    }

    @Override
    public void setOptions(String[] strings) throws Exception {
    }

    @Override
    public String[] getOptions() {
        return null;

    }

    @Override
    public TechnicalInformation getTechnicalInformation() {
        return null;

    }
}