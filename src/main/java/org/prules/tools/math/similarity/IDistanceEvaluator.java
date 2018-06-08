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
public interface IDistanceEvaluator {    
    
    DistanceMeasure getDistance() ;
    double evaluateDistance(Instance values1, Instance values2);
    double evaluateDistance(Vector values1, Vector values2);
    
}
