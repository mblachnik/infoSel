/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.ISPRExample;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Implementation of Instance for handling labels or extra features of an
 * instance. Equivalent to RapidMiner special attributes
 * 
 * @author Marcin
 */

//TODO Przetestować i podpiąć w miejsce starego InstanceLabels
public class InstanceLabels_new implements IInstanceLabels, IGenericContainer {

    double label;
    double prediction;
    double[] confidences;
    double cluster;
    double weight;
    int id;
    int index_container;
    int index_exampleset;
    double noise;
    Map<String, Object> map;

    protected InstanceLabels_new(Map<String, Object> values) {
        for (Entry<String, Object> e : values.entrySet()) {
            putValue(e.getKey(), e.getValue());
        }
    }

    protected InstanceLabels_new(IGenericContainer values) {
        for (String e : values.keySet()) {
            putValue(e, values.get(e));
        }
    }

    protected InstanceLabels_new(InstanceLabels_new values) {
        this.label = values.label;
        this.prediction = values.label;
        this.confidences = values.confidences;
        this.cluster = values.cluster;
        this.weight = values.weight;
        this.id = values.id;
        this.index_container = values.index_container;
        this.index_exampleset = values.index_exampleset;
        this.noise = values.noise;
        if (values.map != null) {
            this.map = new HashMap<>(values.map);
        }
    }

    /**
     * Return ale label
     *
     * @return
     */
    @Override
    public double getLabel() {
        return this.label;
    }

    /**
     * Set label
     *
     * @param label
     */
    @Override
    public void setLabel(double label) {
        this.label = label;
    }

    /**
     * Get Instance ID (enumerator)
     *
     * @return
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Set instance ID (enumerator)
     *
     * @param id
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get instance weight
     *
     * @return
     */
    @Override
    public double getWeight() {
        return weight;
    }

    /**
     * Get instance cluster
     *
     * @return
     */
    @Override
    public double getCluster() {
        return cluster;
    }

    /**
     * set instance weight
     *
     * @param weight
     */
    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * set cluster label
     *
     * @param cluster
     */
    @Override
    public void setCluster(double cluster) {
        this.cluster = cluster;
    }

    /**
     * Set properties of labels which are gathered from RapidMiner Special
     * attributes
     *
     * @param example
     */
    @Override
    public void set(Example example) {
        setWeight(example.getWeight());
        setCluster(example.getValue(example.getAttributes().getCluster()));
        setId((int) example.getId());
        setLabel(example.getLabel());
        if (example instanceof ISPRExample) {
            this.index_exampleset = ((ISPRExample) example).getIndex();
        }
    }

    /** Returns value assosiated to given key as float
     * 
     * @param key - key value
     * @return 
     */
    @Override
    public float getValueAsFloat(String key) {
        return (float) getValueAsDouble(key);
    }

    /** Returns value assosiated to given key as long
     * 
     * @param key - key value
     * @return 
     */
    @Override
    public long getValueAsLong(String s) {
        return (long) getValueAsDouble(s);
    }

    /** Returns value assosiated to given key as object
     * This is generic version of all others get methods. Note that here you can specify the return type
     * @param key - key value
     * @return 
     */
    @Override
    public <T> T get(String key) {
        Double tmp = null;
        T out;
        switch (key) {
            case Const.LABEL:
                tmp = label;
            case Const.PREDICTION:
                tmp = prediction;
            case Const.CONFIDENCE:
                throw new ClassCastException("confidence is an array");
            case Const.CLUSTER:
                tmp = cluster;
            case Const.WEIGHT:
                tmp = weight;
            case Const.NOISE:
                tmp = weight;
            case Const.ID:
                tmp = (double) id;
            case Const.INDEX_CONTAINER:
                tmp = (double) index_container;
            case Const.INDEX_EXAMPLESET:
                tmp = (double) index_exampleset;
        }
        out = (T) tmp;
        if (out == null && map != null) {
            out = (T) map.get(key);
        }
        return out;
    }

