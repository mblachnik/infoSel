package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.*;
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
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.tools.math.similarity.numerical.SquareEuclidianDistance;

import java.util.List;

/**
 * The class implements NearestPrototypeBatchOperator. It takes the prototypes
 * and for each training vector it identifies two closest prototypes from opposite classes.
 * This allows to mark all training vectors with appropriate pair of prototypes.
 * This allows to split the training data along the decision boundary. As an output it adds
 * three new attributes to the training data: batch which indicates data subsets
 * which belong to a single pair, and two additional attributes ID_Proto_1 and ID_Proto_2
 * which indicates respectively index of closest prototype from same class label and opposite
 * class label respectively.
 * @author Marcin
 */
public class NearestPrototypeBatchOperator extends Operator implements CapabilityProvider {

    public static final String PARAMETER_MIN_COUNT_FACTOR = "Min. counts factor";
    public static final String PARAMETER_MINIMUM_SUPPORT = "Min. support";

    /**
     * Input data - training set
     */
    private final InputPort exampleSetInputPort = this.getInputPorts().createPort("example set");
    /**
     * Input data - prototypes
     */
    private final InputPort prototypesInputPort = this.getInputPorts().createPort("prototypes");
    /**
     * example set with three additional attributes as described in class description
     */
    private final OutputPort exampleSetOutputPort = this.getOutputPorts().createPort("example set");
    /**
     * Distance measure helper for creating appropriate distance measure
     */
    //private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this); //For now DistanceMeasureHelper is switched off becouse here we use SquaredEuclidianDistance to make all calculations faster. If we use other measure, then there may be an issue with adding distances.

    /**
     * <p>
     * Creates a  NearestPrototypeBatch operator. S
     * </p>
     * <p>
     * NOTE: the preferred way for operator creation is using one of the factory
     * methods of {@link OperatorService}.
     * </p>
     *
     * @param description
     */
    public NearestPrototypeBatchOperator(OperatorDescription description) {
        super(description);
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.NUMERICAL_MEASURES_TYPE;//DistanceMeasures.MIXED_MEASURES_TYPE;
//            try {
//                measureType = measureHelper.getSelectedMeasureType();
//            } catch (UndefinedParameterError ignored) {
//            }
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
                case POLYNOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, exampleSetInputPort));
        prototypesInputPort.addPrecondition(new DistanceMeasurePrecondition(prototypesInputPort, this));
        prototypesInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.NUMERICAL_MEASURES_TYPE;//DistanceMeasures.MIXED_MEASURES_TYPE;
//            try {
//                measureType = measureHelper.getSelectedMeasureType();
//            } catch (UndefinedParameterError ignored) {
//            }
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
                case POLYNOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, prototypesInputPort));
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);
    }

    /**
     * This method performs prediction model training for every value of batch attribute. The algorithm starts by
     * identifing examples with given batch value, and than in a loop it trains prediction model defined within the
     * inner process. Each prediction model is collected and returned to the output as PrototypesEnsemblePredictionModel
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        double minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        int minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        //boolean detectPureSubsets = getParameterAsBoolean(PARAMETER_DETECT_PURE_SUBSETS);
        ExampleSet exampleSet = this.exampleSetInputPort.getDataOrNull(ExampleSet.class);
        ExampleSet prototypeSet = this.prototypesInputPort.getDataOrNull(ExampleSet.class);
        SquareEuclidianDistance measure = new SquareEuclidianDistance();//measureHelper.getInitializedMeasure(exampleSet)
        measure.init(exampleSet);
        //measureHelper.getInitializedMeasure(exampleSet)
        NearestPrototypesSplitter inputModel = new NearestPrototypesSplitterV2(prototypeSet,measure,minFactor,minSupport);
        exampleSet = inputModel.split(exampleSet);
        this.exampleSetOutputPort.deliver(exampleSet);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.NUMERICAL_MEASURES_TYPE;//DistanceMeasures.MIXED_MEASURES_TYPE;
//        try {
//            measureType = measureHelper.getSelectedMeasureType();
//        } catch (UndefinedParameterError e) {
//        }
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
        ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_COUNT_FACTOR, "Factor indicating minimum number of instances in a single batch. It is multiplayed by the max counts.", 0, 1, 0.1);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMUM_SUPPORT, "Minimum number of samples in a single batch. It it has lower number of samples it will be removed and the samples will be redistributed into another batches", 0, Integer.MAX_VALUE, 20);
        types.add(type);
        //type = new ParameterTypeBoolean(PARAMETER_DETECT_PURE_SUBSETS,"Detect pure subsets and keep them (for examples falling into this pair label will be determined without training a model) ",false);
        //types.add(type);
        //types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
