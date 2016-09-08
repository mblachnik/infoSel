/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.IValuesStoreInstance;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IVector;

/**
 * ISThresholdRelativeLinearDecisionFunction is an implementation of IISThresholdDecisionFunction. It represents
 * decision function which calculates the difference between real (R) and predicted (P) value of given instance, and 
 * calculates relative error (R-P)/R then checks if the error is greater then threshold. If so returns 1
 * @author Marcin
 */
public class ISThresholdRelativeLinearDecisionFunction implements IISThresholdDecisionFunction {
    
    private double threshold = 0;    
    private boolean blockInit = false;    


    public ISThresholdRelativeLinearDecisionFunction() {        
    }
    
    @Override
    public void setBlockInit(boolean block) {        
        blockInit = block;
    }

    @Override
    public boolean isBlockInit() {        
        return blockInit;
    }
    
    @Override
    public void init(ExampleSet exampleSet, DistanceMeasure distance) {
    }

    @Override
    public void init(ISPRGeometricDataCollection<IValuesStoreLabels> samples){                
    }    
    
    @Override
    public double getValue(IValuesStoreInstance instance){              
        double real = instance.getLabels().getLabel();
        double predicted = instance.getPrediction().getLabel();        
        return  Math.abs(real - predicted) / Math.abs(real) > threshold ? 1 : 0;
    } 
       
    @Override
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    @Override
    public String name() {
        return "Threshold Relative Linear Loss";
    }

    @Override
    public String description() {
        return "Y=(R-P/R) > Thresh";
    }
    
    @Override
    public boolean supportedLabelTypes(OperatorCapability capabilities){
        switch (capabilities){
            case NUMERICAL_LABEL:
                return true;
        }
        return false;
    }
}
