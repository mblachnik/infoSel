/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This operator performs a loop over batch attribute, such that all samples with given
 * batch value are delivered to the input subprocess where a prediction model is trained.
 * As an output the operator returns a local ensemble model with models defined for each batch value, where batch is defined by a pair of prototypes.
 *
 * @author Marcin
 */
public class BatchLoopOperator extends OperatorChain {
    /**
     * Training data returned by the NearestPrototypesOperator.
     */
    private final InputPort exampleSetInputPort = getInputPorts().createPort("example Set", ExampleSet.class);
    /**
     * PrototypesEnsembleModel returned by the NearestPrototypesOperator
     */
    private final InputPort prototypesEnsemble = getInputPorts().createPort("prototype model", PrototypesEnsembleModel.class);
    /**
     * training data delivered to the subProcess
     */
    private final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("example Set");
    /**
     * PredictionModel obtained after training the prediction model
     */
    private final InputPort predictionModelInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("prediction model", PredictionModel.class);
    /**
     * THe final prediction model ensemble
     */
    private final OutputPort finalModelOutputPort = getOutputPorts().createPort("prediction model");

    /**
     * Constructor for the BatchLoopOperator
     *
     * @param description
     */
    public BatchLoopOperator(OperatorDescription description) {
        super(description, "Train prediction model");
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleInnerSourcePort);
        getTransformer().addGenerationRule(finalModelOutputPort, PrototypesEnsemblePredictionModel.class);
    }

    /**
     * Main computation method. It takes input example set, generates subsets based
     * on batch attribute and in a loop each batch is delivered to the subProcess
     * After each subProcess execution all prediction models are collected in a map,
     * and returned as a prediction model.
     *
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        Map<Long, PredictionModel> modelsMap;
        ExampleSet exampleSet = exampleSetInputPort.getData(ExampleSet.class);
        PrototypesEnsembleModel inputModel = prototypesEnsemble.getData(PrototypesEnsembleModel.class);
        Attribute attr = exampleSet.getAttributes().findRoleBySpecialName(Attributes.BATCH_NAME).getAttribute();
        //Map which cantons list of elements which belong to given batch
        Map<Long, IDataIndex> pairsMap = new HashMap<>();
        //Get all possible pairs and samples which belong to given pair
        IDataIndex idx;
        int exampleIndex = 0;
        for (Example e : exampleSet) {
            double pairId = e.getValue(attr);
            if (pairsMap.containsKey((long) pairId)) {
                idx = pairsMap.get((long) pairId);
            } else {
                idx = new DataIndex(exampleSet.size());
                idx.setAllFalse();
                pairsMap.put((long) pairId, idx);
            }
            idx.set(exampleIndex, true);
            exampleIndex++;
        }
        modelsMap = new HashMap<>();
        for (Entry<Long, NearestPrototypesOperator.PairedTuple> entry : inputModel.getSelectedPairs().entrySet()) {
            Long pair = entry.getKey();
            if (pairsMap.containsKey(pair)) {
                idx = pairsMap.get(pair);
            } else {
                continue;
            }
            //Select samples from given batch
            SelectedExampleSet selectedExampleSet = new SelectedExampleSet(exampleSet);
            selectedExampleSet.setIndex(idx);
            //And deliver these samples to train a model
            exampleInnerSourcePort.deliver(selectedExampleSet);
            //Execute inner process (train the model)
            getSubprocess(0).execute();
            inApplyLoop();

            PredictionModel model = predictionModelInnerSourcePort.getData(PredictionModel.class);
            modelsMap.put(pair, model);
        }

        PredictionModel finalModel = new PrototypesEnsemblePredictionModel(inputModel, modelsMap, exampleSet, ExampleSetUtilities.SetsCompareOption.ALLOW_SUPERSET, ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS);
        finalModelOutputPort.deliver(finalModel);
    }
}