    /** Returns value assosiated to given key as String
     * 
     * @param key - key value
     * @return 
     */
    @Override
    public String getValueAsString(String s) {
        if (map != null && map.containsKey(s)) {
            return (String) map.get(s);
        }
        return "" + this.getValueAsDouble(s);
    }

    /** Returns value assosiated to given key as double
     * 
     * @param key - key value
     * @return 
     */
    @Override
    public double getValueAsDouble(String key) {
        switch (key) {
            case Const.LABEL:
                return label;
            case Const.PREDICTION:
                return prediction;
            case Const.CONFIDENCE:
                throw new ClassCastException("confidence is an array");
            case Const.CLUSTER:
                return cluster;
            case Const.WEIGHT:
                return weight;
            case Const.NOISE:
                return weight;
            case Const.ID:
                return id;
            case Const.INDEX_CONTAINER:
                return index_container;
            case Const.INDEX_EXAMPLESET:
                return index_exampleset;
            default:
                if (map != null) {
                    return (Double) map.get(key);
                }
        }
        return Double.NaN;
    }

    /** Returns value assosiated to given key as int
     * 
     * @param key - key value
     * @return 
     */
    @Override
    public int getValueAsInt(String key) {
        switch (key) {
            case Const.ID:
                return id;
            case Const.INDEX_CONTAINER:
                return index_container;
            case Const.INDEX_EXAMPLESET:
                return index_exampleset;
            case Const.LABEL:
                return (int) label;
            case Const.PREDICTION:
                return (int) prediction;
            case Const.CONFIDENCE:
                throw new ClassCastException("confidence is an array");
            case Const.CLUSTER:
                return (int) cluster;
            case Const.WEIGHT:
                return (int) weight;
            case Const.NOISE:
                return (int) weight;
        }
        if (map != null) {
            return (int) map.get(key);
        }
        return Integer.MIN_VALUE;
    }

    /**Creates a clone of all elements. 
     * @return 
     */
    @Override
    public Object clone() {        
        return new InstanceLabels_new(this);
    }

    /** Returns all avaliable keys
     * @return 
     */
    @Override
    public Set<String> keySet() {
        Set<String> set = Const.labelsKeySet();
        if (map != null) {
            set.addAll(map.keySet());
        }
        return set;
    }

    /** A generic version to set or put elements to the container
     * 
     * @param key - key value
     * @param value
     * @return 
     */
    private Object putValue(String key, Object value) {
        Object out;
        switch (key) {
            case Const.LABEL:
                out = label;
                label = (Double) value;
                break;
            case Const.PREDICTION:
                out = prediction;
                prediction = (Double) value;
                break;
            case Const.CONFIDENCE:
                out = confidences;
                confidences = (double[]) value;
                break;
            case Const.CLUSTER:
                out = cluster;
                cluster = (Double) value;
                break;
            case Const.WEIGHT:
                out = weight;
                weight = (Double) value;
                break;
            case Const.NOISE:
                out = noise;
                noise = (Double) value;
                break;
            case Const.ID:
                out = id;
                id = (Integer) value;
                break;
            case Const.INDEX_CONTAINER:
                out = index_container;
                index_container = (Integer) value;
                break;
            case Const.INDEX_EXAMPLESET:
                out = index_exampleset;
                index_exampleset = (Integer) value;
                break;
            default:
                initMap();
                out = map.put(key, value);
                break;
        }
        return out;
    }

    /** Writes value to the container under given key
     * 
     * @param key - key value
     * @param value
     * @return 
     */
    @Override
    public Object put(String key, Object value) {
        return putValue(key, value);
    }

    /** Writes value to the container under given key
     * 
     * @param key - key value 
     */
    @Override
    public void set(String key, Object value) {
        this.putValue(key, value);
    }

    /**
     * Returns true if container is empty. Here always false is returned
     * @return 
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Number of elements in the container
     * @return 
     */
    @Override
    public int size() {
        int size = Const.labelsKeySet().size();
        if (map != null) {
            size += map.size();
        }
        return size;
    }

