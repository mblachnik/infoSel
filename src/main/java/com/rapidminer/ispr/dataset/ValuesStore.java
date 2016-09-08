/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Marcin
 */
public class ValuesStore extends HashMap<String,Object> implements IValuesStore {
    
    public ValuesStore(Map<String, Object> values) {
        super(values);
    }

    public ValuesStore() {        
    }

        
    @Override
    public int getValueAsInt(String s) {
        return (Integer) this.get(s);
    }

    @Override
    public double getValueAsDouble(String s) {
        return (Double)this.get(s);
    }
    
    @Override
    public String getValueAsString(String s) {
        return (String)this.get(s);
    }

    @Override
    public <T> T get(String s) {
        return (T)super.get(s);
    }

    @Override
    public long getValueAsLong(String s) {
        return (Long)super.get(s);
    }

    @Override
    public float getValueAsFloat(String s) {
        return (Float)super.get(s);
    }
}
