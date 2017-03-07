/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.clustering.ClusterModel;

/**
 * General base cluster model returned as IOObject for clustering methods implemented in this library
 * @author Marcin
 */
public abstract class IS_ClusterModel extends ClusterModel{
    
    public IS_ClusterModel(ExampleSet exampleSet, int k, boolean addClusterAsLabel, boolean removeUnknown) {
        super(exampleSet, k, addClusterAsLabel, removeUnknown);
    }
    
    /**
     * Apply clustering model and label examples. If setClusterAssigment is set to true it also 
     * calculates statistics required by RapidMiner clustering based models.
     * Usually apply method from ClusterModel calls this method with setClusterAssigment set to false
     * The setClusterAssignments derives from RapidMiner clusterModel support.
     *
     * @param trainingSet
     * @param setClusterAssignments if true then performs some of the steps required by RapidMiner base model
     * @return
     */    
    public abstract ExampleSet apply(ExampleSet trainingSet, boolean setClusterAssignments);
}
