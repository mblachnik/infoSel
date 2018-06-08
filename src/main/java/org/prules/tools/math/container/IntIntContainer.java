/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import java.io.Serializable;

/**
 * Container for storing two int double values. It sotres values as primitives for performance reasons. It also implements @{see Comparable} interface, and the comparison is mad according to first value
 * @param <N>
 * @param <M>
 * @author Marcin
 */
public class IntIntContainer implements Comparable<IntIntContainer>, Serializable {

    /**
     *
     */
    public static final long serialVersionUID = 1L;
    /**
     *
     */
    public int first;
    /**
     *
     */
    public int second;

    /**
     * Input values
     * @param valueA also used for ordering calues
     * @param valueB
     */
    public IntIntContainer(int valueA, int valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int valueA) {
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
        IntDoubleContainer other = (IntDoubleContainer) obj;
        if (first != other.first) {
            return false;
        }
        if (second != other.second) {

            return false;
        }
        return true;
    }

    @Override
    public int compareTo(IntIntContainer o) {
        int result = first - o.first;
        if (result == 0) {
            return 0;
        }
        if (result > 0) {
            return 1;
        }
        return -1;
    }
}
