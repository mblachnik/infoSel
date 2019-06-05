/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container;

import com.rapidminer.tools.container.Tupel;
import java.io.Serializable;

/**
 *
 * @param <N>
 * @param <M>
 * @author Marcin
 */
public class DoubleLongContainer implements Comparable<DoubleLongContainer>, Serializable {

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
    public long second;

    /**
     *
     * @param valueA
     * @param valueB
     */
    public DoubleLongContainer(double valueA, long valueB) {
        this.first = valueA;
        this.second = valueB;
    }

    public double getFirst() {
        return first;
    }

    public void setFirst(double valueA) {
        this.first = valueA;
    }

    public long getSecond() {
        return second;
    }

    public void setSecond(long valueB) {
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
        result = (int)(prime * first) + (prime*(int)second);
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
        DoubleLongContainer other = (DoubleLongContainer) obj;
        if (first != other.first) {            
                return false;
            }
        return second == other.second;
    }

    @Override
    public int compareTo(DoubleLongContainer o) {
        //double result = first-o.first;
        if (first == o.first)
            return 0;
        if (first > o.first)
            return 1;
        return -1;
    }
}
