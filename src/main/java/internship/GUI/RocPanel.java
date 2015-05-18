/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Florian
 */
public class RocPanel extends JPanel {

    public ArrayList<double[]> coords = new ArrayList<double[]>();
    public int ofset = 50;
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(10, 10, this.getWidth() - 20, this.getHeight() - 20);
        int h = this.getHeight() - 200;
        int w = this.getWidth() - 200;
        g2d.setColor(Color.RED);
        g2d.drawLine(ofset, h, w, ofset);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(ofset, h, ofset, ofset);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(ofset, h, w, h);
        for (double i = 0; i < 11; i += 1) {
            g2d.drawString("" + i / 10.0, (int) (ofset + (i * ((w - ofset) / 10))), h + 20);
        }
        for (double i = 0; i < 11; i += 1) {
            g2d.drawString("" + i / 10.0, 20, (int) (ofset + ((10 - i) * ((h - ofset) / 10))));
        }

        g2d.drawString("The red line indicates the ROC curve if AUC = 0.5", ofset, h + ofset);
        g2d.drawString("The blue squares are the results for each treshold", ofset, h + 75);
        g2d.drawString("The thin blue line is the resulting convex ROC curve", ofset, h + 100);
        g2d.drawString("Horizontal axis: FPR, vertical: TPR", ofset, h + 125);
        for (double[] c : coords) {
            g2d.setColor(Color.BLUE);
            g2d.fillRect(ofset + (int) (c[1] * (w-ofset)), h - 5 - (int) + (c[0] * (h-ofset)), 5, 5);
        }
        

        int i = 0;
        while (i < coords.size() - 1) {
            double bestSlope = -1.0;
            int cur = 0;
            for (int j = i; j < coords.size(); j++) {
                double a = Math.atan((coords.get(j)[0] - coords.get(i)[0]) / (coords.get(j)[1] - coords.get(i)[1]));
                if (a > bestSlope) {
                    bestSlope = a;
                    cur = j;
                }
            }
           g2d.drawLine(ofset + (int)(coords.get(cur)[1] * (w-ofset)), h - (int) (coords.get(cur)[0]* (h-ofset)), ofset + (int)( coords.get(i)[1] * (w-ofset)), h - (int) (coords.get(i)[0]* (h-ofset)));
            i = cur;
        }


    }
}
