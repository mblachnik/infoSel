/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

import java.util.ListIterator;

/**
 *
 * @author Marcin
 */
public interface IDataIndex extends Iterable<Integer> {

    /**
     * The same as set but if "i" is out range add new field in the binary index
     * @param i
     * @param value
     */
    void add(int i, boolean value);

    /**
     * add to the end of index new value
     * @param value
     */
    void add(boolean value);

    /*
     * Create a clon by inserting "index" a colling {@link #DataIndex(boolean[])}
     */
    Object clone();

    boolean equals(Object obj);

    /**
     * reads the index at position i
     * @param i
     * @return
     */
    boolean get(int i);

    /**
     * Returns and index of selected elements
     */
    int[] getAsInt();

    /**
     * Total number of elements in the index (both selected and unselected)
     * @return
     */
    int getFullLength();

    /**
     * returns new DataIndex to all elements marked as active (selected)
     * @return
     */
    IDataIndex getIndex();

    /**
     * Return handle to binary index. The booleans array is now shared
     * @return @deprecated
     */
    @Deprecated
    boolean[] getIndexHandle();

    /**
     * Number of elements indexed true
     * @return
     */
    int getLength();

    /**
     * Returns index of the original base data structure
     * @param i
     * @return
     */
    int getOryginalIndex(int i);

    int hashCode();

    /**
     * Iterator over elements
     * @return
     */
    ListIterator<Integer> iterator();

    /**
     * Iterator over elements which starts from index
     * @return
     */
    ListIterator<Integer> iterator(int index);

    /**
     * Makes an inverse of selected elements
     */
    void negate();

    /**
     * mremove i'th value from binary index (it realocates memory)
     * @param i
     */
    void remove(int i);

    /**
     * Set specific index into true/false
     * @param i
     * @param value
     */
    void set(int i, boolean value);

    /**
     * Mark all elements as unselected
     */
    void setAllFalse();

    /**
     * Mark all elements as selected
     */
    void setAllTrue();

   
    /**
     * Acquire new index to all selected elements, such that all these elements
     * which were set to true in the original data would now have new index value.
     * Size of input dataindex must be equal to this.length()
     * @param index
     */
    void setIndex(IDataIndex index);
    
}
