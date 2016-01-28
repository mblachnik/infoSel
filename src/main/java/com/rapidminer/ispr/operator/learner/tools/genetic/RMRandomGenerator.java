/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools.genetic;

/**
 *
 * @author Marcin
 */

//TODO Implement different LossFunction support
public class RMRandomGenerator implements RandomGenerator {
    com.rapidminer.tools.RandomGenerator random;
    
    
    /**
     * 
     * @param random
     */
    public RMRandomGenerator(com.rapidminer.tools.RandomGenerator random){
        this.random = random;
    }

    /**
     * 
     * @return
     */
    @Override
    public int nextInteger() {
        return random.nextInt();
    }

    /**
     * 
     * @param n
     * @return
     */
    @Override
    public int nextInteger(int n) {
        return random.nextInt(n);
    }

    /**
     * 
     * @return
     */
    @Override
    public double nextDouble() {
        return random.nextDouble();
    }
    
    
}
