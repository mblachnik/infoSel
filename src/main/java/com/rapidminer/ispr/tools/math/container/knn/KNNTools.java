package com.rapidminer.ispr.tools.math.container.knn;

import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.knn.LinearList;
import com.rapidminer.ispr.tools.math.container.knn.KDTree;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.knn.SimpleNNCachedLineraList;
import com.rapidminer.ispr.tools.math.container.knn.BallTree;
import java.util.Collection;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.dataset.VectorDense;
import com.rapidminer.ispr.dataset.Const;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.tools.Associates;
import static com.rapidminer.ispr.operator.learner.classifiers.VotingType.EXPONENTIAL;
import static com.rapidminer.ispr.operator.learner.classifiers.VotingType.GAUSSIAN;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IVector;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;

/**
 *
 * @author Marcin
 */
public class KNNTools {

  

    public static Associates findAssociatedInstances(ExampleSet exampleSet, ISPRGeometricDataCollection<IValuesStoreLabels> knn, int k) {
        int numExamples = exampleSet.size();
        Associates nearestAssociates = new Associates(numExamples, k); //store nearest associates for each vector (list of vectors for which I am the nearest neighbor)
        Attributes attributes = exampleSet.getAttributes();
        IVector instance = ValuesStoreFactory.createVector(new double[attributes.size()]);
        int i = 0;
        for (Example example : exampleSet) {
            instance.setValues(example);
            Collection<IValuesStoreLabels> nearestNeighbors = knn.getNearestValues(k, instance);
            for (IValuesStoreLabels neighbor : nearestNeighbors) {
                nearestAssociates.add((int) neighbor.getValueAsDouble(Const.INDEX_CONTAINER), i);
            }
            i++;
        }
        return nearestAssociates;
    }

    /**
     * Returns collection of nearest neighbors
     *
     * @param example - sample for which we do prediction
     * @param samples - kNN data streucture
     * @param k - number of nearest neighbors
     * @return
     */
    public static Collection<IValuesStoreLabels> returnKNearestNeighbors(Example example, ISPRGeometricDataCollection<IValuesStoreLabels> samples, int k) {
        return samples.getNearestValues(k, ValuesStoreFactory.createVector(example));
    }

    /**
     * Returns label of 1NN
     *
     * @param example data sample
     * @param colection NearestNeighbor structure
     * @return
     */
    @Deprecated
    public static double predictOneNearestNeighbor(Example example, ISPRGeometricDataCollection<IValuesStoreLabels> colection) {
        Collection<IValuesStoreLabels> resultSet = colection.getNearestValues(1, ValuesStoreFactory.createVector(example));
        return resultSet.iterator().next().getLabel();
    }

    /**
     * Returns label of 1NN
     *
     * @param values data sample
     * @param colection NearestNeighbor structure
     * @return
     */
    public static double predictOneNearestNeighbor(IVector values, ISPRGeometricDataCollection<IValuesStoreLabels> colection) {
        Collection<IValuesStoreLabels> resultSet = colection.getNearestValues(1, values);
        return resultSet.iterator().next().getLabel();
    }

    /**
     * Predict 1NN
     *
     * @param exampleSet - dataset
     * @param example - current example
     * @param distance - distance measure
     * @return the value of distance
     */
    public static double predictOneNearestNeighbor(ExampleSet exampleSet, double[] example, DistanceMeasure distance) {
        double dMin = Double.MAX_VALUE;
        double nlabel = -1;
        for (Example secondInstance : exampleSet) {
            double d = distance.calculateDistance(secondInstance, example);
            if (d < dMin) {
                dMin = d;
                nlabel = secondInstance.getLabel();
            }
        }
        return nlabel;
    }

