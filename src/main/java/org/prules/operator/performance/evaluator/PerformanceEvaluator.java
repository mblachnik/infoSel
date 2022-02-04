package org.prules.operator.performance.evaluator;

public interface PerformanceEvaluator {
    double getPerformance(double[] trueLabels, double[] predictions);
}
