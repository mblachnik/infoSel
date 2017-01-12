/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.Const;
import com.rapidminer.ispr.dataset.IValuesStoreInstance;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.knn.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IValuesStorePrediction;
import com.rapidminer.ispr.dataset.IVector;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.tools.math.container.knn.KNNFactory;
import java.util.Collections;

/**
 * Implementation of All-KNN instance selection algorithm. It runs ENN algorithm
 * for a set of different k values and selects samples which all have neighbors
 * for the same class
 *
 * @author Marcin
 */
public class AllKNNInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure; //distance measure
    private final int lowerK, upperK; // lower and upper bounds for k value
    private final IISDecisionFunction loss; //decision function which is used to determine if certain condition is valid or not. It allows to support both classification and regression tasks    
    IS_KNNClassificationModel<IValuesStoreLabels> model;

    /**
     * Constructor
     *
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
     *
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        DataIndex index = exampleSet.getIndex();
        Attribute labelAttribute = attributes.getLabel();

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollection<IValuesStoreLabels> samples;
        samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        //All - kNN EDITTING
        loss.init(samples);

        IValuesStorePrediction prediction = ValuesStoreFactory.createPrediction(Double.NaN, null);
        IValuesStoreInstance instance = ValuesStoreFactory.createEmptyValuesStoreInstance();
        IValuesStoreLabels label;
        
        if (labelAttribute.isNominal()) {
            double[] counter;
            int instanceIndex = 0;
            double predictedLabel;
            counter = new double[labelAttribute.getMapping().size()];
            Iterator<IVector> samplesIterator = samples.samplesIterator();
            Iterator<IValuesStoreLabels> labelsIterator = samples.storedValueIterator();            
            double performanceStep = 1.0 / upperK;
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                label = labelsIterator.next();
                IVector values = samplesIterator.next();
                Arrays.fill(counter, 0);
                counter[(int)label.getLabel()] = -performanceStep; //Note this is becouse we use k+1, becouse always we are the most similar to outselves
                Collection<DoubleObjectContainer<IValuesStoreLabels>> res = samples.getNearestValueDistances(upperK+1, values); //It is not gquaranteed that the res is sorted according to the value of distance, so first we have to sort it
                DoubleObjectContainer<IValuesStoreLabels>[] resArray;                                     
                resArray = res.toArray(new DoubleObjectContainer[res.size()]);
                Arrays.sort(resArray);
                int k = 0;
                for (DoubleObjectContainer<IValuesStoreLabels> it : resArray) {
                    int i = (int) it.getSecond().getLabel();
                    counter[i] += performanceStep;
                    if (k > lowerK+1) {
                        predictedLabel = PRulesUtil.findMostFrequentValue(counter);
                        prediction.put(Const.LABEL, predictedLabel);
                        prediction.put(Const.CONFIDENCE, counter);
                        instance.put(Const.VECTOR, values);
                        instance.put(Const.LABELS, label);
                        instance.put(Const.PREDICTION, prediction);
                        if (loss.getValue(instance) > 0) {
                            index.set(instanceIndex, false);
                            break;
                        }
                    }
                    k++;
                }
                instanceIndex++;
            }
        } else if (labelAttribute.isNumerical()) {
            int instanceIndex = 0;
            double predictedLabel;
            double sum;
            Iterator<IVector> samplesIterator = samples.samplesIterator();
            Iterator<IValuesStoreLabels> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                label = labelsIterator.next();
                IVector values = samplesIterator.next();
                sum = 0;
                Collection<DoubleObjectContainer<IValuesStoreLabels>> res = samples.getNearestValueDistances(upperK, values);
                int k = 0;
                for (DoubleObjectContainer<IValuesStoreLabels> it : res) {
                    sum += it.getSecond().getLabel();
                    if (k > lowerK) {
                        predictedLabel = sum / k;                                                
                        prediction.put(Const.LABEL, predictedLabel);
                        prediction.put(Const.CONFIDENCE, null);
                        instance.put(Const.VECTOR, values);
                        instance.put(Const.LABELS, label);
                        instance.put(Const.PREDICTION, prediction);
                        
                        if (loss.getValue(instance) > 0) {
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
