/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.feature.selection;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.operator.*;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.*;
import com.rapidminer.parameter.conditions.EqualTypeCondition;

import java.util.List;

/**
 * <p>
 * <code>FeatureWeightsTransformerOperator</code> Transforms input feature weights into new feature weights.
 *
 * <p>
 * For example it can be used for transforming weights obtained from some feature weighting method into simple
 * 0 / 1 weights that can be used for feature selection (without extra weighting). In feature plans we think to
 * add extra feature that will be able to transform feature weights by given math formula ex. <var>weight^2</var>
 *
 * <p>
 * This operator has two internal states that are number of features and string representing feature weights.
 * </p>
 *
 * @author Marcin Blachnik
 */
public class FeatureWeightsTransformerOperator extends Operator {

    /**
     * The parameter name for &quot;Use this weight for the selection relation.&quot;
     */
    private static final String PARAMETER_WEIGHT = "weight";
    /**
     * The parameter name for &quot;Selects only weights which fulfill this relation.&quot;
     */
    private static final String PARAMETER_WEIGHT_RELATION = "weight_relation";
    /**
     * The parameter name for &quot;Number k of attributes to be selected for weight-relations 'top k' or 'bottom k'.&quot;
     */
    public static final String PARAMETER_K = "k";
    /**
     * The parameter name for &quot;Percentage of attributes to be selected for weight-relations 'top p%' or 'bottom p%'.&quot;
     */
    private static final String PARAMETER_P = "p";
    /**
     * The parameter name for &quot;Indicates if the absolute values of the weights should be used for comparison.&quot;
     */
    private static final String PARAMETER_USE_ABSOLUTE_WEIGHTS = "use_absolute_weights";
    private static final String[] WEIGHT_RELATIONS = {"greater", "greater equals", "equals", "less equals", "less", "top k", "bottom k", "all but top k", "all but bottom k", "top p%", "bottom p%"};
    private static final int GREATER = 0;
    private static final int GREATER_EQUALS = 1;
    private static final int EQUALS = 2;
    private static final int LESS_EQUALS = 3;
    private static final int LESS = 4;
    private static final int TOP_K = 5;
    private static final int BOTTOM_K = 6;
    private static final int ALL_BOTTOM_PK = 7;
    private static final int ALL_BOTTOM_K = 8;
    private static final int TOP_PERCENT = 9;
    private static final int BOTTOM_PERCENT = 10;
    private InputPort weightsInputPort = getInputPorts().createPort("attribute weights", AttributeWeights.class);
    private OutputPort weightsOutputPort = getOutputPorts().createPort("attribute weights");
    private String stringRepresentationOfFeatureWeights;
    private int selectedFeaturesCount;

    /**
     * @param description
     */
    public FeatureWeightsTransformerOperator(OperatorDescription description) {
        super(description);
        addValue(new ValueString("FeatureWeights", "Obtained feature weights") {
            @Override
            public String getStringValue() {
                return FeatureWeightsTransformerOperator.this.stringRepresentationOfFeatureWeights;
            }
        });

        addValue(new ValueDouble("FeaturesCount", "Number of selected features") {
            @Override
            public double getDoubleValue() {
                return FeatureWeightsTransformerOperator.this.selectedFeaturesCount;
            }
        });
        getTransformer().addPassThroughRule(weightsInputPort, weightsOutputPort);
    }

    @Override
    public void doWork() throws OperatorException {
        super.doWork();
        AttributeWeights inputWeights = weightsInputPort.getData(AttributeWeights.class);
        AttributeWeights attributeWeights = processWeights(inputWeights);
        weightsOutputPort.deliver(attributeWeights);
    }

