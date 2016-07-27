package com.rapidminer.ispr.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * 
 * @author Marcin
 */
public class WLVQModel extends AbstractLVQModel {

    DistanceMeasure measure;
    private int currentIteration, iterations;
    private double alpha;
    private double initialAlpha;

    /**
     * 
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @throws OperatorException
     */
    public WLVQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;        
        this.alpha = alpha;        
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
    }

    /**
     * Update codebooks position. Evaluated for every example in the example set until nextIteration returns false
     */
    @Override
    public void update() {
        double dist, minDist = Double.MAX_VALUE;
        int selectedPrototype = 0, i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, getCurrentExampleValues());
            if (dist < minDist) {
                minDist = dist;
                selectedPrototype = i;
            }
            i++;
        }
        double weight = getCurrentExample().getWeight();
        if (prototypeLabels[selectedPrototype] == exampleLabel) {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[selectedPrototype][i];
                value += alpha * weight * (exampleValues[i] - value);
                prototypeValues[selectedPrototype][i] = value;
            }
        } else {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[selectedPrototype][i];
                value -= alpha * weight * (exampleValues[i] - value);
                prototypeValues[selectedPrototype][i] = value;
            }
        }
    }

    /**
     * If return true, then next iteration will be performed
     * @return
     */
    @Override
    public boolean nextIteration() {
        currentIteration++;
        alpha = LVQTools.learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);        
        return currentIteration < iterations;
    }
}