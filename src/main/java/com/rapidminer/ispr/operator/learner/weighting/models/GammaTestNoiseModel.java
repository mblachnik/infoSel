/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.weighting.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IVector;
import com.rapidminer.ispr.tools.math.SimpleLinearRegressionModel;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.PairContainer;
import com.rapidminer.ispr.tools.math.container.knn.KNNFactory;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Marcin
 */
public class GammaTestNoiseModel extends AbstractNoiseEstimatorModel {

    DistanceMeasure distance;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.CACHED_LINEAR_SEARCH;
    int k;
    double nne;
    double nne_slope;
    
    public GammaTestNoiseModel(DistanceMeasure distance, int k) {
        assert distance != null;
        assert k >= 1;
        this.k = k;
        this.distance = distance;
    }

    @Override
    public PairContainer<double[],double[]> run(ExampleSet exampleSet) {
        ISPRGeometricDataCollection<IValuesStoreLabels> knn;
        knn = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int m = attributes.size();
        double[] noise = new double[n];
        double[] slope = new double[n];
        double[] nneDistances = new double[k];
        double[] nneLabels    = new double[k];
        IVector vector;
        Iterator<IVector> sampleIterator = knn.samplesIterator();
        Iterator<IValuesStoreLabels> labelIterator = knn.storedValueIterator();
        Collection<DoubleObjectContainer<IValuesStoreLabels>> res;
        int instanceIndex = 0;
        SimpleLinearRegressionModel linearModel = new SimpleLinearRegressionModel();
        double[] deviationLabels = new double[k]; //The -1 is used to ignore its selve in nearest neighbors
        double[] deviationDist   = new double[k]; //The -1 is used to ignore its selve in nearest neighbors
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValueDistances(k+1, vector);                         
            int nearestIndex = -1;
            double label = Double.NaN;
            double dist  = Double.NaN;
            for (DoubleObjectContainer<IValuesStoreLabels> distAndLabel : res){
                if (nearestIndex>=0){
                    double error = distAndLabel.getSecond().getLabel() - label; 
                    double nearestDistance = distAndLabel.getFirst();
                    deviationLabels[nearestIndex] = 0.5 * error * error;                    
                    deviationDist[nearestIndex]   = nearestDistance * nearestDistance;                    
                    nneLabels[nearestIndex]    += deviationLabels[nearestIndex];
                    nneDistances[nearestIndex] += deviationDist[nearestIndex];
                } else {
                    label = distAndLabel.getSecond().getLabel(); //The first value is the qctual value (it selve)
                }
                nearestIndex++;                                       
            }            
            linearModel.train(deviationDist, deviationLabels);
                        
            noise[instanceIndex] = linearModel.getB();
            slope[instanceIndex] = linearModel.getA();
            instanceIndex++;
        }
        for(int i=0; i<k; i++){
            nneLabels[i] /= instanceIndex;
            nneDistances[i] /= instanceIndex;
        }
        linearModel.train(nneDistances, nneLabels);
        nne       = linearModel.getB();
        nne_slope = linearModel.getA();
        return new PairContainer<>(noise, slope);
    }

    /**
     * Returns the level of noise in the data. Note that this method approximates the level of noise using simple linear regression,
     * In this case we can get both the level of noise as the bias parameter, and the slope of the noise.
     * @return 
     */
    public double getNNE() {
        return nne;
    }

    public double getNNESlope() {
        return nne_slope;
    }        

}
