/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;

/**
 *
 * @author Marcin
 * @param <T>
 */
public interface ISPRGeometricDataCollectionWithIndex<T extends Serializable> extends ISPRGeometricDataCollection<T> {    
    
    /**
     * This method returns a collection of {@code k} stored data values which 
     * are closest to {@code values} according to some distance measure.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please use the {@link #getNearestValueDistances(int, org.prules.dataset.IVector) } and then
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k the number of neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @param index a binary index which allows to switch off several examples
     * @return
     */
    public  Collection<T> getNearestValues(int k, Vector values, IDataIndex index);   
    
    /**
     * This method returns a collection of data from the k nearest sample
     * points. This collection consists of Tupels containing the distance from
     * querrypoint to the samplepoint and in the second component the contained
     * value of the sample point.
     * Note that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please 
     * use {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k the number of neighbours
     * @param values the coordinate of the querry point in the sample dimension
     * @param index a binary index which allows to switch off several examples
     * @return collection of stored values with associated distances
     */
    public  Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Vector values, IDataIndex index);   

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
     * @param values the coordinate of the querry point in the sample dimension
     * @param index a binary index which allows to switch off several examples 
     * @return ccollection of stored values with associated distances
     */
    public  Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, Vector values, IDataIndex index);
    
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
     * @param butAtLeastK - minimum number of nearest neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @param index a binary index which allows to switch off several examples 
     * @return collection of stored values with associated distances
     */
    public  Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, Vector values, IDataIndex index);   
    
    IDataIndex getIndex();
}
