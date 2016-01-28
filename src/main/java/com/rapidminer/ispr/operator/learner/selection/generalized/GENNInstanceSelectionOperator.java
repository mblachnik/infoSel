package com.rapidminer.ispr.operator.learner.selection.generalized;

import java.util.List;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 *
 * @author Marcin
 */
public class GENNInstanceSelectionOperator extends AbstractInstanceSelectorChain {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_K = "k";
    /**
     *
     */
    public static final String PARAMETER_MAX_ERROR = "max error";
    /**
     *
     */
    public static final String PARAMETER_RELATIVE_ERROR = "relative error";
    /**
     *
     */
    public static final String PARAMETER_TRAIN_ON_SUBSET = "use subset";
    /**
     *
     */
    public static double vkNN = 1.0;
    
    /**
     *
     * @param description
     */
    public GENNInstanceSelectionOperator(OperatorDescription description) {
        super(description,false);        
    }

    //  @Override
//public List<ParameterType> getParameterTypes() {
//List<ParameterType> types = super.getParameterTypes();
//types.add(new ParameterTypeDouble(PARAMETER_maxdY, "max_dY", 0.02, 2.0, 0.3, false));
//return types;
//}
    /**
     *
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    @Override
    public SelectedExampleSet selectInstances(SelectedExampleSet exampleSet) throws OperatorException {
        //INITIALIZATION
        DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
        int k = getParameterAsInt(PARAMETER_K);
        double max_error = getParameterAsDouble(PARAMETER_MAX_ERROR);
        boolean relativeError = getParameterAsBoolean(PARAMETER_RELATIVE_ERROR);
        boolean trainOnSubset = getParameterAsBoolean(PARAMETER_RELATIVE_ERROR);
        GENNInstanceSelectionModel m = new GENNInstanceSelectionModel(measure, k, max_error ,relativeError,trainOnSubset, this);
        SelectedExampleSet output = (SelectedExampleSet)m.run(exampleSet); 
        if (m.isException())
            throw (OperatorException)m.getException();
        sampleSize = output.size();                
        return output;
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

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type1 = new ParameterTypeInt(PARAMETER_K, "Number of nearest neighbors", 1, 10000, 60);
        ParameterType type2 = new ParameterTypeDouble(PARAMETER_MAX_ERROR, "Range of close neighbors", 0.0001, 100, 0.5);
        ParameterType type3 = new ParameterTypeBoolean(PARAMETER_TRAIN_ON_SUBSET, "Train on subset", false);
        ParameterType type4 = new ParameterTypeBoolean(PARAMETER_RELATIVE_ERROR, "Relative error", true);
        type1.setExpert(false);
        type2.setExpert(false);
        type3.setExpert(true);
        type4.setExpert(true);
        types.add(type1);
        types.add(type2);
        types.add(type3);
        types.add(type4);        

        return types;
    }
}
