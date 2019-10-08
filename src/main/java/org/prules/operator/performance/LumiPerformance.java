/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.performance;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.performance.AbstractExampleSetEvaluator;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import smile.validation.AdjustedRandIndex;
import smile.validation.RandIndex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin
 */
public class LumiPerformance extends AbstractExampleSetEvaluator {

    public static final String PARAMETER_OPTIMIZATION_DIRECTION = "optimization_direction";

    private InputPort protoSetInput = getInputPorts().createPort("prototype set", ExampleSet.class);
    private double adjustedRandIndex = Double.NaN;
    private double randIndex = Double.NaN;
    private double vMeasureScore = Double.NaN;
    protected DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    public LumiPerformance(OperatorDescription description) {
        super(description);
        addValue(new ValueDouble("adjusted_rand_index", "Adjusted Rand Index") {
            @Override
            public double getDoubleValue() {
                return adjustedRandIndex;
            }
        });

        addValue(new ValueDouble("rand_index", "Rand Index") {
            @Override
            public double getDoubleValue() {
                return randIndex;
            }
        });

        addValue(new ValueDouble("v_measure_score", "V-Measure score") {
            @Override
            public double getDoubleValue() {
                return vMeasureScore;
            }
        });
    }

    @Override
    public PerformanceVector evaluate(ExampleSet exampleSet) throws OperatorException {
        PerformanceVector performanceCriteria = new PerformanceVector();
        ExampleSet prototypes = protoSetInput.getDataOrNull(ExampleSet.class);
        if (prototypes != null) count(prototypes, exampleSet);
        EstimatedPerformance adjustRandIndex = new EstimatedPerformance("adjust_rand_index", adjustedRandIndex, 1, true);
        performanceCriteria.addCriterion(adjustRandIndex);
        EstimatedPerformance randIndex = new EstimatedPerformance("rand_index", this.randIndex, 1, true);
        performanceCriteria.addCriterion(randIndex);
        EstimatedPerformance vMeasure = new EstimatedPerformance("v_measure_score", this.vMeasureScore, 1, true);
        performanceCriteria.addCriterion(vMeasure);
        return performanceCriteria;
    }

    private void count(ExampleSet exampleSet1, ExampleSet exampleSet2) throws OperatorException {
        Map<String, Integer> labelIndex1 = new HashMap<>();
        Map<String, Integer> labelIndex2 = new HashMap<>();
        Attribute label1 = exampleSet1.getAttributes().getLabel();
        Attribute label2 = exampleSet2.getAttributes().getLabel();

        int[] exampleSetLabels1 = new int[exampleSet1.size()];
        int[] exampleSetLabels2 = new int[exampleSet2.size()];

        for (String label : label1.getMapping().getValues()) {
            labelIndex1.put(label, labelIndex1.size());
        }

        for (String label : label2.getMapping().getValues()) {
            labelIndex2.put(label, labelIndex2.size());
        }

        Iterator<Example> exampleIterator1 = exampleSet1.iterator();
        int i = 0;
        while (exampleIterator1.hasNext()) {
            Example example = exampleIterator1.next();
            exampleSetLabels1[i] = labelIndex1.get(example.getValueAsString(label1));
            i++;
        }

        i = 0;
        Iterator<Example> exampleIterator2 = exampleSet2.iterator();
        while (exampleIterator2.hasNext()) {
            Example example = exampleIterator2.next();
            exampleSetLabels2[i] = labelIndex2.get(example.getValueAsString(label2));
            i++;
        }

        AdjustedRandIndex adjustedRandIndex = new AdjustedRandIndex();
        this.adjustedRandIndex = adjustedRandIndex.measure(exampleSetLabels1, exampleSetLabels2);

        RandIndex randIndex = new RandIndex();
        this.randIndex = randIndex.measure(exampleSetLabels1, exampleSetLabels2);

        VMeasureScore vMeasure = new VMeasureScore();
        this.vMeasureScore = vMeasure.measure(exampleSetLabels1, exampleSetLabels2);
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
