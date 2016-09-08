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
public class ValuesStorePrediction extends ValuesStore implements IValuesStorePrediction{
    
    protected ValuesStorePrediction(){
        this(Double.NaN, null);
    }

    protected ValuesStorePrediction(double prediction, double[] confidence){
        this.put(Const.LABEL, prediction);
        this.put(Const.CONFIDENCE, confidence);
    }
    
    @Override
    public double getLabel() {
        return (Double)this.get(Const.LABEL);
    }

    @Override
    public void setLabel(double value) {
        this.put(Const.LABEL,value);
    }

    @Override
    public double[] getConfidence() {
        return (double[])this.get(Const.CONFIDENCE);
    }

    @Override
    public void setConfidence(double[] confidence) {
        this.put(Const.CONFIDENCE,confidence);
    }

    
}
