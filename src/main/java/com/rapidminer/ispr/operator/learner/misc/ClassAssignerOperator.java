/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.misc;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ClassAssignerOperator extends AbstractPRulesOperator {

    /**
     * 
     */
    protected final InputPort prototypesInputPort = getInputPorts().createPort("Prototpes");
    /** The parameter name for &quot;The used number of nearest neighbors.&quot; */
    public static final String PARAMETER_K = "k";
    private DistanceMeasureHelper measureHelper;

    /**
     * 
     * @param description
     */
    public ClassAssignerOperator(OperatorDescription description) {
        super(description);
        getTransformer().addPassThroughRule(prototypesInputPort, prototypesOutputPort);
        prototypesInputPort.addPrecondition(new DistanceMeasurePrecondition(prototypesInputPort, this));
        prototypesInputPort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {

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
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
        }, prototypesInputPort));
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     * 
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        ExampleSet prototypes = prototypesInputPort.getData(ExampleSet.class);
        prototypes = (ExampleSet)prototypes.clone();       
        int prototypesNumber = prototypes.size();
        int trainingNumber = trainingSet.size();
        Attributes trainingAttributes = trainingSet.getAttributes();
        Attributes prototypesAttributes = prototypes.getAttributes();
        Attribute oldLabel = prototypesAttributes.getLabel();
        if (oldLabel != null)
            prototypesAttributes.remove(oldLabel);        
        Attribute labelAttribute = AttributeFactory.createAttribute(trainingAttributes.getLabel());
        prototypesAttributes.addRegular(labelAttribute);
        prototypesAttributes.setLabel(labelAttribute);   
        prototypes.getExampleTable().addAttribute(labelAttribute);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        distance.init(trainingAttributes, prototypesAttributes);
        if (trainingAttributes.getLabel().isNominal()) {
            NominalMapping classMapping = trainingSet.getAttributes().getLabel().getMapping();
            int classNumber = classMapping.size();
            int[][] prototypeLabelRelation = new int[prototypesNumber][classNumber];

            for (Example example : trainingSet) {
                double minDist = Double.MAX_VALUE;
                int minIdx = -1;
                int label = -1;
                int i = 0;
                for (Example prototype : prototypes) {
                    double d = distance.calculateDistance(example, prototype);
                    if (d < minDist) {
                        minDist = d;
                        minIdx = i;
                        label = (int) example.getLabel();
                    }
                    i++;
                }
                /* Warning - here may appear a problem if classMapping has other values then 0 to classNumber-1
                 * If someone use NominalMapping it shouldnt but if they are created in any other way, then the following line would need to be changed
                 */
                if (label >= 0)
                    prototypeLabelRelation[minIdx][label]++;
            }            
            int j = 0;
            for (Example prototype : prototypes) {
                int max = Integer.MIN_VALUE;
                int idMax = -1;
                for (int i = 0; i < classNumber; i++) {
                    if (max < prototypeLabelRelation[j][i]) {
                        max = prototypeLabelRelation[j][i];
                        idMax = i;
                    }
                }
                prototype.setLabel(idMax);
                j++;
            }
        } else {            
            double[] instancesPerPrototypeLabelSum = new double[prototypesNumber];
            int[] instancesPerPrototypeLabelNum = new int[prototypesNumber];

            for (Example example : trainingSet) {
                double minDist = Double.MAX_VALUE;
                int minIdx = -1;
                double label = 0;
                int i = 0;
                for (Example prototype : prototypes) {
                    double d = distance.calculateDistance(example, prototype);
                    if (d < minDist) {
                        minDist = d;
                        minIdx = i;
                        label = example.getLabel();
                    }
                    i++;
                }
                instancesPerPrototypeLabelSum[minIdx] += label;
                instancesPerPrototypeLabelNum[minIdx]++;
            }             
            int j = 0;
            for (Example prototype : prototypes) {  
                double label = instancesPerPrototypeLabelSum[j]/instancesPerPrototypeLabelNum[j];
                prototype.setLabel(label);
                j++;
            }
        }
        return prototypes;
    }

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData emd) throws UndefinedParameterError {
        return emd.getNumberOfExamples();
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
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case NUMERICAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
