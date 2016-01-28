/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.tools.math.container;

import java.io.Serializable;

/**
 *
 * @param <N>
 * @param <M>
 * @author Marcin
 */
public class IntObjectContainer<M> implements Comparable<IntObjectContainer<M>>, Serializable {

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
    public M second;

    /**
     *
     * @param valueA
     * @param valueB
     */
    public IntObjectContainer(int valueA, M valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int valueA) {
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
        IntObjectContainer other = (IntObjectContainer) obj;
        if (first != other.first) {
            return false;
        }
        if (second == null) {
            if (other.second != null) {
                return false;
            }
        } else if (!second.equals(other.second)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(IntObjectContainer<M> o) {
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
