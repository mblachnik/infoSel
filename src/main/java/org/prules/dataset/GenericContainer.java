/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin
 */
public class GenericContainer extends HashMap<String, Object> implements IGenericContainer {

    GenericContainer(Map<String, Object> values) {
        super(values);
    }

    public GenericContainer(IGenericContainer values) {
        Set<String> keys = values.keySet();
        for (String key : keys) {
            super.put(key, values.get(key));
        }
    }

    GenericContainer() {
    }


    @Override
    public int getValueAsInt(String s) {
        return (Integer) this.get(s);
    }

    @Override
    public double getValueAsDouble(String s) {
        return (Double) this.get(s);
    }

    @Override
    public String getValueAsString(String s) {
        return (String) this.get(s);
    }

    @Override
    public <T> T get(String s) {
        return (T) super.get(s);
    }

    @Override
    public long getValueAsLong(String s) {
        return (Long) super.get(s);
    }

    @Override
    public float getValueAsFloat(String s) {
        return (Float) super.get(s);
    }

    @Override
    public void set(String key, Object value) {
        super.put(key, value);
    }

    @Override
    public void setValueAsInt(String key, int value) {
        super.put(key, value);
    }

    @Override
    public void setValueAsDouble(String key, double value) {
        super.put(key, value);
    }

    @Override
    public void setValueAsLong(String key, long value) {
        super.put(key, value);
    }

    @Override
    public void setValueAsFloat(String key, float value) {
        super.put(key, value);
    }

    @Override
    public void setValueAsString(String key, String value) {
        super.put(key, value);
    }
}
