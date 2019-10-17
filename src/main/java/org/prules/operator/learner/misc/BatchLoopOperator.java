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
 * @author Marcin, Paweł
 */
public class BatchLoopOperator extends OperatorChain {
    //<editor-fold desc="Static data" defaultState="collapsed" >
    private static final String PORT_INPUT_EXAMPLE = "example set";
    private static final String PORT_INPUT_MODEL = "prototype model";
    private static final String PORT_INNER_INPUT_EXAMPLE = "example set";
    private static final String PORT_INNER_INPUT_MODEL = "prediction model";
    private static final String PORT_OUTPUT_MODEL = "prediction model";
    //</editor-fold>

    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Training data returned by the NearestPrototypesOperator.
     */
    private final InputPort exampleSetInputPort = getInputPorts().createPort(PORT_INPUT_EXAMPLE, ExampleSet.class);
    /**
     * PrototypesEnsembleModel returned by the NearestPrototypesOperator
     */
    private final InputPort prototypesEnsemble = getInputPorts().createPort(PORT_INPUT_MODEL, PrototypesEnsembleModel.class);
    /**
     * Training data delivered to the subProcess
     */
    private final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort(PORT_INNER_INPUT_EXAMPLE);
    /**
     * PredictionModel obtained after training the prediction model
     */
    private final InputPort predictionModelInnerSourcePort = getSubprocess(0).getInnerSinks().createPort(PORT_INNER_INPUT_MODEL, PredictionModel.class);
    /**
     * The final prediction model ensemble
     */
    private final OutputPort finalModelOutputPort = getOutputPorts().createPort(PORT_OUTPUT_MODEL);
    /**
     * input example set
     */
    private ExampleSet exampleSet;
    /**
     * Input model
     */
    private PrototypesEnsembleModel inputModel;
    /**
     * List of attributes
     */
    private Attribute attr;

    /**
     * Map of pair Id to model
     */
    private Map<Long, PredictionModel> modelsMap;//Map which cantons list of elements which belong to given batch
    /**
     * Map of pair Id to Data Index
     */
    private Map<Long, IDataIndex> pairsMap;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

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
    //</editor-fold>

    // <editor-fold desc="Set up stage" defaultState="collapsed" >

    /**
     * Sets up configuration variables for process computation
     *
     * @throws OperatorException
     */
    private void setup() throws OperatorException {
        //Get example set
        exampleSet = exampleSetInputPort.getData(ExampleSet.class);
        //Get model
        inputModel = prototypesEnsemble.getData(PrototypesEnsembleModel.class);
        //Get attributes
        attr = exampleSet.getAttributes().findRoleBySpecialName(Attributes.BATCH_NAME).getAttribute();
        //Initialize maps
        modelsMap = new HashMap<>();
        pairsMap = new HashMap<>();
    }
    //</editor-fold>

    //<editor-fold desc="Compute stage" defaultState="collapsed" >

    /**
     * Gets all possible pairs and samples which belong to given pair
     */
    private void mapPairs() {
        int exampleIndex = 0;
        IDataIndex idx;
        for (Example example : exampleSet) {
            double pairId = example.getValue(attr);
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
    }

    /**
     * Train experts in inner sub process
     *
     * @throws OperatorException
     */
    private void trainExperts() throws OperatorException {
        IDataIndex idx;
        for (Entry<Long, NearestPrototypesOperator.PrototypeTuple> entry : inputModel.getSelectedPairs().entrySet()) {
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
    }
    //</editor-fold>

    //<editor-fold desc="Delivery stage" defaultState="collapsed" >

    /**
     * Method run at the end, delivers created model from operator
     */
    private void deliver() {
        PredictionModel finalModel = new PrototypesEnsemblePredictionModel(inputModel, modelsMap, exampleSet,
                ExampleSetUtilities.SetsCompareOption.ALLOW_SUPERSET, ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS);
        finalModelOutputPort.deliver(finalModel);
    }
    //</editor-fold>

    //<editor-fold desc="Operator methods" defaultState="collapsed" >

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
        setup();
        mapPairs();
        trainExperts();
        deliver();
    }
    //</editor-fold>
}
