package com.rapidminer.ispr.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Marcin
 */
public abstract class AbstractBatchModel {

    List<Instance> prototypes;
    DistanceMeasure distance;
    double costFunctionValue = Double.MAX_VALUE;
    Collection<double[]> partitionMatrix;

    /**
     * Constructor of any batch based data processing (clusterubg) model
     *
     * @param numberOfPrototypes - number of prototypes => number of clusters
     * @param distance - distanceMeasure
     */
    public AbstractBatchModel(DistanceMeasure distance) {
        this.distance = distance;
    }

    /**
     * Method used to execute the training process. It initialize the model, and
     * then starts a loop. In the loop first updatePrototypes method is
     * executed, then updatePartitionMatrix is executed. The loop ends when
     * nextIteration returns false. After that finalizeTraingin method is
     * executed;
     *
     * @param trainingSet
     * @return
     */
    public Collection<Instance> train(ExampleSet trainingSet) {
        int numberOfAttributes = trainingSet.getAttributes().size();
        int numberOfTrainingExamples = trainingSet.size();
        initialize(trainingSet);
        do {
            updatePrototypes(trainingSet);
            updatePartitionMatrix(trainingSet);
        } while (nextIteration());
        finalizeTraining();
        return prototypes;
    }

    /**
     * This method resets or creates new partition matrix. It checks if partition matrix is 
     * already allocated and size of partition matrix is appropriate, if not then 
     * new partition matrix is allocated.      
     *
     * @param trainingSet
     */
    public void resetPartitionMatrix(ExampleSet trainingSet) {
        int numberOfTrainingExamples = trainingSet.size();
        if (partitionMatrix == null){
            partitionMatrix = new ArrayList<>(numberOfTrainingExamples);
        }
        if (partitionMatrix.size() != trainingSet.size()){
            for (int i = 0; i < numberOfTrainingExamples; i++) {
                double[] row = new double[prototypes.size()];
                partitionMatrix.add(row);
            }
        }
    }

    /**
     * Returns number of cluster centers
     *
     * @return
     */
    public int getNumberOfPrototypes() {
        return prototypes.size();
    }

    /**
     * Returns the value of cost function after training. Otherwise it returns
     * NaN
     *
     * @return
     */
    public double getCostFunctionValue() {
        return costFunctionValue;
    }

    /**
     * Method executed by the train method to check if next iteration of the
     * main algorithm loop should be performed
     *
     * @return
     */
    public abstract boolean nextIteration();

    /**
     * Method executed by the train method inside the main loop. It is
     * responsible for updating prototypes position (determines cluster centers
     * position)
     *
     * @param trainingSet
     */
    public abstract void updatePrototypes(ExampleSet trainingSet);

    /**
     * Method executed by the train method inside the main loop. It is
     * responsible for updating prototypes position (determines cluster centers
     * position)
     *
     * @param trainingSet
     */
    public abstract void updatePartitionMatrix(ExampleSet trainingSet);

    /**
     * Method executed by the train method before the main loop
     *
     * @param trainingSet
     */
    public abstract void initialize(ExampleSet trainingSet);

    /**
     * Method run to finalize training by the train method. By default it is
     * used to reset partition matrix
     */
    public void finalizeTraining() {
    }

    ;    

    /**
     * Returns distance measure used in the calculations
     *
     * @return
     */
    public DistanceMeasure getDistance() {
        return distance;
    }

    /**
     * Returns cluster centers
     *
     * @return
     */
    public Collection<Instance> getPrototypes() {
        return prototypes;
    }

    /**
     * Returns partition matrix
     *
     * @return
     */
    public Collection<double[]> getPartitionMatrix() {
        return partitionMatrix;
    }

    /**
     * Method called to clean partition matrix and set it to null It may be
     * useful when processing large dataset with many cluster centers, than the
     * partition matrix may be very memory consuming (n x m where n-number of
     * training examples, m-number of clusters) So when not needed the partition
     * matrix should be cleaned.
     */
    public void cleanPartitionMatrix() {
        partitionMatrix = null;
    }
}
