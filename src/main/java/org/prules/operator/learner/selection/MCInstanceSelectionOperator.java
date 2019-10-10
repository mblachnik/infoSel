/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.MCInstanceSelectionModel;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import org.prules.operator.learner.tools.genetic.RMRandomGenerator;

import java.util.List;

/**
 * This class is used to provide Monte Carlo instance selection
 * operator It use
 * {@link org.prules.operator.learner.selection.models.MCInstanceSelectionModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class MCInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    private static final String PARAMETER_PROTOTYPES_NUMBER = "Number of prototypes";
    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Number of iterations";

    /**
     * Default RapidMiner constructor
     *
     * @param description
     */
    public MCInstanceSelectionOperator(OperatorDescription description) {
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
        int p = getParameterAsInt(PARAMETER_PROTOTYPES_NUMBER);
        int s = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
        IISDecisionFunction loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this, exampleSet);
        return new MCInstanceSelectionModel(measure, p, s, new RMRandomGenerator(randomGenerator), loss);
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
        } catch (Exception ignored) {
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
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    /**
     * In this method dataset don't have to be randomized
     *
     * @return
     */
    @Override
    public boolean isSampleRandomize() {
        return false;
    }

    /**
     * We need to use DecisionFunction in this operator. This method always
     * returns true (for this algorithm)
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

        ParameterType type = new ParameterTypeInt(PARAMETER_PROTOTYPES_NUMBER, "Size of the population", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "The number of iterations", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);
        types.addAll(RandomGenerator.getRandomGeneratorParameters(this));
        return types;
    }
}
