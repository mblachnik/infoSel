package org.prules.operator.learner.selection;

import java.util.List;

import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import com.rapidminer.operator.OperatorCapability;
import static com.rapidminer.operator.OperatorCapability.NUMERICAL_LABEL;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.selection.models.RENNInstanceSelectionModel;

/**
 * This class is used to provide Repeated Edited Nearest Neighbor instance
 * selection operator It use
 * {@link org.prules.operator.learner.selection.models.ENNInstanceSelectionModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class RENNInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_K = "k";
    public static final String PARAMETER_MAX_ITERATIONS = "max iterations";

    /**
     *
     * @param description
     */
    public RENNInstanceSelectionOperator(OperatorDescription description) {
        super(description);
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
        //INITIALIZATION
        DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
        int k = getParameterAsInt(PARAMETER_K);
        IISDecisionFunction loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this, exampleSet);        
        int maxIterations = getParameterAsInt(PARAMETER_MAX_ITERATIONS);
        //return new RENNInstanceSelectionModel(measure, k, loss, maxIterations, instanceModifier);        
        return new RENNInstanceSelectionModel(measure, k, loss, maxIterations);        
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
            case NUMERICAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    /**
     * Set to true because we use lossFunction
     *
     * @return
     */
    @Override
    public boolean useDecisionFunction() {
        return true;
    }

    /**
     * Samples don't need to be randomized. The algorithm is stable
     * @return 
     */
    @Override
    public boolean isSampleRandomize() {
        return false;
    }
    
    /**
     * Configuring operators input parameters
     * @return 
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type;
        type = new ParameterTypeInt(PARAMETER_K, "The used number of nearest neighbors.", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);
        
        type = new ParameterTypeInt(PARAMETER_MAX_ITERATIONS, "Maximum number of iterations. Negative value is equivalent to no limit", -1, Integer.MAX_VALUE, -1);
        type.setExpert(false);
        types.add(type);
        
        return types;
    }
}
