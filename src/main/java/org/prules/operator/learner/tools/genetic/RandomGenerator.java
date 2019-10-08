/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools.genetic;

/**
 * @author Marcin
 */
public interface RandomGenerator {
    /**
     * @return
     */
    int nextInteger();

    /**
     * @param n
     * @return
     */
    int nextInteger(int n);

    /**
     * @return
     */
    double nextDouble();
}
