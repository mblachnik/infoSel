/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing single prototype such as cluster center or single instance
 *
 * @author Marcin
 */
public class VectorDense implements Vector {

    private double[] values;

    VectorDense(int n) {
        this.values = new double[n];
    }

    VectorDense(double[] values) {
        this.values = values;
    }

    VectorDense(Example example, List<Attribute> attributes) {
        this(attributes.size());
        setValues(example, attributes);
    }

    VectorDense(Example example, Attribute[] attributes) {
        this(attributes.length);
        setValues(example, attributes);
    }

    VectorDense(Example example) {
        this(example.getAttributes().size());
        setValues(example);
    }

    @Override
    public double[] getValues() {
        return values;
    }

    @Override
    public final void setValues(Example example, List<Attribute> attributes) {
        int i = 0;
        for (Attribute a : attributes) {
            this.values[i] = example.getValue(a);
            i++;
        }
    }

    @Override
    public final void setValues(Example example, Attribute[] attributes) {
        int i = 0;
        for (Attribute a : attributes) {
            this.values[i] = example.getValue(a);
            i++;
        }
    }

    @Override
    public final void setValues(Example example) {
        setValues(example, example.getAttributes().createRegularAttributeArray());
    }

    @Override
    public Object clone() {

        try {
            VectorDense instance = (VectorDense) super.clone();            
            instance.values = this.values.clone();
            return instance;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Problem performing clone operation");
        }
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void setValue(int i, double value) {
        values[i] = value;
    }

    @Override
    public double getValue(int i) {
        return values[i];
    }

    @Override
    public boolean isSparse() {
        return false;
    }

    @Override
    public int[] getNonEmptyIndex() {
        int idx[] = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            idx[i] = i;
        }
        return idx;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Arrays.hashCode(this.values);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VectorDense other = (VectorDense) obj;
        return Arrays.equals(this.values, other.values);
    }
}
