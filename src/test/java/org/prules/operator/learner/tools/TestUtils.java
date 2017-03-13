/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.numerical.EuclideanDistance;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Vector;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.LinearList;

/**
 *
 * @author Marcin
 */
public class TestUtils {
    
    public static final double[][] data = { {1, 2},
                            {1, 1},
                            {2.1, 1.1},
                            {2.1, 2.1},
                            {3, 3},
                            {3, 2.1},
                            {0.5, 0.5}};
    public static final double[] labels2 = {0, 0, 0, 1, 1, 1, 0};
    public static final double[] labels3 = {0, 1, 0, 0, 2, 2, 1};
    public static int n = data.length;
    public static ISPRClassGeometricDataCollection<IInstanceLabels> createSampleDataTwoClasses() {
        DistanceMeasure distance = new EuclideanDistance();
        ISPRClassGeometricDataCollection<IInstanceLabels> samples = new LinearList(distance);                
        for (int i = 0; i < n; i++) {
            Vector          v = InstanceFactory.createVector(data[i]);
            IInstanceLabels il = InstanceFactory.createInstanceLabels(labels2[i]);            
            samples.add(v, il);
        }
        return samples;        
    }

    public static ISPRClassGeometricDataCollection<IInstanceLabels> createSampleDataThreeClasses() {
        DistanceMeasure distance = new EuclideanDistance();
        ISPRClassGeometricDataCollection<IInstanceLabels> samples = new LinearList(distance);                        
        for (int i = 0; i < n; i++) {
            Vector          v = InstanceFactory.createVector(data[i]);
            IInstanceLabels il = InstanceFactory.createInstanceLabels(labels3[i]);            
            samples.add(v, il);
        }
        return samples;
    }
}
