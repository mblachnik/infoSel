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
        ISPRGeometricDataCollection<IInstanceLabels> dataSet;
        dataSet = KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, distance);
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int m = attributes.size();        
        Vector vector;
        Iterator<Vector> sampleIterator = dataSet.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = dataSet.storedValueIterator();
        Collection<DoubleObjectContainer<IInstanceLabels>> res;
        int exampleCounter = 0; 
        nne = 0;
        int totalEvaluatedSamples = 0;
        while (sampleIterator.hasNext() && labelIterator.hasNext()) {
            vector = sampleIterator.next();
            if (sigma>0){
                res = dataSet.getNearestValueDistances(sigma, vector);                     
            } else {
                res = dataSet.getNearestValueDistances(2, vector);                     
            }
            double delta = 0;            
            Iterator<DoubleObjectContainer<IInstanceLabels>> resIterator = res.iterator();
            double label = 0;
            if (resIterator.hasNext()) {
                label = resIterator.next().getSecond().getLabel();
            }            
            int nearestNeighborIndex = 0;            
            while(resIterator.hasNext()){                    
                IInstanceLabels otherLabels = resIterator.next().getSecond();
                double otherLabel = otherLabels.getLabel();                        
                double error = otherLabel - label;
                delta  += error * error * 0.5;                
                nearestNeighborIndex++;
            }            
            totalEvaluatedSamples += nearestNeighborIndex;            
            nne += delta;            
            exampleCounter++;
        }      
        nne = totalEvaluatedSamples == 0 ? 0 : nne / totalEvaluatedSamples;
        double[] noise = new double[n];
        Arrays.fill(noise,nne);        
        return new PairContainer<>(noise, null);
    }

    @Override
    public double getNNE() {
        return nne;
    }

}
