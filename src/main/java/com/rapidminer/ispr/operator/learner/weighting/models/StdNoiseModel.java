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
import com.rapidminer.ispr.tools.math.BasicMath;
import com.rapidminer.ispr.tools.math.container.PairContainer;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.knn.KNNFactory;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Marcin
 */
public class StdNoiseModel extends AbstractNoiseEstimatorModel {

    DistanceMeasure distance;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    int k = 1;
    double nne = 0;

    public StdNoiseModel(DistanceMeasure distance, int k) {
        assert distance != null;
        assert k >= 1;
        this.k = k;
        this.distance = distance;
        this.nne = Double.NaN;
    }

    @Override
    public PairContainer<double[], double[]> run(ExampleSet exampleSet) {
        ISPRGeometricDataCollection<IValuesStoreLabels> knn;
        knn = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int m = attributes.size();
        double[] noise = new double[n];
        IVector vector;
        Iterator<IVector> sampleIterator = knn.samplesIterator();
        Iterator<IValuesStoreLabels> labelIterator = knn.storedValueIterator();
        Collection<IValuesStoreLabels> res;
        int exampleIndex = 0;
        nne = 0;
        double[] values = new double[k];
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValues(k + 1, vector);
            int nearestNeighborIndex = -1;            
            double mean = 0;                        
            for (IValuesStoreLabels labels : res) {
                if (nearestNeighborIndex >= 0) {
                    values[nearestNeighborIndex] = labels.getLabel();                    
                    mean += labels.getLabel(); 
                } 
                nearestNeighborIndex++;
            }
            mean /= nearestNeighborIndex;
            double var = BasicMath.simpleVariance(values, mean)*0.5;
            nne += var;
            noise[exampleIndex] = var;
            exampleIndex++;
        }
        nne /= exampleIndex;
        return new PairContainer<>(noise, null);
    }

    @Override
    public double getNNE() {
        return nne;
    }

}