    /**
     * Performance kNN voting of various types
     *
     * @param votes returned results (must be initialized)
     * @param values current samples
     * @param samples - kNN structure
     * @param k - number of nearest neighbors
     * @param voting - type of voting
     */
    public static void doNNVotes(double[] votes, IVector values, ISPRGeometricDataCollection<IValuesStoreLabels> samples, int k, VotingType voting) {
        Arrays.fill(votes, 0);
        double totalDistance, totalSimilarity;;
        Collection<DoubleObjectContainer<IValuesStoreLabels>> neighbours;
        switch (voting) {
            default:
            case MAJORITY:
                Collection<IValuesStoreLabels> neighbourLabels = samples.getNearestValues(k, values);
                Iterator<IValuesStoreLabels> iterator = neighbourLabels.iterator();
                while (iterator.hasNext()) {
                    IValuesStoreLabels v = iterator.next();
                    if (v != null) {
                        int idx = (int) v.getLabel();
                        votes[idx] += 1.0 / k;
                    }
                }
                return;
            case LINEAR:
            case EXPONENTIAL:
            case GAUSSIAN:
                totalDistance = 0;
                neighbours = samples.getNearestValueDistances(k, values);
                for (DoubleObjectContainer<IValuesStoreLabels> tupel : neighbours) {
                    //totalDistance += tupel.getFirst();
                    totalDistance = totalDistance < tupel.getFirst() ? tupel.getFirst() : totalDistance;
                }
                totalDistance = 2 * totalDistance;

                if (totalDistance == 0) {
                    totalDistance = 1;
                    totalSimilarity = k;
                } else {
                    totalSimilarity = Math.max(k - 1, 1);
                }

                switch (voting) {
                    default:
                    case LINEAR:
                        for (DoubleObjectContainer<IValuesStoreLabels> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            votes[idx] += (totalDistance - tupel.getFirst()) / totalDistance;
                        }

                        break;
                    case GAUSSIAN:
                        for (DoubleObjectContainer<IValuesStoreLabels> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            double res = (tupel.getFirst() / totalDistance);
                            votes[idx] += Math.exp(-res * res);
                        }
                        break;
                    case EXPONENTIAL:
                        for (DoubleObjectContainer<IValuesStoreLabels> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            double res = (tupel.getFirst() / totalDistance);
                            votes[idx] += Math.exp(-res);
                        }
                        break;
                }
        }
        double sum = 0;
        for (int i = 0; i < votes.length; i++) {
            sum = +votes[i];
        }
        for (int i = 0; i < votes.length; i++) {
            votes[i] /= sum;
        }
    }

    /**
     * Predictes for kNN for regressin problems
     *
     * @param values - current sample
     * @param samples - kNN structure
     * @param k - number of nearest neighbors
     * @param weighting - type of weighting
     * @return prediction
     */
    public static double getRegVotes(IVector values, ISPRGeometricDataCollection<IValuesStoreLabels> samples, int k, VotingType weighting) {
        double predictedValue = 0;
        switch (weighting) {
            default:
            case MAJORITY:
                // finding next k neighbours
                Collection<IValuesStoreLabels> neighbourLabels = samples.getNearestValues(k, values);
                // distance is 1 for complete neighbourhood                        
                // counting frequency of labels
                Iterator<IValuesStoreLabels> iterator = neighbourLabels.iterator();
                while (iterator.hasNext()) {
                    double nearestOutput = iterator.next().getLabel();
                    predictedValue += nearestOutput;
                }
                predictedValue /= neighbourLabels.size();
                break;
            case LINEAR:
            case GAUSSIAN:
            case EXPONENTIAL:
                //TODO: Implement other weighting types
                // finding next k neighbours and their distances
                Collection<DoubleObjectContainer<IValuesStoreLabels>> neighbours = samples.getNearestValueDistances(k, values);
                double totalSimilarity = 0.0;
                for (DoubleObjectContainer<IValuesStoreLabels> tupel : neighbours) {
                    double nearestOutput = tupel.getSecond().getLabel();
                    double distance = tupel.getFirst();
                    if (distance == 0) {
                        predictedValue = nearestOutput;
                        totalSimilarity = 1;
                        break;
                    }
                    double similarity = 1 / distance;
                    predictedValue += nearestOutput * similarity;
                    totalSimilarity += similarity;
                }
                predictedValue /= totalSimilarity;

        }
        return predictedValue;
    }

    /**
     *
     * @param example
     * @param values
     */
    public static void extractExampleValues(Example example, double[] values) {
        Attributes attributes = example.getAttributes();
        int i = 0;
        for (Attribute attribute : attributes) {
            values[i] = example.getValue(attribute);
            i++;
        }
    }
}
