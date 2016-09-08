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
public interface IValuesStoreInstance extends IValuesStore {

    IVector getVector();

    void setVector(IVector vector);

    IValuesStoreLabels getLabels();

    void setLabels(IValuesStoreLabels vector);

    void setPrediction(IValuesStorePrediction prediction);

    IValuesStorePrediction getPrediction();

}
