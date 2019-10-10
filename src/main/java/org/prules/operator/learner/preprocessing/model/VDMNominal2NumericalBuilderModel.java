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
import org.prules.operator.learner.PRulesModel;
import org.prules.tools.math.container.IntObjectContainer;
import org.prules.tools.math.container.PairContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class creates VDM (Value Difference Matric) transformation class. It's main method - run
 * returns ready to work transformat model. Inside for each selected attribute it creates a probability map
 * which is used to transform attributes.
 *
 * @author Marcin
 */
public class VDMNominal2NumericalBuilderModel implements PRulesModel<VDMNominal2NumericalModel> {

    private final String[] attributeNames;


    /**
     * Constructor for the class which is used to build VDM transformation model.
     * It takes as argument an array of attribute names which should e transformed
     *
     * @param attributeNames
     */
    public VDMNominal2NumericalBuilderModel(String[] attributeNames) {
        this.attributeNames = attributeNames;
    }

    /**
     * returns a list of attribute names
     *
     * @return
     */
    public String[] getAttributeNames() {
        return attributeNames;
    }

    /**
     * This method for each attribute defined in constructor calculates probability matrix
     * which is than injected to VDMNominal2NumericalModel class returned at the output of this method the output.
     * The returned class can be used to process data with the run method
     *
     * @param exampleSet
     * @return
     */
    @Override
    public VDMNominal2NumericalModel run(ExampleSet exampleSet) {
        Map<String, PairContainer<double[][], List<IntObjectContainer<String>>>> attributeTransformationMap = new HashMap<>(attributeNames.length);
        Attributes attributes = exampleSet.getAttributes();
        final Attribute label = exampleSet.getAttributes().getLabel();
        String[] labelNames = null;
        if (label.isNominal()) {
            int c = label.getMapping().size();
            labelNames = new String[c];
            labelNames = label.getMapping().getValues().toArray(labelNames);
            for (String name : attributeNames) {
                final Attribute attr = exampleSet.getAttributes().get(name);
                if (attr != null && attr.isNominal()) {
                    int n = attr.getMapping().size();
                    List<String> vals = attr.getMapping().getValues();
                    List<IntObjectContainer<String>> mappings = new ArrayList<>();
                    //We need the pair of String and associated value id in case in the example set in which we applied
                    // the transformation the symbolic values are associated to different numerical values
                    for (String val : vals) {
                        mappings.add(new IntObjectContainer<>(attr.getMapping().getIndex(val), val));
                    }
                    //Get the transformation matrix
                    double[][] out = processAttribute(exampleSet, attr, c, n);
                    //collect all together - the attribute name, transformation matrix and attribute value - int pairs
                    attributeTransformationMap.put(name, new PairContainer<>(out, mappings));
                }
            }
        }
        return new VDMNominal2NumericalModel(attributeTransformationMap, labelNames);
    }

    /**
     * Method gets exampleset and attribute and calculates propabibities for each class label and each symbolic value.
     * It is assumed that values are enumerated: for labels: 0 and c-1 and for attribute attr between 0 and n-1
     * The returned matrix is ordered n x c
     *
     * @param exampleSet - the input exampleset
     * @param attr       - the attribute to analyze
     * @param c          - number of classes
     * @param n          - number of values for attribute attr
     * @return
     */
    private double[][] processAttribute(ExampleSet exampleSet, Attribute attr, int c, int n) {
        final int[] valueCounter = new int[n];
        final double[][] classValueCounter = new double[n][c];
        int sum = 0;
        //Count frequency of value-label pairs
        for (Example example : exampleSet) {
            double val = example.getValue(attr);
            if (!Double.isNaN(val)) {
                int valId = (int) val;
                valueCounter[valId]++;
                sum++;
                double labelValue = example.getLabel();
                if (!Double.isNaN(labelValue)) {
                    int labelId = (int) labelValue;
                    classValueCounter[valId][labelId]++;
                }
            }
        }
        //Calc probabilities
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < c; j++) {
                classValueCounter[i][j] /= valueCounter[i];
            }
        }
        return classValueCounter;
    }
}
