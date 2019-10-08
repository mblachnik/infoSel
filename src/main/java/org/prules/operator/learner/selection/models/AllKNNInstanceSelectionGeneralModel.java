/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.*;
import org.prules.operator.learner.classifiers.IS_KNNClassificationModel;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.tools.math.container.knn.KNNTools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

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
    IS_KNNClassificationModel<IInstanceLabels> model;

    /**
     * Constructor
     *
     * @param measure - distance measure
     * @param lowerK  - lower bound for k
     * @param upperK  - upper bound for k
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
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        IDataIndex index = exampleSet.getIndex();
        Attribute labelAttribute = attributes.getLabel();

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples;
        samples = (ISPRGeometricDataCollectionWithIndex) KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        //All - kNN EDITING
        loss.init(samples);

        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels label;
        IDataIndex samplesIndex = samples.getIndex();
        samplesIndex.setAllTrue();
        if (labelAttribute.isNominal()) {
            double[] counter;
            int instanceIndex = 0;
            double predictedLabel;
            counter = new double[labelAttribute.getMapping().size()];
            Iterator<Vector> samplesIterator = samples.samplesIterator();
            Iterator<IInstanceLabels> labelsIterator = samples.storedValueIterator();
            double performanceStep = 1.0 / upperK;
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                label = labelsIterator.next();
                Vector values = samplesIterator.next();
                Arrays.fill(counter, 0);
                counter[(int) label.getLabel()] = -performanceStep; //Note this is because we use k+1, because always we are the most similar to ourselves
                samplesIndex.set(instanceIndex, false);
                Collection<DoubleObjectContainer<IInstanceLabels>> res = samples.getNearestValueDistances(upperK, values, samplesIndex);
                //It is not guaranteed that the res is sorted according to the value of distance, so first we have to sort it
                samplesIndex.set(instanceIndex, true);
                DoubleObjectContainer<IInstanceLabels>[] resArray;
                resArray = res.toArray(new DoubleObjectContainer[res.size()]);
                Arrays.sort(resArray);
                int k = 0;
                for (DoubleObjectContainer<IInstanceLabels> it : resArray) {
                    int i = (int) it.getSecond().getLabel();
                    counter[i] += performanceStep;
                    if (k > lowerK) {
                        predictedLabel = KNNTools.getMostFrequentValue(counter);
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
            Iterator<Vector> samplesIterator = samples.samplesIterator();
            Iterator<IInstanceLabels> labelsIterator = samples.storedValueIterator();
            while (samplesIterator.hasNext() && labelsIterator.hasNext()) {
                label = labelsIterator.next();
                Vector values = samplesIterator.next();
                sum = 0;
                samplesIndex.set(instanceIndex, false);
                Collection<DoubleObjectContainer<IInstanceLabels>> res = samples.getNearestValueDistances(upperK, values, samplesIndex);
                samplesIndex.set(instanceIndex, true);
                int k = 0;
                for (DoubleObjectContainer<IInstanceLabels> it : res) {
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
