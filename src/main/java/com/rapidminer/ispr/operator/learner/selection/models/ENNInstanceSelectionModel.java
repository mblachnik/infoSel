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
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.knn.KNNTools;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.tools.EmptyInstanceModifier;
import com.rapidminer.ispr.operator.learner.selection.models.tools.InstanceModifier;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
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

/**
 * Class implements ENN Vector selection algorithm
 *
 * @author Marcin
 */
public class ENNInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private boolean storeConfidence = false;
    private double[] confidence;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private final IISDecisionFunction loss;
    private double[] classWeight;
    private final InstanceModifier modifier;

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param loss - decision function
     */
    public ENNInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        this.classWeight = null;
        this.modifier = new EmptyInstanceModifier();
    }

    /**
     * Constructor for ENN instance selection model. Also supports class weight
     * matrix for inbalanced problems
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param loss - decision function
     * @param classWeight - class weights matrix - table of doubles which
     * represents importance of each class
     * @param modifier element of InstanceModifier class which allows to modify processed instance on the fly. Used for example for noise modifier
     */
    public ENNInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss, double[] classWeight, InstanceModifier modifier) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        this.classWeight = classWeight;        
        if (modifier == null){
            this.modifier = new EmptyInstanceModifier();
        } else {
            this.modifier = modifier;
        }
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
        DataIndex index = exampleSet.getIndex();
        Attribute labelAttribute = attributes.getLabel();

        int sampleSize = exampleSet.size();
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollection<IValuesStoreLabels> samples;
        samples = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);
        loss.init(samples);
        if (storeConfidence) {
            confidence = new double[sampleSize];
        }
        //ENN EDITTING
        IVector vector;
        double realLabel;
        double predictedLabel = 0;

        int instanceIndex = 0;
        Iterator<IVector> sampleIterator = samples.samplesIterator();
        Iterator<IValuesStoreLabels> labelIterator = samples.storedValueIterator();
        IValuesStorePrediction prediction = ValuesStoreFactory.createPrediction(Double.NaN, null);
        IValuesStoreInstance instance = ValuesStoreFactory.createEmptyValuesStoreInstance();
        IValuesStoreLabels label;
        
        if (labelAttribute.isNominal()) {
            if (this.classWeight == null) {
                this.classWeight = new double[labelAttribute.getMapping().size()];
                for (int i = 0; i < classWeight.length; i++) {
                    classWeight[i] = 1.0d;
                }
            }
            int numberOfClasses = labelAttribute.getMapping().size();
            double[] counter = new double[numberOfClasses];            
            while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                Arrays.fill(counter, 0);
                Collection<IValuesStoreLabels> res;
                vector = modifier.modify(sampleIterator.next());                
                label = labelIterator.next();
                realLabel = label.getLabel();
                res = samples.getNearestValues(k + 1, vector);                
                int sum = 0;
                for (IValuesStoreLabels i : res) {
                    int idx = (int)i.getLabel();
                    counter[idx] ++;                     
                    sum ++;
                }
                counter[(int) realLabel] --; //here we have to subtract distanceRate because we took k+1 neighbours 					                            
                sum--;   //as above              
                for(int i = 0; i<counter.length; i++){
                    counter[i] *= classWeight[i] / sum;
                }
                predictedLabel = PRulesUtil.findMostFrequentValue(counter);
                if (storeConfidence) {
                    confidence[instanceIndex] = counter[(int) predictedLabel];
                }                
                prediction.setLabel(predictedLabel);
                prediction.setConfidence(counter);
                instance.put(Const.VECTOR, vector);
                instance.put(Const.LABELS, label);
                instance.put(Const.PREDICTION, prediction);
                if (loss.getValue(instance) > 0) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }
        } else if (labelAttribute.isNumerical()) {
            while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                predictedLabel = 0;
                Collection<IValuesStoreLabels> res;
                vector = modifier.modify(sampleIterator.next());
                label = labelIterator.next();
                realLabel = label.getLabel();
                res = samples.getNearestValues(k + 1, vector);
                double sum = 0;
                for (IValuesStoreLabels i : res) {
                    predictedLabel += i.getLabel();
                    sum++;
                }
                predictedLabel -= realLabel;  //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum--; //here we have to subtract because nearest neighbors includ itself, see line above                
                predictedLabel /= sum;
                prediction.setLabel(predictedLabel);
                instance.put(Const.VECTOR, vector);
                instance.put(Const.LABELS, label);
                instance.put(Const.PREDICTION, prediction);
                if (loss.getValue(instance) > 0) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }
        }
        return index;

    }

    public double[] getConfidence() {
        return confidence;
    }

    public boolean isStoreConfidence() {
        return storeConfidence;
    }

    public void setStoreConfidence(boolean storeConfidence) {
        this.storeConfidence = storeConfidence;
    }
}
