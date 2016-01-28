/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.misc;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class AssymetricGroupDistance implements GroupSimilarity {

    DistanceMeasure measure;

    public AssymetricGroupDistance(DistanceMeasure measure) {
        this.measure = measure;
    }

    @Override
    public double calculate(ExampleSet exampleSet1, ExampleSet exampleSet2) {
        if (exampleSet1.size() == 0 || exampleSet2.size() == 0) return Double.NaN;
        double totalDistance = 0;
        for (Example e1 : exampleSet1) {
            double distance = Double.MAX_VALUE;
            for (Example e2 : exampleSet2) {
                double tmpDist = measure.calculateDistance(e1, e2);
                if (tmpDist < distance){
                    distance = tmpDist;
                }                
            }
            totalDistance += distance;
        }
        totalDistance /= exampleSet1.size();
        return totalDistance;
    }
}
