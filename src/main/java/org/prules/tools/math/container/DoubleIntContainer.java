/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import java.io.Serializable;

/**
 * @author Marcin
 */
public class DoubleIntContainer implements Comparable<DoubleIntContainer>, Serializable {

    /**
     *
     */
    public static final long serialVersionUID = 1L;
    /**
     *
     */
    public double first;
    /**
     *
     */
    public int second;

    /**
     * @param valueA
     * @param valueB
     */
    public DoubleIntContainer(double valueA, int valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double valueA) {
        this.first = valueA;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int valueB) {
        this.second = valueB;
    }

    @Override
    public String toString() {
        return first + " : " + second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (int) (prime * first) + (prime * second);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DoubleIntContainer other = (DoubleIntContainer) obj;
        if (first != other.first) {
            return false;
        }
        return second == other.second;
    }

    @Override
    public int compareTo(DoubleIntContainer o) {
        return Double.compare(first, o.first);
    }
}
