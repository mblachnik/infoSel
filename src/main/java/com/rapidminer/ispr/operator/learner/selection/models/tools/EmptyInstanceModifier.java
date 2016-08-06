/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.tools;

import com.rapidminer.ispr.dataset.Instance;

/**
 *
 * @author Marcin
 */
public class EmptyInstanceModifier implements InstanceModifier{

    @Override
    public Instance modify(Instance instance) {
        return instance;
    }        
}
