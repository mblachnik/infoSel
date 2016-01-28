package com.rapidminer.ispr.operator.learner.optimization.clustering;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.optimization.Prototype;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class FCMModel extends AbstractBatchModel {

    boolean nextItertion = false;
    double m;
    double minGain;
    int iteration, numberOfIterations;
    RandomGenerator randomGenerator;

    /**
     *
     * @param prototypes
     * @param distance
     * @param u
     * @param m
     * @param maxIterations
     * @param minGain
     * @param randomGenerator
     */
    public FCMModel(DistanceMeasure distance, double m, int maxIterations, double minGain, RandomGenerator randomGenerator, int c, ExampleSet trainingSet) {
        super(distance, c, trainingSet);
        this.m = m;
        iteration = 0;
        this.minGain = minGain;
        this.numberOfIterations = maxIterations;
        this.randomGenerator = randomGenerator;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean nextIteration() {
        iteration++;
        return nextItertion && (iteration < numberOfIterations);
    }

    private void updatePrototypes(ExampleSet trainingSet) {
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

    private double updateU(ExampleSet trainingSet) {
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
            for (Prototype prototype : prototypes) {
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
        return objFun;
    }

    /**
     *
     * @param trainingSet
     */
    @Override
    protected void update(ExampleSet trainingSet) {
        double objFun = updateU(trainingSet);
        updatePrototypes(trainingSet);

        if (costFunctionValue - objFun > minGain) {
            nextItertion = true;
            costFunctionValue = objFun;
        } else {
            nextItertion = false;
        }
    }

    /**
     *
     * @param trainingSet
     */
    @Override
    public void initialize(ExampleSet trainingSet) {
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
        updatePrototypes(trainingSet);
        //costFunctionValue = updateU(trainingSet);				
    }

    /**
     *
     * @param trainingSet
     */
    @Override
    public void apply(ExampleSet trainingSet, Attribute clusterAttribute) {
        Iterator<Example> trainingSetIterator = trainingSet.iterator();
        Iterator<double[]> partitionMatrixIterator = partitionMatrix.iterator();
        while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
            double[] partitionMatrixEntry = partitionMatrixIterator.next();
            Example example = trainingSetIterator.next();
            double best = -1;
            int idx = -1;
            for (int i = 0; i < numberOfPrototypes; i++) {
                double curValue = partitionMatrixEntry[i];
                if (curValue > best) {
                    best = curValue;
                    idx = i;
                }
            }
            example.setValue(clusterAttribute, idx);
        }
    }
}