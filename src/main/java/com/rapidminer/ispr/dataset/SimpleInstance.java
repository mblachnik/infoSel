/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;


/**
 * Class representing single prototype such as cluster center or single instance
 * @author Marcin
 */
public class SimpleInstance implements Instance {    
    private double[] values;    

    public SimpleInstance(int n) {
        this.values = new double[n];
    }
    
    public SimpleInstance(double[] values) {
        setValues(values);
    }

    public SimpleInstance(Example example, Attributes attributes) {
        setValues(example, attributes);
    }
    
    public SimpleInstance(Example example) {
        setValues(example);
    }
    
    @Override
    public double[] getValues() {
        return values;
    }        

    @Override
    public void setValues(double[] prototypeValues) {
        this.values = prototypeValues;
    }
    
    @Override
    public void setValues(Example example, Attributes attributes) {
        int i = 0;
        for (Attribute a : attributes){            
            this.values[i] = example.getValue(a);
            i++;
        }
    }
    
    @Override
    public void setValues(Example example) {
       setValues(example,example.getAttributes());
    }
}
