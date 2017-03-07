/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.tools;

import org.prules.dataset.Vector;

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
    Vector modify(Vector instance);
    
}
