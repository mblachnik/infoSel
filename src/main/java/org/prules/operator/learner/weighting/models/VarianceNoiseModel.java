/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.tools.math.BasicMath;
import org.prules.tools.math.container.PairContainer;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Marcin
 */
public class VarianceNoiseModel extends AbstractNoiseEstimatorModel {

    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private DistanceMeasure distance;
    private int k = 1;
    private double nne = 0;

    public VarianceNoiseModel(DistanceMeasure distance, int k) {
        assert distance != null;
        assert k >= 1;
        this.k = k;
        this.distance = distance;
        this.nne = Double.NaN;
    }

    @Override
    public PairContainer<double[], double[]> run(ExampleSet exampleSet) {
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
        int exampleCounter = 0;
        nne = 0;
        double[] values = new double[k];
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            double label = labelIterator.next().getLabel();
            res = knn.getNearestValues(k + 1, vector);
            int nearestNeighborIndex = -1;
            double mean = 0;
            for (IInstanceLabels labels : res) {
                if (nearestNeighborIndex >= 0) {
                    values[nearestNeighborIndex] = labels.getLabel();
                    mean += labels.getLabel();
                }
                nearestNeighborIndex++;
            }
            mean = nearestNeighborIndex == 0 ? 0 : mean / nearestNeighborIndex;
            double var = BasicMath.simpleVariance(values, mean);
            nne += var;
            noise[exampleCounter] = var;
            exampleCounter++;
        }
        nne = exampleCounter == 0 ? 0 : nne / exampleCounter;
        return new PairContainer<>(noise, null);
    }

    @Override
    public double getNNE() {
        return nne;
    }
}
