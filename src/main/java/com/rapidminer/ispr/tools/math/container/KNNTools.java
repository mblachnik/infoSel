package com.rapidminer.ispr.tools.math.container;

import java.util.Collection;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.ispr.dataset.InstanceGenerator;
import com.rapidminer.ispr.dataset.SimpleInstance;
import com.rapidminer.ispr.dataset.StoredValuesHelper;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.tools.Associates;
import static com.rapidminer.ispr.operator.learner.classifiers.VotingType.EXPONENTIAL;
import static com.rapidminer.ispr.operator.learner.classifiers.VotingType.GAUSSIAN;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public class KNNTools {

    public static ISPRGeometricDataCollection<IStoredValues> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, DistanceMeasure measure) {
        Map<Attribute, String> storedAttributes = new HashMap<>();
        storedAttributes.put(exampleSet.getAttributes().getLabel(), StoredValuesHelper.LABEL);
        storedAttributes.put(exampleSet.getAttributes().getId(), StoredValuesHelper.ID);
        storedAttributes.put(exampleSet.getAttributes().getCluster(), StoredValuesHelper.CLUSTER);
        storedAttributes.put(exampleSet.getAttributes().getWeight(), StoredValuesHelper.WEIGHT);
        return initializeKNearestNeighbourFactory(type, exampleSet, storedAttributes, measure);
    }

    /**
     * Returns nearest neighbor data structure
     *
     * @param type - type of the structure
     * @param exampleSet - dataset
     * @param storedAttributes - which attributes to store
     * @param measure - distance measure
     * @return
     */
    public static ISPRGeometricDataCollection<IStoredValues> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, Map<Attribute, String> storedAttributes, DistanceMeasure measure) {
        ISPRGeometricDataCollection samples = null;
        switch (type) {
            case LINEAR_SEARCH:
                samples = new LinearList(exampleSet, storedAttributes, measure);
                break;
            case CACHED_LINEAR_SEARCH:
                samples = new SimpleNNCachedLineraList(exampleSet, storedAttributes, measure);
                break;
            case BALL_TREE_SEARCH:
                samples = new BallTree(exampleSet, storedAttributes, measure);
                break;
            case KD_TREE_SEARCH:
                samples = new KDTree(exampleSet, storedAttributes, measure);
                break;
        }
        return samples;
    }

    /**
     * Returns nearest neighbor data structure
     *
     * @param type - type of the structure
     * @param exampleSet - dataset
     * @param attribute - attribute to store
     * @param storedValueName - name in the store
     * @param measure - distance measure
     * @return
     */
    public static ISPRGeometricDataCollection<IStoredValues> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, Attribute attribute, String storedValueName, DistanceMeasure measure) {
        Map<Attribute, String> map = new HashMap<>();
        map.put(attribute, storedValueName);
        return initializeKNearestNeighbourFactory(type, exampleSet, map, measure);
    }

    public static Associates findAssociatedInstances(ExampleSet exampleSet, ISPRGeometricDataCollection<IStoredValues> knn, int k) {
        int numExamples = exampleSet.size();
        Associates nearestAssociates = new Associates(numExamples, k); //store nearest associates for each vector (list of vectors for which I am the nearest neighbor)
        Attributes attributes = exampleSet.getAttributes();
        Instance instance = InstanceGenerator.generateInstance(new double[attributes.size()]);
        int i = 0;
        for (Example example : exampleSet) {
            instance.setValues(example);
            Collection<IStoredValues> nearestNeighbors = knn.getNearestValues(k, instance);
            for (IStoredValues neighbor : nearestNeighbors) {
                nearestAssociates.add((int) neighbor.getValue(StoredValuesHelper.INDEX), i);
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
    public static Collection<IStoredValues> returnKNearestNeighbors(Example example, ISPRGeometricDataCollection<IStoredValues> samples, int k) {
        return samples.getNearestValues(k, InstanceGenerator.generateInstance(example));
    }

    /**
     * Returns label of 1NN
     *
     * @param example data sample
     * @param colection NearestNeighbor structure
     * @return
     */
    @Deprecated
    public static double predictOneNearestNeighbor(Example example, ISPRGeometricDataCollection<IStoredValues> colection) {
        Collection<IStoredValues> resultSet = colection.getNearestValues(1, InstanceGenerator.generateInstance(example));
        return resultSet.iterator().next().getLabel();
    }

    /**
     * Returns label of 1NN
     *
     * @param values data sample
     * @param colection NearestNeighbor structure
     * @return
     */
    public static double predictOneNearestNeighbor(Instance values, ISPRGeometricDataCollection<IStoredValues> colection) {
        Collection<IStoredValues> resultSet = colection.getNearestValues(1, values);
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
    public static void doNNVotes(double[] votes, Instance values, ISPRGeometricDataCollection<IStoredValues> samples, int k, VotingType voting) {
        Arrays.fill(votes, 0);
        double totalDistance, totalSimilarity;;
        Collection<DoubleObjectContainer<IStoredValues>> neighbours;
        switch (voting) {
            default:
            case MAJORITY:
                Collection<IStoredValues> neighbourLabels = samples.getNearestValues(k, values);
                Iterator<IStoredValues> iterator = neighbourLabels.iterator();
                while (iterator.hasNext()) {
                    IStoredValues v = iterator.next();
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
                for (DoubleObjectContainer<IStoredValues> tupel : neighbours) {
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
                        for (DoubleObjectContainer<IStoredValues> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            votes[idx] += (totalDistance - tupel.getFirst()) / totalDistance;
                        }

                        break;
                    case GAUSSIAN:
                        for (DoubleObjectContainer<IStoredValues> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            double res = (tupel.getFirst() / totalDistance);
                            votes[idx] += Math.exp(-res * res);
                        }
                        break;
                    case EXPONENTIAL:
                        for (DoubleObjectContainer<IStoredValues> tupel : neighbours) {
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
    public static double getRegVotes(Instance values, ISPRGeometricDataCollection<IStoredValues> samples, int k, VotingType weighting) {
        double predictedValue = 0;
        switch (weighting) {
            default:
            case MAJORITY:
                // finding next k neighbours
                Collection<IStoredValues> neighbourLabels = samples.getNearestValues(k, values);
                // distance is 1 for complete neighbourhood                        
                // counting frequency of labels
                Iterator<IStoredValues> iterator = neighbourLabels.iterator();
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
                Collection<DoubleObjectContainer<IStoredValues>> neighbours = samples.getNearestValueDistances(k, values);
                double totalSimilarity = 0.0;
                for (DoubleObjectContainer<IStoredValues> tupel : neighbours) {
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
