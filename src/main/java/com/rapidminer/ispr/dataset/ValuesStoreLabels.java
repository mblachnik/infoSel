/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.ISPRExample;
import java.util.Map;

/**
 * Implementation of ValuesStore for handling machine learning data. It consists
 * of Vector, Label, Prediction, ID,
 *
 * @author Marcin
 */
public class ValuesStoreLabels extends ValuesStore implements IValuesStoreLabels {

    protected ValuesStoreLabels(Map<String, Object> values) {
        super(values);
    }

    protected ValuesStoreLabels() {
        super();
    }

    @Override
    public double getLabel() {
        return (Double) this.get(Const.LABEL);
    }

    @Override
    public void setLabel(double label) {
        this.put(Const.LABEL, label);
    }

    @Override
    public int getId() {
        return (Integer) this.get(Const.ID);
    }

    @Override
    public void setId(int id) {
        this.put(Const.ID, id);
    }

    @Override
    public double getWeight() {
        return (Double) this.get(Const.WEIGHT);
    }

    @Override
    public double getCluster() {
        return (Double) this.get(Const.CLUSTER);
    }

    @Override
    public void setWeight(double weight) {
        this.put(Const.WEIGHT, weight);
    }

    @Override
    public void setCluster(double cluster) {
        this.put(Const.CLUSTER, cluster);
    }

    @Override
    public void set(Example example) {
        setWeight(example.getWeight());
        setCluster(example.getValue(example.getAttributes().getCluster()));
        setId((int) example.getId());
        setLabel(example.getLabel());
        if (example instanceof ISPRExample) {
            this.put(Const.INDEX_EXAMPLESET, ((ISPRExample) example).getIndex());
        }
    }

}
