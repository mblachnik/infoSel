/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.Vector;
import org.prules.tools.math.container.DoubleObjectContainer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * @param <T>
 * @author Marcin
 */
public interface ISPRGeometricDataCollection<T extends Serializable> extends Serializable, Cloneable {
    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedValues
     */
    void initialize(ExampleSet exampleSet, Map<Attribute, String> storedValues);

    /**
     * This method has to be called in order to insert new values into the data
     * structure
     *
     * @param values     specifies the geometric coordinates in data space
     * @param storeValue specifies the value at the given point
     */
    void add(Vector values, T storeValue);

    /**
     * This method returns a collection of {@code k} stored data values which
     * are closest to {@code values} according to some distance measure.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please use the {@link #getNearestValueDistances(int, org.prules.dataset.IVector) } and then
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k      the number of neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @return
     */
    Collection<T> getNearestValues(int k, Vector values);

    /**
     * This method returns a collection of data from the k nearest sample
     * points. This collection consists of Tupels containing the distance from
     * querrypoint to the samplepoint and in the second component the contained
     * value of the sample point.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k      the number of neighbours
     * @param values the coordinate of the querry point in the sample dimension
     * @return collection of stored values with associated distances
     */
    Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Vector values);

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance. This collection consists of Tupels containing the
     * distance from query point to the sample point and in the second component
     * the label value of the sample point.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param withinDistance minimum distance
     * @param values         the coordinate of the querry point in the sample dimension
     * @return ccollection of stored values with associated distances
     */
    Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, Vector values);

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance but at least k points. So the distance might be
     * enlarged if density is to low. This collection consists of Tupels
     * containing the distance from querrypoint to the samplepoint and in the
     * second component the contained value of the sample point.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param withinDistance - max distance range
     * @param butAtLeastK    - minimum number of nearest neighbors
     * @param values         the coordinate of the querry point in the sample dimension
     * @return collection of stored values with associated distances
     */
    Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, Vector values);

    /**
     * This method has to return the number of stored data points.
     *
     * @return number of elements
     */
    int size();

    /**
     * This returns the index-th value added to this collection.
     *
     * @param index
     * @return stored values
     */
    T getStoredValue(int index);

    /**
     * Set new sample value
     *
     * @param index
     * @param sample
     * @param storedValue
     */
    void setSample(int index, Vector sample, T storedValue);

    /**
     * Returns sample of given index
     *
     * @param index
     * @return
     */
    Vector getSample(int index);

    /**
     * remove sample of given index
     *
     * @param index
     */
    void remove(int index);

    /**
     * Iterator over stored values
     *
     * @return
     */
    Iterator<T> storedValueIterator();

    /**
     * Iterator over samples
     *
     * @return
     */
    Iterator<Vector> samplesIterator();

    /**
     * Count how many unique values appears in the storedValue structure
     *
     * @return number of unique values
     */
    int numberOfUniquesOfStoredValues();

    /**
     * A getter for distance measure used to build internal data structure
     *
     * @return
     */
    DistanceMeasure getMeasure();

}
