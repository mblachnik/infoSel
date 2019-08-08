package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Created by Łukasz Migdałek on 2016-07-13.
 */
public class SCSVQModel extends SRVQModel {

	private double[][][][] previousDistFunc;
	private int prototypeIndex, prototypeAttIndex;

	public SCSVQModel(ExampleSet prototypes, int iterations, DistanceMeasure measure, double alpha, double temperature, double temperatureRate) throws OperatorException {
		super(prototypes, iterations, measure, alpha, temperature, temperatureRate);
	}

	@Override
	public ExampleSet run(ExampleSet trainingSet) {
		int trainingSetSize = trainingSet.getExampleTable().size();
		this.previousDistFunc = new double[trainingSetSize][numberOfPrototypes][attributesSize][1];
		return super.run(trainingSet);
	}

	@Override
	public void update() {
		prototypeIndex = 0;
		for (double[] prototype : prototypeValues) {
			prototypeAttIndex = 0;

			for (int i = 0; i < getAttributesSize(); i++) {
				sum = calcSum();
				double value = prototype[i];
				double x_minus_wi = exampleValues[i] - value;
				double up_part_calc = Math.exp((-1 * Math.pow(euclideanDistance.calculateDistance(exampleValues, prototype), 2)) / temperature);
				double gi = distributionFunctionCalc(up_part_calc);
				incrementLearningRate(gi);
				double alpha = calcLearningRate();
				double calc_value = alpha * gi * (x_minus_wi);
				value += calc_value;
				prototype[i] = value;
				prototypeAttIndex++;
			}
			prototypeIndex++;
		}

	}

	@Override
	protected double distributionFunctionCalc(double changeName) {
		// Prevent before NaN.
		if (changeName == 0 || sum == 0) {
			return 0;
		}
		double pi = changeName / sum;
		return pi;
	}

	@Override
	public synchronized boolean nextIteration() {
		currentIteration++;
		temperature *= temperatureRate;
		return currentIteration < iterations;
	}

	protected double calcLearningRate() {
		double learningRate = 1 / (1 + previousDistFunc[trainingExampleIndex][prototypeIndex][prototypeAttIndex][0]);
		return learningRate;
	}

	protected void incrementLearningRate(double gi) {
		previousDistFunc[trainingExampleIndex][prototypeIndex][prototypeAttIndex][0] += gi;
	}


}
