/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.error.AttributeNotFoundError;
import org.prules.dataset.Const;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.DataWeightIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.IDataWeightIndex;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;
import org.prules.dataset.IInstancePrediction;
import org.prules.operator.learner.tools.PredictionProblemType;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;
import org.prules.tools.math.container.knn.KNNTools;

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
    //private Map<Double,Double> classWeightMap;
    private double[] classWeight;
    private boolean weightedNN;

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param loss - decision function
     * @param weightedNN
     */
    public ENNInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss, boolean weightedNN) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        //this.classWeightMap = null;
        this.classWeight = null;
        this.weightedNN = weightedNN;
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
     * @param weightedNN
     */
    public ENNInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss, 
            //Map<Double,Double> classWeight, 
            double[] classWeight, 
            boolean weightedNN) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        //this.classWeightMap = classWeight;
        this.classWeight = classWeight;
        this.weightedNN = weightedNN;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     * performed
     * @return - index of selected examples
     */
    @Override
    public IDataWeightIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        IDataIndex tmpIndex = exampleSet.getIndex();
        IDataWeightIndex index = new DataWeightIndex(tmpIndex);
        Attribute labelAttribute = attributes.getLabel(); 
        if (labelAttribute == null){
            throw new RuntimeException("Label attribute noe found");
        }
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples;
        samples = (ISPRGeometricDataCollectionWithIndex) KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);
        loss.init(samples);
        if (labelAttribute.isNominal()) {
            index = selectInstances(samples,PredictionProblemType.CLASSIFICATION);
        } else if (labelAttribute.isNumerical()){
            index = selectInstances(samples,PredictionProblemType.REGRESSION);
        }
        return index;
    }

    /**
     * This method implements the main algorithm. It requires samples data structure as well as prediction type - regression or classification
     * Here by default loss function is initialized
     * @param samples
     * @param type
     * @return 
     */
    public IDataWeightIndex selectInstances(ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples, PredictionProblemType type) {    
        return selectInstances(samples, type, true);
    }
    
    /**
     * This method implements the main algorithm. It requires samples data structure as well as prediction type - regression or classification
     * The last argument allows to swich if the loss function should be initialized
     * @param samples the data structure
     * @param type the type of problem classification/regression
     * @param lossInitialization switch which allows to set if loss function should be initialized
     * @return 
     */
    public IDataWeightIndex selectInstances(ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples, PredictionProblemType type, boolean lossInitialization) {
        if (lossInitialization){
            loss.init(samples);
        }
        int numberOfSamples = samples.size();               
        if (storeConfidence) {
            confidence = new double[numberOfSamples];
        }
        //ENN EDITTING
        Vector vector;
        double realLabel;
        double predictedLabel = 0;

        int instanceIndex = 0;
        Iterator<Vector> sampleIterator = samples.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = samples.storedValueIterator();
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels label;
        IDataIndex indexSwitchOfClassifiedSample = samples.getIndex();
        indexSwitchOfClassifiedSample.setAllTrue();
        IDataWeightIndex index = new DataWeightIndex(numberOfSamples);
        switch (type) {
            case CLASSIFICATION:
                Map<Double,Integer> classCountMap = PRulesUtil.countClassFrequency(samples);
                Set<Double> uniqueLabels = classCountMap.keySet();
                int numberOfClasses = (int)(Collections.max(uniqueLabels)).doubleValue()+1; //Plus 1 becouse label values starts from 0, so when max=2 there are 3 class labels [0, 1, 2]
                double[] counter = new double[numberOfClasses];
                if (classWeight == null){
                    classWeight = new double[numberOfClasses];
                    for(int i = 0; i < numberOfClasses; i++){
                        classWeight[i] = 1.0;
                    }
                }                
                int iteration = 0;
                while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                    
                    //Reset counter
                    Arrays.fill(counter, 0);
                    Collection<DoubleObjectContainer<IInstanceLabels>> res;
                    vector = sampleIterator.next();
                    label = labelIterator.next();
                    realLabel = label.getLabel();
                    indexSwitchOfClassifiedSample.set(instanceIndex, false);
                    res = samples.getNearestValueDistances(k, vector, indexSwitchOfClassifiedSample);
                    indexSwitchOfClassifiedSample.set(instanceIndex, true);
                    double sum = 0;
                    for (DoubleObjectContainer<IInstanceLabels> i : res) {
                        IInstanceLabels lab = i.getSecond();
                        double dist = i.getFirst();
                        double idx = lab.getLabel();
                        double w;
                        if (weightedNN) {
                            w = 1 / (1 + dist);
                        } else {
                            w = 1;
                        }
                        counter[(int)idx] += w;
                        sum += w;
                    }
                    //counter[(int) realLabel]--; //here we have to subtract distanceRate because we took k+1 neighbours as the dataset containes the query instance for which distance = 0, so the 1/(1+dist) = 1;
                    //sum--;   //as above              
                    //Normalizing counter
                    //First we normalize counter to 0-1 range, then apply weights
                    double norm = 0;
                    for (int i = 0; i < counter.length; i++) {
                        counter[i] /= sum; //We normalize to 1;
                        counter[i] *= classWeight[i];
                        norm += counter[i];
                    }
                    //Finally we normalize including weights                    
                    for (int i = 0; i < counter.length; i++) {
                        counter[i] /= norm;
                    }
                    predictedLabel = KNNTools.<Double>getMostFrequentValue(counter);
     
                    if (storeConfidence) {
//                        confidence[instanceIndex] = counterMap.get(predictedLabel);
                        confidence[instanceIndex] = counter[(int)predictedLabel];
                    }
                    prediction.setLabel(predictedLabel);
                    prediction.setConfidence(counter);
                    instance.put(Const.VECTOR, vector);
                    instance.put(Const.LABELS, label);
                    instance.put(Const.PREDICTION, prediction);
                    double lossValue = loss.getValue(instance);
                    int classCount = classCountMap.get(realLabel);
                    if (lossValue > 0 && classCount > 1) {
                        index.set(instanceIndex, false, lossValue);
                        classCount--;
                        classCountMap.put(realLabel,classCount);
                    }
                    index.setWeight(instanceIndex, lossValue);
                    instanceIndex++;
                }
                break;
            case REGRESSION:

                while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                    predictedLabel = 0;
                    Collection<IInstanceLabels> res;
                    vector = sampleIterator.next();
                    label = labelIterator.next();
                    realLabel = label.getLabel();
                    indexSwitchOfClassifiedSample.set(instanceIndex, false);
                    res = samples.getNearestValues(k, vector, indexSwitchOfClassifiedSample);
                    indexSwitchOfClassifiedSample.set(instanceIndex, true);
                    double sum = 0;
                    for (IInstanceLabels i : res) {
                        predictedLabel += i.getLabel();
                        sum++;
                    }
                    //predictedLabel -= realLabel;  //here we have to subtract distanceRate because we took k+1 neighbours 					            
                    //sum--; //here we have to subtract because nearest neighbors includ itself, see line above                
                    predictedLabel /= sum;
                    prediction.setLabel(predictedLabel);
                    instance.put(Const.VECTOR, vector);
                    instance.put(Const.LABELS, label);
                    instance.put(Const.PREDICTION, prediction);
                    double lossValue = loss.getValue(instance);
                    if (lossValue > 0) {
                        index.set(instanceIndex, false, lossValue);
                    }
                    index.setWeight(instanceIndex, lossValue);
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
