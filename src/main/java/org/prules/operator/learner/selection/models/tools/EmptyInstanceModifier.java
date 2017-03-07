/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.tools;

import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class EmptyInstanceModifier implements InstanceModifier{

    @Override
    public Vector modify(Vector instance) {
        return instance;
    }        
}
