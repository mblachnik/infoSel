/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;


/**
 *
 * @author Marcin
 */
public class Prototype {    
    private double[] values;    

    public Prototype(int n) {
        this.values = new double[n];
    }
    
    public Prototype(double[] values) {
        this.values = values;
    }

    public Prototype(Example example, Attributes attributes) {
        int i = 0;
        for (Attribute a : attributes){            
            this.values[i] = example.getValue(a);
            i++;
        }
    }
    
    public double[] getValues() {
        return values;
    }        

    public void setValues(double[] prototypeValues) {
        this.values = prototypeValues;
    }
    
    public void setValues(Example example, Attributes attributes) {
        int i = 0;
        for (Attribute a : attributes){            
            this.values[i] = example.getValue(a);
            i++;
        }
    }
    
    
}
