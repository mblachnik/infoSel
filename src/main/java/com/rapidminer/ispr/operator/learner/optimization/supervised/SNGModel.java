package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Class implements supervised neural gas algorithm
 * @author Marcin
 */
public class SNGModel extends AbstractLVQModel {

    private DistanceMeasure measure;
    private int currentIteration, iterations;
    private double alpha, lambda;
    private double initialAlpha, initialLambda;
    //private double minLearningRate;

    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha     
     * @throws OperatorException
     */
    public SNGModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha, double lambda) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;        
        this.alpha = alpha;        
        this.initialAlpha = alpha;
        this.measure = measure;
        this.lambda=lambda;
        this.initialLambda=lambda;
        this.measure.init(prototypes);
    }

    /**
     * Update codebooks weights
     */    
    @Override
    public void update() {
            
        double dist, minDist = Double.MAX_VALUE;

        ArrayList<Pair> distanceTable = new ArrayList<Pair>();
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            distanceTable.add(new Pair(i, dist));
            if (dist < minDist) {
                minDist = dist;
            }
            i++;
        }

        //posortowanie neuronów w kolejności rosnącej odległości
        Collections.sort(distanceTable);       
        
        int j=0;
        double g = 0;

        //obliczenie odległości między wektorem zwycięzcą a pozostałymi oraz adaptacja wag zgodnie z regułą WTM
        for (Pair prototypePair : distanceTable) {
    
                
            g = Math.exp(-1*((j+1)/lambda));

            if (prototypeLabels[prototypePair.key] == exampleLabel || (Double.isNaN(prototypeLabels[prototypePair.key]))) {
                for (i = 0; i < getAttributesSize(); i++) {
                    double value = prototypeValues[prototypePair.key][i];
                    value += alpha * g * (exampleValues[i] - value);
                    prototypeValues[prototypePair.key][i] = value;
                }
            } else {
                for (i = 0; i < getAttributesSize(); i++) {
                    double value = prototypeValues[prototypePair.key][i];
                    value -= alpha * g * (exampleValues[i] - value);
                    prototypeValues[prototypePair.key][i] = value;
                }
            }
            j++;
            }
            

    }

    /**
     * If true then next iteration will be performed
     * @return
     */
    @Override
    public boolean nextIteration() {
        currentIteration++;
        alpha = LVQTools.learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);       
        lambda = LVQTools.lambdaRateUpdateRule(lambda, currentIteration, iterations, initialLambda);
        return currentIteration < iterations;
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
