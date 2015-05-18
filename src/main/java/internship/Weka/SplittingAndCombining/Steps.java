/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.SplittingAndCombining;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Recombines the individual steps into the relevant classifiers.
 * @author Florian
 */
public class Steps {

    public ArrayList<ArrayList<String>> bL;
    public ArrayList<ArrayList<String>> v1;
    public ArrayList<ArrayList<String>> v3;
    public ArrayList<ArrayList<String>> v6;
    public ArrayList<ArrayList<String>> v12;
    public ArrayList<ArrayList<String>> v18;
    public ArrayList<ArrayList<String>> other;
    public ArrayList<ArrayList<String>> otherFollow;
    final static Charset ENCODING = StandardCharsets.UTF_8;
    public ArrayList<ArrayList<String>> base1;
    public ArrayList<ArrayList<String>> base3;
    public ArrayList<ArrayList<String>> base6;
    public ArrayList<ArrayList<String>> base12;
    public ArrayList<ArrayList<String>> base18;
    public ArrayList<ArrayList<String>> one3;
    public ArrayList<ArrayList<String>> one6;
    public ArrayList<ArrayList<String>> one12;
    public ArrayList<ArrayList<String>> one18;
    public ArrayList<ArrayList<String>> three6;
    public ArrayList<ArrayList<String>> three12;
    public ArrayList<ArrayList<String>> three18;
    public ArrayList<ArrayList<String>> six12;
    public ArrayList<ArrayList<String>> six18;
    public ArrayList<ArrayList<String>> twelve18;
    public String[][] death;
    public ArrayList<String[]> hospitalization;
    public String hospitalPath;
    private String basePath;
    private String followPath;
    private String bioPath;
    private String targetLocation;
    private boolean hospitazliation;

    public Steps(String basePath, String followPath, String bioPath, String targetLocation, String hospitalPath, boolean hospitazliation) {
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
        this.hospitalPath = hospitalPath;
        this.hospitazliation = hospitazliation;
    }

    public void doWork() throws IOException {
        bL = readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_BL.csv");
        v1 = readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V1.csv");
        v3 = readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V3.csv");
        v6 = readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V6.csv");
        v12 = readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V12.csv");
        v18 = readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V18.csv");
        other = readLargerTextFile(targetLocation + "\\TIME-CHF_Baseline.csv");
        otherFollow = readLargerTextFile(followPath);

        removeSpacesInFirstCollumn();

        listDeath();
        makeFirstRow();
        makeBase();
        makeOne();
        makeThree();
        makeSix();
        makeTwelve();

        Hospital hos = new Hospital();
        hospitalization = hos.hospitalization(hospitalPath, hospitazliation);

        combineOther();


        writeLargerCSVFile(targetLocation + "\\base1.csv", base1);
        writeLargerCSVFile(targetLocation + "\\base3.csv", base3);
        writeLargerCSVFile(targetLocation + "\\base6.csv", base6);
        writeLargerCSVFile(targetLocation + "\\base12.csv", base12);
        writeLargerCSVFile(targetLocation + "\\base18.csv", base18);
        writeLargerCSVFile(targetLocation + "\\one3.csv", one3);
        writeLargerCSVFile(targetLocation + "\\one6.csv", one6);
        writeLargerCSVFile(targetLocation + "\\one12.csv", one12);
        writeLargerCSVFile(targetLocation + "\\one18.csv", one18);
        writeLargerCSVFile(targetLocation + "\\three6.csv", three6);
        writeLargerCSVFile(targetLocation + "\\three12.csv", three12);
        writeLargerCSVFile(targetLocation + "\\three18.csv", three18);
        writeLargerCSVFile(targetLocation + "\\six12.csv", six12);
        writeLargerCSVFile(targetLocation + "\\six18.csv", six18);
        writeLargerCSVFile(targetLocation + "\\twelve18.csv", twelve18);


        writeLargerWekaCSVFile(targetLocation + "\\base1_weka.csv", base1);
        writeLargerWekaCSVFile(targetLocation + "\\base3_weka.csv", base3);
        writeLargerWekaCSVFile(targetLocation + "\\base6_weka.csv", base6);
        writeLargerWekaCSVFile(targetLocation + "\\base12_weka.csv", base12);
        writeLargerWekaCSVFile(targetLocation + "\\base18_weka.csv", base18);
        writeLargerWekaCSVFile(targetLocation + "\\one3_weka.csv", one3);
        writeLargerWekaCSVFile(targetLocation + "\\one6_weka.csv", one6);
        writeLargerWekaCSVFile(targetLocation + "\\one12_weka.csv", one12);
        writeLargerWekaCSVFile(targetLocation + "\\one18_weka.csv", one18);
        writeLargerWekaCSVFile(targetLocation + "\\three6_weka.csv", three6);
        writeLargerWekaCSVFile(targetLocation + "\\three12_weka.csv", three12);
        writeLargerWekaCSVFile(targetLocation + "\\three18_weka.csv", three18);
        writeLargerWekaCSVFile(targetLocation + "\\six12_weka.csv", six12);
        writeLargerWekaCSVFile(targetLocation + "\\six18_weka.csv", six18);
        writeLargerWekaCSVFile(targetLocation + "\\twelve18_weka.csv", twelve18);
    }

