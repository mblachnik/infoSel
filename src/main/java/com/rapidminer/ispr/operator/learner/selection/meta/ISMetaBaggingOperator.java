/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.meta;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
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
public class ISMetaBaggingOperator extends AbstractISMetaOperator {

    public static final String PARAMETER_RATIO = "Sampling ratio";

    public ISMetaBaggingOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    ExampleSet prepareExampleSet(ExampleSet trainingSet) throws OperatorException {
        double ratio = getParameterAsDouble(PARAMETER_RATIO);        
        int size = (int) Math.round(trainingSet.size() * ratio);
        RandomGenerator random = RandomGenerator.getRandomGenerator(this);        
        int[] mappingFinal;
        /*
        int[] mappingInit;
        mappingInit = PRulesUtil.randomPermutation(size, random);
        MappedExampleSet shuffledSet = new MappedExampleSet((ExampleSet) trainingSet.clone(), mappingInit, true);        
        DataIndex index = PRulesUtil.stratifiedSelection(shuffledSet,size,random);                
        int[] mapping = index.getAsInt();        
        
        mappingFinal = new int[mapping.length];
        for (int i=0; i<mapping.length; i++){
            mappingFinal[i] = mappingInit[mapping[i]];
        }
        */                
        mappingFinal = MappedExampleSet.createBootstrappingMapping(trainingSet, size, random);        
        MappedExampleSet trainingSubSet = new MappedExampleSet((ExampleSet) trainingSet.clone(), mappingFinal, true);
        return trainingSubSet;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeDouble(PARAMETER_RATIO, "Sampling ratio", 0.0001, 1, 0.8);
        type.setExpert(false);
        types.add(type);
        
        return types;
    }
}
