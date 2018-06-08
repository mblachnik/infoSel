package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class LVQ2ModelMy extends AbstractLVQModel {

    private final DistanceMeasure measure;
    private final int iterations;
    private int currentIteration;
    private double alpha;
    private final double initialAlpha;

    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @param alphaNegative
     * @throws OperatorException
     */
    public LVQ2ModelMy(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alpha) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;        
        this.alpha = alpha;        
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
    }

    /**
     *
     */
    @Override
    public void update() {
        double dist;
        double minDistCorrect = Double.MAX_VALUE;
        double minDistIncorrect = Double.MAX_VALUE;
        int selectedPrototypeCorrect = 0;
        int selectedPrototypeIncorrect = 0;
        int i = 0;

        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            double protoLabel = prototypeLabels[i];
            if (dist < minDistCorrect && exampleLabel == protoLabel) {
                minDistCorrect = dist;
                selectedPrototypeCorrect = i;
            }
            if (dist < minDistIncorrect && exampleLabel != protoLabel) {
                minDistIncorrect = dist;
                selectedPrototypeIncorrect = i;
            }
            i++;
        }

        for (i = 0; i < getAttributesSize(); i++) {
            double trainValue = exampleValues[i];
            double valueCorrect = prototypeValues[selectedPrototypeCorrect][i];
            double valueIncorrect = prototypeValues[selectedPrototypeIncorrect][i];
            valueCorrect += alpha * (trainValue - valueCorrect);
            valueIncorrect -= alpha * (trainValue - valueIncorrect);
            prototypeValues[selectedPrototypeCorrect][i] = valueCorrect;
            prototypeValues[selectedPrototypeIncorrect][i] = valueIncorrect;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean nextIteration(ExampleSet trainingSet) {
        currentIteration++;
        alpha = LVQTools.learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);        
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
