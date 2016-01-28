/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.weka;

//import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Marcin
 */
public class WekaInstanceHelper {
    /**
     * Method converts input ExampleSet into weka Instances. It recognizes if exists label attribute, and also adds it to the Instances.
     * This method assumes that all attributes are numerical.
     * @param exampleSet
     * @return 
     */
    public static Instances cretateInstances(ExampleSet exampleSet){                 
        Attributes rmAttributes = exampleSet.getAttributes();
        int numAttr = rmAttributes.size();
        boolean isLabel = exampleSet.getAttributes().getLabel() != null;
        ArrayList<Attribute> wAttributes = null;
        if (isLabel)
            wAttributes = new ArrayList<>(numAttr + 1);
        else
            wAttributes = new ArrayList<>(numAttr);
        for(com.rapidminer.example.Attribute rmAttr : rmAttributes){
            if (rmAttr.isNominal()){                
                Attribute wAttr = new Attribute(rmAttr.getName(),rmAttr.getMapping().getValues());
                wAttributes.add(wAttr);
            } else
                wAttributes.add(new Attribute(rmAttr.getName()));
        }
        if (isLabel)
            wAttributes.add(new Attribute("Label"));
        Instances instances = new Instances("exampleSet",wAttributes,exampleSet.size());        
        for(Example example : exampleSet){
            Instance instance = new DenseInstance(numAttr+1);            
            int i = 0;
            for(com.rapidminer.example.Attribute rmAttr : rmAttributes){
                instance.setValue(i, example.getValue(rmAttr));
                i++;
            }
            if (isLabel)
                instance.setValue(i,example.getLabel());             
            instances.add(instance);            
        }
        if (isLabel)
            instances.setClassIndex(numAttr);  //Warning it is the last attribute but as we count from 0 this is numAttr and not numAttr+1
        return instances;
    }
}
