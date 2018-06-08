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
public class LVQTools {
    
    /**
     * 
     * @param learningRate
     * @param currentIteration
     * @param iterations
     * @param initialLearningRate
     * @return
     */
    public static double learingRateUpdateRule(double learningRate, int currentIteration, int iterations, double initialLearningRate) {
        //learningRate = learningRate * Math.exp(-(double)currentIteration/(double)iterations);
        //learningRate = learningRate / (1 + Math.sqrt(iterations) / (double) iterations + learningRate);
        learningRate = initialLearningRate * ((double)(iterations - currentIteration)) / ((double) iterations);
        //learningRate = learningRate / (1 + learningRate);
        return learningRate;
    }
    
    public static double lambdaRateUpdateRule(double lambda, int currentIteration, int iterations, double initialLambdaRate){
        lambda = initialLambdaRate * ((double)(iterations - currentIteration)) / ((double) iterations);
        return lambda;
    }        
}
