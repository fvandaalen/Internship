/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka;

/**
 *
 * @author Florian
 */
public class Comparison {
    public double mean;
    public double sdev;
    public double tvalue;
    public boolean correct;
    public int size;
    
    public Comparison(double mean, double sdev, double tvalue, boolean correct, int size){
        this.mean = mean;
        this.sdev =sdev;
        this.tvalue = tvalue;
        this.correct = correct;
        this.size = size;
        
    }
}
