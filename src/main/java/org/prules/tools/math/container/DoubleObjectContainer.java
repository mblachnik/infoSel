/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import java.io.Serializable;

/**
 * @param <M>
 * @author Marcin
 */
public class DoubleObjectContainer<M> implements Comparable<DoubleObjectContainer<M>>, Serializable {

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
    public M second;

    /**
     * @param valueA
     * @param valueB
     */
    public DoubleObjectContainer(double valueA, M valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double valueA) {
        this.first = valueA;
    }

    public M getSecond() {
        return second;
    }

    public void setSecond(M valueB) {
        this.second = valueB;
    }

    @Override
    public String toString() {
        String kString = (getSecond() == null) ? "null" : getSecond().toString();
        return first + " : " + kString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (int) (prime * first);
        result = prime * result + ((second == null) ? 0 : second.hashCode());
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
        DoubleObjectContainer other = (DoubleObjectContainer) obj;
        if (first != other.first) {
            return false;
        }
        if (second == null) {
            return other.second == null;
        } else return second.equals(other.second);
    }

    @Override
    public int compareTo(DoubleObjectContainer<M> o) {
        return Double.compare(first, o.first);
    }
}
