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
public class SLVQ1Model extends AbstractLVQModel {

    private DistanceMeasure measure;
    private int currentIteration, iterations;
    private double alpha;
    private double initialAlpha;

    /**
     * 
     * @param prototypes
     * @param maxIterations
     * @param measure
     * @param alpha
     * @throws OperatorException
     */
    public SLVQ1Model(ExampleSet prototypes, int maxIterations, DistanceMeasure measure, double alpha) throws OperatorException {
        super(prototypes);
        this.iterations = maxIterations;
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

        if ((prototypeLabels[selectedPrototype] == exampleLabel) || (Double.isNaN(prototypeLabels[selectedPrototype]))) {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[selectedPrototype][i];
                value += alpha * (exampleValues[i] - value);
                prototypeValues[selectedPrototype][i] = value;
            }
        } else {
            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[selectedPrototype][i];
                value -= alpha * (exampleValues[i] - value);
                prototypeValues[selectedPrototype][i] = value;
            }
        }        
    }

    /**
     * If return true, then next iteration will be performed
     * @return
     */
    @Override
    public boolean isNextIteration(ExampleSet trainingSet) {
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
