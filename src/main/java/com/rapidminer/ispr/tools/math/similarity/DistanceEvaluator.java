/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.tools.math.similarity;

import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class DistanceEvaluator {
    public static double evaluateDistance(DistanceMeasure distance, Instance values1, Instance values2){
        return distance.calculateDistance(values1.getValues(), values2.getValues());
    }
}
