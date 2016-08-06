/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Marcin
 */
public class SimpleValuesStore implements IStoredValues {
    Map<String, Double> values = new HashMap<>();

    public SimpleValuesStore(Map<String,Double> values) {
        for(Entry<String, Double> entry : values.entrySet()){
            this.values.put(entry.getKey(), entry.getValue());
        }
    }
    
    public SimpleValuesStore() {
        values = new HashMap<>();
    }
    

    @Override
    public double getLabel() {
        return values.get(StoredValuesHelper.LABEL);
    }

    @Override
    public void setLabel(double label) {
        values.put(StoredValuesHelper.LABEL,label);
    }

    @Override
    public double getId() {
        return values.get(StoredValuesHelper.ID);
    }

    @Override
    public void setId(double id) {
        values.put(StoredValuesHelper.ID,id);
    }
    
    @Override
    public double getValue(String name) {        
        return values.get(name);
    }    

    @Override
    public double getWeight() {
        return values.get(StoredValuesHelper.WEIGHT);
    }

    @Override
    public double getCluster() {
        return values.get(StoredValuesHelper.CLUSTER);
    }

    @Override
    public void setWeight(double weight) {
        values.put(StoredValuesHelper.WEIGHT, weight);
    }

    @Override
    public void setCluster(double cluster) {
        values.put(StoredValuesHelper.CLUSTER, cluster);
    }

    @Override
    public void setValue(String s, double value) {
        values.put(s,value);
    }
}
