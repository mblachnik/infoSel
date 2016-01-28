/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.ispr.operator.performance;

import com.rapidminer.example.ExampleSet;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.MDLCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;


/**
 * Returns a performance vector just counting the number of support vectors 
 * of a given support vector based model (kernel model). Please note that
 * this operator will try to derive the number of support vectors of the
 * first delivered model and might fail on this task if no appropriate
 * kernel based model is delivered. Currently, at least the models delivered 
 * by the operator JMySVM, MyKLR, LibSVM, GPLearner, KernelLogisticRegression,
 * RVM, and the EvoSVM should be supported. 
 * 
 * @author Ingo Mierswa
 */
public class InstanceSSelectionPerformance extends Operator {

	/** The parameter name for &quot;Indicates if the fitness should for maximal or minimal number of features.&quot; */
	public static final String PARAMETER_OPTIMIZATION_DIRECTION = "optimization_direction";

	private double sizeReduction = Double.NaN; //Abs of difference betwenn sizes of example sets
        private double relativeSizeReduction = Double.NaN; //size reduction / size of exampleset 1
        private double compression = Double.NaN; //size of exampleset1 / sizeof exampleset2

	private InputPort exampleSet1Input = getInputPorts().createPort("exampleSet1", ExampleSet.class);
        private InputPort exampleSet2Input = getInputPorts().createPort("exampleSet2", ExampleSet.class);
	private InputPort performanceInput = getInputPorts().createPort("performance vector");

	private OutputPort exampleSet1Output = getOutputPorts().createPort("exampleSet1");
        private OutputPort exampleSet2Output = getOutputPorts().createPort("exampleSet2");
	private OutputPort performanceOutput = getOutputPorts().createPort("performance vector");

	public InstanceSSelectionPerformance(OperatorDescription description) {
		super(description);

		getTransformer().addGenerationRule(performanceOutput, PerformanceVector.class);
		getTransformer().addPassThroughRule(exampleSet1Input, exampleSet1Output);
                getTransformer().addPassThroughRule(exampleSet2Input, exampleSet2Output);

		addValue(new ValueDouble("size_reduction", "The difference between the sizes of the example sets.") {
			@Override
			public double getDoubleValue() {
				return sizeReduction; 
			}
		});
                addValue(new ValueDouble("relative_size_reduction", "The difference between the sizes of the example sets devided by the size of the first example set.") {
			@Override
			public double getDoubleValue() {
				return relativeSizeReduction; 
			}
		});
                addValue(new ValueDouble("compression", "Compressin calculated as the ratio between example sets sizes") {
			@Override
			public double getDoubleValue() {
				return compression; 
			}
		});
	}

	@Override
	public void doWork() throws OperatorException {
		ExampleSet exampleSet1 = exampleSet1Input.getData(ExampleSet.class);
                ExampleSet exampleSet2 = exampleSet2Input.getData(ExampleSet.class);		

		PerformanceVector inputPerformance = performanceInput.getDataOrNull(PerformanceVector.class);

		PerformanceVector performance = count(exampleSet1, exampleSet2, inputPerformance);

		exampleSet1Output.deliver(exampleSet1);
                exampleSet2Output.deliver(exampleSet2);
		performanceOutput.deliver(performance);
	}


	/**
	 * Creates a new performance vector if the given one is null. Adds a new estimated criterion. 
	 * If the criterion was already part of the performance vector before it will be overwritten.
	 */
	private PerformanceVector count(ExampleSet exampleSet1, ExampleSet exampleSet2, PerformanceVector performanceCriteria) throws OperatorException {
		if (performanceCriteria == null)
			performanceCriteria = new PerformanceVector();
                double size1 = exampleSet1.size();
                double size2 = exampleSet2.size();
                sizeReduction = Math.abs(size1 - size2);
                relativeSizeReduction = sizeReduction / size1;
                compression = size1 > size2 ? size2 / size1 : size1 / size2;
                
                /*
		this.lastCount = 0;
		int svNumber = model.getNumberOfSupportVectors();
		for (int i = 0; i < svNumber; i++) {
			SupportVector sv = model.getSupportVector(i);
			if (Math.abs(sv.getAlpha()) > 0.0d)
				this.lastCount++;
		}
                */ 
		EstimatedPerformance sizeReductionCriterion = new EstimatedPerformance("size_reduction", sizeReduction, 1, getParameterAsInt(PARAMETER_OPTIMIZATION_DIRECTION) == MDLCriterion.MINIMIZATION);
                EstimatedPerformance relativeReductionCriterion = new EstimatedPerformance("relative_size_reduction", relativeSizeReduction, 1, getParameterAsInt(PARAMETER_OPTIMIZATION_DIRECTION) == MDLCriterion.MINIMIZATION);
                EstimatedPerformance compressionCriterion = new EstimatedPerformance("compression", compression, 1, getParameterAsInt(PARAMETER_OPTIMIZATION_DIRECTION) == MDLCriterion.MINIMIZATION);
		performanceCriteria.addCriterion(sizeReductionCriterion);
                performanceCriteria.addCriterion(relativeReductionCriterion);
                performanceCriteria.addCriterion(compressionCriterion);
		return performanceCriteria;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeCategory(PARAMETER_OPTIMIZATION_DIRECTION, "Indicates if the fitness should be maximal for the maximal or the minimal number of the performance.", MDLCriterion.DIRECTIONS, MDLCriterion.MINIMIZATION));
		return types;
	}
}
