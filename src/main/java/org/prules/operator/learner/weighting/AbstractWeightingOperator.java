/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.weighting;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import org.prules.operator.AbstractPRulesBasicOperator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.tools.Ontology;

/**
 *
 * @author Marcin
 */
public abstract class AbstractWeightingOperator extends AbstractPRulesBasicOperator {

    String attributeName;
    String attributeRole;
    public AbstractWeightingOperator(OperatorDescription description) {
        this(description,Attributes.WEIGHT_NAME,Attributes.WEIGHT_NAME);
        
    }
    
    public AbstractWeightingOperator(OperatorDescription description, final String attributeName, final String attributeRole) {
        super(description);
        this.attributeRole = attributeRole;
        this.attributeName = attributeName;
        PassThroughRule addWeightRule = new PassThroughRule(exampleSetInputPort, exampleSetOutputPort, true) {
            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData){
                    ExampleSetMetaData esmd = (ExampleSetMetaData)metaData;
                    AttributeMetaData attribute= new AttributeMetaData(attributeName,Ontology.NUMERICAL,attributeRole);
                    esmd.addAttribute(attribute);
                }
                return metaData;
            }
        }; 
        getTransformer().addRule(addWeightRule);
    }

    /**
     * It overloads the executeOperator, and executes the processExamples
     * method.
     *
     * @param trainingSet - training set
     * @throws OperatorException
     */
    @Override
    public void executeOperator(ExampleSet trainingSet) throws OperatorException {
        ExampleSet exampleSet = (ExampleSet) trainingSet.clone();
        Attribute weightAttribute = AttributeFactory.createAttribute(this.attributeName, Ontology.NUMERICAL);
        exampleSet.getExampleTable().addAttribute(weightAttribute);
        exampleSet.getAttributes().setSpecialAttribute(weightAttribute,this.attributeRole);
        processExamples(exampleSet);
        exampleSetOutputPort.deliver(exampleSet);
    }

    /**
     * Method which implements main data process method.
     *
     * @param trainingSet - training set
     * @return
     * @throws OperatorException
     */
    public abstract void processExamples(ExampleSet trainingSet) throws OperatorException;
}
