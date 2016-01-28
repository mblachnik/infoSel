/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Marcin
 */
public class AllKNNInstanceSelectionModel_OLD implements PRulesModel<ExampleSet> {

    private DistanceMeasure measure;
    private int k1, k2;

    /**
     *
     * @param measure
     * @param k1
     * @param k2
     */
    public AllKNNInstanceSelectionModel_OLD(DistanceMeasure measure, int k1, int k2) {
        this.measure = measure;
        this.k1 = k1;
        this.k2 = k2;
    }

    /**
     *
     * @param inputExampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet;
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        Attributes attributes = exampleSet.getAttributes();
        DataIndex index = exampleSet.getIndex();
        Attribute label = attributes.getLabel();

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollection<Number> samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);

        //All - kNN EDITTING
        double[] values = new double[attributes.size()];
        int[] counter = new int[label.getMapping().size()];
        int instanceIndex = 0;
        for (Example example : exampleSet) {
            Arrays.fill(counter, 0);
            KNNTools.extractExampleValues(example, values);
            for (int k = k1; k < k2; k++) {
                Collection<Number> res = samples.getNearestValues(k, values);
                for (Number ii : res) {
                    int i = ii.intValue();
                    counter[i]++;
                }
                int mostFrequent = PRulesUtil.findMostFrequentValue(counter);
                if (example.getLabel() != mostFrequent) {
                    index.set(instanceIndex, false);
                }
            }
            instanceIndex++;
        }

        exampleSet.setIndex(index);
        return exampleSet;
    }
}
