/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.tools.math.container;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import com.rapidminer.ispr.dataset.IVector;

/**
 *
 * @author Marcin
 * @param <T>
 */
public interface ISPRGeometricDataCollection<T extends Serializable> extends Serializable {
    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedValues
     */
    void initialize(ExampleSet exampleSet, Map<Attribute,String> storedValues);
    /**
     * This method has to be called in order to insert new values into the data
     * structure
     *
     * @param values specifies the geometric coordinates in data space
     * @param storeValue specifies the value at the given point
     */
    public  void add(IVector values, T storeValue);

    /**
     * This method returns a collection of the stored data values from the k
     * nearest sample points.
     *
     * @param k the number of neighbours
     * @param values the coordinate of the querry point in the sample dimension
     * @return
     */
    public  Collection<T> getNearestValues(int k, IVector values);

    /**
     * This method returns a collection of data from the k nearest sample
     * points. This collection consists of Tupels containing the distance from
     * querrypoint to the samplepoint and in the second component the contained
     * value of the sample point.
     *
     * @param k the number of neighbours
     * @param values the coordinate of the querry point in the sample dimension
     * @return collection of stored values with associated distances
     */
    public  Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, IVector values);

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance. This collection consists of Tupels containing the
     * distance from querrypoint to the samplepoint and in the second component
     * the contained value of the sample point.
     *
     * @param withinDistance minimum distance
     * @param values the coordinate of the querry point in the sample dimension
     * @return ccollection of stored values with associated distances
     */
    public  Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, IVector values);

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance but at least k points. So the distance might be
     * enlarged if density is to low. This collection consists of Tupels
     * containing the distance from querrypoint to the samplepoint and in the
     * second component the contained value of the sample point.
     *
     * @param withinDistance - max distance range
     * @param butAtLeastK - minimum number of nearest neighbors
     * @param values the coordinate of the querry point in the sample dimension
     * @return collection of stored values with associated distances
     */
    public  Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, IVector values);

    /**
     * This method has to return the number of stored data points.
     *
     * @return number of elements
     */
    public  int size();

    /**
     * This returns the index-th value added to this collection.
     *
     * @param index
     * @return stored values
     */
    public  T getStoredValue(int index);

    /**
     * Set new sample value
     * @param index
     * @param sample
     * @param storedValue 
     */
    public  void setSample(int index, IVector sample, T storedValue);

    /**
     * Returns sample of given index
     * @param index
     * @return 
     */
    public  IVector getSample(int index);

    /**
     * remove sample of given index
     * @param index 
     */
    public  void remove(int index);

    /**
     * Iterator over stored values
     * @return 
     */
    public Iterator<T> storedValueIterator();

    /**
     * Iterator over samples
     * @return 
     */
    public Iterator<IVector> samplesIterator();

    /**
     * Count how many unique values appears in the storedValue structure
     *
     * @return number of unique values
     */
    public int numberOfUniquesOfStoredValues();

}
