/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import java.io.Serializable;

/**
 *
 * @author Marcin
 */
public interface IStoredValues extends Serializable {

    double getId();

    double getLabel();   
    
    double getWeight();

    double getCluster();   

    void setId(double id);

    void setLabel(double label);    
    
    void setWeight(double weight);

    void setCluster(double cluster);   
    
    double getValue(String s);  
    
    void setValue(String s, double value);  
                        
}
