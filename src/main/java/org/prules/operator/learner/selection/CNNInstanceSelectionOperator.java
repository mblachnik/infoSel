package org.prules.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.CNNInstanceSelectionGeneralModel;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * This class is used to provide Condensed Nearest Neighbor instance selection
 * operator It use
 * {@link org.prules.operator.learner.selection.models.CNNInstanceSelectionGeneralModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class CNNInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    //private final CNNInstanceSelection cnnInstanceSelection;
    /**
     * Default RapidMiner constructor
     *
     * @param description
     */
    public CNNInstanceSelectionOperator(OperatorDescription description) {
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
        IISDecisionFunction loss = null;
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(this, exampleSet);
        return new CNNInstanceSelectionGeneralModel(distance, loss);
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

}
