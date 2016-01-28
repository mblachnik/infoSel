/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.misc;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttribute;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class GroupDistanceOperator extends Operator implements CapabilityProvider{

    public static final String ATTRIBUTE_NAME_PARAMETER = "Attribute name";
    /**
     * Input port which delivers training ExampleSet
     */
    protected final InputPort exampleSetInputPort = getInputPorts().createPort("ExampleSet");
    /**
     * Output port which returns an initial ExampleSet
     */
    protected final OutputPort exampleSetOutputPort = getOutputPorts().createPassThroughPort("ExampleSet");
    DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    public GroupDistanceOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void doWork() throws OperatorException {
        ExampleSet exampleSet = exampleSetInputPort.getData(ExampleSet.class);        
        String attributeName = getParameterAsString(ATTRIBUTE_NAME_PARAMETER);
        Attributes attributes = exampleSet.getAttributes();
        Attribute grupingAttribute = attributes.get(attributeName);
        NominalMapping map = grupingAttribute.getMapping();
        int mapSize = map.size();
        ArrayList<Attribute> list = new ArrayList<Attribute>(mapSize+1);
        Attribute idAttribute = AttributeFactory.createAttribute("id", Ontology.POLYNOMINAL);
        list.add( idAttribute );        
        for(String labelValue : map.getValues()){
            list.add( AttributeFactory.createAttribute(labelValue, Ontology.NUMERICAL) );
        }        
        NominalMapping idMap = (NominalMapping)map.clone();
        idAttribute.setMapping(idMap);
        ExampleTable returnTable = new MemoryExampleTable(list, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), mapSize);
        ExampleSet returnSet = new SimpleExampleSet(returnTable, list);
        Attributes returnSetAttributes = returnSet.getAttributes();
        returnSetAttributes.setSpecialAttribute(idAttribute, "id");
        
        
        DistanceMeasure distance = measureHelper.getInitializedMeasure(returnSet);
        GroupSimilarity similarity = new AssymetricGroupDistance(distance);
        
        int j,i = 0;
        Example example1,example2;
        for(String conditionValue1 : map.getValues()){            
            Condition condition1 = new AttributeValueFilterSingleCondition(grupingAttribute,AttributeValueFilterSingleCondition.EQUALS,conditionValue1);
            ConditionedExampleSet groupExammpleSet1 = new ConditionedExampleSet(exampleSet,condition1);
            example1 = returnSet.getExample(i);   
            example1.setValue(idAttribute, idMap.mapIndex(i));
            i++;
            j = 0;
            for(String conditionValue2 : map.getValues()){
                Condition condition2 = new AttributeValueFilterSingleCondition(grupingAttribute,AttributeValueFilterSingleCondition.EQUALS,conditionValue2);
                ConditionedExampleSet groupExammpleSet2 = new ConditionedExampleSet(exampleSet,condition2);                            
                double value1 = similarity.calculate(groupExammpleSet1,groupExammpleSet2);                
                example1.setValue(returnSetAttributes.get(conditionValue2), value1);
                double value2 = similarity.calculate(groupExammpleSet2,groupExammpleSet1);
                example2 = returnSet.getExample(j);
                example2.setValue(returnSetAttributes.get(conditionValue1), value2);
                j++;
            }
        }                
        exampleSetOutputPort.deliver(returnSet);
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        //ParameterType type = new ParameterTypeString(ATTRIBUTE_NAME_PARAMETER, "Gruping attribute");        
        ParameterType type = new ParameterTypeAttribute(ATTRIBUTE_NAME_PARAMETER, "Gruping attribute", exampleSetInputPort);
        types.add(type);
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
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
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }
}
