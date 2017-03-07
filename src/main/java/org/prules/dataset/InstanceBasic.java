package org.prules.dataset;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marcin
 */
public class InstanceBasic extends GenericContainer implements Instance {
    
    protected InstanceBasic(){
        this(null,null);
    }

    protected InstanceBasic(Vector vector, IInstanceLabels labels){
        this.put(Const.VECTOR, vector);
        this.put(Const.LABELS, labels);
    }
    
    @Override
    public IInstanceLabels getLabels() {
        return this.get(Const.LABELS);
    }

    @Override
    public void setLabels(IInstanceLabels labels) {
        this.put(Const.LABELS, labels );
    }

   
    @Override
    public IInstancePrediction getPrediction() {
        return (IInstancePrediction)this.get(Const.PREDICTION);
    }

    @Override
    public void setPrediction(IInstancePrediction prediction) {
        this.put(Const.PREDICTION, prediction);
    }

    @Override
    public Vector getVector() {
        return (Vector)this.get(Const.VECTOR);
    }

    @Override
    public void setVector(Vector vector) {
        this.put(Const.VECTOR, vector);
    }        
    
}
