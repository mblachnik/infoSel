/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.Const;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;
import org.prules.dataset.IInstancePrediction;

/**
 * Class implements ENN Vector selection algorithm
 * @author Marcin
 */
public class ICFInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private boolean storeConfidence = false;
    private double[] confidence;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private final IISDecisionFunction loss;
    private double[] classWeight;

/**
     * Constructor for ENN instance selection model.
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param loss - decision function     
     */
    public ICFInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        this.classWeight = null;
    }

    /**
     * Constructor for ENN instance selection model. Also supports class weight matrix for inbalanced problems
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param loss - decision function
     * @param classWeight - class weights matrix - table of doubles which represents importance of each class
     */
    public ICFInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss, double[] classWeight) {
        this.measure = measure;
        this.k = k;
        this.loss = loss;
        this.classWeight = classWeight;
    }

    /**
     * Performs instance selection
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        IDataIndex index = exampleSet.getIndex();
        Attribute labelAttribute = attributes.getLabel();

        int sampleSize = exampleSet.size();
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollection<IInstanceLabels> samples;
        samples = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);
        loss.init(samples);        
        Vector vector;
        double realLabel;
        double predictedLabel = 0;

        int instanceIndex = 0;
        Iterator<Vector> sampleIterator = samples.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = samples.storedValueIterator();
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels label;
        
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
                Collection<IInstanceLabels> res;
                vector = sampleIterator.next();
                label = labelIterator.next();
                realLabel = label.getLabel();
                res = samples.getNearestValues(k + 1, vector);
                double sum = 0;
                for (IInstanceLabels i : res) {
                    int idx = (int)i.getLabel();
                    counter[idx] += classWeight[idx];
                    sum += classWeight[idx];
                }
                counter[(int) realLabel] -= classWeight[(int) realLabel]; //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum -= classWeight[(int) realLabel]; //here we have to subtract because nearest neighbors includ itself, see line above
                predictedLabel = PRulesUtil.findMostFrequentValue(counter);
                if (storeConfidence) {
                    confidence[instanceIndex] = counter[(int) predictedLabel] / sum;
                }        
                
                prediction.setLabel(predictedLabel);
                prediction.setConfidence(counter);
                instance.put(Const.VECTOR, vector);
                instance.put(Const.LABELS, label);
                instance.put(Const.PREDICTION, prediction);
                if (loss.getValue(instance)> 0) {
                    index.set(instanceIndex, false);
                }
                instanceIndex++;
            }            
            for(int i : index.getAsInt()){
                
            }
        } else if (labelAttribute.isNumerical()) {
            while (sampleIterator.hasNext() && labelIterator.hasNext()) {
                predictedLabel = 0;
                Collection<IInstanceLabels> res;
                vector = sampleIterator.next();
                label = labelIterator.next();
                realLabel = label.getLabel();
                res = samples.getNearestValues(k + 1, vector);
                double sum = 0;
                for (IInstanceLabels i : res) {
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
                if (loss.getValue(instance)> 0) {
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
