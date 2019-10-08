package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin
 */
public class LVQ3ModelMy extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alpha;
    private final double initialAlpha;
    private final double window;
    private final double epsilon;

    /**
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @param window
     * @param epsilon
     * @throws OperatorException
     */
    public LVQ3ModelMy(ExampleSet prototypes, int iterations,
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
        double dist, minDistCorrect = Double.MAX_VALUE, minDistOther = Double.MAX_VALUE;
        int selectedPrototypeCorrect = 0;
        int selectedPrototypeOther = 0;
        double labelOther = -1;
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            double protoLabel = prototypeLabels[i];
            if (dist < minDistCorrect && exampleLabel == protoLabel) {
                minDistCorrect = dist;
                selectedPrototypeCorrect = i;
            } else if (dist < minDistOther) {
                minDistOther = dist;
                selectedPrototypeOther = i;
                labelOther = prototypeLabels[selectedPrototypeOther];
            }
            i++;
        }

        if (labelOther != exampleLabel) {
            double threshold = Math.min(minDistCorrect / minDistOther, minDistOther / minDistCorrect);
            if (threshold > window) {
                for (i = 0; i < getAttributesSize(); i++) {
                    double trainValue = exampleValues[i];
                    double value1 = prototypeValues[selectedPrototypeCorrect][i];
                    double value2 = prototypeValues[selectedPrototypeOther][i];
                    value1 += alpha * (trainValue - value1);
                    value2 -= alpha * (trainValue - value2);
                    prototypeValues[selectedPrototypeCorrect][i] = value1;
                    prototypeValues[selectedPrototypeOther][i] = value2;
                }
            }
        } else {
            for (i = 0; i < getAttributesSize(); i++) {
                double trainValue = exampleValues[i];
                double value1 = prototypeValues[selectedPrototypeCorrect][i];
                double value2 = prototypeValues[selectedPrototypeOther][i];
                value1 += epsilon * alpha * (trainValue - value1);
                value2 += epsilon * alpha * (trainValue - value2);
                prototypeValues[selectedPrototypeCorrect][i] = value1;
                prototypeValues[selectedPrototypeOther][i] = value2;
            }
        }
    }

    /**
     * @return
     */
    @Override
    public boolean nextIteration(ExampleSet trainingSet) {
        currentIteration++;
        alpha = LVQTools.learningRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);
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
}
