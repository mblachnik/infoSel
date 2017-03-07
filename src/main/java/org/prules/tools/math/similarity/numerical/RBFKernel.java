package org.prules.tools.math.similarity.numerical;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * The Manhattan distance.
 *
 * @author Sebastian Land, Michael Wurst
 */
public class RBFKernel extends DistanceMeasure {

    private static final long serialVersionUID = -6657784365192589335L;

    @Override
    public double calculateDistance(double[] value1, double[] value2) {
        double d = 0.0;
        int counter = 0;
        for (int i = 0; i < value1.length; i++) {
            if ((!Double.isNaN(value1[i])) && (!Double.isNaN(value2[i]))) {
                double df = (value1[i] - value2[i]);
                d += df * df;
                counter++;
            }
        }
        if (counter > 0) {
            return Math.exp(-d);
        } else {
            return 0;
        }
    }

    @Override
    public double calculateSimilarity(double[] value1, double[] value2) {
        return 1 / calculateDistance(value1, value2) - 1;
    }

    @Override
    public void init(ExampleSet exampleSet) throws OperatorException {
        super.init(exampleSet);
        Tools.onlyNumericalAttributes(exampleSet, "value based similarities");
    }

    @Override
    public void init(ExampleSet exampleSet, ParameterHandler parameterHandler) throws OperatorException {
        super.init(exampleSet, parameterHandler);
    }

    @Override
    public boolean isDistance() {
        return false;
    }

    @Override
    public String toString() {
        return "RBF Kernel";
    }

}
