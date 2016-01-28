/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization.supervised;

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
        learningRate = learningRate * ((double)(iterations - currentIteration)) / ((double) iterations);
        return learningRate;
    }
    
    public static double lambdaRateUpdateRule(double lambda, int currentIteration, int iterations, double initialLambdaRate){
        lambda = lambda * ((double)(iterations - currentIteration)) / ((double) iterations);
        return lambda;
    }        
}
