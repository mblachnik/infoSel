/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.prototype;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import org.prules.operator.learner.prototype.model.AbstractBatchLoopModel;
import org.prules.operator.learner.prototype.model.BasicBatchLoopModel;
import org.prules.operator.learner.prototype.model.interfaces.BatchLoopInterface;

/**
 * This operator performs a loop over batch attribute, such that all samples with given
 * batch value are delivered to the input subprocess where a prediction model is trained.
 * As an output the operator returns a local ensemble model with models defined for each batch value, where batch is defined by a pair of prototypes.
 *
 * @author Marcin, Paweł
 */
public class BatchLoopOperator extends OperatorChain implements BatchLoopInterface {
    //<editor-fold desc="Static data" defaultState="collapsed" >
    private static final String PORT_INPUT_EXAMPLE = NearestPrototypesOperator.PORT_OUTPUT_PROTOTYPES;
    private static final String PORT_INPUT_MODEL = NearestPrototypesOperator.PORT_OUTPUT_TUPLES;
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
        //Get example set
        ExampleSet exampleSet = exampleSetInputPort.getData(ExampleSet.class);
        //Get model
        PrototypesEnsembleModel inputModel = prototypesEnsemble.getData(PrototypesEnsembleModel.class);
        //Compute
        AbstractBatchLoopModel model = new BasicBatchLoopModel(exampleSet, inputModel, this);
        model.process();
        PredictionModel finalModel = model.retrieveModel();
        finalModelOutputPort.deliver(finalModel);
    }
    //</editor-fold>

    //<editor-fold desc="BatchLoopInterface implementation" defaultState="collapsed" >
    @Override
    public PredictionModel trainExpert(SelectedExampleSet selectedExamples) throws OperatorException {
        //And deliver these samples to train a model
        exampleInnerSourcePort.deliver(selectedExamples);
        //Execute inner process (train the model)
        getSubprocess(0).execute();
        inApplyLoop();

        return predictionModelInnerSourcePort.getData(PredictionModel.class);
    }
    //</editor-fold>
}
