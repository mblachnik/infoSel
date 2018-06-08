/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.preprocessing.model;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.prules.exceptions.IncorrectAttributeException;
import org.prules.operator.learner.PRulesModel;
import org.prules.tools.math.container.IntIntContainer;
import org.prules.tools.math.container.IntObjectContainer;
import org.prules.tools.math.container.PairContainer;

/**
 *
 * @author Marcin
 */
public class VDMNominal2NumericalModel implements PRulesModel<ExampleSet> {

    final Map<String, PairContainer<double[][],List<IntObjectContainer<String>>>> attributeTransofrmationMap;
    final String[] labelNames;

    
    public VDMNominal2NumericalModel(Map<String, PairContainer<double[][],List<IntObjectContainer<String>>>> attributeTransofrmationMap, String[] labelNames) {
        this.attributeTransofrmationMap = attributeTransofrmationMap;
        this.labelNames = labelNames;
    }

    /**
     * Returns a Map which maps attribute name to its transformation matrix The
     * transformation matrix is of size n x c where n-is the number of unique
     * values of given attribute, and c-is the number of class labels
     *
     * @return
     */
    public Map<String, PairContainer<double[][],List<IntObjectContainer<String>>>> getAttributeTransofrmationMap() {
        return attributeTransofrmationMap;
    }

    /**
     * Returns list of label names - it is used to name attributes
     *
     * @return
     */
    public String[] getLabelNames() {
        return labelNames;
    }

    /**
     * Execute VDM attribute transformation process. It takes each attribute
     * which appear in the filed {
     *
     * @see attributeTransformatinoMap } as a key and applies to it VDM
     * transformation using double[][] mapping, which is a value in the map.
     * Method returns converted example set, or null when labelNames is null.
     * @param exampleSet
     * @return
     */    
    @Override
    public ExampleSet run(ExampleSet exampleSet) {
        if (labelNames != null) {
            int c = labelNames.length;            
            Attributes attributes = exampleSet.getAttributes();            
            for (Entry<String, PairContainer<double[][],List<IntObjectContainer<String>>>> entry : attributeTransofrmationMap.entrySet()) {
                String name = entry.getKey();
                PairContainer<double[][],List<IntObjectContainer<String>>> pair = entry.getValue();                
                double[][] probabilitieMap = pair.getFirst();
                List<IntObjectContainer<String>> valNum2StrMapping = pair.getSecond();
                int n = probabilitieMap.length;
                Attribute attr = attributes.get(name);
                if (!attr.isNominal()) { 
                    throw new IncorrectAttributeException("Incorrect attribute type");
                }
                if (attr.getMapping().size() != n){
                    throw new IncorrectAttributeException("Incorrect number of symbols in the attribute");
                }                
                List<Attribute> attributes2Change = new ArrayList<>(c);
                //Generating new attributes which would store all the data
                for (String value : labelNames) {
                    Attribute attrTmp = AttributeFactory.createAttribute(name + "(" + value + ")", Ontology.NUMERICAL);
                    attributes2Change.add(attrTmp);
                }
                exampleSet.getExampleTable().addAttributes(attributes2Change);
                for (Attribute attrTmp : attributes2Change) {
                    exampleSet.getAttributes().addRegular(attrTmp);
                }
                /*
                 *This part is to synchronize attribute values. 
                It may happen that two attributes has different mappings betwenn symbols and numerical values, so we need to synchronize them between two examplesets
                To do it, we collect old numerical value of the the new one associated to the same label in a newOldValMap.
                Than the collected values are sorted according to the newVal - theat is the new id, such that we can go to the list and get i'th element 
                and the second colum will reflect the old value, therefore the appropriate column in probabilitieMap
                 */
                List<IntIntContainer> newOldValMap = new ArrayList<>();
                for(IntObjectContainer<String> intStrPair : valNum2StrMapping){
                    String strVal = intStrPair.getSecond();
                    int intVal    = attr.getMapping().getIndex(strVal);
                    int oldVal    = intStrPair.getFirst();
                    newOldValMap.add(new IntIntContainer(intVal,oldVal));                    
                }
                Collections.sort(newOldValMap);
                //Fill with new values
                for (Example example : exampleSet) {
                    double val = example.getValue(attr);
                    if (!Double.isNaN(val)) {
                        int valId = newOldValMap.get((int) val).getSecond();                        
                        for (int i = 0; i < c; i++) {
                            Attribute attrTmp = attributes2Change.get(i);                            
                            double newVal = probabilitieMap[valId][i];
                            example.setValue(attrTmp, newVal);
                        }
                    }
                }
                exampleSet.getAttributes().remove(attr);
            }
            return exampleSet;
        }
        return null;
    }

}
