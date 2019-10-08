/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.neuralnet.models;

/**
 * @author Marcin
 */
public interface LearningRateUpdateRule {
    double update(double learningRate);
}
