/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.weighting.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.tools.math.container.PairContainer;

/**
 * Abstract method for all noise estimators
 * @author Marcin
 */
public abstract class AbstractNoiseEstimatorModel {
    
    /**
     * Method used to execute noise estimator. For each instance in exampleSet it calculates residual level of noise
     * @param exampleSet 
     * @return  return a table of noise level values
     */
    public abstract PairContainer<double[],double[]> run(ExampleSet exampleSet);  
    
    /**
     * Returns the level of noise in the data. Note that first run(...) method needs to be called, otherwise you get NaN.
     * @return 
     */
    public abstract double getNNE();
}
