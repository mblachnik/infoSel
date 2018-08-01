package org.prules.tools.math.container.knn;

import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.LinearList;
import org.prules.tools.math.container.knn.KDTree;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.SimpleNNCachedLineraList;
import org.prules.tools.math.container.knn.BallTree;
import java.util.Collection;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.VectorDense;
import org.prules.dataset.Const;
import org.prules.operator.learner.classifiers.VotingType;
import org.prules.operator.learner.tools.Associates;
import static org.prules.operator.learner.classifiers.VotingType.EXPONENTIAL;
import static org.prules.operator.learner.classifiers.VotingType.GAUSSIAN;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;

/**
 *
 * @author Marcin
 */
public class KNNTools {
    public static long t[] = new long[5];
  

    /**
     * Returns collection of nearest neighbors
     *
     * @param example - sample for which we do prediction
     * @param samples - kNN data streucture
     * @param k - number of nearest neighbors
     * @return
     */
    public static Collection<IInstanceLabels> returnKNearestNeighbors(Example example, ISPRGeometricDataCollection<IInstanceLabels> samples, int k) {
        return samples.getNearestValues(k, InstanceFactory.createVector(example));
    }

    /**
     * Returns label of 1NN
     *
     * @param example data sample
     * @param colection NearestNeighbor structure
     * @return
     */
    @Deprecated
    public static double predictOneNearestNeighbor(Example example, ISPRGeometricDataCollection<IInstanceLabels> colection) {
        Collection<IInstanceLabels> resultSet = colection.getNearestValues(1, InstanceFactory.createVector(example));
        return resultSet.iterator().next().getLabel();
    }

    public static double predictOneNearestNeighbor(Vector instance, List<Instance>  instances, DistanceMeasure distance){
        double minDist = Double.MAX_VALUE;
        double out = Double.NaN;
        
        for(Instance i : instances){
            double d = distance.calculateDistance(instance.getValues(), i.getVector().getValues());
            if (d<minDist){
                minDist = d;
                out = i.getLabels().getLabel();
            }
        }
        return out;
    }
    
