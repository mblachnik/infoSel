/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.meta;

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
public class ISMetaRndFeatureOperator extends AbstractISMetaOperator {

    public static final String PARAMETER_RATIO = "Sampling ratio";

    public ISMetaRndFeatureOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    ExampleSet prepareExampleSet(ExampleSet trainingSet) throws OperatorException {
        RandomGenerator random = RandomGenerator.getRandomGenerator(this);
        double ratio = getParameterAsDouble(PARAMETER_RATIO);
        ExampleSet trainingSubSet = (ExampleSet) trainingSet.clone();
        Attributes attributes = trainingSubSet.getAttributes();
        int size = (int) Math.round(attributes.size() * ratio);
        int attributesToRemove = attributes.size() - size;
        ArrayList<Attribute> list = new ArrayList<Attribute>(attributes.size());
        for (Attribute attribute : attributes) {
            list.add(attribute);
        }
        for (int i = 0; i < attributesToRemove; i++) {
            int attributeID = random.nextInt(attributes.size());
            attributes.remove(list.get(attributeID));
        }
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
