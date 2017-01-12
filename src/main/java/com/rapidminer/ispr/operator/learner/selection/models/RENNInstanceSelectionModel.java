/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.knn.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IVector;
import com.rapidminer.ispr.tools.math.container.knn.KNNFactory;

/**
 * Class implements repeated ENN algorithm (RENN). It repeats ENN algorithm
 * until any instance can be marked for removal
 *
 * @author Marcin
 */
public class RENNInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private int k;
    private DistanceMeasure measure;
    private IISDecisionFunction loss;

    /**
     * Constructor of repeated ENN algorithm (RENN). It repeats ENN algorithm
     * until any instance can be marked for removal
     *
     * @param measure
     * @param k
     */
    public RENNInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss) {
        this.k = k;
        this.measure = measure;
        this.loss = loss;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     * performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        Attribute label = attributes.getLabel();
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollection<IValuesStoreLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        loss.init(samples);
        int numberOfClasses = label.getMapping().size();
        //ENN EDITTING
        IVector values = ValuesStoreFactory.createVector(exampleSet);
        int[] counter = new int[numberOfClasses];
        DataIndex mainIndex = exampleSet.getIndex();
        while (true) {
            int instanceIndex = 0;
            DataIndex index = exampleSet.getIndex();
            for (Example example : exampleSet) {
                Arrays.fill(counter, 0);
                Collection<IValuesStoreLabels> res;
                values.setValues(example);
                res = samples.getNearestValues(k + 1, values);
                double sum = 0;
                for (IValuesStoreLabels i : res) {
                    counter[(int)i.getLabel()]++;
                    sum++;
                }
                counter[(int) example.getLabel()] --; //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum--; //here we have to subtract because nearest neighbors includ itself, see line above
                int mostFrequent = PRulesUtil.findMostFrequentValue(counter);
                if (example.getLabel() != mostFrequent) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }
            if (index.getLength() == index.getFullLength()) {
                break;
            }
            exampleSet.setIndex(index);
            mainIndex.setIndex(index);
            for (int i = index.getFullLength() - 1; i > -1; i--) {
                if (!index.get(i)) {
                    samples.remove(i);
                }
            }
        }
        return mainIndex;
    }
}
