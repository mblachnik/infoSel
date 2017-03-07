/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.tools;

import org.prules.operator.learner.selection.AbstractInstanceSelectorOperator;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.RandomGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class InstanceModifierHelper {

    public static final String PARAMETER_INSTANCE_MODIFIER_TYPE = "Instance modifier types";
    public static final String PARAMETER_INSTANCE_MODIFIER_NOISE_LEVEL = "Level of noise";

    public static List<ParameterType> getParameterTypes(AbstractInstanceSelectorOperator operator) {
        List<ParameterType> types = new ArrayList<>();
        ParameterType type;
        type = new ParameterTypeCategory(PARAMETER_INSTANCE_MODIFIER_TYPE, "Defines type of possible instance modification which is applied on the fly while evaluating instance selection method.", InstanceModifierTypes.getDescriptions(), 0, true);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_INSTANCE_MODIFIER_NOISE_LEVEL, "Level of noise", 0, Double.MAX_VALUE, 0.05);
        type.setExpert(true);
        type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_INSTANCE_MODIFIER_TYPE, InstanceModifierTypes.getDescriptions(), false,
                InstanceModifierTypes.GAUSSIAN_NOISE.ordinal()));
        types.add(type);

        if (!operator.isSampleRandomize()) {
            List<ParameterType> rgTypes = RandomGenerator.getRandomGeneratorParameters(operator);
            for (ParameterType rgType : rgTypes) {
                rgType.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_INSTANCE_MODIFIER_TYPE, InstanceModifierTypes.getDescriptions(), false,
                        InstanceModifierTypes.GAUSSIAN_NOISE.ordinal()));
            }
            types.addAll(rgTypes);
        }
        return types;
    }

    public static InstanceModifier getConfiguredInstanceModifier(AbstractInstanceSelectorOperator operator) throws UndefinedParameterError {
        int idIdentifierType = operator.getParameterAsInt(PARAMETER_INSTANCE_MODIFIER_TYPE);
        InstanceModifierTypes modifierType = InstanceModifierTypes.values()[idIdentifierType];
        InstanceModifier modifier = null;
        switch (modifierType) {
            case NONE:
                modifier = new EmptyInstanceModifier();
                break;
            case GAUSSIAN_NOISE:
                double noiseLevel = operator.getParameterAsDouble(PARAMETER_INSTANCE_MODIFIER_NOISE_LEVEL);
                RandomGenerator generator = RandomGenerator.getRandomGenerator(operator);
                modifier = new GaussianNoiseInstanceModifier(noiseLevel, generator);
        }
        return modifier;
    }
}
