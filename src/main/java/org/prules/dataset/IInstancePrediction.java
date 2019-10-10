/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

/**
 * @author Marcin
 */
public interface IInstancePrediction extends IGenericContainer {
    double getLabel();

    void setLabel(double value);

    double[] getConfidence();

    void setConfidence(double[] confidence);
}
