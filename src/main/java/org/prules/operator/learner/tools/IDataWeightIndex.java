/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

/**
 *
 * @author Marcin
 */
public interface IDataWeightIndex extends Iterable<Integer>, IDataIndex {

    /**
     * The same as set but if "i" is out range add new field in the binary index
     * @param i
     * @param value
     * @param weight
     */
    void add(int i, boolean value, double weight);

    /**
     * add to the end of index new value
     * @param value
     * @param weight
     */
    void add(boolean value, double weight);

    /**
     * reads the weight at position i
     * @param i
     * @return
     */
    double getWeight(int i);        

    

    /**
     * returns new DataIndex to all elements marked as active (selected)
     * @return
     */
    @Override
    IDataWeightIndex getIndex();    

    /**
     * Set specific index into true/false
     * @param i
     * @param value
     * @param weight
     */
    void set(int i, boolean value, double weight);    
    
    /**
     * Set specific index into true/false
     * @param i     
     * @param weight
     */
    void setWeight(int i, double weight);    
   
    /**
     * Acquire new index to all selected elements, such that all these elements
     * which were set to true in the original data would now have new index value.
     * Size of input dataindex must be equal to this.length()
     * @param index
     */
    void setIndex(IDataWeightIndex index);
    
}
