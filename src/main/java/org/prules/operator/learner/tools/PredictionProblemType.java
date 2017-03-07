/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

/**
 *
 * @author Marcin
 */
public enum PredictionProblemType {    
    CLASSIFICATION(1),
    REGRESSION(2),
    ANY(3);
    
    private final int identifier;
    
    PredictionProblemType(int identifier){
        this.identifier = identifier;
    }
    
    public boolean checkCompatability(PredictionProblemType type){
        return (type.identifier & this.identifier)>0;
    }
}
