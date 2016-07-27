/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.PairContainer;
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

    
    private DistanceMeasure measure; //distance measure
    private int lowerK, upperK; // lower and upper bounds for k value
    private IISDecisionFunction loss; //decision function which is used to determine if certain condition is valid or not. It allows to support both classification and regression tasks    
    IS_KNNClassificationModel<Number> model;

    /**
     * Constructor
     * @param measure - distance measure
     * @param lowerK - lower bound for k
     * @param upperK - upper bound for k
     */
    public AllKNNInstanceSelectionGeneralModel(DistanceMeasure measure, int lowerK, int upperK, IISDecisionFunction loss) {
        this.measure = measure;
        this.lowerK = lowerK;
        this.upperK = upperK;        
        this.loss = loss;
    }

    /**
     * Performs instance selection
     * @param inputExampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        DataIndex index = exampleSet.getIndex();
        Attribute label = attributes.getLabel();

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollection<Number> samples;
        samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        //All - kNN EDITTING
        loss.init(samples);
        
        if (label.isNominal()) {
            int[] counter;
            int instanceIndex = 0;
            double predictedLabel;
            counter = new int[label.getMapping().size()];
            Iterator<double[]> samplesIterator = samples.samplesIterator();
            Iterator<Number> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                double realLabel = labelsIterator.next().doubleValue();
                double[] values = samplesIterator.next();
                Arrays.fill(counter, 0);
                Collection<DoubleObjectContainer<Number>> res = samples.getNearestValueDistances(upperK, values);
                int k = 0;
                for (DoubleObjectContainer<Number> it : res) {
                    int i = it.getSecond().intValue();
                    counter[i]++;
                    if (k > lowerK) {
                        predictedLabel = PRulesUtil.findMostFrequentValue(counter);                        
                        if (loss.getValue(realLabel,predictedLabel,values) > 0) {
                            index.set(instanceIndex, false);
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
            Iterator<double[]> samplesIterator = samples.samplesIterator();
            Iterator<Number> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                double realLabel = labelsIterator.next().doubleValue();
                double[] values = samplesIterator.next();
                sum = 0;
                Collection<DoubleObjectContainer<Number>> res = samples.getNearestValueDistances(upperK, values);
                int k = 0;
                for (DoubleObjectContainer<Number> it : res) {
                    sum += it.getSecond().doubleValue();
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
