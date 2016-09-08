package com.rapidminer.ispr.operator.learner.clustering.models;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import com.rapidminer.ispr.dataset.IVector;

/**
 *
 * @author Marcin
 */
public class FCMModel extends AbstractBatchModel {

    boolean nextItertion = false;
    double m;
    double minGain;
    int iteration, numberOfIterations, numberOfPrototypes;
    RandomGenerator randomGenerator;    

    /**
     * Constructor of the model
     * @param distance - distance measure
     * @param m - fuzziness parameter
     * @param maxIterations - number of iterations
     * @param minGain - minimal gain required to perform new iteration
     * @param randomGenerator - random number generators
     * @param numberOfPrototypes - number of clusters
     */
    public FCMModel(DistanceMeasure distance, double m, int maxIterations, double minGain, RandomGenerator randomGenerator, int numberOfPrototypes) {
        super(distance);
        this.m = m;
        iteration = 0;
        this.minGain = minGain;
        this.numberOfIterations = maxIterations;
        this.randomGenerator = randomGenerator;
        this.numberOfPrototypes = numberOfPrototypes;
    }

    /**
     * Method returns true if next iteration of the clustering algorithm should be performed
     * @return
     */
    @Override
    public boolean nextIteration() {
        iteration++;
        return nextItertion && (iteration < numberOfIterations);
    }

    /**
     * Update prototype posiotion based on partition matrix
     * @param trainingSet 
     */
    @Override
    public void updatePrototypes(ExampleSet trainingSet) {
        int prototypeIndex = 0;
        for (IVector prototype : prototypes) {
            Iterator<Attribute> trainingAttributesIterator = trainingSet.getAttributes().iterator();
            int attribute = 0;
            while (trainingAttributesIterator.hasNext()) {
                Attribute trainingAttribute = trainingAttributesIterator.next();
                double value = 0; //prototype.getValue(prototypeAttribute);
                double sum = 0;
                Iterator<Example> trainingSetIterator = trainingSet.iterator();
                Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
                while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
                    Example example = trainingSetIterator.next();
                    double[] partitionMatrixEntry = partitionMatrixIterator.next();
                    //double mf = example.getValue(u.get(i));
                    double mf = partitionMatrixEntry[prototypeIndex];
                    mf = Math.pow(mf, m);
                    value += example.getValue(trainingAttribute) * mf;
                    sum += mf;
                }
                prototype.getValues()[attribute] = value / sum;
                attribute++;
            }
            prototypeIndex++;
        }
    }

    /**
     * Based on prototypes calculates partition matrix
     * @param trainingSet     
     */
    
    @Override
    public void updatePartitionMatrix(ExampleSet trainingSet) {
        double objFun = 0;
        double exp = -2.0 / (m - 1.0);
        double[] exampleValues = new double[trainingSet.getAttributes().size()];
        Iterator<Example> trainingSetIterator = trainingSet.iterator();
        Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
        while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
            Example example = trainingSetIterator.next();
            KNNTools.extractExampleValues(example, exampleValues);
            double[] partitionMatrixEntry = partitionMatrixIterator.next();
            int prototypeIndex = 0;
            double sum = 0;
            for (IVector prototype : prototypes) {
                double d = distance.calculateDistance(exampleValues, prototype.getValues());
                double v;
                if (d != 0) {
                    double mf = partitionMatrixEntry[prototypeIndex];
                    objFun += Math.pow(d, 2) * mf;
                    //mf = Math.pow(mf, m);
                    v = Math.pow(d, exp);
                } else {
                    v = 1;
                }
                partitionMatrixEntry[prototypeIndex] = v;
                sum += v;
                prototypeIndex++;
            }
            for (prototypeIndex = 0; prototypeIndex < numberOfPrototypes; prototypeIndex++) {
                double v = partitionMatrixEntry[prototypeIndex];
                partitionMatrixEntry[prototypeIndex] = v / sum;
            }
        }        
        if (costFunctionValue - objFun > minGain) {
            nextItertion = true;
            costFunctionValue = objFun;
        } else {
            nextItertion = false;
        }        
    }

    /**
     * Method executed before main training used to initialize data
     * @param trainingSet
     */
    @Override
    public void initialize(ExampleSet trainingSet) {  
        int numberOfAttributes = trainingSet.getAttributes().size();
        prototypes = new ArrayList<>(numberOfPrototypes);
        for (int i = 0; i < numberOfPrototypes; i++) {
            prototypes.add(ValuesStoreFactory.createVector(new double[numberOfAttributes]));
        }  
        resetPartitionMatrix(trainingSet);
        int i;
        //partition matrix initialization				     
        for (double[] partitionMatrixEntry : partitionMatrix) {
            double sum = 0;
            for (i = 0; i < numberOfPrototypes; i++) {
                double value = randomGenerator.nextDouble();
                partitionMatrixEntry[i] = value;
                sum += value;
            }
            for (i = 0; i < numberOfPrototypes; i++) {
                double value = partitionMatrixEntry[i];
                partitionMatrixEntry[i] = value / sum;
            }
        }			
    }
            
//    /**
//     * Calculates clustering results based on partition matrix
//     *
//     * @param trainingSet - dataset to cluster
//     * @return index of assigment given example from exampleSet to cluster
//     */
//    @Override
//    public int[] getClusterAssignments(ExampleSet trainingSet) {
//        updateU(trainingSet);
//        Iterator<Example> trainingSetIterator = trainingSet.iterator();
//        Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
//        int[] results = new int[trainingSet.size()];
//        int j = 0;
//        while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
//            double[] partitionMatrixEntry = partitionMatrixIterator.next();
//            Example example = trainingSetIterator.next();
//            double best = -1;
//            for (int i = 0; i < numberOfPrototypes; i++) {
//                double curValue = partitionMatrixEntry[i];
//                if (curValue > best) {
//                    best = curValue;
//                    results[j] = i;
//                }
//            }
//            j++;
//
//        }
//        return results;
//    }
}
