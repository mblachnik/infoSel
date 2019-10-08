package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Created by Łukasz Migdałek on 2016-08-29.
 */
public final class SCSVQModelBuilder {
    protected ExampleSet prototypes; //Set of prototypes
    private int iterations;
    private double alpha;
    private double temperature;
    private double temperatureRate;
    private DistanceMeasure measure;

    private SCSVQModelBuilder() {
    }

    public static SCSVQModelBuilder builder() {
        return new SCSVQModelBuilder();
    }

    public SCSVQModelBuilder withIterations(int iterations) {
        this.iterations = iterations;
        return this;
    }

    public SCSVQModelBuilder withAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public SCSVQModelBuilder withTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public SCSVQModelBuilder withTemperatureRate(double temperatureRate) {
        this.temperatureRate = temperatureRate;
        return this;
    }

    public SCSVQModelBuilder withMeasure(DistanceMeasure measure) {
        this.measure = measure;
        return this;
    }

    public SCSVQModelBuilder withPrototypes(ExampleSet prototypes) {
        this.prototypes = prototypes;
        return this;
    }

    public SCSVQModel build() throws OperatorException {
        SCSVQModel sRVQModel = new SCSVQModel(prototypes, iterations, measure, alpha, temperature, temperatureRate);
        return sRVQModel;
    }
}
