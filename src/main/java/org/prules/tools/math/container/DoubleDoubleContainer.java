/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import java.io.Serializable;

/**
 * @author Marcin
 */
public class DoubleDoubleContainer implements Comparable<DoubleDoubleContainer>, Serializable {

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
    public double second;

    /**
     * @param valueA
     * @param valueB
     */
    public DoubleDoubleContainer(double valueA, double valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double valueA) {
        this.first = valueA;
    }

    public double getSecond() {
        return second;
    }

    public void setSecond(double valueB) {
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
        result = (int) (prime * first) + (int) (prime * second);
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
        DoubleDoubleContainer other = (DoubleDoubleContainer) obj;
        if (first != other.first) {
            return false;
        }
        return !(second != other.second);
    }

    @Override
    public int compareTo(DoubleDoubleContainer o) {
        return Double.compare(first, o.first);
    }
}
