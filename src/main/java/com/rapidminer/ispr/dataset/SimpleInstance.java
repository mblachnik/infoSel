/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class representing single prototype such as cluster center or single instance
 * @author Marcin
 */
public class SimpleInstance implements Instance {    
    private double[] values;    
    private double   label;

    SimpleInstance(int n) {
        this.values = new double[n];
    }
    
    SimpleInstance(double[] values) {
        this.values = values;
    }
    
    SimpleInstance(Example example, List<Attribute> attributes) {
        this(attributes.size());
        setValues(example, attributes);
    }
    
    SimpleInstance(Example example, Attribute[] attributes) {
        this(attributes.length);
        setValues(example, attributes);
    }
    
    SimpleInstance(Example example) {
        this(example.getAttributes().size());
        setValues(example);
    }
    
    @Override
    public double[] getValues() {
        return values;
    }        
    
    @Override
    public final void setValues(Example example, List<Attribute> attributes) {
        int i = 0;        
        for (Attribute a : attributes){            
            this.values[i] = example.getValue(a);
            i++;
        }
    }
    
    @Override
    public final void setValues(Example example, Attribute[] attributes) {
        int i = 0;        
        for (Attribute a : attributes){            
            this.values[i] = example.getValue(a);
            i++;
        }
    }
    
    @Override
    public final void setValues(Example example) {
       setValues(example,example.getAttributes().createRegularAttributeArray());
    } 

    @Override
    public Object clone() {
        
        try {
            SimpleInstance instance = (SimpleInstance)super.clone();
            instance.label = this.label;
            instance.values = this.values.clone();        
            return instance;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Problem performing clone operation");
        }        
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void setValue(int i, double value) {
        values[i] = value;
    }

    @Override
    public double getValue(int i) {
        return values[i];
    }
    
    @Override
    public boolean isSparse(){
        return false;
    }
    
    @Override
    public int[] getNonEmptyIndex(){
        int idx[] = new int[values.length];
        for(int i=0; i<values.length; i++)
            idx[i] = i;
        return idx;
    }
}
