/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka;

import java.io.Serializable;

/**
 * Used to keep track of the resulting classification by an ensemble.
 * Classification is the chance it is true (so death/hospitalized)
 * @author Florian
 */
public class IndividualClass implements Serializable{
    public String in;
    public double classification;
    public boolean trueClass;
    
    public IndividualClass(String in, double classi, boolean trueClass){
        this.in = in;
        this.trueClass = trueClass;
        this.classification = classi;
    }
}
