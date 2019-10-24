package org.prules.operator.learner.prototype;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.prototype.model.AbstractNearestProtoModel;
import org.prules.operator.learner.prototype.model.BasicNearestProtoModel;

import java.util.List;

/**
 * The class implements NearestPrototypeBatchOperator. It takes the prototypes
 * and for each training vector it identifies two closest prototypes from opposite classes.
 * This allows to mark all training vectors with appropriate pair of prototypes.
 * This allows to split the training data along the decision boundary. As an output it adds
 * three new attributes to the training data: batch which indicates data subsets
 * which belong to a single pair, and two additional attributes ID_Proto_1 and ID_Proto_2
 * which indicates respectively index of closest prototype from same class label and opposite
 * class label respectively. This operator also returns PrototypeEnsembleModel which
 * contains information on prototypes and its labels and additional information require to finally build ensemble etc.
 *
 * @author Marcin, Paweł
 */
public class NearestPrototypesOperator extends Operator implements CapabilityProvider {
    //<editor-fold desc="Static data" defaultState="collapsed" >
    static final String PARAMETER_MIN_COUNT_FACTOR = "Min. counts factor";
    static final String PARAMETER_MINIMUM_SUPPORT = "Min. support";
    static final String PORT_INPUT_EXAMPLE = "example set";
    static final String PORT_INPUT_PROTOTYPES = "prototypes";
    static final String PORT_OUTPUT_PROTOTYPES = "example set";
    static final String PORT_OUTPUT_TUPLES = "tuplesModel";
    static final String FACTOR_DESCRIPTION = "Factor indicating minimum number of instances in a single batch. It is multiplied by the max counts.";
    static final String MINIMUM_NUMBER_SUPPORT_DESCRIPTION = "Minimum number of samples in a single batch. It it has lower number of samples it will be removed and the samples will be redistributed into another batches";
    //</editor-fold>

    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Input data - training set
     */
    private final InputPort exampleSetInputPort = this.getInputPorts().createPort(PORT_INPUT_EXAMPLE);
    /**
     * Input data - prototypes
     */
    private final InputPort prototypesInputPort = this.getInputPorts().createPort(PORT_INPUT_PROTOTYPES);
    /**
     * example set with three additional attributes as described in class description
     */
    private final OutputPort exampleSetOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_PROTOTYPES);
    /**
     * Model representing prototypes and its relations
     */
    private final OutputPort modelOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_TUPLES);
    /**
     * Distance measure helper for creating appropriate distance measure
     */
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * <p>
     * Creates a  NearestPrototypeBatch operator. S
     * </p>
     * <p>
     * NOTE: the preferred way for operator creation is using one of the factory
     * methods of {@link OperatorService}.
     * </p>
     *
     * @param description Operator description
     */
    public NearestPrototypesOperator(OperatorDescription description) {
        super(description);
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
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
        }, exampleSetInputPort));
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
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);
        getTransformer().addGenerationRule(modelOutputPort, PrototypesEnsembleModel.class);
    }
    //</editor-fold>

    //<editor-fold desc="Operator methods" defaultState="collapsed" >

    /**
     * Main method which performs all calculations
     *
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        double minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        int minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        ExampleSet examples = this.exampleSetInputPort.getDataOrNull(ExampleSet.class);
        ExampleSet prototypes = this.prototypesInputPort.getDataOrNull(ExampleSet.class);
        AbstractNearestProtoModel model = new BasicNearestProtoModel(examples, prototypes, measureHelper, minFactor, minSupport);
        model.process();
        PrototypesEnsembleModel ensModel = model.retrieveModel();
        ExampleSet outputSet = model.retrieveOutputSet();
        this.exampleSetOutputPort.deliver(outputSet);
        this.modelOutputPort.deliver(ensModel);
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
}
