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
//public class PairContainer<N extends Comparable, M> implements Comparable<PairContainer<N, M>>, Serializable {
public class PairContainer<N, M> implements Serializable {

    /**
     *
     */
    public static final long serialVersionUID = 1L;
    /**
     *
     */
    public N first;
    /**
     *
     */
    public M second;

    /**
     *
     * @param valueA
     * @param valueB
     */
    public PairContainer(N valueA, M valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public N getFirst() {
        return first;
    }

    public void setFirst(N valueA) {
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
        String tString = (getFirst() == null) ? "null" : getFirst().toString();
        String kString = (getSecond() == null) ? "null" : getSecond().toString();
        return tString + " : " + kString;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
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
        PairContainer other = (PairContainer) obj;
        if (first == null) {
            if (other.first != null) {
                return false;
            }
        } else if (!first.equals(other.first)) {
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
/*
    @Override
    public int compareTo(PairContainer<N, M> o) {
        return first.compareTo(o.getFirst());
    }
    * */
}
