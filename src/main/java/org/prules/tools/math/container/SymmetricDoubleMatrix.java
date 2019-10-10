/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

/**
 * @author Marcin
 */
public class SymmetricDoubleMatrix {
    private double[] matrix;
    private int size;

    public SymmetricDoubleMatrix(int size) {
        matrix = new double[size * (size + 1) / 2];
        this.size = size;
    }

    public void set(int i1, int i2, double value) {
        matrix[getIndex(i1, i2)] = value;
    }

    public double get(int i1, int i2) {
        return matrix[getIndex(i1, i2)];
    }

    private int getIndex(int i1, int i2) {
        if (i2 > i1) {
            int i = i1;
            i1 = i2;
            i2 = i;
        }
        return i1 + i2 * size - i2 * (i2 + 1) / 2;
    }
}
