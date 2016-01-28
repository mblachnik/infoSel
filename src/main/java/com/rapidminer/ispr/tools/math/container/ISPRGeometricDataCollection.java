/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.tools.math.container;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Marcin
 */
public interface ISPRGeometricDataCollection<T extends Serializable> extends Serializable {    
    
	/**
	 * This method has to be called in order to insert new values into the data structure 
	 * @param values specifies the geometric coordinates in data space
	 * @param storeValue specifies the value at the given point
	 */
	
	public abstract void add(double[] values, T storeValue);

	/**
	 * This method returns a collection of the stored data values from the k nearest sample points.
	 * @param k   the number of neighbours
	 * @param values the coordinate of the querry point in the sample dimension
	 */
	public abstract Collection<T> getNearestValues(int k, double[] values);
	
	/**
	 * This method returns a collection of data from the k nearest sample points.
	 * This collection consists of Tupels containing the distance from querrypoint
	 * to the samplepoint and in the second component the contained value of the sample
	 * point.
	 * @param k   the number of neighbours
	 * @param values the coordinate of the querry point in the sample dimension
	 */
	public abstract Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, double[] values);

	
	/**
	 * This method returns a collection of data from all sample points inside the specified distance.
	 * This collection consists of Tupels containing the distance from querrypoint
	 * to the samplepoint and in the second component the contained value of the sample
	 * point.
	 * 
	 * @param values the coordinate of the querry point in the sample dimension
	 */
	public abstract Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, double[] values);
	
	/**
	 * This method returns a collection of data from all sample points inside the specified distance but at least
	 * k points. So the distance might be enlarged if density is to low.
	 * This collection consists of Tupels containing the distance from querrypoint
	 * to the samplepoint and in the second component the contained value of the sample
	 * point.
	 * 
	 * @param values the coordinate of the querry point in the sample dimension
	 */
	public abstract Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, double[] values);

	/**
	 * This method has to return the number of stored data points.
	 */
	public abstract int size();

	/**
	 * This returns the index-th value added to this collection.
	 */
	public abstract T getStoredValue(int index);
                        
        public abstract void setSample(int index, double[] sample, T storedValue);
        
        public abstract double[] getSample(int index);
        
        public abstract void remove(int index);
        
        public Iterator<T> storedValueIterator();
        
        public Iterator<double[]> samplesIterator();
        
}
