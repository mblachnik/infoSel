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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class LocalGammaTestNoiseModel extends AbstractNoiseEstimatorModel {

    DistanceMeasure distance;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.CACHED_LINEAR_SEARCH;
    /**
     * Number of nearest neighbors
     */
    int k;
    /**
     * Range for which we calculate local GammaTest
     */
    int range;
    double nne;
    double nne_slope;

    public LocalGammaTestNoiseModel(DistanceMeasure distance, int k, int range) {
        assert distance != null;
        assert k >= 1;
        this.k = k;
        this.range = range > k ? range : k;
        this.distance = distance;
    }

    @Override
    public PairContainer<double[], double[]> run(ExampleSet exampleSet) {
        ISPRGeometricDataCollection<IInstanceLabels> knn;
        knn = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int m = attributes.size();
        double[] noise = new double[n];
        double[] slope = new double[n];
        double[] nneDistances = new double[k];
        double[] nneLabels = new double[k];
        Vector vector;
        Iterator<Vector> sampleIterator = knn.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = knn.storedValueIterator();
        Collection<DoubleObjectContainer<IInstanceLabels>> localRes;
        Collection<IInstanceLabels> res;
        int instanceIndex = 0;
        SimpleLinearRegressionModel linearModel = new SimpleLinearRegressionModel();
        double deviationLabels; //The -1 is used to ignore its selve in nearest neighbors
        double deviationDist; //The -1 is used to ignore its selve in nearest neighbors
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValues(range, vector);              
            IInstanceLabels[] resTab = res.toArray(new IInstanceLabels[0]);
            //Arrays.sort(resTab);
            for (int i=0; i<resTab.length; i++) {
                IInstanceLabels lab = resTab[i];
                Vector localVector = knn.getSample(lab.getValueAsInt(Const.INDEX_CONTAINER));
                localRes = knn.getNearestValueDistances(k, localVector);                
                double dist = Double.NaN;                
                DoubleObjectContainer<IInstanceLabels>[] localResTab = localRes.toArray(new DoubleObjectContainer[0]);
                int nearestIndex = 0;
                double label = localResTab[nearestIndex].getSecond().getLabel();
                for(nearestIndex = 0; nearestIndex<localResTab.length-1; nearestIndex++){
                    DoubleObjectContainer<IInstanceLabels> distAndLabel = localResTab[nearestIndex+1];                    
                    double error = distAndLabel.getSecond().getLabel() - label;
                    double nearestDistance = distAndLabel.getFirst();
                    deviationLabels = 0.5 * error * error;
                    deviationDist = nearestDistance * nearestDistance;
                    nneLabels[nearestIndex] += deviationLabels;
                    nneDistances[nearestIndex] += deviationDist;                    
                }
            }
            double size = res.size();
            for (int i = 0; i < k; i++) {
                nneLabels[i] /= size;
                nneDistances[i] /= size;
            }
            linearModel.train(nneDistances, nneLabels);
            noise[instanceIndex] = linearModel.getB();
            slope[instanceIndex] = linearModel.getA();
            instanceIndex++;
        }

        return new PairContainer<>(noise, slope);
    }

    /**
     * Returns the level of noise in the data. Note that this method
     * approximates the level of noise using simple linear regression, In this
     * case we can get both the level of noise as the bias parameter, and the
     * slope of the noise.
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
