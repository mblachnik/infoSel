/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.PairContainer;

import java.io.Serializable;
import java.util.Collection;

/**
 * A data collection which allows for nearest neighbor search which also supports finding enemies of given instance,
 * such that the result set contains a list of both nearest neighbors as well as nearest enemies
 *
 * @param <T>
 * @author Marcin
 */
public interface ISPRClassGeometricDataCollection<T extends Serializable> extends ISPRGeometricDataCollectionWithIndex<T> {

    /**
     * This method returns a collection of {@code k} stored data values which
     * are closest to {@code values} according to some distance measure.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please use the {@link #getNearestValueDistances(int, org.prules.dataset.IVector) } and then
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k      the number of neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @param label  class label
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors
     */
    PairContainer<Collection<T>, Collection<T>> getNearestNeighborsAndAnymies(int k, Vector values, T label);

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
     * @param label  class label
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors (anymies) )Returned value also contains a distance to each neighbor
     */
    PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(int k, Vector values, T label);

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
     * @param label          class label
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors (anymies) )Returned value also contains a distance to each neighbor
     */
    PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, Vector values, T label);

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
     * @param label          class label
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors (anymies) )Returned value also contains a distance to each neighbor
     */
    PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, int butAtLeastK, Vector values, T label);


//=============================

    /**
     * This method returns a collection of {@code k} stored data values which
     * are closest to {@code values} according to some distance measure.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please use the {@link #getNearestValueDistances(int, org.prules.dataset.IVector) } and then
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k      the number of neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @param label  class label
     * @param index  index of instances considered for nearest neighbors search
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors
     */
    PairContainer<Collection<T>, Collection<T>> getNearestNeighborsAndAnymies(int k, Vector values, T label, IDataIndex index);

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
     * @param label  class label
     * @param index  index of instances considered for nearest neighbors search
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors (anymies) )Returned value also contains a distance to each neighbor
     */
    PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(int k, Vector values, T label, IDataIndex index);

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
     * @param label          class label
     * @param index          index of instances considered for nearest neighbors search
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors (anymies) )Returned value also contains a distance to each neighbor
     */
    PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, Vector values, T label, IDataIndex index);

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
     * @param label          class label
     * @param index          index of instances considered for nearest neighbors search
     * @return Pair of collection of stored values with associated distances, the first element of the pair containes positive nearest neighbors, the second element contains a collection of negative nearest neighbors (anymies) )Returned value also contains a distance to each neighbor
     */
    PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, int butAtLeastK, Vector values, T label, IDataIndex index);

}
