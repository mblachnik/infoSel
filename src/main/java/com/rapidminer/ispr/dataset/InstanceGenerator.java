/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class InstanceGenerator {

    public static Instance generateInstance(Example example) {
        return new SimpleInstance(example);
    }    
    
    public static Instance generateInstance(double[] values) {
        return new SimpleInstance(values);
    }
    
    public static Instance generateInstance(Example example, List<Attribute> attributes) {
        return new SimpleInstance(example,attributes);
    }
    
    public static Instance generateInstance(Example example, Attribute[] attributes) {
        return new SimpleInstance(example,attributes);
    }
    
     public static Instance generateInstance(ExampleSet exampleSet) {
        return new SimpleInstance(exampleSet.getAttributes().size());
    }
}
