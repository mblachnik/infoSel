/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.neuralnet.models;

/**
 *
 * @author Marcin
 */
public class SimpleLearningRateUpdateRule implements LearningRateUpdateRule{
    private final int iterations;    
    private final double alphaInit;
    private int iteration;

    public SimpleLearningRateUpdateRule(int iterations, double alphaInit) {
        this.iterations = iterations;
        this.iteration = 0;
        this.alphaInit = alphaInit;
    }
    
    @Override
    public double update(double learningRate) {
        learningRate -= learningRate * 10 / iterations;
        iteration++;
        return learningRate;
    }
    
}
