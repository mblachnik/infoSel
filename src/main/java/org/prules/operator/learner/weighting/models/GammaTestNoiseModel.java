/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.tools.math.SimpleLinearRegressionModel;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.PairContainer;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Marcin
 */
public class GammaTestNoiseModel extends AbstractNoiseEstimatorModel {

    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.CACHED_LINEAR_SEARCH;
    private DistanceMeasure distance;
    private int k;
    private double nne;
    private double nne_slope;

    public GammaTestNoiseModel(DistanceMeasure distance, int k) {
        assert distance != null;
        assert k >= 1;
        this.k = k;
        this.distance = distance;
    }

    @Override
    public PairContainer<double[], double[]> run(ExampleSet exampleSet) {
        ISPRGeometricDataCollection<IInstanceLabels> knn;
        knn = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        double[] noise = new double[n];
        double[] slope = new double[n];
        double[] nneDistances = new double[k];
        double[] nneLabels = new double[k];
        Vector vector;
        Iterator<Vector> sampleIterator = knn.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = knn.storedValueIterator();
        Collection<DoubleObjectContainer<IInstanceLabels>> res;
        double[] deviationLabels = new double[k]; //The -1 is used to ignore its selve in nearest neighbors
        double[] deviationDist = new double[k]; //The -1 is used to ignore its selve in nearest neighbors
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValueDistances(k + 1, vector);
            double label = Double.NaN;
            Iterator<DoubleObjectContainer<IInstanceLabels>> resIterator = res.iterator();
            if (resIterator.hasNext()) {
                label = resIterator.next().getSecond().getLabel();
            }
            int nearestIndex = 0;
            while (resIterator.hasNext()) {
                DoubleObjectContainer<IInstanceLabels> distAndLabel = resIterator.next();
                double error = label - distAndLabel.getSecond().getLabel();
                double nearestDistance = distAndLabel.getFirst();
                deviationLabels[nearestIndex] = 0.5 * error * error;
                deviationDist[nearestIndex] = nearestDistance * nearestDistance;
                nneLabels[nearestIndex] += deviationLabels[nearestIndex];
                nneDistances[nearestIndex] += deviationDist[nearestIndex];
                nearestIndex++;
            }
        }
        for (int i = 0; i < k; i++) {
            nneLabels[i] = n == 0 ? 0 : nneLabels[i] / n;
            nneDistances[i] = n == 0 ? 0 : nneDistances[i] / n;
        }
        SimpleLinearRegressionModel linearModel = new SimpleLinearRegressionModel();
        linearModel.train(nneDistances, nneLabels);
        nne = linearModel.getB();
        nne_slope = linearModel.getA();
        Arrays.fill(noise, nne);
        Arrays.fill(slope, nne_slope);
        return new PairContainer<>(noise, slope);
    }

    /**
     * Returns the level of noise in the data. Note that this method approximates the level of noise using simple linear regression,
     * In this case we can get both the level of noise as the bias parameter, and the slope of the noise.
     *
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
