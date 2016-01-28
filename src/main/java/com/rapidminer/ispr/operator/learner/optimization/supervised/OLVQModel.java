package com.rapidminer.ispr.operator.learner.optimization.supervised;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

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
     * @param iterations
     * @param measure
     * @param alpha
     * @throws OperatorException
     */
    public OLVQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
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
    public void update() {
        double dist, minDist = Double.MAX_VALUE;
        int selectedPrototype = 0;
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, getCurrentExampleValues());
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
    public boolean nextIteration() {
        currentIteration++;
        return currentIteration < iterations;
    }
}
