/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.GA;

/**
 *
 * @author Florian
 */
public class Individual {
    public boolean[] selected; // selected attributes, 1st and last are always included as they are ID & class respectivly
    public double AUC; // average AUC over the entire thing with these attributes
    public int name; // a name to keep track of which it is.
    public Individual(boolean[] s, int n){
        this.selected = s;
        this.name = n;
    }
}
