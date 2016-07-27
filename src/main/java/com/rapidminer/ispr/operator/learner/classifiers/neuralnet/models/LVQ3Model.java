package com.rapidminer.ispr.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class LVQ3Model extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alpha;
    private final double initialAlpha;
    private final double window;
    private final double epsilon;

    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @param alphaNegative
     * @param window
     * @param epsilon
     * @throws OperatorException
     */
    public LVQ3Model(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alpha,
            double window, double epsilon) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;        
        this.alpha = alpha;
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
        this.window = (1 - window) / (1 + window);
        this.epsilon = epsilon;
    }

    /**
     *
     */
    @Override
    public void update() {
        double dist, minDist1 = Double.MAX_VALUE, minDist2 = Double.MAX_VALUE;
        int selectedPrototypeNr1 = 0;
        int selectedPrototypeNr2 = 0;
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, getCurrentExampleValues());
            if (dist < minDist1) {
                minDist2 = minDist1;
                minDist1 = dist;
                selectedPrototypeNr2 = selectedPrototypeNr1;
                selectedPrototypeNr1 = i;
            } else if (dist < minDist2) {
                minDist2 = dist;
                selectedPrototypeNr2 = i;
            }
            i++;
        }

        double label = exampleLabel;
        double label1 = prototypeLabels[selectedPrototypeNr1];
        double label2 = prototypeLabels[selectedPrototypeNr2];
        if (label1 != label2) {
            if ((label1 == label) || (label2 == label)) {
                if (minDist1 / minDist2 > window) {
                    if (label2 == label) {
                        int prototype = selectedPrototypeNr1;
                        selectedPrototypeNr1 = selectedPrototypeNr2;
                        selectedPrototypeNr2 = prototype;
                    }

                    for (i = 0; i < getAttributesSize(); i++) {
                        double trainValue = exampleValues[i];
                        double value1 = prototypeValues[selectedPrototypeNr1][i];
                        double value2 = prototypeValues[selectedPrototypeNr2][i];
                        value1 += alpha * (trainValue - value1);
                        value2 -= alpha * (trainValue - value2);
                        prototypeValues[selectedPrototypeNr1][i] = value1;
                        prototypeValues[selectedPrototypeNr2][i] = value2;
                    }
                }
            }
        } else if (label1 == label) {
            for (i = 0; i < getAttributesSize(); i++) {
                double trainValue = exampleValues[i];
                double value1 = prototypeValues[selectedPrototypeNr1][i];
                double value2 = prototypeValues[selectedPrototypeNr2][i];
                value1 += epsilon * alpha * (trainValue - value1);
                value2 += epsilon * alpha * (trainValue - value2);
                prototypeValues[selectedPrototypeNr1][i] = value1;
                prototypeValues[selectedPrototypeNr2][i] = value2;
            }
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean nextIteration() {
        currentIteration++;
        alpha = LVQTools.learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);        
        return currentIteration < iterations;
    }
}
