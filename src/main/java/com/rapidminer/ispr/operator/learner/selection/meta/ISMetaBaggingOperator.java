/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.meta;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import static com.rapidminer.operator.validation.RandomSplitValidationChain.PARAMETER_SAMPLING_TYPE;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.RandomGenerator;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ISMetaBaggingOperator extends AbstractISMetaOperator {
    
    SplittedExampleSet dataSet;

    public ISMetaBaggingOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {
        int numberOfSubsets = getParameterAsInt(PARAMETER_ITERATIOINS);
        boolean useLocalRandomSeed = getParameterAsBoolean(RandomGenerator.PARAMETER_USE_LOCAL_RANDOM_SEED);
        int seed = getParameterAsInt(RandomGenerator.PARAMETER_LOCAL_RANDOM_SEED);
        int samplingType = getParameterAsInt(PARAMETER_SAMPLING_TYPE);
        dataSet = new SplittedExampleSet(exampleSet, numberOfSubsets, samplingType, useLocalRandomSeed, seed, true);
    }

    @Override
    ExampleSet prepareExampleSet(ExampleSet trainingSet) throws OperatorException {         
        dataSet.selectAllSubsetsBut(this.getIteration());
        return dataSet;
    }

    /**
     * 
     */
    @Override
    public void finalizeProcessExamples() {
        dataSet = null;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes(); 
        		types.add(new ParameterTypeCategory(
				PARAMETER_SAMPLING_TYPE,
				"Defines the sampling type of the cross validation (linear = consecutive subsets, shuffled = random subsets, stratified = random subsets with class distribution kept constant)",
				SplittedExampleSet.SAMPLING_NAMES, SplittedExampleSet.STRATIFIED_SAMPLING));
        types.addAll(RandomGenerator.getRandomGeneratorParameters(this));
        return types;
    }
}
