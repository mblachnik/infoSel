package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.ispr.operator.learner.selection.models.IB2InstanceSelectionModel;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.tools.InstanceModifier;
import com.rapidminer.ispr.operator.learner.selection.models.tools.InstanceModifierHelper;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 * This class is used to provide IB2 instance selection operator It use
 * {@link com.rapidminer.ispr.operator.learner.selection.models.IB2InstanceSelectionModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class IB2InstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * Default RapidMiner operator constructor
     *
     * @param description
     */
    public IB2InstanceSelectionOperator(OperatorDescription description) {
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
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
        IISDecisionFunction loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this, exampleSet);
        InstanceModifier instanceModifier = InstanceModifierHelper.getConfiguredInstanceModifier(this);
        return new IB2InstanceSelectionModel(distance, loss, instanceModifier);
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
            case WEIGHTED_EXAMPLES:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Setting GUI parameters of this operator
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
