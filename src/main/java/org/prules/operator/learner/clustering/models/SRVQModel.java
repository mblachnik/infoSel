package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.mixed.MixedEuclideanDistance;

import java.util.Random;

/**
 * Created by Łukasz Migdałek on 2016-07-13.
 */
public class SRVQModel extends AbstractVQModel {

    private final Random random = new Random();
    protected int iterations;
    protected int currentIteration;
    private final double p;
    private double alpha;
    double temperature;
    double temperatureRate = 0.89;
    private final double initialAlpha;
    private final DistanceMeasure measure;
    MixedEuclideanDistance euclideanDistance;
    protected double sum = Double.MAX_VALUE;

    SRVQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha, double temperature, double temperatureRate) throws OperatorException {
        super(prototypes);
        this.temperature = temperature;
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alpha = alpha;
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
        this.p = random.nextDouble();
        this.euclideanDistance = new MixedEuclideanDistance();
        this.euclideanDistance.init(prototypes);
        this.temperatureRate = temperatureRate;

    }

    @Override
    public void update() {
        double[] dist = new double[this.getNumberOfPrototypes()];
        double distSum = 0;
        int j = 0;
        for (double[] prototype : prototypeValues) {
            dist[j] = Math.exp((-1 * Math.pow(euclideanDistance.calculateDistance(exampleValues, prototype), 2)) / temperature);
            distSum += dist[j];
            j++;
        }
        j = 0;
        for (double[] prototype : prototypeValues) {
            for (int i = 0; i < getAttributesSize(); i++) {
                double value = prototype[i];
                double x_minus_wi = exampleValues[i] - value;
                double calc_value = alpha * distributionFunctionCalc(dist[j] / distSum) * (x_minus_wi);
                value += calc_value;
                prototype[i] = value;
            }
            j++;
        }

    }

    @Override
    public boolean nextIteration() {
        //sum = calcSum();
        currentIteration++;
        alpha = initialAlpha * (iterations - currentIteration) / iterations;
//		alpha = learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);
        temperature *= temperatureRate;

        return currentIteration < iterations;
    }

    protected double calcSum() {
        double energy = 0;
        for (double[] dist : prototypeValues) {
            double euclideanDistValue = euclideanDistance.calculateDistance(exampleValues, dist);
            energy += Math.exp((-1 * euclideanDistValue * euclideanDistValue) / temperature);
        }
        return energy;
    }

    /**
     * Todo: Calculate every iteration or cache this values?
     *
     * @param up_part_calc
     * @return
     */
    protected double distributionFunctionCalc(double up_part_calc) {
        //if (currentIteration == 0 && sum == Double.MAX_VALUE) sum = calcSum();
        // Prevent before NaN.
        if (up_part_calc == 0) {//|| sum == 0) {
            return 0;
        }
        double pi = up_part_calc;
        double p = random.nextDouble();
        return pi > p ? 1 : 0;
    }
}
