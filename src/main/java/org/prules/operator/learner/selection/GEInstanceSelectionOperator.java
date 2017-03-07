package org.prules.operator.learner.selection;

import org.prules.operator.learner.selection.models.GEInstanceSelectionModel;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.operator.OperatorCapability;
import static com.rapidminer.operator.OperatorCapability.NUMERICAL_LABEL;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;
import org.prules.operator.learner.selection.models.tools.InstanceModifier;
import org.prules.operator.learner.selection.models.tools.InstanceModifierHelper;

/**
 * This class is used to provide Gabriel Graph Editing instance selection
 * operator It use
 * {@link org.prules.operator.learner.selection.models.GEInstanceSelectionModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class GEInstanceSelectionOperator extends AbstractInstanceSelectorOperator {
    //private final CNNInstanceSelection cnnInstanceSelection;

    /**
     * Default RapidMiner constructor
     *
     * @param description
     */
    public GEInstanceSelectionOperator(OperatorDescription description) {
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
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet);
        IISDecisionFunction loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this, exampleSet);
        InstanceModifier instanceModifier = InstanceModifierHelper.getConfiguredInstanceModifier(this);
        return new GEInstanceSelectionModel(distance, loss, instanceModifier);
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
     * Input dataset should be randomized by default (method always returns true)
     * @return 
     */
    @Override
    public boolean isSampleRandomize() {
        return false;
    }
    
    /**
     * Operator configuration parameters
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
                
        types.addAll(InstanceModifierHelper.getParameterTypes(this));
        return types;
    }
}
