/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.performance;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import com.rapidminer.ispr.operator.learner.weighting.models.AbstractNoiseEstimatorModel;
import com.rapidminer.ispr.operator.learner.weighting.models.GammaTestNoiseModel;
import com.rapidminer.ispr.operator.learner.weighting.models.DeltaTestNoiseModel;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughOrGenerateRule;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class NNEGammaTestPerformance extends Operator {

    private OutputPort performanceOutput = getOutputPorts().createPort("performance vector");
    private OutputPort exampleSetOutput = getOutputPorts().createPort("exampleSet");
    private InputPort exampleSetInput = getInputPorts().createPort("exampleSet", ExampleSet.class);
    private InputPort performanceInput = getInputPorts().createPort("performance vector");

    public static final String PARAMETER_K = "k";
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    double nne = Double.NaN;
    double nneSlope = Double.NaN;

    public NNEGammaTestPerformance(OperatorDescription description) {
        super(description);
        getTransformer().addPassThroughRule(exampleSetInput, exampleSetOutput);
        addValue(new ValueDouble("NNE G-test", "The level of noise") {

            @Override
            public double getDoubleValue() {
                return nne;
            }
        });
        
        addValue(new ValueDouble("NNE G-test slope", "The slope of the level of noise") {

            @Override
            public double getDoubleValue() {
                return nneSlope;
            }
        });
        
        performanceInput.addPrecondition(new SimplePrecondition(performanceInput, new MetaData(PerformanceVector.class), false));
	PassThroughOrGenerateRule performanceRule = new PassThroughOrGenerateRule(performanceInput, performanceOutput,new MetaData(PerformanceVector.class));
        getTransformer().addRule(performanceRule);
        exampleSetInput.addPrecondition(
                new SimplePrecondition(exampleSetInput, new ExampleSetMetaData(), true) {

                    @Override
                    public void makeAdditionalChecks(MetaData received) {
                        if (received instanceof ExampleSetMetaData) {
                            ExampleSetMetaData emd = (ExampleSetMetaData) received;
                            switch (emd.hasSpecial(Attributes.LABEL_NAME)) {
                                case NO:
                                    exampleSetInput.addError(new SimpleMetaDataError(ProcessSetupError.Severity.WARNING, exampleSetInput, "special_missing", Attributes.LABEL_NAME));
                                    break;
                                case YES:
                                    if (!emd.getLabelMetaData().isNumerical()) {
                                        exampleSetInput.addError(new SimpleMetaDataError(ProcessSetupError.Severity.WARNING, exampleSetInput, "special_attribute_has_wrong_type", emd.getLabelMetaData().getName() ,Attributes.LABEL_NAME, Ontology.VALUE_TYPE_NAMES[Ontology.NUMERICAL]));
                                    }
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void doWork() throws OperatorException {
        ExampleSet exampleSet = exampleSetInput.getDataOrNull(ExampleSet.class);
        exampleSetOutput.deliver(exampleSet);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet);
        int k = getParameterAsInt(PARAMETER_K);
        GammaTestNoiseModel model = new GammaTestNoiseModel(distance, k);
        model.run(exampleSet);
        nne = model.getNNE();      
        nneSlope = model.getNNESlope();      
        PerformanceVector performance;
        performance = performanceInput.getDataOrNull(PerformanceVector.class);
        if (performance==null) {
                performance = new PerformanceVector();
        }
        performance.addCriterion(
                new EstimatedPerformance("NNE G-test", nne, exampleSet.size(), true)
        );
        performance.addCriterion(
                new EstimatedPerformance("NNE G-test Slope", nneSlope, exampleSet.size(), true)
        );
        performanceOutput.deliver(performance);
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_K, "The number of nearest neighbors.", 2, Integer.MAX_VALUE, 2);
        type.setExpert(false);
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));

        return types;
    }
}
