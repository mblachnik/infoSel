/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.tools;

import com.rapidminer.ispr.dataset.IVector;

/**
 * An interface for all @{see Vector} modifier, it is used to modify instance values on the fly
 * @author Marcin
 */
public interface InstanceModifier {

    /**
     * Method which is responsible for instance modification. 
     * Note that modifications don't affect input instance
     * @param instance
     * @return 
     */
    IVector modify(IVector instance);
    
}
