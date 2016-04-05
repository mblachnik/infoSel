/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.ispr.tools.math.container;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import com.rapidminer.ispr.operator.learner.tools.SymetricDoubleMatrix;
import com.rapidminer.tools.container.Tupel;
import com.rapidminer.tools.math.container.BoundedPriorityQueue;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is an implementation of the GeometricDataCollection interface,
 * It implements nearest neighbor algorithm with an internal cache, such that 
 * if given distance between points was first calculated its value is stored in 
 * the internal cache, so next time it doesn't have to recalculate all of the distances.
 * The distance ceche is recalculated each time new sample is added
 * It use simple linear search algorithm
 *
 * @author Marcin Blachnik
 *
 * @param <T> This is the type of value with is stored with the points and
 * retrieved on nearest neighbour search
 */
public class SimpleNNCachedLineraList<T extends Serializable> implements ISPRCachedGeometricDataCollection<T>, RandomAccess {

    private static final long serialVersionUID = -746048910140779285L;
    DistanceMeasure distance;
    ArrayList<double[]> samples;
    ArrayList<T> storedValues;
    SymetricDoubleMatrix distanceCache; //Structure which holds symetrix matrix
    int index = -1;

    /**
     * Constructor of the class
     * @param distance - distance measure (it is assumed that it is symetric)
     * @param n - size of internal data structures and cache size 
     */
    public SimpleNNCachedLineraList(DistanceMeasure distance, int n) {
        this.distance = distance;
        samples = new ArrayList<double[]>(n);
        storedValues = new ArrayList<T>(n);
        //int cacheSize  = (n*n + n)/2;                
        distanceCache = new SymetricDoubleMatrix(n);
    }

    public SimpleNNCachedLineraList(ExampleSet exampleSet, Attribute storedValuesAttribute, DistanceMeasure distance) {
        this.distance = distance;
        initialize(exampleSet, storedValuesAttribute);
    }

    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedValuesAttribute     
     */
    @Override
    public final void initialize(ExampleSet exampleSet, Attribute storedValuesAttribute) {
        int n = exampleSet.size();
        samples = new ArrayList<double[]>(n);
        storedValues = new ArrayList<T>(n);
        Attributes attributes = exampleSet.getAttributes();
        int valuesSize = attributes.size();
        for (Example example : exampleSet) {
            double[] values = new double[valuesSize];
            int i = 0;
            for (Attribute attribute : attributes) {
                values[i] = example.getValue(attribute);
                i++;
            }
            Number labelValue = example.getValue(storedValuesAttribute);
            this.add(values, (T)labelValue);
        }
    }
    /**
     * Add new sample to the nearest neighbor structure
     * @param values
     * @param storeValue 
     */
    @Override
    public void add(double[] values, T storeValue) {
        index++;
        this.samples.add(values);
        this.storedValues.add(storeValue);
        int i = 0;
        for (double[] sample : samples) {
            double dist = distance.calculateDistance(sample, values);
            distanceCache.set(i, index, dist);
            i++;
        }
    }

    /**
     * Returns a collection of values associated with k nearest samples to the input sample
     * @param k - number of nearest neighbors
     * @param values - coordinates of input sample
     * @return 
     */
    @Override
    public Collection<T> getNearestValues(int k, double[] values) {
        Collection<T> result = new ArrayList<T>(k);
        if (k > 1) {
            BoundedPriorityQueue<Tupel<Double, T>> queue = new BoundedPriorityQueue<Tupel<Double, T>>(k);
            int i = 0;
            for (double[] sample : this.samples) {
                double dist = distance.calculateDistance(sample, values);
                queue.add(new Tupel<Double, T>(dist, storedValues.get(i)));
                i++;
            }
            for (Tupel<Double, T> tupel : queue) {
                result.add(tupel.getSecond());
            }
        } else {
            int i = 0;
            double minDist = Double.MAX_VALUE;
            T subResult = null;
            for (double[] sample : this.samples) {
                double dist = distance.calculateDistance(sample, values);
                if (dist < minDist) {
                    minDist = dist;
                    subResult = storedValues.get(i);
                }
                i++;
            }
            result.add(subResult);
        }
        return result;
    }

