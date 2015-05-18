/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka;


import java.io.Serializable;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Contains the data of each step in the ensemble.
 * @author Florian
 */
public class Data implements  Serializable{

    public Instances data; //data currently in use, possibly adapted using feature selection for the current relevant ensemble
    public int measurement, start, end;
    public Classifier classifier; //classifier in use
    public int folds = 10;
    public int seed = 100;
    public double AUC = 0;
    public double[][] cM = new double[2][2];
    public Result res;
    
    public Instances original; // original data
    public Instances filtered; // data with feature selection used just for this individual step
    public Data(Instances data, int m, int start, int end) throws Exception {
        this.data = data;
        this.original = data;
        this.filtered = data;
        this.measurement = m;
        this.start = start;
        this.end = end;
    }

    public Data() {
    }

    public void crossValidate() throws Exception {
        res = new Result();
        cM = new double[2][2];
        for (int i = 0; i < folds; i++) {
            Instances randData = new Instances(data);
            Instances train = randData.trainCV(folds, i);
            Instances test = randData.testCV(folds, i);
            classifier.buildClassifier(train);
            for (int j = 0; j < test.numInstances(); j++) {
                Double vote = classifier.distributionForInstance(test.instance(j))[1];
                boolean trueClass;
                if (test.instance(j).stringValue(test.numAttributes() - 1).equals("True")) {
                    trueClass = true;
                    if (vote >= 0.5) {
                        cM[0][0]++;
                    } else {
                        cM[1][0]++;
                    }
                } else {
                    if (vote >= 0.5) {
                        cM[0][1]++;
                    } else {
                        cM[1][1]++;
                    }
                    trueClass = false;
                }
                res.addIndividual(test.instance(j).stringValue(0), vote, trueClass);
            }
        }

    }

    public double[][] crossvalidateCMatrix() {
        return cM;
    }

    public double getAUC() throws Exception {
        return res.calcAUC();
    }
    

}
