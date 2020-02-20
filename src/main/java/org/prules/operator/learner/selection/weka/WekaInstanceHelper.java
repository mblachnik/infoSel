/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.weka;

//import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import java.util.ArrayList;
import java.util.List;

import weka.core.*;

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
        FastVector wAttributes = null;
        if (isLabel)
            wAttributes = new FastVector(numAttr + 1);
        else
            wAttributes = new FastVector(numAttr);
        for(com.rapidminer.example.Attribute rmAttr : rmAttributes){
            if (rmAttr.isNominal()){
                FastVector attributeValues =new FastVector();
                for (String value : rmAttr.getMapping().getValues())
                    attributeValues.addElement(value);
                String attrName = rmAttr.getName();
                Attribute wAttr = new Attribute(attrName,attributeValues);
                wAttributes.addElement(wAttr);
            } else
                wAttributes.addElement(new Attribute(rmAttr.getName()));
        }
        if (isLabel)
            wAttributes.addElement(new Attribute("Label"));
        FastVector fwAttributes = new FastVector();

        Instances instances = new Instances("exampleSet",wAttributes,exampleSet.size());        
        for(Example example : exampleSet){
            //Instance instance = new DenseInstance(numAttr+1);
            Instance instance = new Instance(numAttr+1);
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
