package com.rapidminer.ispr.operator.learner.optimization.clustering;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Collection;
import com.rapidminer.ispr.operator.learner.optimization.Prototype;

/**
 *
 * @author Marcin
 */
public abstract class AbstractBatchModel {

    Collection<Prototype> prototypes;
    Collection<double[]> partitionMatrix;

    int numberOfPrototypes, numberOfTrainingExamples, numberOfAttributes; //Number of prototypes and number of attributes
    DistanceMeasure distance;
    double costFunctionValue = Double.MAX_VALUE;

    /**
     *
     * @param prototypes
     * @param distance
     */
    public AbstractBatchModel(DistanceMeasure distance, int numberOfPrototypes, ExampleSet trainingSet) {
        this.distance = distance;
        this.numberOfPrototypes = numberOfPrototypes;
        numberOfTrainingExamples = trainingSet.size();
        numberOfAttributes = trainingSet.getAttributes().size();
        prototypes = new ArrayList<Prototype>(numberOfTrainingExamples);
        partitionMatrix = new ArrayList<double[]>(numberOfTrainingExamples);
        for (int i = 0; i < numberOfTrainingExamples; i++) {
            partitionMatrix.add(new double[numberOfPrototypes]);
        }
        for (int i = 0; i < numberOfPrototypes; i++) {
            prototypes.add(new Prototype(numberOfAttributes));
        }
    }

    /**
     *
     * @param trainingSet
     * @return
     */
    public Collection<Prototype> train(ExampleSet trainingSet) {
        initialize(trainingSet);
        do {
            update(trainingSet);
        } while (nextIteration());
        return prototypes;
    }

    /**
     *
     * @return
     */
    protected int getNumberOfPrototypes() {
        return numberOfPrototypes;
    }

    /**
     *
     * @return
     */
    public double getCostFunctionValue() {
        return costFunctionValue;
    }

    /**
     *
     * @return
     */
    protected abstract boolean nextIteration();

    /**
     *
     * @param trainingSet
     */
    protected abstract void update(ExampleSet trainingSet);

    /**
     *
     * @param trainingSet
     */
    public abstract void initialize(ExampleSet trainingSet);

    /**
     *
     * @param trainingSet
     */
    public abstract void apply(ExampleSet trainingSet, Attribute attribute);

    public DistanceMeasure getDistance() {
        return distance;
    }

    public int getNumberOfAttributes() {
        return numberOfAttributes;
    }

    public int getNumberOfTrainingExamples() {
        return numberOfTrainingExamples;
    }

    public Collection<double[]> getPartitionMatrix() {
        return partitionMatrix;
    }

    public Collection<Prototype> getPrototypes() {
        return prototypes;
    }
}
