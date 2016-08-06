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
 *
 * @author Marcin 
 */
public interface Instance extends Cloneable {

    double[] getValues();            
    
    void setValues(Example example, List<Attribute> attributes);
    
    void setValues(Example example, Attribute[] attributes);

    void setValues(Example example);   
    
    int size();
    
    void setValue(int i, double value);
    
    double getValue(int i);
    
    boolean isSparse();
    
    int[] getNonEmptyIndex();

    public Object clone();
}
