/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.Tests;

import internship.Weka.WekaMain;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

/**
 *
 * @author Florian
 */
public class WrapperTest {

    public void wrapperTest() {
//        "C:\\Florian\\Universiteit\\Internship\\TIME-CHF\\CVS files\\TIME-CHF_baseline.csv"; //"/home/fvandalen/Temp/TIME-CHF_baseline.csv";
//        String followUpPath = "C:\\Florian\\Universiteit\\Internship\\TIME-CHF\\CVS files\\TIME_CHF_follow_up.csv"; //"/home/fvandalen/Temp/TIME_CHF_follow_up.csv";
//        String bioMarkersPath = "C:\\Florian\\Universiteit\\Internship\\TIME-CHF\\CVS files\\TIME-CHF_Biomarkers.csv"; //"/home/fvandalen/Temp/TIME-CHF_Biomarkers.csv";
//        String targetLocation = "C:\\Temp\\";// "/home/fvandalen/Temp/Ensemble/";
//        String hospitalLocation = "C:\\Florian\\Universiteit\\Internship\\TIME-CHF\\CVS files\\HF_hospitalization.csv"; //"/home/fvandalen/Temp/HF_hospitalization.csv";
//        

        String baseLinePath = "/home/fvandalen/Temp/TIME-CHF_baseline.csv";
        String followUpPath = "/home/fvandalen/Temp/TIME_CHF_follow_up.csv";
        String bioMarkersPath = "/home/fvandalen/Temp/TIME-CHF_Biomarkers.csv";
        String targetLocation = "/home/fvandalen/Temp/Ensemble/";
        String hospitalLocation = "/home/fvandalen/Temp/HF_hospitalization.csv";
        int maxFU = 5;
        String testType = "normal";

        boolean prune = false;
        String classifier = "logReg";

        String numAttributes = "10";
        String minLeavesRandom = "1";
        ArrayList<String> unwanted = new ArrayList<String>();
        unwanted.add("Centre");
        unwanted.add("PatientNumber");
        unwanted.add("DOB");
        unwanted.add("Confession");
        unwanted.add("Atheist");
        unwanted.add("CADYear");
        unwanted.add("DateMIyear");
        unwanted.add("DateMImonth");
        unwanted.add("DatePTCAyear");
        unwanted.add("DatePTCAmonth");
        unwanted.add("DateCABGyear");
        unwanted.add("DateCABGmonth");
        unwanted.add("DCMYear");
        unwanted.add("HypertensionYear");
        unwanted.add("ValvularYear");
        unwanted.add("OtherHDComment");
        unwanted.add("OtherYear");
        unwanted.add("DmYear");
        unwanted.add("CancerYear");
        unwanted.add("CancerComment");
        unwanted.add("DateDis");
        unwanted.add("Meals");
        unwanted.add("Housework");
        unwanted.add("Cost_imputation_selection");
        unwanted.add("FU_costs");
        unwanted.add("Cost_imputation_selectionB");
        unwanted.add("Cost_patient_selection");
        unwanted.add("Rehab_resource");
        unwanted.add("Hosp_resource");
        unwanted.add("ICU_resource");
        unwanted.add("EH_resource");
        unwanted.add("NH_resource");
        unwanted.add("Proc_resource");
        unwanted.add("Visits_resource");
        unwanted.add("SomVanVisite_ext_home");
        unwanted.add("Proc_CV_resource");
        unwanted.add("Proc_total_resource");
        unwanted.add("TimeRandtoBL");
        try {
            WekaMain weka = new WekaMain(baseLinePath, followUpPath, bioMarkersPath, targetLocation, unwanted, prune, classifier, numAttributes,
                    minLeavesRandom, hospitalLocation);
            weka.hospitalization = true;
            weka.baseFilter =  weka.baseFilter.univariate;
            weka.filterRecombination = weka.filterRecombination.none;
            weka.selected = 20;
            weka.run();
            int numTree = 0;
            int numForestAtt = 0;
            double ridgeFactor = 0.1;
            int numNeighbours = 0;
            weka.setClassifierAtt(prune, Integer.parseInt(numAttributes), Integer.parseInt(minLeavesRandom), numTree, numForestAtt, numNeighbours, ridgeFactor);
            weka.voteType = weka.voteType.averageProb;

            weka.Test123();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println(e.getStackTrace());
            e.printStackTrace();
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(frame, "Something went wrong, please check if the paths given are valid");
        }
    }
    
    public static void main(String[] args){
        WrapperTest t = new WrapperTest();
        t.wrapperTest();
    }
}
