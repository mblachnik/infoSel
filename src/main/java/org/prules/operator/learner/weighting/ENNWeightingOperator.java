/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import org.prules.operator.AbstractPRulesBasicOperator;
import org.prules.operator.learner.selection.models.decisionfunctions.ISClassDecisionFunction;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.selection.models.ENNWithInstanceModifierInstanceSelectionModel;
import org.prules.operator.learner.selection.ENNInstanceSelectionOperator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ENNWeightingOperator  extends AbstractWeightingOperator {
    /*
    public static final String PARAMETER_TRANSFORM_WEIGHTS = "Wether to nonlineary transform weights";
    public static final String PARAMETER_MEAN = "Mean value of weights transformer";
    public static final String PARAMETER_GAMMA = "Gamma value of weights transformer";
    */
    public static final String PARAMETER_WEIGHTED_NN = "weighted_vote";
    
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this); 
    private int k;
   //private boolean transformWeights;
    
    
    

    public ENNWeightingOperator(OperatorDescription description) {
        super(description);
    }
    
    
     @Override
    public void processExamples(ExampleSet exampleSet) throws OperatorException {        
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet); 
        k = getParameterAsInt(ENNInstanceSelectionOperator.PARAMETER_K);        
        IISDecisionFunction loss = new ISClassDecisionFunction();
        boolean weightedNN = getParameterAsBoolean(PARAMETER_WEIGHTED_NN);
        ENNWithInstanceModifierInstanceSelectionModel model = new ENNWithInstanceModifierInstanceSelectionModel(distance, k, loss, weightedNN);
        model.setStoreConfidence(true);
        model.run(exampleSet);
        Attribute weights = exampleSet.getAttributes().getWeight();        
        int i = 0;
        double[] confidence = model.getConfidence();

        for (Example example : exampleSet){
            example.setValue(weights, confidence[i]);
            i ++;
        }             
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
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(ENNInstanceSelectionOperator.PARAMETER_K, "The used number of nearest neighbors.", 3, Integer.MAX_VALUE, 3);
        type.setExpert(false);
        types.add(type);
        /*
        type = new ParameterTypeBoolean(PARAMETER_TRANSFORM_WEIGHTS, "Wether to nonlinearly transform weights", false);
        type.setExpert(false);
        types.add(type);
        
        type = new ParameterTypeDouble(PARAMETER_MEAN, "Mean value of gausian weights transformer", Double.MIN_VALUE, Double.MAX_VALUE, 0.5);
        type.setExpert(false);
        type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_TRANSFORM_WEIGHTS, true, true));                
        types.add(type);
        
        type = new ParameterTypeDouble(PARAMETER_GAMMA, "Mean value of gausian weights transformer", Double.MIN_VALUE, Double.MAX_VALUE, 0.5);
        type.setExpert(false);
        type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_TRANSFORM_WEIGHTS, true, true));
        types.add(type);
        */
        
        type = new ParameterTypeBoolean(PARAMETER_WEIGHTED_NN, "Use of weighted vote in nearest neighbor search.", false);
        type.setExpert(true);
        types.add(type);
        
        types.addAll(DistanceMeasures.getParameterTypes(this));

        return types;
    }   
}
