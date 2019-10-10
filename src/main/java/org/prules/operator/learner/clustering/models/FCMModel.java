package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Vector;
import org.prules.tools.math.container.knn.KNNTools;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Marcin
 */
public class FCMModel extends AbstractBatchModel {

    private boolean nextIteration = false;
    private double m;
    private double minGain;
    private int iteration, numberOfIterations, numberOfPrototypes;
    private RandomGenerator randomGenerator;

    /**
     * Constructor of the model
     *
     * @param distance           - distance measure
     * @param m                  - fuzziness parameter
     * @param maxIterations      - number of iterations
     * @param minGain            - minimal gain required to perform new iteration
     * @param randomGenerator    - random number generators
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
     *
     * @return
     */
    @Override
    public boolean nextIteration() {
        iteration++;
        return nextIteration && (iteration < numberOfIterations);
    }

    /**
     * Update prototype posiotion based on partition matrix
     *
     * @param trainingSet
     */
    @Override
    public void updatePrototypes(ExampleSet trainingSet) {
        int prototypeIndex = 0;
        for (Vector prototype : prototypes) {
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
     *
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
            for (Vector prototype : prototypes) {
                double d = distance.calculateDistance(exampleValues, prototype.getValues());
                double mf = partitionMatrixEntry[prototypeIndex];
                objFun += Math.pow(d, 2) * mf; //It is only here because in else section the distance is 0 so 0^2*mf = 0
                //mf = Math.pow(mf, m);
                double v = d == 0 ? Double.MAX_VALUE : Math.pow(d, exp);
                sum += v;
                partitionMatrixEntry[prototypeIndex] = v;
                prototypeIndex++;
            }
            for (prototypeIndex = 0; prototypeIndex < numberOfPrototypes; prototypeIndex++) {
                double v = partitionMatrixEntry[prototypeIndex];
                partitionMatrixEntry[prototypeIndex] = v / sum;
            }
        }
        costFunctionValue.add(objFun);
        int size = costFunctionValue.size();
        double gain = costFunctionValue.get(size - 1) - costFunctionValue.get(size - 2);
        nextIteration = !(Math.abs(gain) < minGain);
    }

    /**
     * Method executed before main training used to initialize data
     *
     * @param trainingSet
     */
    @Override
    public void initialize(ExampleSet trainingSet) {
        int numberOfAttributes = trainingSet.getAttributes().size();
        prototypes = new ArrayList<>(numberOfPrototypes);
        for (int i = 0; i < numberOfPrototypes; i++) {
            prototypes.add(InstanceFactory.createVector(new double[numberOfAttributes]));
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

    @Override
    public void finalizeTraining() {
    }

//    /**
//     * Calculates clustering results based on partition matrix
//     *
//     * @param trainingSet - data set to cluster
//     * @return index of assignment given example from exampleSet to cluster
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
