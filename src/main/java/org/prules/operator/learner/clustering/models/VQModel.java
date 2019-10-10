package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

import java.util.logging.Logger;

/**
 * @author Marcin
 */
public class VQModel extends AbstractVQModel {
    private static final Logger LOG = Logger.getLogger(VQModel.class.getName());

    private final DistanceMeasure measure;
    private int currentIteration;
    private final int iterations;
    private double alpha;
    private final double initialAlpha;

    /**
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @throws OperatorException
     */
    public VQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha) throws OperatorException {
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

        //LOG.log(Level.FINEST, "VQ:{0}, Updating, Iteration:{1}", new Object[]{this.toString(), currentIteration});

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

        for (i = 0; i < getAttributesSize(); i++) {
            double value = prototypeValues[selectedPrototype][i];
            value += alpha * (exampleValues[i] - value);
            prototypeValues[selectedPrototype][i] = value;
        }
    }

    /**
     * @return
     */
    @Override
    public boolean nextIteration() {
        currentIteration++;
        alpha = learningRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);

        //LOG.log(Level.FINE, "VQ:{0}, NextIteration, Iteration:{1}", new Object[]{this.toString(), currentIteration});

        return currentIteration < iterations;
    }
}
