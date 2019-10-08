package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Created by Łukasz Migdałek on 2016-08-29.
 */
public final class SRVQModelBuilder {
    protected ExampleSet prototypes; //Set of prototypes
    private int iterations;
    private double alpha;
    private double temperature;
    private double temperatureRate;
    private DistanceMeasure measure;

    private SRVQModelBuilder() {
    }

    public static SRVQModelBuilder builder() {
        return new SRVQModelBuilder();
    }

    public SRVQModelBuilder withIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public SRVQModelBuilder withAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public SRVQModelBuilder withTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public SRVQModelBuilder withTemperatureRate(double temperatureRate) {
        this.temperatureRate = temperatureRate;
        return this;
    }

    public SRVQModelBuilder withMeasure(DistanceMeasure measure) {
        this.measure = measure;
        return this;
    }

    public SRVQModelBuilder withPrototypes(ExampleSet prototypes) {
        this.prototypes = prototypes;
        return this;
    }

    public SRVQModel build() throws OperatorException {
        return new SRVQModel(prototypes, iterations, measure, alpha, temperature, temperatureRate);
    }
}
