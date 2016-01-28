/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;

/**
 *
 * @author Marcin
 */
public enum TaskType {    
    CLASSIFICATION(1),
    REGRESSION(2),
    ANY(3);
    
    private final int identifier;
    
    TaskType(int identifier){
        this.identifier = identifier;
    }
    
    public boolean checkCompatability(TaskType type){
        return (type.identifier & this.identifier)>0;
    }
}
