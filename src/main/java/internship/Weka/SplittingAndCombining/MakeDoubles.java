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
 * Changes all doubles to a notation with a dot instead of a comma
 * @author Florian
 */
public class MakeDoubles {
    
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
   
     private String basePath; 
    private String followPath; 
    private String  bioPath;
    private String targetLocation;
    
    public MakeDoubles(String basePath, String followPath, String  bioPath, String targetLocation){
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
    }
    
    public void doWork() throws IOException{
        alphabet();
        base1 = readLargerTextFile(targetLocation + "\\base1.csv");
        base3 = readLargerTextFile(targetLocation + "\\base3.csv");
        base6 = readLargerTextFile(targetLocation + "\\base6.csv");
        base12 = readLargerTextFile(targetLocation + "\\base12.csv");
        base18 = readLargerTextFile(targetLocation + "\\base18.csv");
        one3 = readLargerTextFile(targetLocation + "\\one3.csv");
        one6 = readLargerTextFile(targetLocation + "\\one6.csv");
        one12 = readLargerTextFile(targetLocation + "\\one12.csv");
        one18 = readLargerTextFile(targetLocation + "\\one18.csv");
        three6 = readLargerTextFile(targetLocation + "\\three6.csv");
        three12 = readLargerTextFile(targetLocation + "\\three12.csv");
        three18 = readLargerTextFile(targetLocation + "\\three18.csv");
        six12 = readLargerTextFile(targetLocation + "\\six12.csv");
        six18 = readLargerTextFile(targetLocation + "\\six18.csv");
        twelve18 = readLargerTextFile(targetLocation + "\\twelve18.csv");
        
       
        base1 = makeDoubles(base1);
        base3 = makeDoubles(base3);
        base6 = makeDoubles(base6);
        base12 = makeDoubles(base12);
        base18 = makeDoubles(base18);
        one3 = makeDoubles(one3);
        one6 = makeDoubles(one6);
        one12 = makeDoubles(one12);
        one18 = makeDoubles(one18);
        three6 = makeDoubles(three6);
        three12 = makeDoubles(three12);
        three18 = makeDoubles(three18);
        six12 = makeDoubles(six12);
        six18 = makeDoubles(six18);
        twelve18 = makeDoubles(twelve18);
        
        
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
    
    ArrayList<ArrayList<String>> makeDoubles(ArrayList<ArrayList<String>> array){
        for(int i = 0; i <  array.size(); i++){
            for(int j = 0; j < array.get(i).size();j++){
                String s = array.get(i).get(j);
                if(!array.get(i).get(j).contains(".") && isNumber(array.get(i).get(j))){
                    s = s.replace(",",".");
                    String l = "";
                    int c =-1;
                    
                    
                    array.get(i).set(j, s);
                }
            }
        }
         return array;
    }
    
    void writeLargerCSVFile(String aFileName, ArrayList<ArrayList<String>> array) throws IOException {
        Path path = Paths.get(aFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(path, ENCODING)) {

            for (int i = 0; i < array.size(); i++) {
                String s = "";
                for (int j = 0; j < array.get(i).size(); j++) {
                    String s1 = array.get(i).get(j);
                    if(s1.equals("EMPTY")){
                        s+= "; EMPTY";
                    }else if (j == 0) {
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
                    String s1 = array.get(i).get(j);
                    if(s1.equals("EMPTY")){
                        s+= ",";
                    }else if (j == 0) {
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
                    } else if (s.substring(i - 1, i).equals("\"")) {
                        //skip
                    }else {
                        s2 += s.substring(i - 1, i);
                    }

                }
                text.get(row).add(s2);
            }
        }
        return text;
    }
    
    
    boolean isNumber(String s) {
        boolean numb;
        s = s.toLowerCase();
        for(String s1: alpha){
            if(s.contains(s1)){
                return false;
            }
        }
                
        for (Integer i = 0; i < 10; i++) {
            if (s.contains(i.toString())) {
                return true;
            }
        }
        return false;
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
}

