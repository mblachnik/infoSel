/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.preprocessing;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.operator.tools.AttributeSubsetSelector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.prules.operator.learner.preprocessing.model.VDMNominal2NumericalBuilderModel;
import org.prules.operator.learner.preprocessing.model.VDMNominal2NumericalModel;

/**
 * This class creates operator for VDM (Value Difference Metric) nominal to numerical data transformation
 * It takes input exampleSet and creates a probabilitMatric which is than used to
 * transform selected nominal attributes to numerical ones. In the VDM transformation each nominal attribute 
 * is converted into c-numerical attributes where c is the number of class labels in the label attribute.
 * @author Marcin
 */
public class VDMNominal2NumericalOperator extends Operator implements CapabilityProvider {        
    private final AttributeSubsetSelector attributeSelector = new AttributeSubsetSelector(this, getExampleSetInputPort());
    InputPort exampleSetInputPort = getInputPorts().createPort("exampleset", ExampleSet.class);
    OutputPort exampleSetOutputPort = getOutputPorts().createPort("exampleset");
    OutputPort preprocessingModelOutputPort = getOutputPorts().createPort("preprocessing model");
    OutputPort originalExampleSetOutputPort = getOutputPorts().createPort("original exampleset");

    /**
     * Creates operator for VDM data preprocessing
     * @param description 
     */
    public VDMNominal2NumericalOperator(OperatorDescription description) {
        super(description);
        getTransformer().addPassThroughRule(exampleSetInputPort, originalExampleSetOutputPort);
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(this, exampleSetInputPort));
        
        getTransformer().addRule(new PassThroughRule(exampleSetInputPort, exampleSetOutputPort, false) {
            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData) {
                    try {
                        return VDMNominal2NumericalOperator.this.modifyMetaData((ExampleSetMetaData) metaData);
                    } catch (UndefinedParameterError ex) {
                        return metaData;
                    }
                } else {
                    return metaData;
                }
            }
        });
        
        getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, preprocessingModelOutputPort, VDMNominal2NumericalRMModel.class));
    }
    
    /**
     * Returns input port with the exampleSet
     * @return 
     */
    public InputPort getExampleSetInputPort(){
        return exampleSetInputPort;
    }

    /**
     * Method used to process metadata related to the VDM model
     * @param metaData
     * @return
     * @throws UndefinedParameterError 
     */
    public ExampleSetMetaData modifyMetaData(ExampleSetMetaData metaData) throws UndefinedParameterError {        
        boolean includeSpecial = getParameterAsBoolean(AttributeSubsetSelector.PARAMETER_INCLUDE_SPECIAL_ATTRIBUTES);
        ExampleSetMetaData selectedAttributesExampleSet = attributeSelector.getMetaDataSubset(metaData, includeSpecial);        
        Collection<AttributeMetaData> selectedAttributes = selectedAttributesExampleSet.getAllAttributes();                
        if (selectedAttributes.size() > 0) {
            String[] attributeNames = new String[selectedAttributes.size()];
            int i = 0;
            for(AttributeMetaData attribute : selectedAttributes){
                attributeNames[i] = attribute.getName();
                i++;
            }             
            Set<String> labels = metaData.getLabelMetaData().getValueSet();
            for (String name : attributeNames) {
                for (String s : labels) {
                    String newName = name + "(" + s + ")";
                    AttributeMetaData attribute = new AttributeMetaData(newName, Ontology.NUMERICAL);
                    metaData.addAttribute(attribute);
                }
                metaData.removeAttribute(metaData.getAttributeByName(name));
            }
        }
        return metaData;
    }

    /**
     * Read exampleSet input port and construct preprocessing model. 
     * Finally the VDM preprocessing model is applied on the input exampleSet
     * @throws OperatorException 
     */
    @Override
    public void doWork() throws OperatorException {
        super.doWork();
        ExampleSet exampleSet = exampleSetInputPort.getData(ExampleSet.class);
        originalExampleSetOutputPort.deliver(exampleSet);
        ExampleSet vdmExampleSet = (ExampleSet) exampleSet.clone();
        boolean includeSpecial = getParameterAsBoolean(AttributeSubsetSelector.PARAMETER_INCLUDE_SPECIAL_ATTRIBUTES);
        Set<Attribute> selectedAttributes = attributeSelector.getAttributeSubset(exampleSet, includeSpecial);
        
        //String attributeNamesString = getParameterAsString(PARAMETER_ATTRIBUTES_TO_VDM);
        if (selectedAttributes.size() > 0) {
            String[] attributeNames = new String[selectedAttributes.size()];
            int i=0;
            for (Attribute attrTmp : selectedAttributes){
                attributeNames[i] = attrTmp.getName();
                i++;
            }
            VDMNominal2NumericalBuilderModel modelBuilder = new VDMNominal2NumericalBuilderModel(attributeNames);
            VDMNominal2NumericalModel model = modelBuilder.run(exampleSet);
            vdmExampleSet = model.run(vdmExampleSet);
            AbstractModel preprocessingRMModel = new VDMNominal2NumericalRMModel(exampleSet, model);
            preprocessingModelOutputPort.deliver(preprocessingRMModel);
        }
        exampleSetOutputPort.deliver(vdmExampleSet);
    }

   

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();        
        types.addAll(attributeSelector.getParameterTypes());        
        return types;
    }

}
