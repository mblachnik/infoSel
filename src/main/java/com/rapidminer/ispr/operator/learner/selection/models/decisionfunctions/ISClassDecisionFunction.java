/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.IValuesStoreInstance;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;

/**
 * ClassDecisionFunction is an implementation of IISDecisionFunction. It represents
 * a binary value decision function which checks if label of real example is 
 * identical with predicted label. real == predicted ? 0 : 1;
 * @author Marcin
 */
public class ISClassDecisionFunction extends AbstractISDecisionFunction implements IISDecisionFunction {   
    private boolean blockInit = false;    
    @Override
    public void setBlockInit(boolean block) {        
        blockInit = block;
    }

    @Override
    public boolean isBlockInit() {        
        return blockInit;
    }
   
    @Override
    public void init(ExampleSet exampleSet, DistanceMeasure distance){
        
    }
    
    @Override
    public void init(ISPRGeometricDataCollection<IValuesStoreLabels> samples) {
        
    }
    
    @Override
    public double getValue(IValuesStoreInstance instance){        
        double real = instance.getLabels().getLabel();
        double predicted = instance.getPrediction().getLabel();
        return  real == predicted ? 0 : 1;
    }    
    
    @Override
    public String name() {
        return "Class loss";
    }

    @Override
    public String description() {
        return "R == P ? 0 : 1";
    }
    
    @Override
    public boolean supportedLabelTypes(OperatorCapability capabilities){
        switch (capabilities){
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
                return true;
        }
        return false;
    }

}
