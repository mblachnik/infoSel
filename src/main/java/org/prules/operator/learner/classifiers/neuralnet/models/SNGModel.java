package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class implements supervised neural gas algorithm
 *
 * @author Marcin
 */
public class SNGModel extends AbstractLVQModel {

    private DistanceMeasure measure;
    private int currentIteration, iterations;
    private double alpha, lambda;
    private double initialAlpha, initialLambda;
    private Pair[] distanceTable;
    private double minimumAcceptabelRate = 1e-6;
    private final List<Double> learningRateValues;
    private final List<Double> lambdaRateValues;
    
    //private double minLearningRate;

    /**
     *
     * @param prototypes
     * @param maxIterations
     * @param measure
     * @param alpha
     * @param lambda
     * @throws OperatorException
     */
    public SNGModel(ExampleSet prototypes, int maxIterations, DistanceMeasure measure, double alpha, double lambda) throws OperatorException {
        super(prototypes);
        this.iterations = maxIterations;
        this.currentIteration = 0;
        this.alpha = alpha;
        this.initialAlpha = alpha;
        this.measure = measure;
        this.lambda = lambda;
        this.initialLambda = lambda;
        this.measure.init(prototypes);
        this.distanceTable = new Pair[prototypes.size()];
        for(int i=0; i<prototypes.size(); i++ ){
            distanceTable[i] = new Pair();
        }
        learningRateValues = new ArrayList<>(maxIterations);
        lambdaRateValues = new ArrayList<>(maxIterations);
        addStoredValue(LEARNING_RATE_KEY, learningRateValues);
        addStoredValue(LAMBDA_RATE_KEY, lambdaRateValues);
    }

    /**
     * Update codebooks weights
     */
    @Override
    public void update(double[][] prototypeValues, double[] prototypeLabels, double[] exampleValues, double exampleLabel, Example example) {

        double dist, minDist = Double.MAX_VALUE;

        
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            distanceTable[i].key = i;
            distanceTable[i].value = dist;
            if (dist < minDist) {
                minDist = dist;
            }
            i++;
        }

        //posortowanie neuronów w kolejności rosnącej odległości
        Arrays.sort(distanceTable);

        int j = 0;
        double g = 0;

        //obliczenie odległości między wektorem zwycięzcą a pozostałymi oraz adaptacja wag zgodnie z regułą WTM
        for (Pair prototypePair : distanceTable) {

            g = Math.exp(-1 * ((j + 1) / lambda));
            double rate = alpha * g;
            //if the rate is too small break the iteration
            if (rate < minimumAcceptabelRate) { 
                break;
            }
            if (prototypeLabels[prototypePair.key] == exampleLabel || (Double.isNaN(prototypeLabels[prototypePair.key]))) {
                for (i = 0; i < getAttributesSize(); i++) {
                    double value = prototypeValues[prototypePair.key][i];
                    value += rate * (exampleValues[i] - value);
                    prototypeValues[prototypePair.key][i] = value;
                }
            } else {
                for (i = 0; i < getAttributesSize(); i++) {
                    double value = prototypeValues[prototypePair.key][i];
                    value -= rate * (exampleValues[i] - value);
                    prototypeValues[prototypePair.key][i] = value;
                }
            }
            j++;

        }

    }

    /**
     * If true then next iteration will be performed
     *
     * @return
     */
    @Override
    public boolean isNextIteration(ExampleSet trainingSet) {
        currentIteration++;
        learningRateValues.add(alpha);
        lambdaRateValues.add(lambda);
        alpha  = LVQTools.learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);
        lambda = LVQTools.lambdaRateUpdateRule(lambda, currentIteration, iterations, initialLambda);
        return currentIteration < iterations;
    }

    /**
     * Returns total number of iterations (maximum number of iterations)
     *
     * @return
     */
    @Override
    public int getMaxIterations() {
        return iterations;
    }

    /**
     * Returns current iteration
     *
     * @return
     */
    @Override
    public int getIteration() {
        return currentIteration;
    }

    /**
     * Returns the value of cost function
     *
     * @return
     */
    @Override
    public double getCostFunctionValue() {
        return Double.NaN;
    }

    /**
     * Returns list of cost function values
     *
     * @return
     */
    @Override
    public List<Double> getCostFunctionValues() {
        return new ArrayList<>(0);
    }
    
    
    private class Pair implements Comparable<Pair> {

        public int key;
        public double value;

        public Pair(int key, double value) {
            this.key = key;
            this.value = value;
        }

        private Pair() {

        }

        @Override
        public int compareTo(Pair o) {
            if (this.value < o.value) {
                return -1;
            } else if (this.value == o.value) {
                return 0;
            } else {
                return 1;
            }
        }

    }

}
