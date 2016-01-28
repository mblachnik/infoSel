/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rapidminer.ispr.operator.learner.feature.selection;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.AbstractPRulesBasicOperator;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;

/**
 *
 * @author Marcin
 */
public abstract class AbstractPRulesFeatureSelection extends AbstractPRulesBasicOperator{

    private OutputPort weightsOutputPort = getOutputPorts().createPort("attribute weights");


    /**
     * 
     * @param description
     */
    public AbstractPRulesFeatureSelection(OperatorDescription description) {
        super(description);

        getTransformer().addGenerationRule(weightsOutputPort, AttributeWeights.class);
        exampleSetInputPort.addPrecondition(new SimplePrecondition(exampleSetInputPort, getRequiredMetaData()));
    }

    /**
     * 
     * @param trainingSet
     * @throws OperatorException
     */
    @Override
    public void executeOperator(ExampleSet trainingSet) throws OperatorException {    
        AttributeWeights attributeWeights = doSelection(trainingSet);
        weightsOutputPort.deliver(attributeWeights);
    }

    /**
     * 
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    public abstract AttributeWeights doSelection(ExampleSet exampleSet) throws OperatorException;
    
    
    private ExampleSetMetaData getRequiredMetaData() {
        ExampleSetMetaData condition = new ExampleSetMetaData();
        //condition.addAttribute(new AttributeMetaData);
        return condition;
    }



}
