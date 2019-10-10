/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.neuralnet.models;

/**
 * @author Marcin
 */
public class LinearLearningRateUpdateRule implements LearningRateUpdateRule {
    private int iterations;
    private int iteration;
    private double initialLearningRate;

    public LinearLearningRateUpdateRule(double initialLearningRate, int iterations) {
        this.iterations = iterations;
        this.initialLearningRate = initialLearningRate;
    }

    @Override
    public double update(double learningRate) {
        learningRate = initialLearningRate * (iterations - iteration) / ((double) iterations);
        iteration++;
        return learningRate;
    }
}
