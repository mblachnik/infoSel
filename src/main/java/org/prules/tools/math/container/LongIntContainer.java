/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import java.io.Serializable;

/**
 * Container which allows to store long int values as primitive types. Not that it implements comparable interface.
 * COmparison is made according to the first element
 *
 * @author Marcin
 */
public class LongIntContainer implements Comparable<LongIntContainer>, Serializable {

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
    public int second;

    /**
     * @param valueA
     * @param valueB
     */
    public LongIntContainer(long valueA, int valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public long getFirst() {
        return first;
    }

    public void setFirst(long valueA) {
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
        LongIntContainer other = (LongIntContainer) obj;
        if (first != other.first) {
            return false;
        }
        return second == other.second;
    }

    @Override
    public int compareTo(LongIntContainer o) {
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
