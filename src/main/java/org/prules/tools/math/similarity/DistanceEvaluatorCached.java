/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.similarity;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.HashMap;
import java.util.Map;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class DistanceEvaluatorCached implements IDistanceEvaluator {

    final DistanceMeasure distance;
    final Map<Integer, Double> map;

    public DistanceEvaluatorCached(DistanceMeasure distance, int cacheSize) {
        this.distance = distance;
        this.map = new HashMap<>(cacheSize);
    }

    @Override
    public DistanceMeasure getDistance() {
        return distance;
    }

    @Override
    public double evaluateDistance(Instance values1, Instance values2) {
        IInstanceLabels l1 = values1.getLabels();
        IInstanceLabels l2 = values1.getLabels();
        int id1 = l1.getId();
        int id2 = l2.getId();
        double dist = 0;
        if (id1 >= 0 && id2 >= 0) {
            int id = (int) 0.5 * (id1 + id2) * (id1 + id2 + 1) + id2;
            if (map.containsKey(id)) {
                dist = map.get(id);
            } else {
                Vector v1 = values1.getVector();
                Vector v2 = values2.getVector();
                dist = distance.calculateDistance(v1.getValues(), v2.getValues());
                map.put(id, dist);
            }
        } else {
            Vector v1 = values1.getVector();
            Vector v2 = values2.getVector();
            dist = distance.calculateDistance(v1.getValues(), v2.getValues());
        }
        return dist;
    }

    @Override
    public double evaluateDistance(Vector values1, Vector values2) {
        return distance.calculateDistance(values1.getValues(), values2.getValues());
    }
}
