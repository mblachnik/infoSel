package org.prules.operator.learner.selection;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.GAInstanceSelectionModel;
import org.prules.operator.performance.evaluator.Accuracy;
import org.prules.operator.performance.evaluator.PerformanceEvaluator;

import java.util.List;

/**
 * Genetic Algorithms-based instance selection operator based on Jenetics library
 */
public class GAInstanceSelectionOperator extends AbstractInstanceSelectorOperator{
    /**
     * Default constructor for Genetic Algorithms-based instance selection
     *
     * @param description
     */
    public GAInstanceSelectionOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet trainingSet) throws OperatorException {
        int liczbaGeneracji = 100;
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        int k = 1;
        double performanceRatio = 0.95;
        PerformanceEvaluator evaluator = new Accuracy();

        GAInstanceSelectionModel model = new GAInstanceSelectionModel(distance,liczbaGeneracji,k,performanceRatio,evaluator);
        return model;
    }

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
        return false;
    }

    /**
     * This method may be override if an algorithm doesn't want to allow sample
     * randomization. This may be used for ENN algorithm because the order of
     * samples doesn't influence the result. This cannot be solved using class
     * field because in the constructor DistanceMeasureHelper executes the
     * geParametersType method
     *
     * @return
     */
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

        return types;
    }
}
