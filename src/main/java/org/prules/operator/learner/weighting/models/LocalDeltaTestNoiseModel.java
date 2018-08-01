/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.PairContainer;
import org.prules.tools.math.container.knn.KNNFactory;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import java.util.Iterator;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class LocalDeltaTestNoiseModel extends AbstractNoiseEstimatorModel {

    DistanceMeasure distance;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.CACHED_LINEAR_SEARCH;
    /**
     * The width of the sourrounding where we search for nearest neighbors
     */
    double sigma;
    /**
     * Range for which we calculate local GammaTest
     */
    int range;
    /**
     * The value of the noise
     */
    double nne;    

    public LocalDeltaTestNoiseModel(DistanceMeasure distance, double sigma, int range) {
        assert distance != null;
        assert sigma > 0;
        assert range > 1;
        this.sigma = sigma;
        this.range = range;
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
        nne = 0;
        Vector vector;
        Iterator<Vector> sampleIterator = knn.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = knn.storedValueIterator();
        Collection<DoubleObjectContainer<IInstanceLabels>> localRes;
        Collection<IInstanceLabels> res;
        int instanceIndex = 0;        
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            res = knn.getNearestValues(range, vector);                          
            Iterator<IInstanceLabels> resIterator = res.iterator();            
            int nearestNeighborsCount = 0;            
            double localNNE = 0;
            while (resIterator.hasNext()) {                                
                IInstanceLabels lab = resIterator.next();
                Vector localVector = knn.getSample(lab.getValueAsInt(Const.INDEX_CONTAINER));
                localRes = knn.getNearestValueDistances(sigma, localVector);                                                              
                Iterator<DoubleObjectContainer<IInstanceLabels>> localResIterator = localRes.iterator();
                double delta = 0;  
                double label = 0;
                if (localResIterator.hasNext()) {
                    label = localResIterator.next().getSecond().getLabel();
                }                
                int nearestIndex = 0;
                while(localResIterator.hasNext()){
                        //nearestIndex = 0; nearestIndex<localResTab.length-1; nearestIndex++){
                    IInstanceLabels otherLabels = localResIterator.next().getSecond();
                    double otherLabel = otherLabels.getLabel();
                    double error = otherLabel - label;                    
                    delta += 0.5 * error * error;                                                            
                    nearestIndex++;
                }
                nearestNeighborsCount += nearestIndex;
                localNNE += delta;
            }
            localNNE /= nearestNeighborsCount;
            noise[instanceIndex] = localNNE;
            nne += localNNE;            
            instanceIndex++;
        }
        nne /= exampleSet.size();        
                
        return new PairContainer<>(noise, null);
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

}
