/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.SplittingAndCombining;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Changes the death value to be (death || hospitalization) within that particular timeframe
 * @author Florian
 */
public class Hospital {

    public ArrayList<ArrayList<String>> text = new ArrayList<ArrayList<String>>();
    final static Charset ENCODING = StandardCharsets.UTF_8;

    //returns an arraylist of arrays of hospitalization in the following way:
    //0: ID, 1: month 1, 2: month 3, 3: month 6, 4: month 12, 5month 18
    public ArrayList<String[]> hospitalization(String path, boolean hospitazliation) throws IOException {
        text.clear();
        ArrayList<String[]> hosp = new ArrayList<String[]>();
        readLargerTextFile(path);
        for (int i = 1; i < text.size(); i++) {
            boolean contained = false;
            for (int j = 0; j < hosp.size(); j++) {
                if (hosp.get(j)[0].equals(text.get(i).get(0))) {
                    contained = true;
                    if(!hospitazliation){
                        continue;
                    }
                    if (Double.parseDouble(text.get(i).get(1)) / 30 <= 1) {
                        hosp.get(j)[1] = "True";
                    } else if (Double.parseDouble(text.get(i).get(1)) / 30 <= 3) {
                        hosp.get(j)[2] = "True";
                    } else if (Double.parseDouble(text.get(i).get(1)) / 30 <= 6) {
                        hosp.get(j)[3] = "True";
                    } else if (Double.parseDouble(text.get(i).get(1)) / 30 <= 12) {
                        hosp.get(j)[4] = "True";
                    } else {
                        hosp.get(j)[5] = "True";
                    }
                }
            }
            if (!contained) {
                String[] h = new String[6];
                h[0] = text.get(i).get(0);
                h[1] = "False";
                h[2] = "False";
                h[3] = "False";
                h[4] = "False";
                h[5] = "False";
                hosp.add(h);
                if(!hospitazliation){
                    continue;
                }
                if (Double.parseDouble(text.get(i).get(1)) / 30 <= 1) {
                    hosp.get(hosp.size()-1)[1] = "True";
                } else if (Double.parseDouble(text.get(i).get(1)) / 30 <= 3) {
                    hosp.get(hosp.size()-1)[2] = "True";
                } else if (Double.parseDouble(text.get(i).get(1)) / 30 <= 6) {
                    hosp.get(hosp.size()-1)[3] = "True";
                } else if (Double.parseDouble(text.get(i).get(1)) / 30 <= 12) {
                    hosp.get(hosp.size()-1)[4] = "True";
                } else {
                    hosp.get(hosp.size()-1)[5] = "True";
                }
            }
        }
        return hosp;

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
}