    /**
     * Returns a collection of pairs including distance value and value associated with k nearest samples to the input sample
     * @param k - number of nearest neighbors
     * @param values - coordinates of input sample
     * @return 
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, double[] values) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
        int i = 0;
        for (double[] sample : this.samples) {
            double dist = distance.calculateDistance(sample, values);
            queue.add(new DoubleObjectContainer<T>(dist, storedValues.get(i)));
            i++;
        }
        return queue;
    }

    /**     
     * Returns a collection values associated with nearest samples that fall into a hyper-sphere of radius withinDistance     
     * @param withinDistance - size of radius
     * @param values - input sample coordinates
     * @return 
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, double[] values) {
        ArrayList<DoubleObjectContainer<T>> queue = new ArrayList<DoubleObjectContainer<T>>();
        int i = 0;
        for (double[] sample : this.samples) {
            double currentDistance = distance.calculateDistance(sample, values);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<T>(currentDistance, storedValues.get(i)));
            }
            i++;
        }
        return queue;
    }
   
    /**     
     * Returns a collection values associated with nearest samples that fall into a hyper-sphere of radius withinDistance,
     * but if the number of samples is less then  butAtLeastK, then at least k values are returned    
     * @param withinDistance - size of radius
     * @param butAtLeastK - 
     * @param values - input sample coordinates     
     * @return 
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, double[] values) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, values);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, values);
        }
        return result;
    }

            /**
     * Returns a collection of values associated with k nearest samples to the input sample which is already an element of input data.
     * For that purpose this algorithm uses internal cache. This method is very useful for instance selection.
     * @param k - number of nearest neighbors
     * @param values - coordinates of input sample
     * @return 
     */
    @Override
    public Collection<T> getNearestValues(int k, int index) {
        Collection<T> result = new ArrayList<T>(k);
        if (k > 1) {
            BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
            int i = 0;
            for (double[] sample : this.samples) {
                double dist = distanceCache.get(index, i);
                queue.add(new DoubleObjectContainer<T>(dist, storedValues.get(i)));
                i++;
            }
            for (DoubleObjectContainer<T> tupel : queue) {
                result.add(tupel.getSecond());
            }
        } else {
            int i = 0;
            double minDist = Double.MAX_VALUE;
            T subResult = null;
            for (double[] sample : this.samples) {
                double dist = distanceCache.get(index, i);
                if (dist < minDist) {
                    minDist = dist;
                    subResult = storedValues.get(i);
                }
                i++;
            }
            result.add(subResult);
        }
        return result;
    }

        /**
     * Returns a collection of pairs including distance value and value associated with k nearest samples to the input sample which is already an element of input data.
     * For that purpose this algorithm uses internal cache. This method is very useful for instance selection.
     * which is a member of the dataset 
     * @param k - number of nearest neighbors
     * @param values - coordinates of input sample
     * @return 
     */   
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, int index) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
        int i = 0;
        for (double[] sample : this.samples) {
            double dist = distanceCache.get(index, i);
            queue.add(new DoubleObjectContainer<T>(dist, storedValues.get(i)));
            i++;
        }
        return queue;
    }

     /**     
     * Returns a collection values associated with nearest samples that fall into a hyper-sphere of radius withinDistance. It is assumed that the input sample  already is an element of input data.
     * For that purpose this algorithm uses internal cache. This method is very useful for instance selection. 
     * @param withinDistance - size of radius
     * @param index - index of the input sample. It should be an element of the input data added by the addSample method
     * @return 
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int index) {
        ArrayList<DoubleObjectContainer<T>> queue = new ArrayList<DoubleObjectContainer<T>>();
        int i = 0;
        for (double[] sample : this.samples) {
            double currentDistance = distanceCache.get(index, i);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<T>(currentDistance, storedValues.get(i)));
            }
            i++;
        }
        return queue;
    }

        /**     
     * Returns a collection values associated with nearest samples that fall into a hyper-sphere of radius withinDistance,
     * but if the number of samples is less then  butAtLeastK, then at least k values are returned. It is assumed that the input sample  already is an element of input data.
     * For that purpose this algorithm uses internal cache. This method is very useful for instance selection.   
     * @param withinDistance - size of radius
     * @param butAtLeastK - number of nearest neighbors
     * @param index - index of the input sample. It should be an element of the input data added by the addSample method
     * @return 
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, int index) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, index);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, index);
        }
        return result;
    }

    /**
     * Number of samples stored in the internal collection
     * @return 
     */
    @Override
    public int size() {
        return samples.size();
    }

    /**
     * Method allows to retrieve value associated with the input sample of given index
     * @param index
     * @return 
     */
    @Override
    public T getStoredValue(int index) {
        return storedValues.get(index);
    }

    /**
     * Method allows to retrieve the input sample of given index
     * @param index
     * @return 
     */
    @Override
    public double[] getSample(int index) {
        return samples.get(index);
    }

    /**
     * Not implemented
     * @param n 
     */
    @Override
    public void remove(int n) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns iterator over values associated with input samples
     * @return 
     */
    @Override
    public Iterator<T> storedValueIterator() {
        return storedValues.iterator();
    }

    /**
     * Returns iterator over input data
     * @return 
     */
    @Override
    public Iterator<double[]> samplesIterator() {
        return this.samples.iterator();
    }

    /**
     * THis method allows to modify values associated with input data
     * @param index - index of input data to modify
     * @param sample - coordinated of the input data
     * @param storedValue - new value associated with the input data
     */
    @Override
    public void setSample(int index, double[] sample, T storedValue) {
        samples.set(index, sample);
        storedValues.set(index, storedValue);
        int i=0;
        for (double[] values : samples) {
            double dist = distance.calculateDistance(values, sample);
            distanceCache.set(i, index, dist);
            i++;
        }
    }

    /**
     * Count how many unique appears in the storedValue structure
     *
     * @return number of unique values
     */
    @Override
    public int numberOfUniquesOfStoredValues() {
        Set<T> uniqueValues = new HashSet<>();
        for (T value : storedValues) {
            uniqueValues.add(value);
        }
        return uniqueValues.size();
    }
}
