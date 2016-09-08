/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

/**
 *
 * @author Marcin
 */
public interface IValuesStorePrediction extends IValuesStore {
    double getLabel();
    
    void setLabel(double value);
    
    double[] getConfidence();
    
    void setConfidence(double[] confidence);
}
