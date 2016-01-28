/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools.genetic;

import com.rapidminer.ispr.tools.math.container.DoubleDoubleContainer;

/**
 *
 * @author Marcin
 */
public interface BinaryCoding {
    
    /**
     * 
     * @param bits
     * @param min
     * @param max
     * @return
     */
    public double decode(boolean[] bits, int min, int max);
    
    public double decode(boolean[] bits);
    
    /**
     * 
     * @param val
     * @param bits
     * @param min
     * @param max
     */
    public void code(double val, boolean[] bits, int min, int max);
        
    public void code(double val, boolean[] bits);
        
}
