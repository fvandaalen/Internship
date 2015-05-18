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
 * Splits the data set into the individual timesteps. So into visit 0,1,3,6,12,18 etc.
 * @author Florian
 */
public class Splitting {

    final static String OUTPUT_FILE_NAME = "C:\\Florian\\output.txt";
    final static Charset ENCODING = StandardCharsets.UTF_8;
    public ArrayList<ArrayList<String>> text = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> bL = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> b1 = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> b3 = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> b6 = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> b12 = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> b18 = new ArrayList<ArrayList<String>>();
    private String basePath;
    private String followPath;
    private String bioPath;
    private String targetLocation;

    public Splitting(String basePath, String followPath, String bioPath, String targetLocation) {
        this.basePath = basePath;
        this.followPath = followPath;
        this.bioPath = bioPath;
        this.targetLocation = targetLocation;
    }

    public void work() throws IOException {

        text.clear();
        readLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers.csv");
        split();
        writeLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_BL.csv", bL);
        writeLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V1.csv", b1);
        writeLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V3.csv", b3);
        writeLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V6.csv", b6);
        writeLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V12.csv", b12);
        writeLargerTextFile(targetLocation + "\\TIME-CHF_Biomarkers_V18.csv", b18);

    }

    ArrayList<ArrayList<String>> readOtherData(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
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
        ArrayList<ArrayList<String>> text2 = new ArrayList<ArrayList<String>>();
        for (int i = 0; i < text.size(); i++) {
            text2.add(new ArrayList<String>());
        }
        for (int i = 0; i < text.get(0).size(); i++) {
            if (text.get(0).get(i).contains("_XX") || text.get(0).get(i).contains("XXX")) {
                //skip
            } else {
                text2.get(0).add(text.get(0).get(i));
                for (int j = 1; j < text.size(); j++) {
                    text2.get(j).add(text.get(j).get(i));
                }
            }
        }
        return text2;
    }

    void writeOtherData(String aFileName, ArrayList<ArrayList<String>> array) throws IOException {
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

    void readLargerTextFile(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
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
    }

    void split() {
        for (int i = 0; i < text.size(); i++) {
            bL.add(new ArrayList<String>());
            b1.add(new ArrayList<String>());
            b3.add(new ArrayList<String>());
            b6.add(new ArrayList<String>());
            b12.add(new ArrayList<String>());
            b18.add(new ArrayList<String>());
        }

        for (int i = 0; i < text.size(); i++) {
            bL.get(i).add(text.get(i).get(0));
            b1.get(i).add(text.get(i).get(0));
            b3.get(i).add(text.get(i).get(0));
            b6.get(i).add(text.get(i).get(0));
            b12.get(i).add(text.get(i).get(0));
            b18.get(i).add(text.get(i).get(0));
        }
        for (int i = 1; i < text.get(0).size(); i++) {
            if (text.get(0).get(i).contains("_XX") || text.get(0).get(i).contains("_BL_") || text.get(0).get(i).contains("_V1_")
                    || text.get(0).get(i).contains("_V3_") || text.get(0).get(i).contains("_V6_") || text.get(0).get(i).contains("_V12_")
                    || text.get(0).get(i).contains("_V18_") || text.get(0).get(i).contains("_BLall") || text.get(0).get(i).contains("_V6all") || text.get(0).get(i).contains("XXX")) {
                //skip
            } else if (text.get(0).get(i).contains("_BL")) {

                bL.get(0).add(text.get(0).get(i).substring(0, text.get(0).get(i).indexOf("_BL")));
                for (int j = 1; j < text.size(); j++) {
                    bL.get(j).add(text.get(j).get(i));
                }
            } else if (text.get(0).get(i).contains("_V3")) {
                b3.get(0).add(text.get(0).get(i).substring(0, text.get(0).get(i).indexOf("_V3")));
                for (int j = 1; j < text.size(); j++) {
                    b3.get(j).add(text.get(j).get(i));
                }
            } else if (text.get(0).get(i).contains("_V6")) {
                b6.get(0).add(text.get(0).get(i).substring(0, text.get(0).get(i).indexOf("_V6")));
                for (int j = 1; j < text.size(); j++) {
                    b6.get(j).add(text.get(j).get(i));
                }
            } else if (text.get(0).get(i).contains("_V12")) {
                b12.get(0).add(text.get(0).get(i).substring(0, text.get(0).get(i).indexOf("_V12")));
                for (int j = 1; j < text.size(); j++) {
                    b12.get(j).add(text.get(j).get(i));
                }
            } else if (text.get(0).get(i).contains("_V18")) {
                b18.get(0).add(text.get(0).get(i).substring(0, text.get(0).get(i).indexOf("_V18")));
                for (int j = 1; j < text.size(); j++) {
                    b18.get(j).add(text.get(j).get(i));
                }
            } else if (text.get(0).get(i).contains("_V1")) {
                b1.get(0).add(text.get(0).get(i).substring(0, text.get(0).get(i).indexOf("_V1")));
                for (int j = 1; j < text.size(); j++) {
                    b1.get(j).add(text.get(j).get(i));
                }
            }
        }
    }

    void writeLargerTextFile(String aFileName, ArrayList<ArrayList<String>> array) throws IOException {
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
