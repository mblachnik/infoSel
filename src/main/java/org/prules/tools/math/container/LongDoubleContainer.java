/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import java.io.Serializable;

/**
 *Container which allows to store long double values as primitive types. Not that it implements comparable interface.
 * COmparison is made according to the first element
 * @author Marcin
 */
public class LongDoubleContainer implements Comparable<LongDoubleContainer>, Serializable {

    /**
     *
     */
    public static final long serialVersionUID = 1L;
    /**
     *
     */
    public long first;
    /**
     *
     */
    public double second;

    /**
     *
     * @param valueA
     * @param valueB
     */
    public LongDoubleContainer(long valueA, double valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public long getFirst() {
        return first;
    }

    public void setFirst(long valueA) {
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
        result = (int)(prime * first) + (int) (prime * second);
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
        LongDoubleContainer other = (LongDoubleContainer) obj;
        if (first != other.first) {
            return false;
        }
        return !(second != other.second);
    }

    @Override
    public int compareTo(LongDoubleContainer o) {
        long result = first - o.first;
        if (result == 0) {
            return 0;
        }
        if (result > 0) {
            return 1;
        }
        return -1;
    }
}
