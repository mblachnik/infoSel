/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ISPRExample;
import static com.rapidminer.ispr.dataset.Const.CLUSTER;
import static com.rapidminer.ispr.dataset.Const.ID;
import static com.rapidminer.ispr.dataset.Const.INDEX_EXAMPLESET;
import static com.rapidminer.ispr.dataset.Const.LABEL;
import static com.rapidminer.ispr.dataset.Const.WEIGHT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public class ValuesStoreFactory {

    public static IValuesStoreLabels createValuesStoreLabels(Example ex, Map<Attribute, String> attributes) {
        ValuesStoreLabels values = new ValuesStoreLabels();
        for (Map.Entry<Attribute, String> a : attributes.entrySet()) {
            values.put(a.getValue(), ex.getValue(a.getKey()));
        }
        if (ex instanceof ISPRExample) {
            values.put(INDEX_EXAMPLESET, ((ISPRExample) ex).getIndex());
        }
        return values;
    }

    public static IValuesStoreLabels createValuesStoreLabels(Example ex) {
        ValuesStoreLabels values = new ValuesStoreLabels();
        values.put(LABEL, ex.getLabel());
        values.put(CLUSTER, ex.getValue(ex.getAttributes().getCluster()));
        values.put(ID, ex.getId());
        values.put(WEIGHT, ex.getWeight());
        if (ex instanceof ISPRExample) {
            values.put(INDEX_EXAMPLESET, ((ISPRExample) ex).getIndex());
        }
        return values;
    }

    public static IValuesStoreLabels createEmptyValuesStoreLabels() {
        Map<String, Object> map = new HashMap<>();
        map.put(LABEL, Double.NaN);
        map.put(CLUSTER, Double.NaN);
        map.put(ID, Double.NaN);
        map.put(WEIGHT, 1);
        return new ValuesStoreLabels(map);
    }

    public static IValuesStoreInstance createValuesStoreInstance(Example ex) {
        IValuesStoreLabels labels = createValuesStoreLabels(ex);
        IVector vector = createVector(ex);
        IValuesStoreInstance instance = new ValuesStoreInstance(vector, labels);
        return instance;
    }

    public static IValuesStoreInstance createEmptyValuesStoreInstance() {
        IValuesStoreInstance instance = new ValuesStoreInstance(null, null);
        return instance;
    }

    public static IVector createVector(Example example) {
        return new VectorDense(example);
    }

    public static IVector createVector(double[] values) {
        return new VectorDense(values);
    }

    public static IVector createVector(Example example, List<Attribute> attributes) {
        return new VectorDense(example, attributes);
    }

    public static IVector createVector(Example example, Attribute[] attributes) {
        return new VectorDense(example, attributes);
    }

    public static IVector createVector(ExampleSet exampleSet) {
        return new VectorDense(exampleSet.getAttributes().size());
    }

    public static IValuesStorePrediction createPrediction(double prediction, double[] confidence) {
        return new ValuesStorePrediction(prediction, confidence);
    }

    public static IValuesStorePrediction createPrediction(Example example) {
        double prediction = example.getPredictedLabel();
        double[] confidence = new double[]{example.getConfidence(CLUSTER)};
        return new ValuesStorePrediction(prediction, confidence);
    }
}
