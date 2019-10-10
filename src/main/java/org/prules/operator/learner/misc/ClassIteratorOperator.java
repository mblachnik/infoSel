/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.*;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.PRulesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin
 */
public class ClassIteratorOperator extends OperatorChain {

    private final InputPort exampleSetInputPort = getInputPorts().createPort("exampleSet");
    private final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("example Set");
    private final InputPort prototypesInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("prototypes");
    private final OutputPort prototypesOutputPort = getOutputPorts().createPort("prototypes");
    private final OutputPort exampleSetOutputPort = getOutputPorts().createPort("example Set");
    //CollectingPortPairExtender outExtender = new CollectingPortPairExtender("out", getSubprocess(0).getInnerSinks(), getOutputPorts());

    /**
     * @param description
     */
    public ClassIteratorOperator(OperatorDescription description) {
        super(description, "Iteration");

        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
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
        }, exampleSetInputPort));
        prototypesInnerSourcePort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
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
        }, prototypesInnerSourcePort));
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, exampleInnerSourcePort, SetRelation.EQUAL));
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, exampleSetOutputPort, SetRelation.EQUAL));
        getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
        getTransformer().addRule(new PassThroughRule(prototypesInnerSourcePort, prototypesOutputPort, false) {
            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData) {
                    //Tutaj trzeba dodac przetwarzanie metadanych dot. zbioru prototypów
                    return metaData;
                } else {
                    return metaData;
                }
            }
        });

    }

    @Override
    public void doWork() throws OperatorException {
        ExampleSet exampleSet = exampleSetInputPort.getData(ExampleSet.class);
        Attributes attributes = exampleSet.getAttributes();
        Attribute label = attributes.getLabel();
        NominalMapping classLabels = label.getMapping();
        int classNumber = classLabels.size();
        boolean[][] indexes = new boolean[classNumber][exampleSet.size()];
        int j = 0;
        for (Example example : exampleSet) {
            indexes[(int) example.getLabel()][j] = true;
            j++;
        }
        List<ExampleSet> extractedSets = new ArrayList<ExampleSet>(classNumber);
        for (int i = 0; i < classNumber; i++) {
            SelectedExampleSet oneClassExampleSet = new SelectedExampleSet(exampleSet, new DataIndex(indexes[i]));
            exampleInnerSourcePort.deliver(oneClassExampleSet);
            getSubprocess(0).execute();
            inApplyLoop();
            ExampleSet extractedPrototypes = prototypesInnerSourcePort.getData(ExampleSet.class);
            extractedSets.add(extractedPrototypes);
        }
        ExampleSet outputSet = PRulesUtil.combineExampleSets(extractedSets);
        prototypesOutputPort.deliver(outputSet);
        exampleSetOutputPort.deliver(exampleSet);
    }
}

/*

package com.rapidminer.operator.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.CollectingPortPairExtender;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SubProcessTransformRule;
import com.rapidminer.parameter.UndefinedParameterError;

public class MultipleLabelIterator extends OperatorChain {

	private final InputPort exampleSetInput = getInputPorts().createPort("example set");
	private final OutputPort exampleInnerSource = getSubProcess(0).getInnerSources().createPort("example set");
	CollectingPortPairExtender outExtender = new CollectingPortPairExtender("out", getSubProcess(0).getInnerSinks(), getOutputPorts());

	public MultipleLabelIterator(OperatorDescription description) {
		super(description, "Iteration");
		outExtender.start();
		getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInput, exampleInnerSource, SetRelation.EQUAL) {
			@Override
			public ExampleSetMetaData modifyExampleSet(ExampleSetMetaData metaData) throws UndefinedParameterError {
				AttributeMetaData myLabel = metaData.getLabelMetaData();
				if (myLabel != null)
					metaData.removeAttribute(myLabel);
				for (AttributeMetaData amd : metaData.getAllAttributes()) {
					if (amd.getName().startsWith(Attributes.LABEL_NAME)) { 
						amd.setRole(Attributes.LABEL_NAME);
						break;
					}
				}					
				return metaData;
			}
		});
		getTransformer().addRule(new SubProcessTransformRule(getSubProcess(0)));
		getTransformer().addRule(outExtender.makePassThroughRule());
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet exampleSet = exampleSetInput.getData();

		Attribute[] labels = getLabels(exampleSet);
		if (labels.length == 0) {
			throw new UserError(this, 105);
		}

		outExtender.reset();
		for (int i = 0; i < labels.length; i++) {
			ExampleSet cloneSet = (ExampleSet) exampleSet.clone();
			cloneSet.getAttributes().setLabel(labels[i]);
			exampleInnerSource.deliver(cloneSet);

			getSubProcess(0).execute();
			outExtender.collect();            
			inApplyLoop();
		}
	}

	private Attribute[] getLabels(ExampleSet exampleSet) {
		List<Attribute> attributes = new LinkedList<Attribute>();
		Iterator<AttributeRole> i = exampleSet.getAttributes().specialAttributes();
		while (i.hasNext()) {
			AttributeRole role = i.next();
			String name = role.getSpecialName();
			if (name.startsWith(Attributes.LABEL_NAME)) {
				attributes.add(role.getAttribute());
			}
		}
		Attribute[] result = new Attribute[attributes.size()];
		attributes.toArray(result);
		return result;
	}
}
 */