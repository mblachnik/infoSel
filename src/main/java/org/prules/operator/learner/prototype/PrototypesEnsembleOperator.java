/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.prototype;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.prototype.model.AbstractBatchLoopModel;
import org.prules.operator.learner.prototype.model.AbstractNearestProtoModel;
import org.prules.operator.learner.prototype.model.BasicBatchLoopModel;
import org.prules.operator.learner.prototype.model.BasicNearestProtoModel;
import org.prules.operator.learner.prototype.model.interfaces.BatchLoopInterface;

import java.util.List;

/**
 * This operator performs a loop over batch attribute, such that all samples with given
 * batch value are delivered to the input subprocess where a prediction model is trained.
 * As an output the operator returns a local ensemble model with models defined for each batch value, where batch is defined by a pair of prototypes.
 *
 * @author Marcin, Paweł
 */
public class PrototypesEnsembleOperator extends OperatorChain implements BatchLoopInterface, CapabilityProvider {
    //<editor-fold desc="Static data" defaultState="collapsed" >
    private static final String PORT_INPUT_PROTOTYPES = NearestPrototypesOperator.PORT_INPUT_PROTOTYPES;
    private static final String PORT_INPUT_EXAMPLE = NearestPrototypesOperator.PORT_INPUT_EXAMPLE;
    private static final String PORT_OUTPUT_PROTOTYPES = NearestPrototypesOperator.PORT_INPUT_PROTOTYPES;
    private static final String PORT_OUTPUT_EXAMPLE = NearestPrototypesOperator.PORT_INPUT_EXAMPLE;
    private static final String PORT_OUTPUT_EXAMPLE_MODIFIED = "batches " + NearestPrototypesOperator.PORT_INPUT_EXAMPLE;
    private static final String PORT_OUTPUT_MODEL = "model";
    private static final String PORT_INNER_INPUT_EXAMPLE = "example set";
    private static final String PORT_INNER_INPUT_MODEL = "prediction model";
    private static final String PARAMETER_MIN_COUNT_FACTOR = NearestPrototypesOperator.PARAMETER_MIN_COUNT_FACTOR;
    private static final String PARAMETER_MINIMUM_SUPPORT = NearestPrototypesOperator.PARAMETER_MINIMUM_SUPPORT;
    private static final String FACTOR_DESCRIPTION = NearestPrototypesOperator.FACTOR_DESCRIPTION;
    private static final String MINIMUM_NUMBER_SUPPORT_DESCRIPTION = NearestPrototypesOperator.MINIMUM_NUMBER_SUPPORT_DESCRIPTION;
    //</editor-fold>

    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Input data - training set
     */
    private final InputPort examplesInputPort = this.getInputPorts().createPort(PORT_INPUT_EXAMPLE);
    /**
     * Input data - prototypes
     */
    private final InputPort prototypesInputPort = this.getInputPorts().createPort(PORT_INPUT_PROTOTYPES);
    /**
     * Output data - training set not modified
     */
    private final OutputPort examplesOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_EXAMPLE);
    /**
     * Output data - prototypes
     */
    private final OutputPort prototypesOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_PROTOTYPES);
    /**
     * Output data - training set modified
     */
    private final OutputPort examplesBatchesOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_EXAMPLE_MODIFIED);
    /**
     * Output data - final model
     */
    private final OutputPort modelOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_MODEL);
    /**
     * Inner data - training data delivered to the subProcess
     */
    private final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort(PORT_INNER_INPUT_EXAMPLE);
    /**
     * Inner data - prediction model obtained after training the prediction model
     */
    private final InputPort predictionModelInnerSourcePort = getSubprocess(0).getInnerSinks().createPort(PORT_INNER_INPUT_MODEL, PredictionModel.class);
    /**
     * Distance measure helper for creating appropriate distance measure
     */
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
    /**
     * Examples
     */
    private ExampleSet examples;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * Constructor for the BatchLoopOperator
     *
     * @param description
     */
    public PrototypesEnsembleOperator(OperatorDescription description) {
        super(description, "Train experts on prototypes");
        examplesInputPort.addPrecondition(new DistanceMeasurePrecondition(examplesInputPort, this));
        examplesInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
            try {
                measureType = measureHelper.getSelectedMeasureType();
            } catch (UndefinedParameterError ignored) {
            }
            switch (capability) {
                case BINOMINAL_ATTRIBUTES:
                case POLYNOMINAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
                case NUMERICAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                            || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
                case MISSING_VALUES:
                case BINOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, examplesInputPort));
        prototypesInputPort.addPrecondition(new DistanceMeasurePrecondition(prototypesInputPort, this));
        prototypesInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
            try {
                measureType = measureHelper.getSelectedMeasureType();
            } catch (UndefinedParameterError ignored) {
            }
            switch (capability) {
                case BINOMINAL_ATTRIBUTES:
                case POLYNOMINAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
                case NUMERICAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                            || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
                case MISSING_VALUES:
                case BINOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, prototypesInputPort));
        getTransformer().addPassThroughRule(examplesInputPort, examplesOutputPort);
        getTransformer().addPassThroughRule(prototypesInputPort, prototypesOutputPort);
        getTransformer().addGenerationRule(modelOutputPort, PrototypesEnsemblePredictionModel.class);

//        getTransformer().addGenerationRule(modelOutputPort, PrototypesEnsembleModel.class);
    }
    //</editor-fold>

    //<editor-fold desc="Operator methods" defaultState="collapsed" >

    /**
     * Main computation method
     *
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        //Get  parameters
        double minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        int minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        examples = examplesInputPort.getDataOrNull(ExampleSet.class);
        ExampleSet prototypes = prototypesInputPort.getDataOrNull(ExampleSet.class);
        //Compute
        PrototypesEnsembleModel model = computeNearest(prototypes, measureHelper, minFactor, minSupport);
        PredictionModel finalModel = computeBatches(model);
        //Deliver data
        modelOutputPort.deliver(finalModel);
        examplesBatchesOutputPort.deliver(examples);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (UndefinedParameterError ignored) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            case BINOMINAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_COUNT_FACTOR, FACTOR_DESCRIPTION, 0, 1, 0.1);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMUM_SUPPORT, MINIMUM_NUMBER_SUPPORT_DESCRIPTION, 0, Integer.MAX_VALUE, 20);
        types.add(type);
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
    //</editor-fold>

    //<editor-fold desc="Work on models" defaultState="collapsed" >

    /**
     * Computes batches
     *
     * @param prototypes    - {@link BasicNearestProtoModel}
     * @param measureHelper - {@link BasicNearestProtoModel}
     * @param minFactor     - {@link BasicNearestProtoModel}
     * @param minSupport    - {@link BasicNearestProtoModel}
     * @return PrototypesEnsembleModel
     * @throws OperatorException
     */
    private PrototypesEnsembleModel computeNearest(ExampleSet prototypes, DistanceMeasureHelper measureHelper, double minFactor, int minSupport) throws OperatorException {
        AbstractNearestProtoModel model = new BasicNearestProtoModel(examples, prototypes, measureHelper, minFactor, minSupport);
        model.process();
        examples = model.retrieveOutputSet();
        return model.retrieveModel();
    }

    /**
     * Loops batches
     *
     * @param inputModel PrototypesEnsembleModel
     * @return PredictionModel
     * @throws OperatorException
     */
    private PredictionModel computeBatches(PrototypesEnsembleModel inputModel) throws OperatorException {
        AbstractBatchLoopModel model = new BasicBatchLoopModel(examples, inputModel, this);
        model.process();
        return model.retrieveModel();
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
