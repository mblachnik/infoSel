/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.tools.RandomGenerator;

import java.util.List;
import java.util.Set;

/**
 * @author Marcin
 */
public class ISEnsembleBaggingOperator extends AbstractISEnsembleOperator {

    private static final String PARAMETER_USE_ENTRY_EXAMPLE_SET = "Use entry set";
    private static final String PARAMETER_SAMPLE_RATIO = "Sample ratio";
    private double sampleRatio = 0.8;
    private transient boolean useEntrySet;
    private transient RandomGenerator random;

    public ISEnsembleBaggingOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {
        super.initializeProcessExamples(exampleSet);
        useEntrySet = getParameterAsBoolean(PARAMETER_USE_ENTRY_EXAMPLE_SET);
        if (!useEntrySet) {
            random = RandomGenerator.getRandomGenerator(this);
        }
    }

    @Override
    protected ExampleSet preProcessExampleSet(ExampleSet trainingSet) throws OperatorException {
        ExampleSet dataSet;
        if (useEntrySet) {
            dataSet = trainingSet;
        } else {
            sampleRatio = getParameterAsDouble(PARAMETER_SAMPLE_RATIO);
            int sampleSize = (int) (trainingSet.size() * sampleRatio);
            Set<Integer> idxSet = random.nextIntSetWithRange(0, trainingSet.size(), sampleSize);
            int[] idx = new int[sampleSize];
            int j = 0;
            for (Integer i : idxSet) {
                idx[j] = i;
                j++;
            }
            dataSet = new MappedExampleSet(trainingSet, idx);
        }
        return dataSet;
    }

    /**
     *
     */
    @Override
    public void finalizeProcessExamples() {
        super.finalizeProcessExamples();
    }

    @Override
    public List<ParameterType> getParameterTypes() {

        List<ParameterType> types = super.getParameterTypes();
        ParameterType type;
        type = new ParameterTypeBoolean(PARAMETER_USE_ENTRY_EXAMPLE_SET, "Use entry exampleSet for each iteration", false, true);
        types.add(3, type);
        type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "Sample size", 0.0001, 1, 0.8);
        type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_USE_ENTRY_EXAMPLE_SET, false, false));
        types.add(4, type);
        return types;
    }
}
