/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.generalized;

import com.rapidminer.example.ExampleSet;
import java.util.List; 

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * 
 * @author Marcin
 */
public class GCNNInstanceSelectionOperator extends AbstractInstanceSelectorChain {
    //private final CNNInstanceSelection cnnInstanceSelection;

    /**
     * 
     */
    public static final String PARAMETER_K = "k";
    /**
     * 
     */
    public static final String PARAMETER_MAX_ERROR = "Max Error";
    /**
     * 
     */
    public static final String PARAMETER_RELATIVE_ERR = "Relative error";
    

    /**
     * 
     * @param description
     */
    public GCNNInstanceSelectionOperator(OperatorDescription description) {
        super(description);
    }

    /**
     * 
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    @Override
    public ExampleSet selectInstances(SelectedExampleSet exampleSet) throws OperatorException {        
        
        
        DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
        int k = getParameterAsInt(PARAMETER_K);
        double maxError = getParameterAsDouble(PARAMETER_MAX_ERROR);
        boolean relativeError = getParameterAsBoolean(PARAMETER_RELATIVE_ERR);
        GCNNInstanceSelectionModel m = new GCNNInstanceSelectionModel(measure, relativeError, maxError, k, this);
        ExampleSet output = m.run(exampleSet);
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
        ParameterType type = new ParameterTypeInt(PARAMETER_K, "Number of nearest neighbors", 1, 10000, 60);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeDouble(PARAMETER_MAX_ERROR, "maxdY", 0.0001, 100, 0.15);        
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeBoolean(PARAMETER_RELATIVE_ERR,"Relative error" , true);                        
        type.setExpert(true);
        types.add(type);        
        return types;
    }
}
