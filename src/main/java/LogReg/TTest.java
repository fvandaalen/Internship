/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package LogReg;

/**
 * freedom is going to be massive anyway due to the high amount of individuals vs 
    attributes especially after selection so not bothereing to actually calculate them. and just picking the
    the highest in my table in the book: Probability and Statisics for Engineers and Scientists by  Walpole et all, 8th edition
    * Actual freedom is like 600- 200-1 with no feature selection so yea...
 * @author Florian
 */
public class TTest {

    double s;
    double[][] pvalue; //p[n][0] is the Tvalue, p[n][1] the corresponding P
    public TTest(double s) {
        this.s = s;
        initiateP();
    }
    
    private void initiateP(){
        pvalue = new double[14][2];
        pvalue[0][0] = 0.253;
        pvalue[0][1] = 0.40;
        pvalue[1][0] = 0.524;
        pvalue[1][1] = 0.30;
        pvalue[2][0] = 0.842;
        pvalue[2][1] = 0.20;
        pvalue[3][0] = 1.036;
        pvalue[3][1] = 0.15;
        pvalue[4][0] = 1.282;
        pvalue[4][1] = 0.10;
        pvalue[5][0] = 1.645;
        pvalue[5][1] = 0.05;
        pvalue[6][0] = 1.960;
        pvalue[6][1] = 0.025;
        pvalue[7][0] = 2.054;
        pvalue[7][1] = 0.02;
        pvalue[8][0] = 2.170;
        pvalue[8][1] = 0.015;
        pvalue[9][0] = 2.326;
        pvalue[9][1] = 0.01;
        pvalue[10][0] = 2.432;
        pvalue[10][1] = 0.0075;
        pvalue[11][0] = 2.576;
        pvalue[11][1] = 0.005;
        pvalue[12][0] = 2.807;
        pvalue[12][1] = 0.0025;
        pvalue[13][0] = 3.290;
        pvalue[13][1] = 0.0005;
    }

    public double tValue(double b, double std) {
        return b / (s * Math.sqrt(std));
    }
   
    public double pValue(double tValue) {
        for(int i = 0; i < pvalue.length;i++){
            if(tValue <= pvalue[i][0]){
                return pvalue[i][1];
            }
        }
        //if you got here the T value is higher than the highest possible in my table so just
        //return that one
        return pvalue[13][1];
    }
}