    public static double predictOneNearestNeighbor(Vector instance, List<Vector>  samples, List<IInstanceLabels> labels, DistanceMeasure distance, IDataIndex index){
        double minDist = Double.MAX_VALUE;        
        IInstanceLabels bestLabel = null;
        //long t1[] = new long[6];
        for(Integer id : index){
          //  t1[0] = System.currentTimeMillis();
            Vector i = samples.get(id);
            //  t1[1] = System.currentTimeMillis();
            double[] val1 = instance.getValues();
            //  t1[2] = System.currentTimeMillis();
            double[] val2 = i.getValues();
            //  t1[3] = System.currentTimeMillis();
            double d = distance.calculateDistance(val1, val2);
            //  t1[4] = System.currentTimeMillis();
            if (d<minDist){
                minDist = d;
                bestLabel = labels.get(id);                
            }
            //  t1[5] = System.currentTimeMillis();
            //  t[0] += t1[1]-t1[0];
            //  t[1] += t1[2]-t1[1];
            //  t[2] += t1[3]-t1[2];
            //  t[3] += t1[4]-t1[3];
            //  t[4] += t1[5]-t1[4];
        }        
        double out = bestLabel != null ? bestLabel.getLabel() : Double.NaN;
        return out;
    }
    /**
     * Returns label of 1NN
     *
     * @param values data sample
     * @param colection NearestNeighbor structure
     * @return
     */
    public static double predictOneNearestNeighbor(Vector values, ISPRGeometricDataCollection<IInstanceLabels> colection) {
        Collection<IInstanceLabels> resultSet = colection.getNearestValues(1, values);
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
     * Performance kNN voting using different voting scheme such as majority, weighted voting with linear or Gaussian weights see {@see VotingType}
     *
     * @param votes returned results (must be initialized)
     * @param values current samples
     * @param samples - kNN structure
     * @param k - number of nearest neighbors
     * @param voting - type of voting
     */
    public static void doNNVotes(double[] votes, Vector values, ISPRGeometricDataCollection<IInstanceLabels> samples, int k, VotingType voting) {
        Arrays.fill(votes, 0);
        double totalDistance, totalSimilarity;;
        Collection<DoubleObjectContainer<IInstanceLabels>> neighbours;
        switch (voting) {
            default:
            case MAJORITY:
                Collection<IInstanceLabels> neighbourLabels = samples.getNearestValues(k, values);
                Iterator<IInstanceLabels> iterator = neighbourLabels.iterator();
                while (iterator.hasNext()) {
                    IInstanceLabels v = iterator.next();
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
                for (DoubleObjectContainer<IInstanceLabels> tupel : neighbours) {
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
                        for (DoubleObjectContainer<IInstanceLabels> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            votes[idx] += (totalDistance - tupel.getFirst()) / totalDistance;
                        }

                        break;
                    case GAUSSIAN:
                        for (DoubleObjectContainer<IInstanceLabels> tupel : neighbours) {
                            int idx = (int) tupel.getSecond().getLabel();
                            double res = (tupel.getFirst() / totalDistance);
                            votes[idx] += Math.exp(-res * res);
                        }
                        break;
                    case EXPONENTIAL:
                        for (DoubleObjectContainer<IInstanceLabels> tupel : neighbours) {
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
    public static double getRegVotes(Vector values, ISPRGeometricDataCollection<IInstanceLabels> samples, int k, VotingType weighting) {
        double predictedValue = 0;
        switch (weighting) {
            default:
            case MAJORITY:
                // finding next k neighbours
                Collection<IInstanceLabels> neighbourLabels = samples.getNearestValues(k, values);
                // distance is 1 for complete neighbourhood                        
                // counting frequency of labels
                Iterator<IInstanceLabels> iterator = neighbourLabels.iterator();
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
                Collection<DoubleObjectContainer<IInstanceLabels>> neighbours = samples.getNearestValueDistances(k, values);
                double totalSimilarity = 0.0;
                for (DoubleObjectContainer<IInstanceLabels> tupel : neighbours) {
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
    
     /**
     * Performs voting and returns the most frequent value
     * @param res a collection of {@see IInstanceLabels}
     * @return 
     */
    public static double getMostFrequentValue(Collection<IInstanceLabels> res){
        Map<Double,Integer> frequencyMap = new HashMap<>();
        for(IInstanceLabels lab : res){
            double label = lab.getLabel();
            if (frequencyMap.containsKey(label)){
                frequencyMap.put(label,frequencyMap.get(label)+1);
            } else {
                frequencyMap.put(label,1);
            }
        }
        int maxFrequency = -1;
        double outLabel = -1;
        for(Map.Entry<Double,Integer> e : frequencyMap.entrySet()){
            double label = e.getKey();
            int frequency = e.getValue();
            if (frequency > maxFrequency){
                maxFrequency = frequency;
                outLabel = label;
            }
        }
        return outLabel;
    }
    
    /**
     * Performs voting and returns the most frequent value
     * @param res a collection of {@see DoubleObjectContainer} which is returned by methods which also returns distances 
     * In the case of voting only the second parameter is important, that is {@see IInstanceLabels}
     * @return 
     */
    public static double getMostFrequentValueWithDistance(Collection<DoubleObjectContainer<IInstanceLabels>> res){
        Map<Double,Integer> frequencyMap = new HashMap<>();
        for(DoubleObjectContainer<IInstanceLabels> it : res){
            IInstanceLabels lab = it.getSecond();
            double label = lab.getLabel();
            if (frequencyMap.containsKey(label)){
                frequencyMap.put(label,frequencyMap.get(label)+1);
            } else {
                frequencyMap.put(label,1);
            }
        }
        int maxFrequency = -1;
        double outLabel = -1;
        for(Map.Entry<Double,Integer> e : frequencyMap.entrySet()){
            double label = e.getKey();
            int frequency = e.getValue();
            if (frequency > maxFrequency){
                maxFrequency = frequency;
                outLabel = label;
            }
        }
        return outLabel;
    }

    /**
     * Returns index of the value with the highest value
     * @param counter
     * @return
     */
    public static int getMostFrequentValue(double[] counter) {
        int mostFrequentIndex = Integer.MIN_VALUE;
        double mostFrequentFrequency = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < counter.length; j++) {
            if (mostFrequentFrequency < counter[j]) {
                mostFrequentFrequency = counter[j];
                mostFrequentIndex = j;
            }
        }
        return mostFrequentIndex;
    }
    
    /**
     * Returns index of the value with the highest value
     * @param <T>
     * @param counter
     * @return
     */
    public static <T> T getMostFrequentValue(Map<T,Double> counter) {
        T mostFrequentIndex = null;
        double mostFrequentFrequency = Double.NEGATIVE_INFINITY;
        for (Entry<T,Double> e : counter.entrySet()) {
            double value = e.getValue();
            if (mostFrequentFrequency < value) {
                mostFrequentFrequency = value;
                mostFrequentIndex = e.getKey();
            }
        }
        return mostFrequentIndex;
    }

    /**
     * Returns index of the value with the highest value
     * @param counter
     * @return
     */
    public static int getMostFrequentValue(int[] counter) {
        int mostFrequentIndex = Integer.MIN_VALUE;
        double mostFrequentFrequency = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < counter.length; j++) {
            if (mostFrequentFrequency < counter[j]) {
                mostFrequentFrequency = counter[j];
                mostFrequentIndex = j;
            }
        }
        return mostFrequentIndex;
    }
    
     /**
     * Returns nearest neighbor data structure
     *
     * @param samples
     * @param index
     * @return
     */
    public static ISPRClassGeometricDataCollection<IInstanceLabels> takeSelected(ISPRClassGeometricDataCollection<IInstanceLabels> samples, IDataIndex index) {
        ISPRClassGeometricDataCollection<IInstanceLabels> samplesNew;
        samplesNew = new LinearList<>(samples.getMeasure(), index.getLength());
        int j = 0;
        for (int i : index) {
            Vector values = samples.getSample(i);
            IInstanceLabels storeValue = samples.getStoredValue(i);
            samplesNew.add(values, storeValue);
        }
        return samplesNew;
    }

    /**
     * Returns nearest neighbor data structure
     *
     * @param samples
     * @param index
     * @return
     */
    public static ISPRGeometricDataCollection<IInstanceLabels> takeSelected(ISPRGeometricDataCollection<IInstanceLabels> samples, IDataIndex index) {
        ISPRClassGeometricDataCollection<IInstanceLabels> samplesNew;
        samplesNew = new LinearList<>(samples.getMeasure(), index.getLength());
        int j = 0;
        for (int i : index) {
            Vector values = samples.getSample(i);
            IInstanceLabels storeValue = samples.getStoredValue(i);
            samplesNew.add(values, storeValue);
        }
        return samplesNew;
    }
}
