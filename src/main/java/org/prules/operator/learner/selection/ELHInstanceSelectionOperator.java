/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection;

import org.prules.operator.learner.selection.models.ELHInstanceSelectionModel;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * This class is used to provide Encoding Length Heuristic instance selection
 * operator It use
 * {@link org.prules.operator.learner.selection.models.ELHInstanceSelectionModel}
 * class where the algorithm is implemented
 *
 * @author Marcin
 */
public class ELHInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    public ELHInstanceSelectionOperator(OperatorDescription description) {
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
        return new ELHInstanceSelectionModel(distance);
    }

    /**
     * Return false because we don't use lossFunction
     *
     * @return
     */
    @Override
    public boolean useDecisionFunction() {
        return false;
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
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }
}
