/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.GA;

import internship.Weka.Data;
import internship.Weka.WekaMain;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import weka.core.Instances;

/**
 *
 * @author Florian
 */
public class GA {

    private WekaMain weka;
    private double mutationChance = 0.01;
    private int selected = 2; // number of top survivors
    private double survivalChance = 0.1; //randomchance for suboptimal results to survive
    private int randomSurvivors = 2; // number of random survivors
    private double initiationChance = 0.1; //chance an attribute gets set to true in the initial population
    private Random r = new Random();
    private int generations = 4;
    private int count = 0;
    private ArrayList<Individual> population = new ArrayList<Individual>();
    private Individual best;
    private int numAtt;
    private int initialPop = 10;
    public GA(WekaMain weka) throws FileNotFoundException, IOException, Exception {
        this.weka = weka;
        weka.filterRecombination = weka.filterRecombination.none;
        weka.voteType = weka.voteType.averageProb;

        weka.run();
        numAtt = weka.sets.get(0).original.numAttributes();
    }

    public void run() throws Exception {
        createPopulation();
        for (int i = 0; i < generations; i++) {
            breed(select());
            System.out.println("Currently at generation " + i);
            System.out.println("Current best AUC: " + best.AUC + " name: " + best.name);
        }
        System.out.println("The ultimate survivor: ");
        System.out.println("Resulting AUC: " + best.AUC + " name: " + best.name);
        System.out.println("");
        System.out.println("selected attributes:");
        for(int i = 0; i < best.selected.length; i++){
            System.out.println(" Attribute " + i + " " + best.selected[i]);
        }

    }

    private void createPopulation() throws Exception {
        weka.classifier = "logReg";
        for (int i = 0; i < initialPop; i++) {
            boolean[] b = new boolean[numAtt];
            //set class & ID to true
            b[0] = true;
            b[b.length - 1] = true;
            //randomly make a bunch true
            for (int j = 1; j < b.length - 2; j++) {
                if (r.nextDouble() < initiationChance) {
                    b[j] = true;
                } else {
                    b[j] = false;
                }
            }
            population.add(new Individual(b, count));
            count++;

        }
    }

    private void breed(ArrayList<Individual> survivors) {
        //remove the old
        population.clear();
        //add the new
        population.addAll(survivors);
        //now breed
        for(int i = 0; i < survivors.size(); i++){
            for(int j = 0; j < survivors.size(); j++){
                boolean[] b = new boolean[numAtt];
                //set the first and last value
                b[0] = true;
                b[b.length-1] = true;
                //randomly pick the rest from parents
                for(int k = 1; k < b.length -2; k++){
                    if(r.nextDouble() < 0.5){
                        b[k] = survivors.get(i).selected[k];
                    }else{
                        b[k] = survivors.get(j).selected[k];
                    }
                }
                population.add(new Individual(b, count));
                count++;
            }
        }
        //mutate
        for(Individual in: population){
            for(int i = 1; i < numAtt-2; i++){
                if(r.nextDouble() < mutationChance){
                    in.selected[i] = (!in.selected[i]);
                }
            }
        }
    }

    private ArrayList<Individual> select() throws Exception {
        ArrayList<Individual> ranking = new ArrayList<Individual>();
        ArrayList<Individual> survivors = new ArrayList<Individual>();
        for (Individual in : population) {
            weka.setAttributes(in.selected);
            double auc = 0;
            //loop over all ensembles to get their AUC
            for (int i = 0; i < 5; i++) {
                for (int j = i + 1; j < 6; j++) {
                    auc += weka.testEnsemble(i, j, 5, 5, true).calcAUC();
                }
            }
            auc /= 15; //there's 15 ensembles in total, average their AUC
            in.AUC = auc;
            boolean added = false;
            //if it's the first just add it to the list
            if (ranking.size() == 0) {
                ranking.add(in);
                added = true;
            } else {
                //add it to the list as soon as you find one that is worse
                for (int i = 0; i < ranking.size(); i++) {
                    if (ranking.get(i).AUC < in.AUC) {
                        ranking.add(i, in);
                        added = true;
                        break;
                    }
                }
                //if you haven't added it, just put it at the end
                if(!added){
                    ranking.add(in);
                }
            }
        }
        //add the top N
        for(int i = 0; i < selected; i++){
            survivors.add(ranking.get(i));
        }
        ranking.removeAll(survivors);
        //add another bunch at random
        for(int i = 0; i < randomSurvivors; i++){
            int j = r.nextInt(ranking.size());
            survivors.add(ranking.get(j));
            ranking.remove(j);
        }
        best = survivors.get(0);
        return survivors;
    }

}
