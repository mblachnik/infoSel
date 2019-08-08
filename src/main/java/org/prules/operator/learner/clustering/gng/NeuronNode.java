package org.prules.operator.learner.clustering.gng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Łukasz Migdałek on 2016-08-01.
 */
public class NeuronNode {

    protected double[] values;
    protected List<NeuronNode> neighbors = new ArrayList<>();
    protected Map<NeuronNode, Integer> ages = new HashMap<>();
    protected double localError;
    protected double dist;
    protected int maxAge;

    public NeuronNode(double[] values, int maxAge) {
        this.values = values;
        this.maxAge = maxAge;
    }

    public void incrementAges() {
        List<NeuronNode> toRemove = new ArrayList<>();
        for (Map.Entry<NeuronNode, Integer> entry : ages.entrySet()) {
            // Assign values from map
            Integer age = entry.getValue();
            Integer neighborAge = entry.getKey().getAges().get(this);

            age++;
            neighborAge++;

            if (age >= maxAge) {
                toRemove.add(entry.getKey());
            }
        }

        //Remove too old neurons
        for (NeuronNode neuron : toRemove) {
            deleteNeighbor(neuron);
        }
    }

    public void addNeighbor(NeuronNode neuron) {
        if (!neighbors.contains(neuron)) {
            neighbors.add(neuron);
            ages.put(neuron, 0);
            neuron.getNeighbors().add(this);
            neuron.getAges().put(this, 0);
        }
    }

    public void deleteNeighbor(NeuronNode neuron) {
        neighbors.remove(neuron);
        neuron.getNeighbors().remove(this);
    }

    public List<NeuronNode> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<NeuronNode> neighbors) {
        this.neighbors = neighbors;
    }

    public double getLocalError() {
        return localError;
    }

    public void setLocalError(double localError) {
        this.localError = localError;
    }

    public Map<NeuronNode, Integer> getAges() {
        return ages;
    }

    public void setAges(Map<NeuronNode, Integer> ages) {
        this.ages = ages;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

}
