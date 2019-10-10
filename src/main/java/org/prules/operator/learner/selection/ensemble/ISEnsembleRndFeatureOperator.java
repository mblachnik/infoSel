/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.RandomGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This meta operator for instance selection utilize the random features
 * principle to support divergence of the model. This algorithm repeats instance
 * selection process, and in each iteration a subset of features (ratio defines
 * the percentage of features to be select) is selected and delivered to the
 * input of the process. According to the selected instances in each iteration a
 * ranking is made, such that the weight represents the number of times given
 * instance was returned by the instance selection subprocess. The threshold
 * parameter allows to manipulate the popularity of selected instances. If the
 * "return weight" option is selected instead of selecting the instances a
 * weight attribute is returned As in input to that process this operator
 * requires ExampleSet with ID attribute.
 *
 * @author Marcin
 */
public class ISEnsembleRndFeatureOperator extends AbstractISEnsembleOperator {

    private static final String PARAMETER_RATIO = "Sampling ratio";
    private ArrayList<Attribute> list;
    private double ratio;
    private transient RandomGenerator random;

    public ISEnsembleRndFeatureOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {
        super.initializeProcessExamples(exampleSet);
        ratio = getParameterAsDouble(PARAMETER_RATIO);
        random = RandomGenerator.getRandomGenerator(this);
        Attributes attributes = exampleSet.getAttributes();
        list = new ArrayList<>(attributes.size());
        for (Attribute attribute : attributes) {
            list.add(attribute);
        }
    }

    @Override
    protected ExampleSet preProcessExampleSet(ExampleSet trainingSet) {
        ExampleSet trainingSubSet = (ExampleSet) trainingSet.clone();
        Attributes attributes = trainingSubSet.getAttributes();
        int size = attributes.size();
        int sizeToRemove = (int) Math.round(size * (1 - ratio));
        Set<Integer> idxSet = random.nextIntSetWithRange(0, size, sizeToRemove);
        for (int i : idxSet) {
            attributes.remove(list.get(i));
        }
        return trainingSubSet;
    }

    /**
     * FInalize ensemble instance selection
     */
    @Override
    public void finalizeProcessExamples() {
        //Clear list of attributes
        list = null;
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
