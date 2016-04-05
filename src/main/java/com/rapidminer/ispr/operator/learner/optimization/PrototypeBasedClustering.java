/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.clustering.ClusterModel;

/**
 *
 * @author Marcin
 */
public class PrototypeBasedClustering extends ClusterModel {

    public PrototypeBasedClustering(ExampleSet exampleSet, int k, boolean addClusterAsLabel, boolean removeUnknown) {
        super(exampleSet, k, addClusterAsLabel, removeUnknown);
    }

   
    
}
