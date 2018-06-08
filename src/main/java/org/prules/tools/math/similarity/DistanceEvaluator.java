/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.similarity;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class DistanceEvaluator implements IDistanceEvaluator {    
    DistanceMeasure distance;

    public DistanceEvaluator(DistanceMeasure distance){
        this.distance = distance;
    }
            
    @Override
    public DistanceMeasure getDistance() {
        return distance;
    }
    
    @Override
    public double evaluateDistance(Instance values1, Instance values2){
        return evaluateDistance(values1.getVector(), values2.getVector());
    }
        
    @Override
    public double evaluateDistance(Vector values1, Vector values2){
        return distance.calculateDistance(values1.getValues(), values2.getValues());
    }
}
