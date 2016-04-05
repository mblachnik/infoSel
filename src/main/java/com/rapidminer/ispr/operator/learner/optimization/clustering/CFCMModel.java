package com.rapidminer.ispr.operator.learner.optimization.clustering;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.optimization.Prototype;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Marcin
 */
public class CFCMModel extends AbstractBatchModel {

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
    public CFCMModel(DistanceMeasure distance, double m, int maxIterations, double minGain, RandomGenerator randomGenerator, int numberOfPrototypes) {
        super(distance);
        this.m = m;
        iteration = 0;
        this.minGain = minGain;
        this.numberOfIterations = maxIterations;
        this.randomGenerator = randomGenerator;
        this.numberOfPrototypes = numberOfPrototypes;
    }

    /**
     * Method executed by the super class to check if next iteration should be performed
     * @return
     */
    @Override
    public boolean nextIteration() {
        iteration++;
        return nextItertion && (iteration < numberOfIterations);
    }

    @Override
    public void updatePrototypes(ExampleSet trainingSet) {
        int prototypeIndex = 0;
        for (Prototype prototype : prototypes) {
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

    @Override
    public void updatePartitionMatrix(ExampleSet trainingSet) {
        double objFun = 0;
        int prototypeIndex = 0;
        double exp = -2.0 / (m - 1.0);
        for (Prototype prototype : prototypes) {
            Iterator<Example> trainingSetIterator = trainingSet.iterator();
            Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
            while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
                Example example = trainingSetIterator.next();
                double[] partitionMatrixEntry = partitionMatrixIterator.next();
                double d = distance.calculateDistance(example, prototype.getValues());
                double v;
                if (d != 0) {
                    double mf = partitionMatrixEntry[prototypeIndex];
                    objFun += Math.pow(d, 2) * mf;
                    //mf = Math.pow(mf, m);
                    v = Math.pow(d, exp);
                } else {
                    v = 1;
                }
                //example.setValue(attribute, v);
                partitionMatrixEntry[prototypeIndex] = v;
            }
            prototypeIndex++;
        }
        
        Iterator<Example> trainingSetIterator = trainingSet.iterator();
        Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
        while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
            Example example = trainingSetIterator.next();
            double[] partitionMatrixEntry = partitionMatrixIterator.next();
            
            double sum = 0;
            for (prototypeIndex = 0; prototypeIndex < numberOfPrototypes; prototypeIndex++) {
                sum += partitionMatrixEntry[prototypeIndex];
            }
            double weight = example.getWeight();
            weight = Double.isNaN(weight) ? 1 : weight;
            for (prototypeIndex = 0; prototypeIndex < numberOfPrototypes; prototypeIndex++) {
                double v = partitionMatrixEntry[prototypeIndex];
                partitionMatrixEntry[prototypeIndex] = weight * v / sum;
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
     * Method executed by the superclass before the main loop starts. Used to initialize
     * partition matrix
     * @param trainingSet
     */
    @Override
    public void initialize(ExampleSet trainingSet) {
        int numberOfAttributes = trainingSet.getAttributes().size();
        prototypes = new ArrayList<>(numberOfPrototypes);
        for (int i = 0; i < numberOfPrototypes; i++) {
            prototypes.add(new Prototype(numberOfAttributes));
        } 
        resetPartitionMatrix(trainingSet);
        int i;
        //partition matrix initialization				     
        Iterator<Example> trainingSetIterator = trainingSet.iterator();
        Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
        while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
            Example example = trainingSetIterator.next();
            double[] partitionMatrixEntry = partitionMatrixIterator.next();
                        
            double sum = 0;
            for (i = 0; i < numberOfPrototypes; i++) {
                double value = randomGenerator.nextDouble();
                partitionMatrixEntry[i] = value;
                sum += value;
            }
            double weight = example.getWeight();
            weight = Double.isNaN(weight) ? 1 : weight;
            for (i = 0; i < numberOfPrototypes; i++) {
                double value = partitionMatrixEntry[i];
                value *= weight;
                partitionMatrixEntry[i] = value / sum;
            }
        }                
    }


    
//    /**
//     * This method first calculates partition matrix for given dataset, then 
//     * it return output based on the maximum value of partition matrix
//     * @param trainingSet
//     * @return 
//     */
//    @Override
//    public int[] getClusterAssignments(ExampleSet trainingSet) {
//        updateU(trainingSet);
//        Iterator<Example> trainingSetIterator = trainingSet.iterator();
//        Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
//        int[] results = new int[trainingSet.size()];
//        int j=0;
//        while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
//            double[] partitionMatrixEntry = partitionMatrixIterator.next();
//            Example example = trainingSetIterator.next();
//            double best = -1;
//            int idx = -1;
//            for (int i = 0; i < numberOfPrototypes; i++) {
//                double curValue = partitionMatrixEntry[i];
//                if (curValue > best) {
//                    best = curValue;
//                    results[j] = i;
//                }
//            }
//            j++;
//        }
//        return results;
//    }
}