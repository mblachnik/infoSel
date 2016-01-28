/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.rapidminer.ispr.operator.learner.selection.meta;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.RandomGenerator;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ISMetaNoisOperator extends AbstractISMetaOperator{
    
    public static final String PARAMETER_NOISE_LEVEL = "Noise level";

    public ISMetaNoisOperator(OperatorDescription description) {
        super(description);
    }
    
    @Override
    ExampleSet prepareExampleSet(ExampleSet trainingSet) throws OperatorException {
        ExampleSet trainingSubSet = PRulesUtil.duplicateExampleSet(trainingSet);
        double noiseLevel = getParameterAsDouble(PARAMETER_NOISE_LEVEL);
        Attributes attributes = trainingSubSet.getAttributes();
        RandomGenerator random = RandomGenerator.getRandomGenerator(this);        
        for (Example e : trainingSubSet){
            for (Attribute a : attributes){                
                double value = e.getValue(a);
                double noise = random.nextGaussian()* noiseLevel;
                e.setValue(a, value + noise);
            }                
        }
        return trainingSubSet;
    }
    
        @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeDouble(PARAMETER_NOISE_LEVEL, PARAMETER_NOISE_LEVEL, 0, Double.MAX_VALUE, 0.1);
        type.setExpert(false);
        types.add(type);
        return types;
    }
    
}
