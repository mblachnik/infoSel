package org.prules.operator.performance.evaluator;

public class Accuracy implements PerformanceEvaluator{
    @Override
    public double getPerformance(double[] trueLabels, double[] predictions) {
        int counter = 0;
        for (int i =0; i<trueLabels.length; i++){
            counter += trueLabels[i] == predictions[i] ? 1 : 0;
        }
        double accuracy = ((double)counter)/trueLabels.length;
        return accuracy;
    }
}
