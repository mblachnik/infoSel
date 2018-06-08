/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import com.rapidminer.example.Example;
import java.io.Serializable;

/**
 * Interface for storing labels of an instance. In fact labels are any attributes or properties associated with an instance describing its properties
 * It can be any complex data structure but for simplicity this interface provides a set of basic methods for access of "classical" properties
 * @author Marcin
 */
public interface IInstanceLabels extends IGenericContainer {

    /**
     * Read enumerator of an instance
     * @return 
     */
    int getId();

    /**
     * Read label as double value
     * @return 
     */
    double getLabel();   
    
    /**
     * Read instance weight
     * @return 
     */
    double getWeight();

    /**
     * Read cluster label or identifier of clustering problem
     * @return 
     */
    double getCluster();           

    /**
     * Set instance id
     * @param id 
     */
    void setId(int id);

    /**
     * Set label
     * @param label 
     */
    void setLabel(double label);    
    
    /**
     * Set instance weight
     * @param weight 
     */
    void setWeight(double weight);

    /**
     * Set instsance cluster
     * @param cluster 
     */
    void setCluster(double cluster);   
    
    /**
     * Method which reads all of the instance properties from RapidMiner Example
     * @param example 
     */
    void set(Example example);
    
    /**
     * Set value
     * @param name
     * @param value
     */
    @Override
    void set(String name, Object value);
}
