/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

/**
 * Interface for various criteria of Graph editing algorithms such us Gabriel Editing or RNG algorithm
 * @author Marcin
 */
public interface EditedDistanceGraphCriteria {
    /**
     * Function used to evaluate relation between three instances. Usually this is triangular inequality
     * @param a distance between samples 1 and 3
     * @param b distance between samples 1 and 2
     * @param c distance between samples 2 and 3
     * @return true id criteria is valid
     */
    boolean evaluate(double a, double b, double c);
}
