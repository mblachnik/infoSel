package com.rapidminer.ispr.dataset;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marcin
 */
public class ValuesStoreInstance extends ValuesStore implements IValuesStoreInstance {
    
    protected ValuesStoreInstance(){
        this(null,null);
    }

    protected ValuesStoreInstance(IVector vector, IValuesStoreLabels labels){
        this.put(Const.VECTOR, vector);
        this.put(Const.LABELS, labels);
    }
    
    @Override
    public IValuesStoreLabels getLabels() {
        return this.get(Const.LABELS);
    }

    @Override
    public void setLabels(IValuesStoreLabels labels) {
        this.put(Const.LABELS, labels );
    }

   
    @Override
    public IValuesStorePrediction getPrediction() {
        return (IValuesStorePrediction)this.get(Const.PREDICTION);
    }

    @Override
    public void setPrediction(IValuesStorePrediction prediction) {
        this.put(Const.PREDICTION, prediction);
    }

    @Override
    public IVector getVector() {
        return (IVector)this.get(Const.VECTOR);
    }

    @Override
    public void setVector(IVector vector) {
        this.put(Const.VECTOR, vector);
    }        
    
}
