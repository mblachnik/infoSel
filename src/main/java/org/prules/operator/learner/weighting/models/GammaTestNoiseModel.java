/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import org.prules.tools.math.SimpleLinearRegressionModel;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.PairContainer;
import org.prules.tools.math.container.knn.KNNFactory;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

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
        ISPRGeometricDataCollection<IInstanceLabels> knn;
        knn = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int m = attributes.size();
        double[] noise = new double[n];
        double[] slope = new double[n];
        double[] nneDistances = new double[k];
        double[] nneLabels    = new double[k];
        Vector vector;
        Iterator<Vector> sampleIterator = knn.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = knn.storedValueIterator();
        Collection<DoubleObjectContainer<IInstanceLabels>> res;
        int instanceIndex = 0;
        SimpleLinearRegressionModel linearModel = new SimpleLinearRegressionModel();
        double[] deviationLabels = new double[k]; //The -1 is used to ignore its selve in nearest neighbors
        double[] deviationDist   = new double[k]; //The -1 is used to ignore its selve in nearest neighbors
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValueDistances(k, vector);              
            double label = Double.NaN;
            double dist  = Double.NaN;
            int nearestIndex = 0;
            for (DoubleObjectContainer<IInstanceLabels> distAndLabel : res){
                if (nearestIndex==0){
                    label = distAndLabel.getSecond().getLabel(); //The first value is the qctual value (it selve)
                }
                double error = label - distAndLabel.getSecond().getLabel(); 
                double nearestDistance = distAndLabel.getFirst();
                deviationLabels[nearestIndex] = 0.5 * error * error;                    
                deviationDist[nearestIndex]   = nearestDistance * nearestDistance;                    
                nneLabels[nearestIndex]    += deviationLabels[nearestIndex];
                nneDistances[nearestIndex] += deviationDist[nearestIndex];                
                nearestIndex++;                                       
            }            
            linearModel.train(deviationDist, deviationLabels);
                        
            noise[instanceIndex] = linearModel.getB();
            slope[instanceIndex] = linearModel.getA();
            instanceIndex++;
        }
        for(int i=0; i<k; i++){
            nneLabels[i] /= knn.size();
            nneDistances[i] /= knn.size();
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
    @Override
    public double getNNE() {
        return nne;
    }

    public double getNNESlope() {
        return nne_slope;
    }        

}
