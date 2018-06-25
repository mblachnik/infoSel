/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.operator.learner.weighting.models.AbstractNoiseEstimatorModel;
import org.prules.operator.learner.weighting.models.DeltaTestNoiseModel;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
//import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class NNEDeltaTestOperator extends AbstractWeightingOperator {
    
    public static final String PARAMETER_SIGMA = "sigma";    
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this); 
    
    private double sigma; //The minimum distance criterion
    double nne;
   //private boolean transformWeights;


    public NNEDeltaTestOperator(OperatorDescription description) {
        super(description,Ontology.ATTRIBUTE_NOISE,Ontology.ATTRIBUTE_NOISE);        
        addValue(new ValueDouble("Nonparametric Noise Estimation", "The level of noise") {
            @Override
            public double getDoubleValue() {
                return nne;
            }
        });
        
        exampleSetInputPort.addPrecondition(
                new SimplePrecondition(exampleSetInputPort, null, true) {

                    @Override
                    public void makeAdditionalChecks(MetaData received) {
                        if (received instanceof ExampleSetMetaData) {
                            ExampleSetMetaData emd = (ExampleSetMetaData) received;
                            switch (emd.hasSpecial(Attributes.LABEL_NAME)) {
                                case NO:
                                    exampleSetInputPort.addError(new SimpleMetaDataError(ProcessSetupError.Severity.WARNING, exampleSetInputPort, "special_missing", Attributes.LABEL_NAME));
                                    break;
                                case YES:
                                    if (!emd.getLabelMetaData().isNumerical()) {
                                        exampleSetInputPort.addError(new SimpleMetaDataError(ProcessSetupError.Severity.WARNING, exampleSetInputPort, "special_attribute_has_wrong_type", emd.getLabelMetaData().getName() ,Attributes.LABEL_NAME, com.rapidminer.tools.Ontology.VALUE_TYPE_NAMES[com.rapidminer.tools.Ontology.NUMERICAL]));
                                    }
                            }
                        }
                    }
                }
        );
    }    
    
    @Override
    public void processExamples(ExampleSet exampleSet) throws OperatorException {                
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet); 
        sigma = getParameterAsDouble(PARAMETER_SIGMA);                
        AbstractNoiseEstimatorModel model = new DeltaTestNoiseModel(distance, sigma);        
        double[] noise = model.run(exampleSet).getFirst();
        nne = model.getNNE();
        Attributes attributes = exampleSet.getAttributes();        
        Attribute weightAttribute = attributes.getSpecial(Ontology.ATTRIBUTE_NOISE);
        int i = 0;        
        for (Example example : exampleSet){
            example.setValue(weightAttribute, noise[i]);
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
            case NUMERICAL_LABEL:            
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeDouble(PARAMETER_SIGMA, "The sigma.", 1e-50, Double.MAX_VALUE, 0.01);
        type.setExpert(false);
        types.add(type);                
        
        types.addAll(DistanceMeasures.getParameterTypes(this));

        return types;
    }    
}
