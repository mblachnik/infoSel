/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.dataset.IValuesStoreInstance;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;

/**
 * IISAcceptanceFunction is an interface for an DecisionFunction which is used 
 * by instance selection methods as a decision function to accept or reject particular instance.
 * This allows to generalize instance selection methods to be able to deal also with regression problems
 * @author Marcin
 */
public interface IISDecisionFunction extends Cloneable{
    public void init(ExampleSet exampleSet, DistanceMeasure distance); 
    public void init(ISPRGeometricDataCollection<IValuesStoreLabels> samples);
    public double getValue(IValuesStoreInstance instance);    
    public String name();
    public String description();
    public void setBlockInit(boolean block);
    public boolean isBlockInit();
    public boolean supportedLabelTypes(OperatorCapability capabilities);
}
