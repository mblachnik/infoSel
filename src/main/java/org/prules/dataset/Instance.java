/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

/**
 * An IGenericContainer with extra tools for storing datamining instances
 * @author Marcin
 */
public interface Instance extends IGenericContainer {

    /**
     * Get attributes of an instance
     * @return 
     */
    Vector getVector();

    /**
     * Set attributes of an instance
     * @param vector 
     */
    void setVector(Vector vector);

    /**
     * Get properties of an instance - all extra attributes
     * @return 
     */
    IInstanceLabels getLabels();

    /**
     * Set properties of an instance - these are all extra descriptors of an instance
     * @param vector 
     */
    void setLabels(IInstanceLabels vector);

    /**
     * Set prediction results of an instance
     * @param prediction 
     */
    void setPrediction(IInstancePrediction prediction);

    /**
     * Read prediction results of an instance
     * @return 
     */
    IInstancePrediction getPrediction();

}
