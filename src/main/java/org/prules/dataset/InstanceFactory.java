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
import static org.prules.dataset.Const.NOISE;
import static org.prules.dataset.Const.WEIGHT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.prules.operator.learner.weighting.Ontology;


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

    /**
     * Create a label with internal fields determined by special attributes of input Example
     * @param ex
     * @return 
     */
    public static IInstanceLabels createInstaceLabels(Example ex) {
        InstanceLabels values = new InstanceLabels();
        values.put(LABEL, ex.getLabel());
        values.put(CLUSTER, ex.getValue(ex.getAttributes().getCluster()));
        values.put(ID, (int)ex.getId());
        values.put(WEIGHT, ex.getWeight());
        Attribute attrNoise = ex.getAttributes().get(Ontology.ATTRIBUTE_NOISE);
        double val = attrNoise!=null ? ex.getValue(attrNoise) : 0; 
        values.put(NOISE, val);        
        if (ex instanceof ISPRExample) {
            values.put(INDEX_EXAMPLESET, ((ISPRExample) ex).getIndex());
        }
        return values;
    }

    /**
     * Creates new instance of label with all values set to Double.NaN
     * @param label
     * @return 
     */
    public static IInstanceLabels createInstanceLabels() {        
        return createInstanceLabels(Double.NaN);
    }
    
    /**
     * Creates new instance of label with Const.LABEL set to label. Remining values are set to NaN
     * @param label
     * @return 
     */
    public static IInstanceLabels createInstanceLabels(double label) {
        Map<String, Object> map = new HashMap<>();
        map.put(LABEL, label);
        map.put(CLUSTER, Double.NaN);
        map.put(ID, Integer.MIN_VALUE);
        map.put(WEIGHT, 1);        
        map.put(NOISE, 0);   
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
