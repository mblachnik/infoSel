/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Interface for any single datarow container, it allows to store any complex instance. An instance can contain any complex elements like labels, predictions, vector etc. These elements are called by String names
 * @author Marcin
 */
public interface IGenericContainer extends Serializable {

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
    
    Object put(String key, Object value);
    void set(String key, Object value);
    
    
    /**
     * Write an element as int value
     * @param key - element name
     * @param value -      
     */
    void setValueAsInt(String key, int value);

    /**
     * Write an element as double value
     * @param key - element name
     * @param value -      
     */
    void  setValueAsDouble(String key, double value);
    
    /**
     * Write an element as long value
     * @param key - element name
     * @param value -      
     */
    void setValueAsLong(String key, long value);
    
    /**
     * Write an element as float
     * @param key - element name
     * @param value -      
     */
    void setValueAsFloat(String key, float value);
        
    /**
     * Write an element as String
     * @param key - element name
     * @param value -      
     */
    void setValueAsString(String key, String value);
    
    /**
     * Returns number of elements in the container
     * @return 
     */
    int size();
    
    /**
     * Returns list of keys of elements in the container
     * @return 
     */
    Set<String> keySet();
    
    /**
     * returns true if container is empty
     * @return 
     */
    boolean isEmpty();
    
}
