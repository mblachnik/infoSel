/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.ISPRExample;
import java.util.Map;
import static org.prules.dataset.Const.NOISE;
import org.prules.operator.learner.weighting.Ontology;

/**
 * Implementation of Instance for handling labels or extra features of an instance. Equivalent to RapidMiner special attributes
 *
 * @author Marcin
 */
public class InstanceLabels extends GenericContainer implements IInstanceLabels {
    
    protected InstanceLabels(Map<String, Object> values) {
        super(values);
    }

    protected InstanceLabels() {
        super();
    }

    /**
     * Return ale label
     * @return 
     */
    @Override
    public double getLabel() {
        return (Double) this.get(Const.LABEL);
    }

    /**
     * Set label
     * @param label 
     */
    @Override
    public void setLabel(double label) {
        this.put(Const.LABEL, label);
    }

    /**
     * Get Instance ID (enumerator)
     * @return 
     */
    @Override
    public int getId() {
        return (Integer) this.get(Const.ID);
    }

    /**
     * Set instance ID (enumerator)
     * @param id 
     */
    @Override
    public void setId(int id) {
        this.put(Const.ID, id);
    }

    /**
     * Get instance weight
     * @return 
     */
    @Override
    public double getWeight() {
        return (Double) this.get(Const.WEIGHT);
    }

    /**
     * Get instance cluster
     * @return 
     */
    @Override
    public double getCluster() {
        return (Double) this.get(Const.CLUSTER);
    }

    /**
     * set instance weight
     * @param weight 
     */
    @Override
    public void setWeight(double weight) {
        this.put(Const.WEIGHT, weight);
    }

    /**
     * set cluster label
     * @param cluster 
     */
    @Override
    public void setCluster(double cluster) {
        this.put(Const.CLUSTER, cluster);
    }

    /**
     * Set properties of labels which are gathered from RapidMiner Special attributes
     * @param example 
     */
    @Override
    public void set(Example example) {
        setWeight(example.getWeight());
        setCluster(example.getValue(example.getAttributes().getCluster()));
        setId((int) example.getId());
        setLabel(example.getLabel());
        Attribute attrNoise = example.getAttributes().get(Ontology.ATTRIBUTE_NOISE);
        double val = attrNoise!=null ? example.getValue(attrNoise) : 0; 
        set(NOISE, val);   
        if (example instanceof ISPRExample) {
            this.put(Const.INDEX_EXAMPLESET, ((ISPRExample) example).getIndex());
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set(String name, Object value) {
        this.put(name, value);
    }
    
    

}
