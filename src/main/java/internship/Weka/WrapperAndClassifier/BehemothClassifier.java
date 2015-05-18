/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package internship.Weka.WrapperAndClassifier;

import internship.Weka.Data;
import internship.Weka.WekaMain;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Enumeration;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.WeightedInstancesHandler;

/**
 * Basicly a dummy to allow the connection between BehemothWrapper and WekaMain, doesnt actually do any classification on its own really.
 * @author Florian
 */
public class BehemothClassifier  implements Classifier, CapabilitiesHandler, WeightedInstancesHandler, TechnicalInformationHandler, Serializable {

    private WekaMain weka;
    static final long serialVersionUID = 3932117032546553727L;

    public BehemothClassifier(WekaMain wekaMain) {
        this.weka = wekaMain;
        weka.filterRecombination = weka.filterRecombination.none;
    }
    
    public double getAverageAUC(int folds) throws Exception{
        double auc = 0;
        weka.folds = folds;
        for(int i = 0; i < 5; i++){
            for(int j = i+1; j < 6; j++){
                auc+= weka.testEnsemble(i, j, 5, 5, true).calcAUC();
            }
        }
        auc/= 15;
        System.out.println(auc);
        return auc;
    }

    @Override
    public Capabilities getCapabilities() {
        Capabilities result = new Capabilities(this);
        result.enable(Capability.BINARY_CLASS);

        // attributes
        result.enable(Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capability.DATE_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);
        result.enable(Capability.STRING_ATTRIBUTES);

        // other
        result.setMinimumNumberInstances(0);
        return result;
    }

    @Override
    public double[] distributionForInstance(Instance in) throws Exception {
        throw new UnsupportedOperationException("Behemoth does not classify individuals");
    }
      @Override
    public double classifyInstance(Instance instnc) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * So this is how this works 1) The data to select features from is given to
     * weka, this does not include the ID attribute Any data set works for this
     * as they all share the same attributes anyway, actual data is still
     * contained in wekaMain 2) The weka gives that data here so I can put it in
     * a wrapper and use their fancy stuff 3) I figure out which attributes are
     * selected and tell that to my wekaMain class while also including the ID
     * attribute manually 4) WekaMain class does stuff
     *
     * @param data
     * @throws Exception
     */
    private int count = 0;
    
    public String[] getOptions(){
        return new String[2];
    }
    @Override
    public void buildClassifier(Instances data) throws Exception {
        System.out.println(count + " hallo " + data.numAttributes());
        count+=1;
        //contrived way to actually get around the whole ID thing and nested and just aaaaaah
        ArrayList<String> att = new ArrayList<String>();
        att.add(weka.sets.get(0).original.attribute(0).name());
        att.add(weka.sets.get(0).original.attribute(weka.sets.get(0).original.numAttributes() - 1).name());
        for (int i = 0; i < data.numAttributes(); i++) {
            att.add(data.attribute(i).name());
        }
        for (Data d : weka.sets) {            
            int i = 1;
            d.data = new Instances(d.original);
            while (i < d.data.numAttributes()) {
                boolean remains = false;
                for (int j = i; j < att.size(); j++) {
                    if (d.data.attribute(i).name().equals(att.get(j)) ||d.data.attribute(i).name().equals("PatientID")) {
                        remains = true;
                        break;
                    }
                }
                if (remains) {
                    i++;
                } else {
                    d.data.deleteAttributeAt(i);
                    i=1;
                }
                
            }
            d.filtered = d.data;
        }
        weka.buildClassifier();

    }


    @Override
    public TechnicalInformation getTechnicalInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void readObject(
            ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();
    }

    /**
     * This is the default implementation of writeObject. Customise if
     * necessary.
     */
    private void writeObject(
            ObjectOutputStream aOutputStream) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }
  
}
