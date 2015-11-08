/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ChiSquare;

import internship.Weka.Data;
import weka.core.Instances;

/**
 *
 * @author Florian
 */
public class Chi {

    private double chisqr(int Dof, double Cv) {
        if (Cv < 0 || Dof < 1) {
            return 0.0;
        }
        double K = ((double) Dof) * 0.5;
        double X = Cv * 0.5;
        if (Dof == 2) {
            return Math.exp(-1.0 * X);
        }
        
        double PValue = igf(K, X);
        if (PValue == Double.NaN || PValue == Double.POSITIVE_INFINITY || PValue <= 1e-8) {
            return 1e-14;
        }

        PValue /= gamma(K);
        //PValue /= tgamma(K); 

        return (1.0 - PValue);
    }

    static double igf(double S, double Z) {
        if (Z < 0.0) {
            return 0.0;
        }
        double Sc = (1.0 / S);
        Sc *= Math.pow(Z, S);
        Sc *= Math.exp(-Z);

        double Sum = 1.0;
        double Nom = 1.0;
        double Denom = 1.0;

        for (int I = 0; I < 200; I++) {
            Nom *= Z;
            S++;
            Denom *= S;
            Sum += (Nom / Denom);
        }

        return Sum * Sc;
    }

    private double gamma(double x) {
      double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
      double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                       + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                       +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
      return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));

    }

    public double[] ChiValues(Instances data) throws Exception {
        double[] list = new double[data.numAttributes() - 1];
        ChiSquaredAttributeEval chi = new ChiSquaredAttributeEval();
        chi.buildEvaluator(data);
        for (int i = 1; i < data.numAttributes(); i++) {
            list[i] = chi.evaluateAttribute(i);
        }
        return list;
    }
    
    public double[] PValues(Instances d) throws Exception{
        Instances data = new Instances(d);
        data.deleteAttributeAt(0);
        double[] pValues = new double[data.numAttributes() - 1];
        double[] chiValues = ChiValues(d);
        for(int i = 0; i < pValues.length;i++){
            pValues[i] = chisqr(data.numAttributes()-1 ,chiValues[i]);
        }
        return pValues;
    }
}
