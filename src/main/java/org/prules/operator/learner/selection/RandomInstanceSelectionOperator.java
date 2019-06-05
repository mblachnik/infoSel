package org.prules.operator.learner.selection;

import java.util.List;

import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.RandomInstanceSelectionModel;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;

/**
 * This class is used to provide simple Random instance selection operator It
 * use randomly pick instances which should be selected
 *
 * @author Marcin
 */
public class RandomInstanceSelectionOperator extends AbstractInstanceSelectorOperator {

    /**
     *
     */
    public static final String INSTANCES_NUMBER = "Number of Instances to select";
    /**
     *
     */
    public static final String STRATIFIED = "Stratified";

    /**
     * Default RapidMiner constructor
     *
     * @param description
     */
    public RandomInstanceSelectionOperator(OperatorDescription description) {
        super(description);
    }

    /**
     * Returns number of prototypes displayed in the MataData related with
     * prototypeOutput
     *
     * @return
     * @throws com.rapidminer.parameter.UndefinedParameterError
     */
    @Override
    public MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
        sampleSize = getParameterAsInt(INSTANCES_NUMBER);
        return new MDInteger(sampleSize);
    }

    /**
     * Main method within which the instance selection is executed
     *
     * @param selectedTrainingSet
     * @return
     * @throws OperatorException
     */
    @Override
    public AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet selectedTrainingSet) throws OperatorException {
        sampleSize = getParameterAsInt(INSTANCES_NUMBER);
        boolean stratifiedSelection;
        RandomGenerator randomGenerator;
        stratifiedSelection = getParameterAsBoolean(STRATIFIED);
        randomGenerator = RandomGenerator.getRandomGenerator(this);
        return new RandomInstanceSelectionModel(sampleSize, stratifiedSelection, randomGenerator);
    }

    /**
     * Capabilities validation - whether given dataset type is supported
     *
     * @param capability
     * @return
     */
    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case NUMERICAL_LABEL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Configuring operator parameters
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType instancesNumber = new ParameterTypeInt(INSTANCES_NUMBER, "Number of instances to be selected", 1, Integer.MAX_VALUE, 2);
        instancesNumber.setExpert(false);
        types.add(instancesNumber);

        ParameterType stratifiedSelectionParameter = new ParameterTypeBoolean(STRATIFIED, "Stratified selection", true);
        stratifiedSelectionParameter.setExpert(false);
        types.add(stratifiedSelectionParameter);

        types.addAll(RandomGenerator.getRandomGeneratorParameters(this));

        return types;
    }

    /**
     * This operator don't use loss/decision function for instance selection
     *
     * @return
     */
    @Override
    public boolean useDecisionFunction() {
        return false;
    }

    /**
     * It is not required to randomize input data, so this method always returns
     * false
     *
     * @return
     */
    @Override
    public boolean isSampleRandomize() {
        return false;
    }

    /**
     * This operator don;t use distance function to perform instance selection
     *
     * @return
     */
    @Override
    public boolean isDistanceBased() {
        return false;
    }

}