    private AttributeWeights processWeights(AttributeWeights weights) throws OperatorException {
        AttributeWeights newWeights = (AttributeWeights) weights.clone();
        double relationWeight = getParameterAsDouble(PARAMETER_WEIGHT);
        int relation = getParameterAsInt(PARAMETER_WEIGHT_RELATION);
        boolean useAbsoluteWeights = getParameterAsBoolean(PARAMETER_USE_ABSOLUTE_WEIGHTS);

        selectedFeaturesCount = 0;

        double threshold = 0;
        int nrAtts = newWeights.size();

        String[] attributeNames = newWeights.getAttributeNames().toArray(new String[nrAtts]);

        int direction;
        int comparatorType = AttributeWeights.ORIGINAL_WEIGHTS;
        if (useAbsoluteWeights)
            comparatorType = AttributeWeights.ABSOLUTE_WEIGHTS;

        switch (relation) {
            case GREATER:
            case GREATER_EQUALS:
            case EQUALS:
            case LESS_EQUALS:
            case LESS:
                threshold = getParameterAsDouble(PARAMETER_WEIGHT);
                break;
            case BOTTOM_K:
                direction = AttributeWeights.DECREASING;
                threshold = getParameterAsInt(PARAMETER_K);
                weights.sortByWeight(attributeNames, direction, comparatorType);
                break;
            case TOP_K:
                direction = AttributeWeights.INCREASING;
                threshold = getParameterAsInt(PARAMETER_K);
                weights.sortByWeight(attributeNames, direction, comparatorType);
                break;
            case ALL_BOTTOM_PK:
                direction = AttributeWeights.DECREASING;
                threshold = nrAtts - getParameterAsInt(PARAMETER_K);
                weights.sortByWeight(attributeNames, direction, comparatorType);
                break;
            case ALL_BOTTOM_K:
                direction = AttributeWeights.INCREASING;
                threshold = nrAtts - getParameterAsInt(PARAMETER_K);
                weights.sortByWeight(attributeNames, direction, comparatorType);
                break;
            case TOP_PERCENT:
                direction = AttributeWeights.INCREASING;
                threshold = getParameterAsDouble(PARAMETER_P);
                weights.sortByWeight(attributeNames, direction, comparatorType);
                break;
            case BOTTOM_PERCENT:
                direction = AttributeWeights.DECREASING;
                threshold = getParameterAsDouble(PARAMETER_P);
                weights.sortByWeight(attributeNames, direction, comparatorType);
                break;
        }

        int i = 0;
        double step = 1.0 / nrAtts;
        StringBuilder strAttrName = new StringBuilder();
        StringBuilder strWeights = new StringBuilder();
        for (String attrName : attributeNames) {
            double weight = newWeights.getWeight(attrName);
            if (Double.isNaN(weight))
                newWeights.setWeight(attrName, 0);
            if (useAbsoluteWeights)
                newWeights.setWeight(attrName, Math.abs(weight));
            switch (relation) {
                case GREATER:
                    weight = weight > threshold ? 1 : 0;
                    break;
                case GREATER_EQUALS:
                    weight = weight >= threshold ? 1 : 0;
                    break;
                case EQUALS:
                    weight = weight == threshold ? 1 : 0;
                    break;
                case LESS_EQUALS:
                    weight = weight <= threshold ? 1 : 0;
                    break;
                case LESS:
                    weight = weight < threshold ? 1 : 0;
                    break;
                case TOP_K:
                case BOTTOM_K:
                case ALL_BOTTOM_PK:
                case ALL_BOTTOM_K:
                    weight = i < threshold ? 1 : 0;
                    break;
                case TOP_PERCENT:
                case BOTTOM_PERCENT:
                    weight = ((i + 1) * step) <= threshold ? 1 : 0;
                    break;
            }
            newWeights.setWeight(attrName, weight);
            selectedFeaturesCount += (weight != 0) & (!Double.isNaN(weight)) & (!Double.isInfinite(weight)) ? 1 : 0;
            strAttrName.append(attrName);
            strAttrName.append(";");
            strWeights.append(weight);
            strWeights.append(";");
            i++;
        }
        stringRepresentationOfFeatureWeights = strAttrName + " \n " + strWeights.toString();
        return newWeights;
    }

    /**
     * @return
     */
    public InputPort getWeightsInputPort() {
        return weightsInputPort;
    }

    /**
     * @return
     */
    public OutputPort getWeightsOutputPort() {
        return weightsOutputPort;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeCategory(PARAMETER_WEIGHT_RELATION, "Selects only weights which fulfill this relation.", WEIGHT_RELATIONS, GREATER_EQUALS);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_WEIGHT, "The selected relation will be evaluated against this value.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_WEIGHT_RELATION, WEIGHT_RELATIONS, true, GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, EQUALS));
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_K, "Number k of attributes to be selected. For example 'top k' with k = 5 will return an example set containing only the 5 highest weighted attributes.", 1, Integer.MAX_VALUE, 10);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_WEIGHT_RELATION, WEIGHT_RELATIONS, true, TOP_K, BOTTOM_K, ALL_BOTTOM_K, ALL_BOTTOM_PK));
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeDouble(PARAMETER_P, "Percentage of attributes to be selected. For example 'top p%' with p = 15 will return an example set containing only attributes which are part of the 15% of the highest weighted attributes.", 0.0d, 1.0d, 0.5d);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_WEIGHT_RELATION, WEIGHT_RELATIONS, true, TOP_PERCENT, BOTTOM_PERCENT));
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeBoolean(PARAMETER_USE_ABSOLUTE_WEIGHTS, "Indicates if the absolute values of the weights should be used for comparison.", true));
        return types;
    }
}
