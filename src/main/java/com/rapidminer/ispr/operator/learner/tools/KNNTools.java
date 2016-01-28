package com.rapidminer.ispr.operator.learner.tools;


import java.util.Collection;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import static com.rapidminer.ispr.operator.learner.classifiers.VotingType.EXPONENTIAL;
import static com.rapidminer.ispr.operator.learner.classifiers.VotingType.GAUSSIAN;
import com.rapidminer.ispr.tools.math.container.BallTree;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.IntDoubleContainer;
import com.rapidminer.ispr.tools.math.container.KDTree;
import com.rapidminer.ispr.tools.math.container.MyLinearList;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.ispr.tools.math.container.SimpleNNCachedLineraList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

/**
 *
 * @author Marcin
 */
public class KNNTools {

    public static ISPRGeometricDataCollection<Number> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, DistanceMeasure measure) {
        ISPRGeometricDataCollection<Number> samples = null;
        switch (type) {
            case LINEAR_SEARCH:            
                samples = new MyLinearList<Number>(measure, exampleSet.size());
                break;
            case CACHED_LINEAR_SEARCH:
                samples = new SimpleNNCachedLineraList<Number>(measure, exampleSet.size());
                break;
            case BALL_TREE_SEARCH:
                samples = new BallTree<Number>(measure);
                break;
            case KD_TREE_SEARCH:
                samples = new KDTree<Number>(measure, exampleSet.getAttributes().size());
                break;
        }
        Attributes attributes = exampleSet.getAttributes();

        int valuesSize = attributes.size();
        for (Example example : exampleSet) {
            double[] values = new double[valuesSize];
            int i = 0;
            for (Attribute attribute : attributes) {
                values[i] = example.getValue(attribute);
                i++;
            }
            Number labelValue = example.getLabel();

            samples.add(values, labelValue);
        }
        return samples;
    }

    /**
     *
     * @param exampleSet
     * @param measure
     * @return
     */
    public static ISPRGeometricDataCollection<IntDoubleContainer> initializeGeneralizedKNearestNeighbour(ExampleSet exampleSet, DistanceMeasure measure) {
        ISPRGeometricDataCollection<IntDoubleContainer> samples = new MyLinearList<IntDoubleContainer>(
                measure, exampleSet.size());

        Attributes attributes = exampleSet.getAttributes();

        int valuesSize = attributes.size();
        int j = 0;
        for (Example example : exampleSet) {
            double[] values = new double[valuesSize];
            int i = 0;
            for (Attribute attribute : attributes) {
                values[i] = example.getValue(attribute);
                i++;
            }
            double labelValue = example.getLabel();
            samples.add(values, new IntDoubleContainer(j, labelValue));
            j++;
        }
        return samples;
    }

    public static Associates findAssociatedInstances(ExampleSet exampleSet, ISPRGeometricDataCollection<IntDoubleContainer> knn, int k) {
        int numExamples = exampleSet.size();
        Associates nearestAssociates = new Associates(numExamples, k); //store nearest associates for each vector (list of vectors for which I am the nearest neighbor)
        Attributes attributes = exampleSet.getAttributes();
        double[] values = new double[attributes.size()];
        int i = 0;
        for (Example example : exampleSet) {
            int j = 0;
            for (Attribute a : attributes) {
                values[j] = example.getValue((a));
                j++;
            }
            Collection<IntDoubleContainer> nearestNeighbors = knn.getNearestValues(k, values);
            for (IntDoubleContainer neighbor : nearestNeighbors) {
                nearestAssociates.add(neighbor.getFirst(), i);
            }
            i++;
        }
        return nearestAssociates;
    }

    /**
     *
     * @param example
     * @param samples
     * @param k
     * @return
     */
    public static Collection<IntDoubleContainer> returnKNearestNeighbors(Example example, ISPRGeometricDataCollection<IntDoubleContainer> samples, int k) {
        Attributes aa = example.getAttributes();
        int i = 0;
        double[] values = new double[aa.size()];
        for (Attribute a : aa) {
            values[i] = example.getValue(a);
            i++;
        }
        return samples.getNearestValues(k, values);
    }

    /**
     *
     * @param exampleSet
     * @param example
     * @param distance
     * @return
     */
    public static double predictOneNearestNeighbor(Example example, ISPRGeometricDataCollection<Number> colection) {
        int i = 0;
        Attributes attributes = example.getAttributes();
        double[] vector = new double[attributes.size()];
        for (Attribute a : attributes) {
            vector[i] = example.getValue(a);
            i++;
        }
        Collection<Number> resultSet = colection.getNearestValues(1, vector);
        return resultSet.iterator().next().doubleValue();
    }

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

    public static void doNNVotes(double[] votes, double[] values, ISPRGeometricDataCollection<Number> samples, int k, VotingType voting) {
        Arrays.fill(votes, 0);
        double totalDistance, totalSimilarity;;
        Collection<DoubleObjectContainer<Number>> neighbours;
        switch (voting) {
            default:
            case MAJORITY:
                Collection<Number> neighbourLabels = samples.getNearestValues(k, values);
                Iterator<Number> iterator = neighbourLabels.iterator();
                while (iterator.hasNext()) {
                    int idx = iterator.next().intValue();
                    votes[idx] += 1.0 / k;
                }
                return;
            case LINEAR:
            case EXPONENTIAL:
            case GAUSSIAN:
                totalDistance = 0;
                neighbours = samples.getNearestValueDistances(k, values);
                for (DoubleObjectContainer<Number> tupel : neighbours) {
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
                        for (DoubleObjectContainer<Number> tupel : neighbours) {
                            int idx = tupel.getSecond().intValue();
                            votes[idx] += (totalDistance - tupel.getFirst()) / totalDistance;
                        }

                        break;
                    case GAUSSIAN:
                        for (DoubleObjectContainer<Number> tupel : neighbours) {                            
                            int idx = tupel.getSecond().intValue();
                            double res = (tupel.getFirst() / totalDistance);
                            votes[idx] += Math.exp(- res * res);
                        }
                        break;
                    case EXPONENTIAL:
                        for (DoubleObjectContainer<Number> tupel : neighbours) {
                            int idx = tupel.getSecond().intValue();
                            double res = (tupel.getFirst() / totalDistance);
                            votes[idx] += Math.exp(- res);
                        }
                        break;
                }
        }
        for (int i = 0; i > votes.length; i++) {
            votes[i] /= totalSimilarity;
        }
    }

    public static double getRegVotes(double[] values, ISPRGeometricDataCollection<Number> samples, int k, VotingType weighting) {
        double predictedValue = 0;
        switch (weighting) {
            default:
            case MAJORITY:
                // finding next k neighbours
                Collection<Number> neighbourLabels = samples.getNearestValues(k, values);
                // distance is 1 for complete neighbourhood                        
                // counting frequency of labels
                Iterator<Number> iterator = neighbourLabels.iterator();
                while (iterator.hasNext()) {
                    double nearestOutput = iterator.next().doubleValue();
                    predictedValue += nearestOutput;
                }
                predictedValue /= k;
                break;
            case LINEAR:
                // finding next k neighbours and their distances
                Collection<DoubleObjectContainer<Number>> neighbours = samples.getNearestValueDistances(k, values);
                double totalSimilarity = 0.0;
                for (DoubleObjectContainer<Number> tupel : neighbours) {
                    double nearestOutput = tupel.getSecond().doubleValue();
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
            case GAUSSIAN:
            case EXPONENTIAL:
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
