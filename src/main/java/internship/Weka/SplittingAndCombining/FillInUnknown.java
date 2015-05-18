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
 * Fills in the unknown data points with "EMPTY" as in the original data some have the value 9999
 * The "Empty" value is later removed again.
 * @author Florian
 */
public class FillInUnknown {

    final static Charset ENCODING = StandardCharsets.UTF_8;
    public ArrayList<ArrayList<String>> text;

    private String basePath; 
    private String followPath; 
    private String  bioPath;
    private String targetLocation;
    
    public FillInUnknown(String basePath, String followPath, String  bioPath, String targetLocation){
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
    }

    public void doWork() throws IOException {
        alphabet();
        text = readLargerTextFile(targetLocation + "\\TIME-CHF_Baseline.csv");
        fillUnknown();
        writeLargerCSVFile(targetLocation + "\\TIME-CHF_Baseline.csv", text);

        text = readLargerTextFile(bioPath);
        fillUnknown();
        writeLargerCSVFile(targetLocation + "\\TIME-CHF_Biomarkers.csv", text);
    }

    void fillUnknown() {
        
        for (int i = 0; i < text.size(); i++) {
            for (int j = 0; j < text.get(i).size(); j++) {
                if(text.get(i).get(j).equals(" ") || text.get(i).get(j).equals("9999") || text.get(i).get(j).isEmpty()){
                text.get(i).set(j, "EMPTY");
                }
                else if (emptyString(text.get(i).get(j))) {
                    text.get(i).set(j, "EMPTY");
                }
            }
        }
    }
    ArrayList<String> alpha = new ArrayList<String>();

    void alphabet() {
        alpha.add("a");
        alpha.add("b");
        alpha.add("c");
        alpha.add("d");
        alpha.add("e");
        alpha.add("f");
        alpha.add("g");
        alpha.add("h");
        alpha.add("i");
        alpha.add("j");
        alpha.add("k");
        alpha.add("l");
        alpha.add("m");
        alpha.add("n");
        alpha.add("o");
        alpha.add("p");
        alpha.add("q");
        alpha.add("r");
        alpha.add("s");
        alpha.add("t");
        alpha.add("u");
        alpha.add("v");
        alpha.add("w");
        alpha.add("x");
        alpha.add("y");
        alpha.add("z");
    }

    boolean emptyString(String s) {
        s = s.toLowerCase();
        for (Integer i = 0; i < 10; i++) {
            if (s.contains(i.toString())) {
                return false;
            }
        }
        for(String s1: alpha){
            if(s.contains(s1)){
                return false;
            }
        }
        
        return true;

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
}
