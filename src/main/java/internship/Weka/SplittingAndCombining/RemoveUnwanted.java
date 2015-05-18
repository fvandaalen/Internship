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
 * Removes the attributes listed as unwanted, see the list provided in the GUI for which specific ones.
 * @author Florian
 */
public class RemoveUnwanted {

    final static Charset ENCODING = StandardCharsets.UTF_8;
    public ArrayList<ArrayList<String>> text;
    public ArrayList<String> list;
    private String basePath;
    private String followPath;
    private String bioPath;
    private String targetLocation;

    public RemoveUnwanted(String basePath, String followPath, String bioPath, String targetLocation) {
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
    }

    public void doWork(ArrayList<String> list) throws IOException {
        text = readLargerTextFile(basePath);
        
        this.list = list;
        removeUnwanted();
        
        writeLargerCSVFile(targetLocation + "\\TIME-CHF_Baseline.csv", text);
    }

    void removeUnwanted() {
        for (int i = 0; i < text.get(0).size(); i++) {
            for (String s : list) {
                if(i >= text.get(0).size()){
                    break;
                }
                if (text.get(0).get(i).equals(s)) {
                    for (int j = 0; j < text.size(); j++) {
                        text.get(j).remove(i);
                    }
                }
                continue;
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
