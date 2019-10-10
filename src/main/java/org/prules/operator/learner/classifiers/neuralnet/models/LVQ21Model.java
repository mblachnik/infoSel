package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin
 */
public class LVQ21Model extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alpha;
    private final double initialAlpha;
    private final double window;

    /**
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @param window
     * @throws OperatorException
     */
    public LVQ21Model(ExampleSet prototypes, int iterations,
                      DistanceMeasure measure, double alpha, double window) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alpha = alpha;
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
        this.window = (1 - window) / (1 + window);
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
            dist = measure.calculateDistance(prototype, exampleValues);
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

        double labelExa = exampleLabel;
        double labelProto1 = prototypeLabels[selectedPrototypeNr1];
        double labelProto2 = prototypeLabels[selectedPrototypeNr2];

        if (labelProto1 != labelProto2) {
            if ((labelProto1 == labelExa) || (labelProto2 == labelExa)) {
                if (labelProto2 == labelExa) {
                    int prototype = selectedPrototypeNr1;
                    selectedPrototypeNr1 = selectedPrototypeNr2;
                    selectedPrototypeNr2 = prototype;
                }
                if (minDist1 / minDist2 > window) {
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
