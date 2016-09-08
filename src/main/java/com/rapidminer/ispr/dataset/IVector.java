/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import java.util.List;

/**
 * Elements stored
 * @author Marcin 
 */
public interface IVector extends Cloneable {
    /**
     * Return stored values as dense double vector
     * @return 
     */
    double[] getValues();            
    
    /**
     * Set values using RapidMiner Example and selected attributes
     * @param example
     * @param attributes 
     */
    void setValues(Example example, List<Attribute> attributes);
    
    /**
     * The same as above but instead of list use array of attributes
     * @param example
     * @param attributes 
     */
    void setValues(Example example, Attribute[] attributes);

    /**
     * The same as above but sotres all elements of example
     * @param example 
     */
    void setValues(Example example);   
    
    /**
     * Returns number of elements in vector
     * @return 
     */
    int size();
    
    /**
     * Set vale of index i given value
     * @param i
     * @param value 
     */
    void setValue(int i, double value);
    
    /**
     * reads i'th value from vector
     * @param i
     * @return 
     */
    double getValue(int i);
    
    /**
     * Returns true if vector is sparse
     * @return 
     */
    boolean isSparse();
    
    /**
     * It returns index of elements which can be non zero. especially used for sparse representation, 
     * than it returns non zero elements stored inside vector
     * @return 
     */
    int[] getNonEmptyIndex();

    /**
     * Creates a shellow copy of the values
     * @return 
     */
    public Object clone();        
}
