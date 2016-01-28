/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * ISThresholdLinearDecisionFunction is an implementation of IISThresholdDecisionFunction. It represents
 * decision function which calculates the difference between real (R) and predicted (P) value of given instance(R-P)
 * then checks if the error is greater then threshold. If so returns 1
 * @author Marcin
 */

public class ISThresholdLinearDecisionFunction implements IISThresholdDecisionFunction {
    
    private double threshold = 0;    
    private boolean blockInit = false;    


    public ISThresholdLinearDecisionFunction() {
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
    public void init(ISPRGeometricDataCollection<Number> samples){                
    }
            
    @Override
    public double getValue(double real, double predicted, double[] values) {
        double value = Math.abs(real - predicted) > threshold ? 1 : 0;
        return value;
    }
    
    @Override
    public double getValue(double real, double predicted, Example example){
        double[] values = new double[example.getAttributes().size()];
        KNNTools.extractExampleValues(example, values);
        return getValue(real, predicted, values);
    }
        
    @Override
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    @Override
    public String name() {
        return "ThresholdLinearLoss";
    }

    @Override
    public String description() {
        return "Y=(R-P) > Thres";
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
