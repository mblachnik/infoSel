/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.rapidminer.operator.ports.metadata.CollectionMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

/**
 * This operator performs a loop over batch attribute, such that all samples with given 
 * batch value are delivered to the input subprocess where a instance selection is performed.
 * As an output the operator returns collection of exampleSets returned in each instance selection subprocess
 * 
 * @author Marcin
 */
public class BatchLoopOperator extends OperatorChain {
    /**
     * Training data returned by the NearestPrototypesOperator.
     */
    private final InputPort exampleSetInputPort = getInputPorts().createPort("example Set",ExampleSet.class);
    /**
     * training data delivered to the subprocess
     */
    private final OutputPort exampleInnerSourcePort   = getSubprocess(0).getInnerSources().createPort("example Set");
    /**
     * PredictionModel obtained after training the prediction model 
     */
    private final InputPort selectedExampleSetInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("prediction model",ExampleSet.class);
    /**
     * THe final prediction model ensemble
     */
    private final OutputPort exampleSetCollection = getOutputPorts().createPort("collection");
    
    /**
     * Constructor for the BatchLoopOperator
     * @param description 
     */
    public BatchLoopOperator(OperatorDescription description) {
        super(description,"Train Prediction Model");
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleInnerSourcePort);
        //getTransformer().addGenerationRule(exampleSetCollection,IOObjectCollection.class);
        getTransformer().addRule(new MDTransformationRule() {

            @Override
            public void transformMD() {
                MetaData md = selectedExampleSetInnerSourcePort.getMetaData();
                exampleSetCollection.deliverMD(new CollectionMetaData(md));
            }
        });
    }

    /**
     * Main computation method. It takes input example set, generates subsets based 
     * on batch attribute and in a loop each batch is delivered to the subprocess
     * After each subprocess execution all selectedSamples are collected
     * and returned as a IOObjectCollection so we can manually investigate each subset, but also single append operator
     * combines all selected instances into single ExampleSet
     * @throws OperatorException 
     */
    @Override
    public void doWork() throws OperatorException {
        ExampleSet exampleSet = exampleSetInputPort.getData(ExampleSet.class);
        Attribute attr = exampleSet.getAttributes().findRoleBySpecialName(Attributes.BATCH_NAME).getAttribute();
        //Map which contons list of elements which belong to given batch
        Map<Double,IDataIndex> batchIndexMap = new HashMap<>();
        //Get all possible pairs and samples which belong to given pair

        int exampleIndex = 0;
        for(Example e : exampleSet){
            double batchVal = e.getValue(attr);
            IDataIndex idx = batchIndexMap.computeIfAbsent(batchVal, val -> {
                IDataIndex index = new DataIndex(exampleSet.size());
                index.setAllFalse();
                return index;
            });
            idx.set(exampleIndex,true);            
            exampleIndex++;
        }
        IOObjectCollection<ExampleSet> resultSets = new IOObjectCollection<>();
        for (IDataIndex  dataIndex : batchIndexMap.values()){
            //Select samples from given batch
            SelectedExampleSet selectedExampleSet = new SelectedExampleSet(exampleSet,dataIndex);
            //And deliver these samples to train a model
            exampleInnerSourcePort.deliver(selectedExampleSet);
            //Execute inner process (train the model)
            getSubprocess(0).execute();
            inApplyLoop();
            
            ExampleSet selectedSet = selectedExampleSetInnerSourcePort.getData(ExampleSet.class);
            resultSets.add(selectedSet);
        }
        exampleSetCollection.deliver(resultSets);
        exampleSetCollection.deliverMD(new CollectionMetaData(resultSets,false));
    }

}
