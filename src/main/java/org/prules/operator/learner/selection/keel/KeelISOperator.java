/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.keel;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import java.util.List;
import org.prules.operator.learner.selection.AbstractInstanceSelectorOperator;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;

/**
 *
 * @author Marcin
 */
public class KeelISOperator extends AbstractInstanceSelectorOperator {

    public static final String CONFIGURATION_PARAMETERS = "k";

    public KeelISOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet trainingSet) throws OperatorException {
        return new KeelISModel(this.getParameterAsString(CONFIGURATION_PARAMETERS), this);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeString(CONFIGURATION_PARAMETERS, "Configuration parameters");
        type.setExpert(false);
        types.add(type);       
        return types;
    }

    /**
     * * Method allows to check if this instance selection method requires
     * initial instance randomization. This method always returns true
     *
     * @return
     */
    @Override
    public boolean isSampleRandomize() {
        return true;
    }

    /**
     * Method allows to check if this instance selection method use RapidMiner
     * DistanceFunction. This method doesn't use it so it returns FALSE
     *
     * @return
     */
    @Override
    public boolean isDistanceBased() {
        return false;
    }

    /**
     * Method allows to check if this instance selection method use special
     * decision function. This method doesn't use it so it returns FALSE
     *
     * @return
     */
    @Override
    public boolean useDecisionFunction() {
        return false;
    }
}
