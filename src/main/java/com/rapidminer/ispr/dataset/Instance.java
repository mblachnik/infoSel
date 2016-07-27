/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;

/**
 *
 * @author Marcin
 */
public interface Instance {

    double[] getValues();

    void setValues(double[] prototypeValues);

    void setValues(Example example, Attributes attributes);

    void setValues(Example example);
    
}
