/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public interface IValuesStore extends Map<String, Object>,Serializable {

    <T> T get(String s);
    
    int getValueAsInt(String s);

    double getValueAsDouble(String s);
    
    long getValueAsLong(String s);
    
    float getValueAsFloat(String s);
        
    String getValueAsString(String s);
        
}