    /**
     * Writes value to the container under given key as int
     * @param key
     * @param value 
     */
    @Override
    public void setValueAsInt(String key, int value) {
        switch (key) {
            case Const.LABEL:
                label = value;
                break;
            case Const.PREDICTION:
                prediction = value;
                break;
            case Const.CONFIDENCE:
                throw new UnsupportedOperationException("Cant set int value as confidence which is double[]");
            case Const.CLUSTER:
                cluster = value;
                break;
            case Const.WEIGHT:
                weight = value;
                break;
            case Const.NOISE:
                noise = value;
                break;
            case Const.ID:
                id = value;
                break;
            case Const.INDEX_CONTAINER:
                index_container = value;
                break;
            case Const.INDEX_EXAMPLESET:
                index_exampleset = value;
                break;
            default:
                initMap();
                map.put(key, value);
        }
    }

    /**
     * Writes value to the container under given key as double
     * @param key
     * @param value 
     */
    @Override
    public void setValueAsDouble(String key, double value) {
        switch (key) {
            case Const.LABEL:
                label = value;
                break;
            case Const.PREDICTION:
                prediction = value;
                break;
            case Const.CONFIDENCE:
                throw new UnsupportedOperationException("Cant set int value as confidence which is double[]");
            case Const.CLUSTER:
                cluster = value;
                break;
            case Const.WEIGHT:
                weight = value;
                break;
            case Const.NOISE:
                noise = value;
                break;
            case Const.ID:
                id = (int) value;
                break;
            case Const.INDEX_CONTAINER:
                index_container = (int) value;
                break;
            case Const.INDEX_EXAMPLESET:
                index_exampleset = (int) value;
                break;
            default:
                initMap();
                map.put(key, value);
        }
    }

    /**
     * Writes value to the container under given key as long
     * @param key
     * @param value 
     */
    @Override
    public void setValueAsLong(String key, long value) {
        switch (key) {
            case Const.LABEL:
                label = value;
                break;
            case Const.PREDICTION:
                prediction = value;
                break;
            case Const.CONFIDENCE:
                throw new UnsupportedOperationException("Cant set int value as confidence which is double[]");
            case Const.CLUSTER:
                cluster = value;
                break;
            case Const.WEIGHT:
                weight = value;
                break;
            case Const.NOISE:
                noise = value;
                break;
            case Const.ID:
                id = (int) value;
                break;
            case Const.INDEX_CONTAINER:
                index_container = (int) value;
                break;
            case Const.INDEX_EXAMPLESET:
                index_exampleset = (int) value;
                break;
            default:
                initMap();
                map.put(key, value);
        }
    }

    /**
     * Writes value to the container under given key as float
     * @param key
     * @param value 
     */
    @Override
    public void setValueAsFloat(String key, float value) {
        switch (key) {
            case Const.LABEL:
                label = value;
                break;
            case Const.PREDICTION:
                prediction = value;
                break;
            case Const.CONFIDENCE:
                throw new UnsupportedOperationException("Cant set int value as confidence which is double[]");
            case Const.CLUSTER:
                cluster = value;
                break;
            case Const.WEIGHT:
                weight = value;
                break;
            case Const.NOISE:
                noise = value;
                break;
            case Const.ID:
                id = (int) value;
                break;
            case Const.INDEX_CONTAINER:
                index_container = (int) value;
                break;
            case Const.INDEX_EXAMPLESET:
                index_exampleset = (int) value;
                break;
            default:
                initMap();
                map.put(key, value);
        }
    }

    /**
     * Writes value to the container under given key as String
     * @param key
     * @param value 
     */
    @Override
    public void setValueAsString(String key, String value) {
        switch (key) {
            case Const.LABEL:
            case Const.PREDICTION:
            case Const.CONFIDENCE:
            case Const.CLUSTER:
            case Const.WEIGHT:
            case Const.NOISE:
            case Const.ID:
            case Const.INDEX_CONTAINER:
            case Const.INDEX_EXAMPLESET:
                throw new UnsupportedOperationException("Not supported. " + key + " requires double or int value, not String");
            default:
                initMap();
                map.put(key, value);
        }

    }

    /**
     * Method to initialize map when needed.
     */
    private void initMap() {
        if (map == null) {
            map = new HashMap<>();
        }
    }
}
