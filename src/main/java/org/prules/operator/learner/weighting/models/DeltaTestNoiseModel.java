/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.PairContainer;
import org.prules.tools.math.container.knn.KNNFactory;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.tools.math.container.DoubleObjectContainer;

/**
 * This class is used to estimate the level of noise for each example in the input data
 * It takes a sample and its k nearest neighbors, than calculates 
 * @author Marcin
 */
public class DeltaTestNoiseModel extends AbstractNoiseEstimatorModel {

    DistanceMeasure distance;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    double sigma = 1;
    double nne = 0;

    public DeltaTestNoiseModel(DistanceMeasure distance, double sigma) {
        assert distance != null;
        assert sigma > 0;
        this.sigma = sigma;
        this.distance = distance;
        this.nne = Double.NaN;
    }

    @Override
    public PairContainer<double[],double[]> run(ExampleSet exampleSet) {
        ISPRGeometricDataCollection<IInstanceLabels> knn;
        knn = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int m = attributes.size();
        double[] noise = new double[n];
        Vector vector;
        Iterator<Vector> sampleIterator = knn.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = knn.storedValueIterator();
        Collection<DoubleObjectContainer<IInstanceLabels>> res;
        int exampleIndex = 0; 
        nne = 0;
        int totalEvaluatedSamples = 0;
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValueDistances(sigma, vector);                     
            double delta = 0;            
            DoubleObjectContainer<IInstanceLabels>[] resTab;
            resTab = res.toArray(new DoubleObjectContainer[0]);
            Arrays.sort(resTab);            
            int nearestNeighborIndex = 0;
            double label = resTab[nearestNeighborIndex].getSecond().getLabel();
            for(nearestNeighborIndex = 0; nearestNeighborIndex < resTab.length-1; nearestNeighborIndex++){ //The loop starts from 0 in case resTab has only one element (itselfe - no neighbors) than if we start from 1 we will add 1 to totalEvaluatedSamples           
                IInstanceLabels labels = resTab[nearestNeighborIndex+1].getSecond();
                double error = labels.getLabel() - label;
                delta  += error * error * 0.5;                
            }            
            totalEvaluatedSamples += nearestNeighborIndex;            
            nne += delta;
            noise[exampleIndex] = delta/nearestNeighborIndex;
            exampleIndex++;
        }        
        nne /= totalEvaluatedSamples;
        return new PairContainer<>(noise, null);
    }

    @Override
    public double getNNE() {
        return nne;
    }

}
