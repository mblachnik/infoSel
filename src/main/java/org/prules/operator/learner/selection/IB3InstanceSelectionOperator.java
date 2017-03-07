package org.prules.operator.learner.selection;

import org.prules.operator.learner.selection.models.IB3InstanceSelectionModel;
import java.util.List;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * This class is used to provide IB3 instance selection operator It use
 * {@link org.prules.operator.learner.selection.models.IB3InstanceSelectionModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class IB3InstanceSelectionOperator extends AbstractInstanceSelectorOperator {
    //private final CNNInstanceSelection cnnInstanceSelection;	    

    private static final String PARAMETER_K = "Parameter K";
    private static final String PARAMETER_UPPER_INTERVAL = "Parameter upper interval";
    private static final String PARAMETER_LOWER_INTERVAL = "Parameter lower interval";

    /**
     * Default RapidMiner operator constructor
     *
     * @param description
     */
    public IB3InstanceSelectionOperator(OperatorDescription description) {
        super(description);
    }

    /**
     * We don't use decision function
     *
     * @return
     */
    @Override
    public boolean useDecisionFunction() {
        return false;
    }

    /**
     * Method used to configure and initialize instance selection model.
     *
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    @Override
    public AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet exampleSet) throws OperatorException {
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet);
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
        int k = getParameterAsInt(PARAMETER_K);
        double lowerInterval = getParameterAsDouble(PARAMETER_LOWER_INTERVAL);
        double upperInterval = getParameterAsDouble(PARAMETER_UPPER_INTERVAL);
        //IISDecisionFunction loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this);
        //AbstractInstanceSelectorModel model = new IB3InstanceSelectionModel(distance,k,upperInterval,lowerInterval,randomGenerator, loss);
        return new IB3InstanceSelectionModel(distance, k, upperInterval, lowerInterval, randomGenerator);
    }

    /**
     * Capabilities validation - whether given dataset type is supported
     *
     * @param capability
     * @return
     */
    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception e) {
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
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case WEIGHTED_EXAMPLES:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeInt(PARAMETER_K, "The number of nearest neighbors.", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPPER_INTERVAL, "Upper interval", 0.0, 1.0, 0.9);
        type.setExpert(true);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_LOWER_INTERVAL, "Lower interval", 0.0, 1.0, 0.7);
        type.setExpert(true);
        types.add(type);

        return types;
    }
}
