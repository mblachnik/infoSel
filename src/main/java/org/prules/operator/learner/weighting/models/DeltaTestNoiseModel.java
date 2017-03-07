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
import java.util.Collection;
import java.util.Iterator;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

/**
 * This class is used to estimate the level of noise for each example in the input data
 * It takes a sample and its k nearest neighbors, than calculates 
 * @author Marcin
 */
public class DeltaTestNoiseModel extends AbstractNoiseEstimatorModel {

    DistanceMeasure distance;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    int k = 1;
    double nne = 0;

    public DeltaTestNoiseModel(DistanceMeasure distance, int k) {
        assert distance != null;
        assert k >= 1;
        this.k = k;
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
        Collection<IInstanceLabels> res;
        int exampleIndex = 0; 
        nne = 0;
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValues(k+1, vector);                         
            int nearestNeighborIndex = -1;
            double label = Double.NaN;
            double delta = 0;
            for (IInstanceLabels labels : res){
                if (nearestNeighborIndex>=0){
                    double error = labels.getLabel() - label;
                    delta  += error * error * 0.5;
                } else {
                    label = labels.getLabel();
                }
                nearestNeighborIndex++;                                       
            }            
            delta /= nearestNeighborIndex;
            nne += delta;
            noise[exampleIndex] = delta;
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
