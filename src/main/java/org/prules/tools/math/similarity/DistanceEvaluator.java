/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.similarity;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class DistanceEvaluator {
    public static double evaluateDistance(DistanceMeasure distance, Vector values1, Vector values2){
        return distance.calculateDistance(values1.getValues(), values2.getValues());
    }
}
