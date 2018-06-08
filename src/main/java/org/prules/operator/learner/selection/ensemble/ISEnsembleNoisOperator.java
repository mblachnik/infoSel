/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.operator.learner.tools.PRulesUtil;
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
public class ISEnsembleNoisOperator extends AbstractISEnsembleOperator {

    public static final String PARAMETER_NOISE_LEVEL = "Noise level";
    transient RandomGenerator random;

    public ISEnsembleNoisOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {
        super.initializeProcessExamples(exampleSet);
        random = RandomGenerator.getRandomGenerator(this);
    }

    @Override
    protected ExampleSet preprocessExampleSet(ExampleSet trainingSet) throws OperatorException {
        ExampleSet trainingSubSet = PRulesUtil.duplicateExampleSet(trainingSet);
        double noiseLevel = getParameterAsDouble(PARAMETER_NOISE_LEVEL);
        Attributes attributes = trainingSubSet.getAttributes();        
        for (Example e : trainingSubSet) {
            for (Attribute a : attributes) {
                double value = e.getValue(a);
                double noise = random.nextGaussian() * noiseLevel;
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
