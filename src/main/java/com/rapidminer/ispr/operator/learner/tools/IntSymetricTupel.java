/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;

import java.io.Serializable;

/**
 *
 * @author Marcin
 */
public class IntSymetricTupel implements Serializable {

    /**
     * This class can be used to build pairs of typed objects and sort them.
     * ATTENTION!! This class is not usable for hashing since only the first
     * version is used as hash entry. To use a hash function on a tupel, use
     * Pair!
     *
     * @author Sebastian Land
     */
    private static final long serialVersionUID = 9219166123756515L;
    public static final int MEM_SIZE = (Integer.SIZE * 2)/8; //Number of bytes used to store the object
    private int t1;
    private int t2;

    public IntSymetricTupel(int t1, int t2) {
        if (t1 > t2) {
            this.t1 = t1;
            this.t2 = t2;
        } else {
            this.t1 = t2;
            this.t2 = t1;
        }
    }

    public int getFirst() {
        return t1;
    }

    public int getSecond() {
        return t2;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof com.rapidminer.ispr.operator.learner.tools.IntSymetricTupel)) {
            return false;
        }
        com.rapidminer.ispr.operator.learner.tools.IntSymetricTupel a = (com.rapidminer.ispr.operator.learner.tools.IntSymetricTupel) o;
        if (this.t1 == a.t1 && this.t2 == a.t2) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return t1 + Integer.MAX_VALUE / 2 + t2;
    }

    @Override
    public String toString() {
        return "(" + t1 + ", " + t2 + ")";
    }
}
