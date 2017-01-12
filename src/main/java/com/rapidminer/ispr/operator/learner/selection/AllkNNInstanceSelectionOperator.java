package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import com.rapidminer.ispr.operator.learner.selection.models.AllKNNInstanceSelectionGeneralModel;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 * This class is used to provide All k-NN instance selection operator It use
 * {@link com.rapidminer.ispr.operator.learner.selection.models.AllKNNInstanceSelectionGeneralModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class AllkNNInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_ADD_WEIGHTS = "k_start";
    /**
     *
     */
    public static final String PARAMETER_K_STOP = "k_stop";

    /**
     * Default constructor
     *
     * @param description
     */
    public AllkNNInstanceSelectionOperator(OperatorDescription description) {
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
        int k1 = getParameterAsInt(PARAMETER_ADD_WEIGHTS);
        int k2 = getParameterAsInt(PARAMETER_K_STOP);
        if (k1 > k2) {
            int tmp = k1;
            k1 = k2;
            k2 = tmp;
        }
        IISDecisionFunction loss = null;
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this, exampleSet);
        return new AllKNNInstanceSelectionGeneralModel(measure, k1, k2, loss);
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
     * Operator configuration parameters
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType typeK1 = new ParameterTypeInt(PARAMETER_ADD_WEIGHTS, "The lower number of nearest neighbors.", 1, Integer.MAX_VALUE, 3);
        typeK1.setExpert(false);
        types.add(typeK1);

        ParameterType typeK2 = new ParameterTypeInt(PARAMETER_K_STOP, "The higher number of nearest neighbors.", 1, Integer.MAX_VALUE, 5);
        typeK2.setExpert(false);
        types.add(typeK2);

        return types;
    }
}
