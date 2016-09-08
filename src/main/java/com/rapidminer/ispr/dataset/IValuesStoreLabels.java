/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Example;
import java.io.Serializable;

/**
 *
 * @author Marcin
 */
public interface IValuesStoreLabels extends IValuesStore {

    int getId();

    double getLabel();   
    
    double getWeight();

    double getCluster();           

    void setId(int id);

    void setLabel(double label);    
    
    void setWeight(double weight);

    void setCluster(double cluster);   
    
    void set(Example example);
                        
}
