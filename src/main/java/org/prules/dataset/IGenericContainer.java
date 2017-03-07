/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface for any single datarow container, it allows to store any complex instance. An instance can contain any complex elements like labels, predictions, vector etc. These elements are called by String names
 * @author Marcin
 */
public interface IGenericContainer extends Map<String, Object>,Serializable {

    /**
     * Read instance element by name of this element
     * @param <T> - type of returned element
     * @param s - element name
     * @return 
     */
    <T> T get(String s); 
    
    /**
     * Read an element as int value
     * @param s - element name
     * @return 
     */
    int getValueAsInt(String s);

    /**
     * Read an element as double value
     * @param s - element name
     * @return 
     */
    double getValueAsDouble(String s);
    
    /**
     * Read an element as long value
     * @param s - element name
     * @return 
     */
    long getValueAsLong(String s);
    
    /**
     * Read an element as float
     * @param s - element name
     * @return 
     */
    float getValueAsFloat(String s);
        
    /**
     * Read an element as String
     * @param s - element name
     * @return 
     */
    String getValueAsString(String s);
        
}
