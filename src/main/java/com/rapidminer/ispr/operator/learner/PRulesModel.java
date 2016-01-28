/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner;

import com.rapidminer.example.ExampleSet;

/**
 * An interface for any instance Selection implementation.
 * It allows for separation between RapidMiner core and method implementation
 * @author Marcin
 */
public interface PRulesModel<T> {
    /**
     * This method implements the pure algorithm based on the trainingData
     * @param exampleSet - training set
     * @return - resultant set
     */
    public T run(ExampleSet exampleSet);    
}
