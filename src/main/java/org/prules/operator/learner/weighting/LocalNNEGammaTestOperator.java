/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import org.prules.operator.learner.weighting.models.GammaTestNoiseModel;
import org.prules.tools.math.container.PairContainer;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
//import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

import java.util.List;

import org.prules.operator.learner.weighting.models.LocalGammaTestNoiseModel;

/**
 * @author Marcin
 */
public class LocalNNEGammaTestOperator extends AbstractWeightingOperator {

    public static final String PARAMETER_K = "k";
    public static final String PARAMETER_RANGE = "Range";
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
    private int k;
    private int range;
    private double nne;
    private double nneSlope;
    //private boolean transformWeights;

    public LocalNNEGammaTestOperator(OperatorDescription description) {
        super(description, Ontology.ATTRIBUTE_NOISE, Ontology.ATTRIBUTE_NOISE);
        addValue(new ValueDouble("Nonparametric Noise Estimation", "The level of noise") {
            @Override
            public double getDoubleValue() {
                return nne;
            }
        });

        addValue(new ValueDouble("Nonparametric Noise Estimation Slope", "The slope of the noise") {

            @Override
            public double getDoubleValue() {
                return nneSlope;
            }
        });

        this.getLogger().info("======================== 12345");

        exampleSetInputPort.addPrecondition(
                new SimplePrecondition(exampleSetInputPort, new MetaData(), true) {
                    @Override
                    public void makeAdditionalChecks(MetaData received) {
                        if (received != null && received instanceof ExampleSetMetaData) {
                            ExampleSetMetaData emd = (ExampleSetMetaData) received;
                            switch (emd.hasSpecial(Attributes.LABEL_NAME)) {
                                case NO:
                                    exampleSetInputPort.addError(new SimpleMetaDataError(ProcessSetupError.Severity.WARNING, exampleSetInputPort, "special_missing", Attributes.LABEL_NAME));
                                    break;
                                case YES:
                                    if (!emd.getLabelMetaData().isNumerical()) {
                                        exampleSetInputPort.addError(new SimpleMetaDataError(ProcessSetupError.Severity.WARNING, exampleSetInputPort, "special_attribute_has_wrong_type", emd.getLabelMetaData().getName(), Attributes.LABEL_NAME, com.rapidminer.tools.Ontology.VALUE_TYPE_NAMES[com.rapidminer.tools.Ontology.NUMERICAL]));
                                    }
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void processExamples(ExampleSet exampleSet) throws OperatorException {
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet);
        k = getParameterAsInt(PARAMETER_K);
        range = getParameterAsInt(PARAMETER_RANGE);
        LocalGammaTestNoiseModel model = new LocalGammaTestNoiseModel(distance, k, range);
        PairContainer<double[], double[]> container = model.run(exampleSet);
        double[] noise = container.getFirst();
        double[] noiseSlope = container.getSecond();
        nne = model.getNNE();
        nneSlope = model.getNNESlope();
        Attributes attributes = exampleSet.getAttributes();
        Attribute noiseAttribute = attributes.getSpecial(Ontology.ATTRIBUTE_NOISE);
        Attribute noiseSlopeAttribute = AttributeFactory.createAttribute(Ontology.ATTRIBUTE_NOISE_SLOPE, com.rapidminer.tools.Ontology.NUMERICAL);
        exampleSet.getExampleTable().addAttribute(noiseSlopeAttribute);
        attributes.setSpecialAttribute(noiseSlopeAttribute, Ontology.ATTRIBUTE_NOISE_SLOPE);
        int i = 0;
        for (Example example : exampleSet) {
            example.setValue(noiseAttribute, noise[i]);
            example.setValue(noiseSlopeAttribute, noiseSlope[i]);
            i++;
        }
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception e) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            case NUMERICAL_LABEL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_K, "The number of nearest neighbors.", 3, Integer.MAX_VALUE, 10);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_RANGE, "Range of the local NNE estimation.", 3, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));

        return types;
    }
}
