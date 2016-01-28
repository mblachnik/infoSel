/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.misc;

import com.rapidminer.example.ExampleSet;

/**
 *
 * @author Marcin
 */
public interface GroupSimilarity {    
    double calculate(ExampleSet exampleSet1, ExampleSet exampleSet2);
}
