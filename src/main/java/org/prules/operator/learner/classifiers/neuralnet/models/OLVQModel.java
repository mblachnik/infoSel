package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Marcin
 */
public class OLVQModel extends AbstractLVQModel {

    DistanceMeasure measure;
    int currentIteration, iterations;
    double alphas[], alpha;

    /**
     * 
     * @param prototypes
     * @param maxIterations
     * @param measure
     * @param alpha
     * @throws OperatorException
     */
    public OLVQModel(ExampleSet prototypes, int maxIterations, DistanceMeasure measure, double alpha) throws OperatorException {
        super(prototypes);
        this.iterations = maxIterations;
        this.currentIteration = 0;
        alphas = new double[prototypes.size()];
        for (int i = 0; i < prototypes.size(); i++) {
            alphas[i] = alpha;
        }
        this.alpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
    }

    /**
     * 
     */
    @Override
    public void update(double[][] prototypeValues, double[] prototypeLabels, double[] exampleValues, double exampleLabel, Example example) {
        double dist, minDist = Double.MAX_VALUE;
        int selectedPrototype = 0;
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            if (dist < minDist) {
                minDist = dist;
                selectedPrototype = i;
            }
            i++;
        }

        if (prototypeLabels[selectedPrototype] == exampleLabel) {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[selectedPrototype][i];
                value += alphas[selectedPrototype] * (exampleValues[i] - value);
                prototypeValues[selectedPrototype][i] = value;
            }
            alphas[selectedPrototype] = alphas[selectedPrototype] / (1 + alphas[selectedPrototype]);
        } else {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[selectedPrototype][i];
                value -= alphas[selectedPrototype] * (exampleValues[i] - value);
                prototypeValues[selectedPrototype][i] = value;
            }
            alphas[selectedPrototype] = alphas[selectedPrototype] / (1 - alphas[selectedPrototype]);
            if (alphas[selectedPrototype] > alpha) {
                alphas[selectedPrototype] = alpha;
            }
        }
    }

    /**
     * 
     * @return
     */
    @Override
    public boolean isNextIteration(ExampleSet trainingSet) {
        currentIteration++;
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
