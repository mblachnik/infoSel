/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implementation of  All-KNN instance selection algorithm. 
 * It runs ENN algorithm for a set of different k values and selects samples which all have neighbors for the same class 
 * @author Marcin
 */
public class AllKNNInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    
    private final DistanceMeasure measure; //distance measure
    private final int lowerK, upperK; // lower and upper bounds for k value
    private final IISDecisionFunction loss; //decision function which is used to determine if certain condition is valid or not. It allows to support both classification and regression tasks    
    IS_KNNClassificationModel<IStoredValues> model;

    /**
     * Constructor
     * @param measure - distance measure
     * @param lowerK - lower bound for k
     * @param upperK - upper bound for k
     * @param loss
     */
    public AllKNNInstanceSelectionGeneralModel(DistanceMeasure measure, int lowerK, int upperK, IISDecisionFunction loss) {
        this.measure = measure;
        this.lowerK = lowerK;
        this.upperK = upperK;        
        this.loss = loss;
    }

    /**
     * Performs instance selection
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        DataIndex index = exampleSet.getIndex();
        Attribute label = attributes.getLabel();

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollection<IStoredValues> samples;
        samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        //All - kNN EDITTING
        loss.init(samples);
        
        if (label.isNominal()) {
            double[] counter;
            int instanceIndex = 0;
            double predictedLabel;
            counter = new double[label.getMapping().size()];
            Iterator<Instance> samplesIterator = samples.samplesIterator();
            Iterator<IStoredValues> labelsIterator = samples.storedValueIterator();
            double performanceStep = 1/upperK;
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                double realLabel = labelsIterator.next().getLabel();
                Instance values = samplesIterator.next();
                Arrays.fill(counter, 0);
                Collection<DoubleObjectContainer<IStoredValues>> res = samples.getNearestValueDistances(upperK, values);
                int k = 0;                
                for (DoubleObjectContainer<IStoredValues> it : res) {
                    int i = (int)it.getSecond().getLabel();
                    counter[i]+= performanceStep;
                    if (k > lowerK) {
                        predictedLabel = PRulesUtil.findMostFrequentValue(counter);                        
                        if (loss.getValue(realLabel,predictedLabel,values) > 0) {
                            index.set(instanceIndex, false);
                            break;
                        }
                    }
                    k++;
                }
                instanceIndex++;
            }
        } else if (label.isNumerical()) {
            int instanceIndex = 0;
            double predictedLabel;
            double sum;
            Iterator<Instance> samplesIterator = samples.samplesIterator();
            Iterator<IStoredValues> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                double realLabel = labelsIterator.next().getLabel();
                Instance values = samplesIterator.next();
                sum = 0;
                Collection<DoubleObjectContainer<IStoredValues>> res = samples.getNearestValueDistances(upperK, values);
                int k = 0;
                for (DoubleObjectContainer<IStoredValues> it : res) {
                    sum += it.getSecond().getLabel();                            
                    if (k > lowerK) {
                        predictedLabel = sum / k;                        
                        if (loss.getValue(realLabel,predictedLabel,values) > 0) {
                            index.set(instanceIndex, false);
                        }
                    }
                    k++;
                }
                instanceIndex++;
            }
        }        
        return index;
    }
}
