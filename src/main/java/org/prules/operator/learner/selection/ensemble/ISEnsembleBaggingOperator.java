/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import static com.rapidminer.operator.validation.RandomSplitValidationChain.PARAMETER_SAMPLING_TYPE;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.RandomGenerator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcin
 */
public class ISEnsembleBaggingOperator extends AbstractISEnsembleOperator {

    public static final String PARAMETER_USE_ENTRY_EXAMPLESET = "Use entry set";        
    public static final String PARAMETER_SAMPE_RATIO = "Sample ratio";        
    double sampleRatio = 0.8;
    
    public ISEnsembleBaggingOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {   
        super.initializeProcessExamples(exampleSet);
    }

    @Override
    ExampleSet prepareExampleSet(ExampleSet trainingSet) throws OperatorException {
        boolean chk = getParameterAsBoolean(PARAMETER_USE_ENTRY_EXAMPLESET);
        ExampleSet dataSet;
        if (chk) {
            dataSet = trainingSet;
        } else {            
            RandomGenerator r = RandomGenerator.getRandomGenerator(this);
            sampleRatio = getParameterAsDouble(PARAMETER_SAMPE_RATIO);
            int sampleSize = (int)(trainingSet.size()*sampleRatio);
            Set<Integer> idxSet = r.nextIntSetWithRange(0, trainingSet.size(), sampleSize);            
            int[] idx = new int[sampleSize];
            int j = 0;
            for(Integer i : idxSet){
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
        type = new ParameterTypeBoolean(PARAMETER_USE_ENTRY_EXAMPLESET, "Use entry exampleSet for each iteration", false, true);                
        types.add(3,type);
        type = new ParameterTypeDouble(PARAMETER_SAMPE_RATIO,"Sample size", 0.0001, 1, 0.8);
        type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_USE_ENTRY_EXAMPLESET, false,false));
        types.add(4,type);        
        return types;
    }       

}
