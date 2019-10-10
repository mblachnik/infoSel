package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.operator.learner.classifiers.neuralnet.models.LVQTools;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Łukasz Migdałek on 2016-07-13.
 */
public class NeuralGasVQModel extends AbstractVQModel {

    private final int iterations;
    private int currentIteration;
    private double alpha;
    private double lambda;
    private final double initialAlpha;
    private final double initialLambda;
    private final DistanceMeasure measure;

    public NeuralGasVQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha, double lambda) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alpha = alpha;
        this.lambda = lambda;
        this.initialLambda = lambda;
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
    }

    @Override
    public void update() {
        double dist, minDist = Double.MAX_VALUE;

        ArrayList<Neuron> distanceTable = new ArrayList<>();
        int i = 0;
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            distanceTable.add(new Neuron(i, dist));
            if (dist < minDist) {
                minDist = dist;
            }
            i++;
        }

        //posortowanie neuronów w kolejności rosnącej odległości
        Collections.sort(distanceTable);

        int j = 0;
        double g;

        //obliczenie odległości między wektorem zwycięzcą a pozostałymi oraz adaptacja wag zgodnie z regułą WTM
        for (Neuron prototypePair : distanceTable) {


            g = Math.exp(-1 * ((j + 1) / lambda));

            for (i = 0; i < getAttributesSize(); i++) {
                double value = prototypeValues[prototypePair.getIndex()][i];
                value += alpha * g * (exampleValues[i] - value);
                prototypeValues[prototypePair.getIndex()][i] = value;
            }

            j++;
        }
    }


    @Override
    public boolean nextIteration() {
        currentIteration++;
        alpha = LVQTools.learningRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);
        lambda = LVQTools.lambdaRateUpdateRule(lambda, currentIteration, iterations, initialLambda);
        return currentIteration < iterations;
    }
}