    void combineOther() {

        for (int i = 1; i < other.get(0).size(); i++) {
            if (other.get(0).get(i).contains("XX_") || other.get(0).get(i).contains("Dateofstudyentry")) {
                for (int j = 0; j < other.size(); j++) {
                    other.get(j).remove(i);
                }
            }
            String s = other.get(0).get(i) + "_baseLine_other_data";
            other.get(0).set(i, s);
        }
        //for some reason patientidea is written in some indescribable way differently so I need to add the variable names manually for this one cuz why the fuck not...
        for (int i = 1; i < other.get(0).size(); i++) {
            base1.get(0).add(other.get(0).get(i));
            base3.get(0).add(other.get(0).get(i));
            base6.get(0).add(other.get(0).get(i));
            base12.get(0).add(other.get(0).get(i));
            base18.get(0).add(other.get(0).get(i));
            one3.get(0).add(other.get(0).get(i));
            one6.get(0).add(other.get(0).get(i));
            one12.get(0).add(other.get(0).get(i));
            one18.get(0).add(other.get(0).get(i));
            three6.get(0).add(other.get(0).get(i));
            three12.get(0).add(other.get(0).get(i));
            three18.get(0).add(other.get(0).get(i));
            six12.get(0).add(other.get(0).get(i));
            six18.get(0).add(other.get(0).get(i));
            twelve18.get(0).add(other.get(0).get(i));
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < base1.size(); j++) {
                if (other.get(i).get(0).equals(base1.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        base1.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < base3.size(); j++) {
                if (other.get(i).get(0).equals(base3.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        base3.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < base6.size(); j++) {
                if (other.get(i).get(0).equals(base6.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        base6.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < base12.size(); j++) {
                if (other.get(i).get(0).equals(base12.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        base12.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < base18.size(); j++) {
                if (other.get(i).get(0).equals(base18.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        base18.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < one3.size(); j++) {
                if (other.get(i).get(0).equals(one3.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        one3.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < one6.size(); j++) {
                if (other.get(i).get(0).equals(one6.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        one6.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < one12.size(); j++) {
                if (other.get(i).get(0).equals(one12.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        one12.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < one18.size(); j++) {
                if (other.get(i).get(0).equals(one18.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        one18.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < three6.size(); j++) {
                if (other.get(i).get(0).equals(three6.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        three6.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < three12.size(); j++) {
                if (other.get(i).get(0).equals(three12.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        three12.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < three18.size(); j++) {
                if (other.get(i).get(0).equals(three18.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        three18.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < six12.size(); j++) {
                if (other.get(i).get(0).equals(six12.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        six12.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < six18.size(); j++) {
                if (other.get(i).get(0).equals(six18.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        six18.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }
        for (int i = 0; i < other.size(); i++) {
            for (int j = 0; j < twelve18.size(); j++) {
                if (other.get(i).get(0).equals(twelve18.get(j).get(0))) {
                    for (int k = 1; k < other.get(i).size(); k++) {
                        twelve18.get(j).add(other.get(i).get(k));
                    }
                }
            }
        }

        base1.get(0).add("Death or Hospitalization");
        base3.get(0).add("Death or Hospitalization");
        base6.get(0).add("Death or Hospitalization");
        base12.get(0).add("Death or Hospitalization");
        base18.get(0).add("Death or Hospitalization");
        one3.get(0).add("Death or Hospitalization");
        one6.get(0).add("Death or Hospitalization");
        one12.get(0).add("Death or Hospitalization");
        one18.get(0).add("Death or Hospitalization");
        three6.get(0).add("Death or Hospitalization");
        three12.get(0).add("Death or Hospitalization");
        three18.get(0).add("Death or Hospitalization");
        six12.get(0).add("Death or Hospitalization");
        six18.get(0).add("Death or Hospitalization");
        twelve18.get(0).add("Death or Hospitalization");
        int index = 0;
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
            for (int j = 1; j < base1.size(); j++) {
                if (otherFollow.get(i).get(0).equals(base1.get(j).get(0))) {
                    for (int k = 0; k < death.length; k++) {
                        boolean h = false;
                        for (int l = 0; l < hospitalization.size(); l++) {
                            if (base1.get(j).get(0).equals(hospitalization.get(l)[0])) {
                                if (hospitalization.get(l)[1].equals("True")) {
                                    h = true;
                                }
                            }
                        }
                        if (base1.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                base1.get(j).add("True");
                            } else if (a > 1) {
                                base1.get(j).add("False");
                            } else {
                                base1.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < base3.size(); j++) {
                if (otherFollow.get(i).get(0).equals(base3.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (base3.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[2].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (base3.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                base3.get(j).add("True");
                            } else if (a > 3) {
                                base3.get(j).add("False");
                            } else {
                                base3.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < base6.size(); j++) {
                if (otherFollow.get(i).get(0).equals(base6.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (base6.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[3].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (base6.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                base6.get(j).add("True");
                            } else if (a > 6) {
                                base6.get(j).add("False");
                            } else {
                                base6.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < base12.size(); j++) {
                if (otherFollow.get(i).get(0).equals(base12.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (base12.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[4].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (base12.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                base12.get(j).add("True");
                            } else if (a > 12) {
                                base12.get(j).add("False");
                            } else {
                                base12.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < base18.size(); j++) {
                if (otherFollow.get(i).get(0).equals(base18.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (base18.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[5].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    if (h) {
                        base18.get(j).add("True");
                    } else {
                        base18.get(j).add(s);
                    }
                }
            }

            for (int j = 1; j < one3.size(); j++) {
                if (otherFollow.get(i).get(0).equals(one3.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (one3.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[2].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (one3.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                one3.get(j).add("True");
                            } else if (a > 3) {
                                one3.get(j).add("False");
                            } else {
                                one3.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < one6.size(); j++) {
                if (otherFollow.get(i).get(0).equals(one6.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (one6.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[3].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (one6.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                one6.get(j).add("True");
                            } else if (a > 6) {
                                one6.get(j).add("False");
                            } else {
                                one6.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < one12.size(); j++) {
                if (otherFollow.get(i).get(0).equals(one12.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (one12.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[4].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (one12.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                one12.get(j).add("True");
                            } else if (a > 12) {
                                one12.get(j).add("False");
                            } else {
                                one12.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < one18.size(); j++) {
                if (otherFollow.get(i).get(0).equals(one18.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (one18.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[5].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    if (h) {
                        one18.get(j).add("True");
                    } else {
                        one18.get(j).add(s);
                    }
                }
            }
            for (int j = 1; j < three6.size(); j++) {
                if (otherFollow.get(i).get(0).equals(three6.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (three6.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[3].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (three6.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                three6.get(j).add("True");
                            } else if (a > 6) {
                                three6.get(j).add("False");
                            } else {
                                three6.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < three12.size(); j++) {
                if (otherFollow.get(i).get(0).equals(three12.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (three12.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[4].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (three12.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                three12.get(j).add("True");
                            } else if (a > 12) {
                                three12.get(j).add("False");
                            } else {
                                three12.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < three18.size(); j++) {
                if (otherFollow.get(i).get(0).equals(three18.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (three18.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[5].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    if (h) {
                        three18.get(j).add("True");
                    } else {
                        three18.get(j).add(s);
                    }
                }
            }
            for (int j = 1; j < six12.size(); j++) {
                if (otherFollow.get(i).get(0).equals(six12.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (six12.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[4].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    for (int k = 0; k < death.length; k++) {
                        if (six12.get(j).get(0).equals(death[k][0])) {
                            Double a = Double.parseDouble(death[k][1]);
                            if (h) {
                                six12.get(j).add("True");
                            } else if (a > 12) {
                                six12.get(j).add("False");
                            } else {
                                six12.get(j).add(s);
                            }
                        }
                    }
                }
            }
            for (int j = 1; j < six18.size(); j++) {
                if (otherFollow.get(i).get(0).equals(six18.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (six18.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[5].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    if (h) {
                        six18.get(j).add("True");
                    } else {
                        six18.get(j).add(s);
                    }
                }
            }
            for (int j = 1; j < twelve18.size(); j++) {
                if (otherFollow.get(i).get(0).equals(twelve18.get(j).get(0))) {
                    boolean h = false;
                    for (int l = 0; l < hospitalization.size(); l++) {
                        if (twelve18.get(j).get(0).equals(hospitalization.get(l)[0])) {
                            if (hospitalization.get(l)[5].equals("True")) {
                                h = true;
                            }
                        }
                    }
                    if (h) {
                        twelve18.get(j).add("True");
                    } else {
                        twelve18.get(j).add(s);
                    }
                }
            }
        }
    }

    void listDeath() {
        death = new String[v3.size() - 1][2];
        int entered = -1;
        int deathTime = -1;
        for (int i = 0; i < other.get(0).size(); i++) {
            if (other.get(0).get(i).equals("Dateofstudyentry")) {
                entered = i;
            }
        }
        for (int i = 0; i < otherFollow.get(0).size(); i++) {
            if (otherFollow.get(0).get(i).equals("Dateofdeath")) {
                deathTime = i;
            }
        }

        for (int i = 1; i < v3.size(); i++) {
            String date1 = "";
            String date2 = "";
            death[i - 1][0] = v3.get(i).get(0);

            for (int j = 1; j < otherFollow.size(); j++) {
                if (otherFollow.get(j).get(0).equals(death[i - 1][0])) {
                    date2 = otherFollow.get(j).get(deathTime);
                }
            }
            for (int j = 1; j < other.size(); j++) {
                if (other.get(j).get(0).equals(death[i - 1][0])) {
                    date1 = other.get(j).get(entered);
                }
            }
            date1 = date1.replace("-", "/");
            date2 = date2.replace("-", "/");
            String timePassed = "";
            if (!containsNumber(date2)) {
                timePassed = "18";
                death[i - 1][1] = timePassed;
                continue;
            }

            String day1 = "";
            String day2 = "";
            String month1 = "";
            String month2 = "";
            String year1 = "";
            String year2 = "";

            int count = 0;
            for (int j = 1; j <= date1.length(); j++) {
                if (date1.substring(j - 1, j).equals("/")) {
                    count++;
                } else if (count == 0) {
                    month1 += date1.substring(j - 1, j);
                } else if (count == 1) {
                    day1 += date1.substring(j - 1, j);
                } else if (count == 2) {
                    year1 += date1.substring(j - 1, j);
                }
            }
            count = 0;
            for (int j = 1; j <= date2.length(); j++) {
                if (date2.substring(j - 1, j).equals("/")) {
                    count++;
                } else if (count == 0) {
                    month2 += date2.substring(j - 1, j);
                } else if (count == 1) {
                    day2 += date2.substring(j - 1, j);
                } else if (count == 2) {
                    year2 += date2.substring(j - 1, j);
                }
            }
            Double yearDiff = Double.parseDouble(year2) - Double.parseDouble(year1);
            Double monthDiff = Double.parseDouble(month2) - Double.parseDouble(month1);
            Double dayDiff = Double.parseDouble(day2) - Double.parseDouble(day1);
            Double timeDiff = yearDiff * 12 + monthDiff;

            if (timeDiff <= 1) {
                timePassed = "1";
            } else if (timeDiff <= 3) {
                timePassed = "3";
            } else if (timeDiff <= 6) {
                timePassed = "6";
            } else if (timeDiff <= 12) {
                timePassed = "12";
            } else {
                timePassed = "18";
            }
            death[i - 1][1] = timePassed;
        }
    }

    void makeTwelve() {
        for (int i = 1; i < v12.size(); i++) {
            Double deathTime = 0.0;
            for (int j = 0; j < death.length; j++) {
                if (death[j][0].equals(v12.get(i).get(0))) {
                    deathTime = Double.parseDouble(death[j][1]);
                }
            }
            if (deathTime >= 18) {
                twelve18.add(new ArrayList<String>());
                int count = twelve18.size() - 1;
                for (int j = 0; j < v12.get(i).size(); j++) {
                    if (j == 0) {
                        twelve18.get(count).add(v12.get(i).get(j));
                    } else {
                        String s1 = v12.get(i).get(j);
                        twelve18.get(count).add(s1);
                    }
                }
            }
        }
    }

    void makeSix() {
        for (int i = 1; i < v6.size(); i++) {
            Double deathTime = 0.0;
            for (int j = 0; j < death.length; j++) {
                if (death[j][0].equals(v6.get(i).get(0))) {
                    deathTime = Double.parseDouble(death[j][1]);
                }
            }
            if (deathTime >= 18) {
                six18.add(new ArrayList<String>());
                int count = six18.size() - 1;
                for (int j = 0; j < v6.get(i).size(); j++) {
                    if (j == 0) {
                        six18.get(count).add(v6.get(i).get(j));
                    } else {
                        String s1 = v6.get(i).get(j);
                        six18.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 12) {
                six12.add(new ArrayList<String>());
                int count = six12.size() - 1;

                for (int j = 0; j < v6.get(i).size(); j++) {
                    if (j == 0) {
                        six12.get(count).add(v6.get(i).get(j));
                    } else {
                        String s1 = v6.get(i).get(j);
                        six12.get(count).add(s1);
                    }
                }
            }
        }
    }

    void makeThree() {
        for (int i = 1; i < v3.size(); i++) {
            Double deathTime = 0.0;
            for (int j = 0; j < death.length; j++) {
                if (death[j][0].equals(v3.get(i).get(0))) {
                    deathTime = Double.parseDouble(death[j][1]);
                }
            }
            if (deathTime >= 18) {
                three18.add(new ArrayList<String>());
                int count = three18.size() - 1;
                for (int j = 0; j < v3.get(i).size(); j++) {
                    if (j == 0) {
                        three18.get(count).add(v3.get(i).get(j));
                    } else {
                        String s1 = v3.get(i).get(j);
                        three18.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 12) {
                three12.add(new ArrayList<String>());
                int count = three12.size() - 1;

                for (int j = 0; j < v3.get(i).size(); j++) {
                    if (j == 0) {
                        three12.get(count).add(v3.get(i).get(j));
                    } else {
                        String s1 = v3.get(i).get(j);
                        three12.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 6) {
                three6.add(new ArrayList<String>());
                int count = three6.size() - 1;

                for (int j = 0; j < v3.get(i).size(); j++) {
                    if (j == 0) {
                        three6.get(count).add(v3.get(i).get(j));
                    } else {
                        String s1 = v3.get(i).get(j);
                        three6.get(count).add(s1);
                    }
                }
            }
        }
    }

    void makeOne() {
        for (int i = 1; i < v1.size(); i++) {
            Double deathTime = 0.0;
            for (int j = 0; j < death.length; j++) {
                if (death[j][0].equals(v1.get(i).get(0))) {
                    deathTime = Double.parseDouble(death[j][1]);
                }
            }
            if (deathTime >= 18) {
                one18.add(new ArrayList<String>());
                int count = one18.size() - 1;
                for (int j = 0; j < v1.get(i).size(); j++) {
                    if (j == 0) {
                        one18.get(count).add(v1.get(i).get(j));
                    } else {
                        String s1 = v1.get(i).get(j);
                        one18.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 12) {
                one12.add(new ArrayList<String>());
                int count = one12.size() - 1;

                for (int j = 0; j < v1.get(i).size(); j++) {
                    if (j == 0) {
                        one12.get(count).add(v1.get(i).get(j));
                    } else {
                        String s1 = v1.get(i).get(j);
                        one12.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 6) {
                one6.add(new ArrayList<String>());
                int count = one6.size() - 1;

                for (int j = 0; j < v1.get(i).size(); j++) {
                    if (j == 0) {
                        one6.get(count).add(v1.get(i).get(j));
                    } else {
                        String s1 = v1.get(i).get(j);
                        one6.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 3) {
                one3.add(new ArrayList<String>());
                int count = one3.size() - 1;

                for (int j = 0; j < v1.get(i).size(); j++) {
                    if (j == 0) {
                        one3.get(count).add(v1.get(i).get(j));
                    } else {
                        String s1 = v1.get(i).get(j);
                        one3.get(count).add(s1);
                    }
                }
            }
        }
    }

    void makeBase() {
        for (int i = 1; i < bL.size(); i++) {
            Double deathTime = 0.0;
            for (int j = 0; j < death.length; j++) {
                if (death[j][0].equals(bL.get(i).get(0))) {
                    deathTime = Double.parseDouble(death[j][1]);
                }
            }
            if (deathTime >= 18) {
                base18.add(new ArrayList<String>());
                int count = base18.size() - 1;
                for (int j = 0; j < bL.get(i).size(); j++) {
                    if (j == 0) {
                        base18.get(count).add(bL.get(i).get(j));
                    } else {
                        String s1 = bL.get(i).get(j);
                        base18.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 12) {
                base12.add(new ArrayList<String>());
                int count = base12.size() - 1;

                for (int j = 0; j < bL.get(i).size(); j++) {
                    if (j == 0) {
                        base12.get(count).add(bL.get(i).get(j));
                    } else {
                        String s1 = bL.get(i).get(j);
                        base12.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 6) {
                base6.add(new ArrayList<String>());
                int count = base6.size() - 1;

                for (int j = 0; j < bL.get(i).size(); j++) {
                    if (j == 0) {
                        base6.get(count).add(bL.get(i).get(j));
                    } else {
                        String s1 = bL.get(i).get(j);
                        base6.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 3) {
                base3.add(new ArrayList<String>());
                int count = base3.size() - 1;

                for (int j = 0; j < bL.get(i).size(); j++) {
                    if (j == 0) {
                        base3.get(count).add(bL.get(i).get(j));
                    } else {
                        String s1 = bL.get(i).get(j);
                        base3.get(count).add(s1);
                    }
                }
            }
            if (deathTime >= 1) {
                base1.add(new ArrayList<String>());
                int count = base1.size() - 1;

                for (int j = 0; j < bL.get(i).size(); j++) {
                    if (j == 0) {
                        base1.get(count).add(bL.get(i).get(j));
                    } else {
                        String s1 = bL.get(i).get(j);
                        base1.get(count).add(s1);
                    }
                }
            }


        }
    }

    void makeFirstRow() {
        base1 = new ArrayList<ArrayList<String>>();
        base1.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            base1.get(0).add(v3.get(0).get(i));
        }
        base3 = new ArrayList<ArrayList<String>>();
        base3.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            base3.get(0).add(v3.get(0).get(i));
        }
        base6 = new ArrayList<ArrayList<String>>();
        base6.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            base6.get(0).add(v3.get(0).get(i));
        }
        base12 = new ArrayList<ArrayList<String>>();
        base12.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            base12.get(0).add(v3.get(0).get(i));
        }
        base18 = new ArrayList<ArrayList<String>>();
        base18.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            base18.get(0).add(v3.get(0).get(i));
        }
        one3 = new ArrayList<ArrayList<String>>();
        one3.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            one3.get(0).add(v3.get(0).get(i));
        }
        one6 = new ArrayList<ArrayList<String>>();
        one6.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            one6.get(0).add(v3.get(0).get(i));
        }
        one12 = new ArrayList<ArrayList<String>>();
        one12.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            one12.get(0).add(v3.get(0).get(i));
        }
        one18 = new ArrayList<ArrayList<String>>();
        one18.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            one18.get(0).add(v3.get(0).get(i));
        }
        three6 = new ArrayList<ArrayList<String>>();
        three6.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            three6.get(0).add(v3.get(0).get(i));
        }
        three12 = new ArrayList<ArrayList<String>>();
        three12.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            three12.get(0).add(v3.get(0).get(i));
        }
        three18 = new ArrayList<ArrayList<String>>();
        three18.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            three18.get(0).add(v3.get(0).get(i));
        }
        six12 = new ArrayList<ArrayList<String>>();
        six12.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            six12.get(0).add(v3.get(0).get(i));
        }
        six18 = new ArrayList<ArrayList<String>>();
        six18.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            six18.get(0).add(v3.get(0).get(i));
        }
        twelve18 = new ArrayList<ArrayList<String>>();
        twelve18.add(new ArrayList<String>());
        for (int i = 0; i < v3.get(0).size(); i++) {
            twelve18.get(0).add(v3.get(0).get(i));
        }
    }

    ArrayList<ArrayList<String>> readLargerTextFile(String aFileName) throws IOException {
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

    void writeLargerCSVFile(String aFileName, ArrayList<ArrayList<String>> array) throws IOException {
        Path path = Paths.get(aFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {

            for (int i = 0; i < array.size(); i++) {
                String s = "";
                for (int j = 0; j < array.get(i).size(); j++) {
                    if (j == 0) {
                        s += array.get(i).get(j);
                    } else {
                        s += ";" + array.get(i).get(j);
                    }
                }
                writer.write(s);
                writer.newLine();
            }
        }
    }

    void writeLargerWekaCSVFile(String aFileName, ArrayList<ArrayList<String>> array) throws IOException {
        Path path = Paths.get(aFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {

            for (int i = 0; i < array.size(); i++) {
                String s = "";
                for (int j = 0; j < array.get(i).size(); j++) {

                    if (j == 0) {
                        s += "\"" + array.get(i).get(j) + "\"";
                    } else {
                        s += ",\"" + array.get(i).get(j) + "\"";
                    }
                }
                writer.write(s);
                writer.newLine();
            }
        }
    }

    boolean containsNumber(String s) {
        for (Integer i = 0; i < 10; i++) {
            if (s.contains(i.toString())) {
                return true;
            }
        }
        return false;
    }

    void removeSpacesInFirstCollumn() {
        for (int i = 0; i < v3.size(); i++) {
            String s = v3.get(i).get(0);
            s.replace(" ", "");
            v3.get(i).set(0, s);
        }
        for (int i = 0; i < v1.size(); i++) {
            String s = v1.get(i).get(0);
            s.replace(" ", "");
            v1.get(i).set(0, s);
        }
        for (int i = 0; i < v3.size(); i++) {
            String s = v3.get(i).get(0);
            s.replace(" ", "");
            v3.get(i).set(0, s);
        }
        for (int i = 0; i < v3.size(); i++) {
            String s = v3.get(i).get(0);
            s.replace(" ", "");
            v3.get(i).set(0, s);
        }
        for (int i = 0; i < v12.size(); i++) {
            String s = v12.get(i).get(0);
            s.replace(" ", "");
            v12.get(i).set(0, s);
        }
        for (int i = 0; i < v18.size(); i++) {
            String s = v18.get(i).get(0);
            s.replace(" ", "");
            v18.get(i).set(0, s);
        }

        for (int i = 0; i < other.size(); i++) {
            String s = other.get(i).get(0);
            s.replace(" ", "");
            other.get(i).set(0, s);
        }
        for (int i = 0; i < otherFollow.size(); i++) {
            String s = otherFollow.get(i).get(0);
            s.replace(" ", "");
            otherFollow.get(i).set(0, s);
        }
    }
}
