/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Contains the results of an ensemble. The confusion matrix etc. Coors are the
 * coordinates within the AUC graph.
 *
 * @author Florian
 */
public class Result implements Serializable {

    public double[][] cMatrix;
    public int veto;
    public int measurement;
    public double value;
    public ArrayList<IndividualClass> votes = new ArrayList<IndividualClass>();
    public ArrayList<double[]> coors = new ArrayList<double[]>();

    public Result(double[][] cMatrix, int veto, int measurement, double value) {
        this.cMatrix = cMatrix;
        this.veto = veto;
        this.measurement = measurement;
        this.value = value;
    }

    public Result(int[][] cMatrix, int veto, int measurement, double value) {
        this.cMatrix = new double[2][2];
        this.cMatrix[0][0] = cMatrix[0][0];
        this.cMatrix[0][1] = cMatrix[0][1];
        this.cMatrix[1][0] = cMatrix[1][0];
        this.cMatrix[1][1] = cMatrix[1][1];
        this.veto = veto;
        this.measurement = measurement;
        this.value = value;
    }

    public Result() {
        cMatrix = new double[2][2];
        //random initiation that is awefull so the tests are always better
        cMatrix[1][0] = 100;
        cMatrix[0][1] = 100;
        cMatrix[0][0] = 0;
        cMatrix[1][1] = 0;
        //to ensure calcAUC actually ends..
    }
    //used for a random initation, otherwise the drawing goes wrong.

    public void work() {

        addIndividual("a", 1, true);
        addIndividual("a", 0.999, true);
        addIndividual("a", 0.999, true);
        addIndividual("a", 0.973, true);
        addIndividual("a", 0.568, false);
        addIndividual("a", 0.421, true);
        addIndividual("a", 0.382, false);
        addIndividual("a", 0.377, true);
        addIndividual("a", 0.146, false);
        addIndividual("a", 0.11, false);
    }

    //a method that keeps track of the way something was classified & it's actual classification
    public void addIndividual(String in, double vote, boolean trueClass) {
        boolean placed = false;

        if (votes.size() == 0) {
            placed = true;
            votes.add(new IndividualClass(in, vote, trueClass));
        }

        for (int i = 0; i < votes.size(); i++) {

            if (votes.get(i).classification < vote) {
                votes.add(i, new IndividualClass(in, vote, trueClass));
                placed = true;
                break;
            }
        }
        if (!placed) {
            votes.add(new IndividualClass(in, vote, trueClass));
        }


    }

    public double calcAUC2() {
        if (votes.size() == 0) {
            return 0.0;
        }
        double totalPos = 0.0;
        double totalNeg = 0.0;
        double count = 0.0;
        for (int i = 0; i < votes.size(); i++) {
            if (votes.get(i).trueClass) {
                totalPos++;
            } else {
                totalNeg++;
            }
        }
        ArrayList<Double> checked = new ArrayList<Double>();
        for (int i = 0; i < votes.size(); i++) {
            double cur = votes.get(i).classification;
            if (checked.contains(cur)) {
                continue;
            } else {
                checked.add(cur);     
                if (votes.get(i).trueClass) {
                    for (int j = 0; j < votes.size(); j++) {
                        if (votes.get(j).classification >= cur && !votes.get(j).trueClass) {
                            count++;
                        }
                    }
                }
            }
        }
        return 1 - count / (totalPos * totalNeg);
    }
    //AUC calculation

    public double calcAUC() {
        if (votes.size() == 0) {
            return 0.0;
        }
        double totalPos = 0.0;
        double totalNeg = 0.0;
        for (int i = 0; i < votes.size(); i++) {
            if (votes.get(i).trueClass) {
                totalPos++;
            } else {
                totalNeg++;
            }
        }
        double unique = 0;

        double AUC = 0.0;
        double preTreshold = 2.0;

        double[] cors = new double[2];
        cors[0] = 0;
        cors[1] = 0;
        coors.add(cors);
        for (int i = 0; i < votes.size(); i++) {
            if (preTreshold != votes.get(i).classification) {
                preTreshold = votes.get(i).classification;
                double TP = 0.0;
                double FP = 0.0;
                for (int j = 0; j < votes.size(); j++) {
                    if (votes.get(j).classification >= preTreshold) {
                        if (votes.get(j).trueClass) {
                            TP++;
                        } else {
                            FP++;
                        }
                    }
                }
                TP /= totalPos;
                FP /= totalNeg;
                double[] cor = new double[2];
                cor[0] = TP;
                cor[1] = FP;
                coors.add(cor);
            }
        }
        double[] cors2 = new double[2];
        cors2[0] = 1;
        cors2[1] = 1;
        int l = 0;
        while (l < coors.size() - 1) {
            double bestSlope = -1.0;
            int cur = 0;
            for (int j = l + 1; j < coors.size(); j++) {
                double a = Math.atan((coors.get(j)[0] - coors.get(l)[0]) / (coors.get(j)[1] - coors.get(l)[1]));
                if (a > bestSlope) {
                    bestSlope = a;
                    cur = j;
                }
            }
            AUC += (coors.get(cur)[1] - coors.get(l)[1]) * (coors.get(cur)[0] + coors.get(l)[0]) / 2.0;
            l = cur;
        }

        return AUC;
    }
    //True positive rate

    public double TPR() {
        return (double) cMatrix[0][0] / (double) (cMatrix[0][0] + cMatrix[1][0]);
    }
    //True Negative rate

    public double TNR() {
        return (double) cMatrix[1][1] / (double) (cMatrix[1][1] + cMatrix[0][1]);
    }
    //Accuracy

    public double ACC() {
        return (double) (cMatrix[1][1] + cMatrix[0][0]) / (double) (cMatrix[1][1] + cMatrix[0][1] + cMatrix[0][0] + cMatrix[1][0]);
    }
    //False positive rate

    public double FPR() {
        return 1.0 - (double) cMatrix[1][1] / (double) (cMatrix[1][1] + cMatrix[0][1]);
    }
    //False Negative rate

    public double FNR() {
        return 1.0 - (double) cMatrix[0][0] / (double) (cMatrix[0][0] + cMatrix[1][0]);
    }

    public static void main(String[] args) {
        Result r = new Result();
        r.work();
        System.out.println(r.calcAUC2());
    }
}
