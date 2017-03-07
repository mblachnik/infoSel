/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ISPRExample;
import static org.prules.dataset.Const.CLUSTER;
import static org.prules.dataset.Const.ID;
import static org.prules.dataset.Const.INDEX_EXAMPLESET;
import static org.prules.dataset.Const.LABEL;
import static org.prules.dataset.Const.WEIGHT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public class InstanceFactory {

    public static IInstanceLabels createInstanceLabels(Example ex, Map<Attribute, String> attributes) {
        InstanceLabels values = new InstanceLabels();
        for (Map.Entry<Attribute, String> a : attributes.entrySet()) {
            values.put(a.getValue(), ex.getValue(a.getKey()));
        }
        if (ex instanceof ISPRExample) {
            values.put(INDEX_EXAMPLESET, ((ISPRExample) ex).getIndex());
        }
        return values;
    }

    public static IInstanceLabels createInstaceLabels(Example ex) {
        InstanceLabels values = new InstanceLabels();
        values.put(LABEL, ex.getLabel());
        values.put(CLUSTER, ex.getValue(ex.getAttributes().getCluster()));
        values.put(ID, ex.getId());
        values.put(WEIGHT, ex.getWeight());
        if (ex instanceof ISPRExample) {
            values.put(INDEX_EXAMPLESET, ((ISPRExample) ex).getIndex());
        }
        return values;
    }

    public static IInstanceLabels createEmptyInstanceLabels() {
        Map<String, Object> map = new HashMap<>();
        map.put(LABEL, Double.NaN);
        map.put(CLUSTER, Double.NaN);
        map.put(ID, Double.NaN);
        map.put(WEIGHT, 1);
        return new InstanceLabels(map);
    }

    public static Instance createInstance(Example ex) {
        IInstanceLabels labels = createInstaceLabels(ex);
        Vector vector = createVector(ex);
        Instance instance = new InstanceBasic(vector, labels);
        return instance;
    }

    public static Instance createEmptyInstance() {
        Instance instance = new InstanceBasic(null, null);
        return instance;
    }

    public static Vector createVector(Example example) {
        return new VectorDense(example);
    }

    public static Vector createVector(double[] values) {
        return new VectorDense(values);
    }

    public static Vector createVector(Example example, List<Attribute> attributes) {
        return new VectorDense(example, attributes);
    }

    public static Vector createVector(Example example, Attribute[] attributes) {
        return new VectorDense(example, attributes);
    }

    public static Vector createVector(ExampleSet exampleSet) {
        return new VectorDense(exampleSet.getAttributes().size());
    }

    public static IInstancePrediction createPrediction(double prediction, double[] confidence) {
        return new InstancePrediction(prediction, confidence);
    }

    public static IInstancePrediction createPrediction(Example example) {
        double prediction = example.getPredictedLabel();
        double[] confidence = new double[]{example.getConfidence(CLUSTER)};
        return new InstancePrediction(prediction, confidence);
    }
}
