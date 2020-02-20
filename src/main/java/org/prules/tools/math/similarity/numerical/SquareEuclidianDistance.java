package org.prules.tools.math.similarity.numerical;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * The Manhattan distance.
 * 
 * @author Sebastian Land, Michael Wurst
 */
public class SquareEuclidianDistance extends DistanceMeasure {

	private static final long serialVersionUID = -6657784365192589335L;

	@Override
	public double calculateDistance(double[] value1, double[] value2) {
		double sum = 0.0;
		int counter = 0;
		for (int i = 0; i < value1.length; i++) {
			if ((!Double.isNaN(value1[i])) && (!Double.isNaN(value2[i]))) {
				double diff = value1[i] - value2[i];
				sum += diff * diff;
				counter++;
			}
		}
		if (counter > 0) {
			return sum;
		} else {
			return Double.NaN;
		}
	}

	@Override
	public double calculateSimilarity(double[] value1, double[] value2) {
		return -calculateDistance(value1, value2);
	}

	@Override
	public void init(ExampleSet exampleSet) throws OperatorException {
	    super.init(exampleSet);
	    Tools.onlyNumericalAttributes(exampleSet, "value based similarities");
	}

	@Override
	public String toString() {            
		return "Squared Euclidian distance";                   
	}

        
}
