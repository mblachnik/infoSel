/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rapidminer.ispr.operator.learner.feature.selection;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.features.selection.AttributeWeightSelection;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPorts;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.UndefinedParameterError;

import java.util.Set;

/**
 *
 * @author Marcin
 */
public class FeatureSelectionOperator{
    /*extends  AbstractPRulesFeatureSelection{
    private InputPort  weightsInputPort = getInputPorts().createPort("attribute weights", AttributeWeights.class);
    private OutputPort weightsOutputPort = getOutputPorts().createPort("attribute weights");
    private OutputPort featureSelectionModelPort = getOutputPorts().createPort("Feature selection model");

    public FeatureSelectionOperator(OperatorDescription description) {
        super(description);
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, exampleSetOutputPort, SetRelation.SUBSET));
        getTransformer().addPassThroughRule(weightsInputPort, weightsOutputPort);
        getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, featureSelectionModelPort, FeatureSelectionModel.class));
        exampleSetInputPort.addPrecondition(new SimplePrecondition(exampleSetInputPort, getRequiredMetaData()));
    }

    @Override
    public void doWork() throws OperatorException {
        super.doWork();
        //AttributeWeights attributeWeights = processWeights((AttributeWeights)weightsInputPort.getData());
        AttributeWeights attributeWeights = (AttributeWeights)weightsInputPort.getData();
        weightsOutputPort.deliver(attributeWeights);
        ExampleSet exampleSet = (ExampleSet)exampleSetInputPort.getData();
        FeatureSelectionModel featureSelectionModel = new FeatureSelectionModel(exampleSet,attributeWeights);
        featureSelectionModelPort.deliver(featureSelectionModel);
        if (exampleSetOutputPort.isConnected()){ //do selection only if connected
            ExampleSet selectedExampleSet = featureSelectionModel.apply(exampleSet);
            exampleSetOutputPort.deliver(selectedExampleSet);
        } else exampleSetOutputPort.deliver(null);

    }

    private AttributeWeights processWeights(AttributeWeights weights){
        AttributeWeights newWeights = (AttributeWeights)weights.clone();
        Set<String> attributeNames = newWeights.getAttributeNames();
        for(String attributeName : attributeNames){
            double w = newWeights.getWeight(attributeName);
            if (w > 0)
                newWeights.setWeight(attributeName, 1);
        }
        return newWeights;
    }

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData emd) throws UndefinedParameterError {
        try {
            return new MDInteger(((ExampleSet) exampleSetInputPort.getData()).getExampleTable().size());
        } catch (OperatorException ex) {
            return new MDInteger();
        }
    }

    public InputPort getWeightsInputPort() {
        return weightsInputPort;
    }

    public OutputPort getWeightsOutputPort() {
        return weightsOutputPort;
    }
    * *?
    */
}
