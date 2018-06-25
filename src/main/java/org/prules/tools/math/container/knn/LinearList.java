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
package org.prules.tools.math.container.knn;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.dataset.Const;
import org.prules.tools.math.similarity.DistanceEvaluator;
import java.util.ArrayList;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.BoundedPriorityQueue;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.PairContainer;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.similarity.IDistanceEvaluator;

/**
 * This class is an implementation of the GeometricDataCollection interface,
 * which searches all datapoints linearly for the next k neighbours. Hence O(n)
 * computations are required for this operation.
 *
 * @author Sebastian Land
 * @param <T>
 */
public class LinearList<T extends IInstanceLabels> implements ISPRClassGeometricDataCollection<T>,  RandomAccess {

    private static final long serialVersionUID = -746048910140779285L;
    final DistanceMeasure distance;
    final List<Vector> samples;
    final List<T> storedValues;
    private long sampleCounter = 0;
    final IDistanceEvaluator distanceEvaluator;

    /**
     * Create data structure for nearest neighbor search using simply priority queue with linear complexity
     * @param distance - type of distance measure used in the calculations
     */
    public LinearList(DistanceMeasure distance) {
        this.distance = distance;
        samples = new ArrayList<>();
        storedValues = new ArrayList<>();
        distanceEvaluator = new DistanceEvaluator(distance);
    }
    
    /**
     * Create data structure for nearest neighbor search using simply priority queue with linear complexity
     * @param distance - type of distance measure used in the calculations
     * @param n - allocate given initial capacity of the internal structure
     */
    public LinearList(DistanceMeasure distance, int n) {
        this.distance = distance;
        samples = new ArrayList<>(n);
        storedValues = new ArrayList<>(n);
        distanceEvaluator = new DistanceEvaluator(distance);
    }

    /**
     * Create data structure for nearest neighbor search using simply priority queue with linear complexity
     * @param samples - the data strucutre is initialized with list of samples 
     * @param storedValues associated values which complement samples
     * @param distance - type of distance measure used in the calculations
     */
    public LinearList(List<Vector> samples, List<T> storedValues, DistanceMeasure distance) {
        this.distance = distance;
        assert samples.size() == storedValues.size();
        this.samples = samples;
        this.storedValues = storedValues;
        for (int i = 0; i < storedValues.size(); i++) {
            storedValues.get(i).put(Const.INDEX_CONTAINER, sampleCounter);
            sampleCounter++;
        }
        distanceEvaluator = new DistanceEvaluator(distance);
    }

    /**
     * Create data structure for nearest neighbor search using simply priority queue with linear complexity
     * It is initialized with RapidMiner exampleSet and a list of attributes which will be stored in the internal associated structure (the list of special attributes)
     * @param exampleSet - input dataset
     * @param storedValuesAttribute - Map which maps given attribute (usually special attribute) into given name of the internal structure
     * @param distance - distance measure
     */
    public LinearList(ExampleSet exampleSet, Map<Attribute, String> storedValuesAttribute, DistanceMeasure distance) {
        this(distance, exampleSet.size());
        initialize(exampleSet, storedValuesAttribute);        
    }

    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedAttributes
     */
    @Override
    public final void initialize(ExampleSet exampleSet, Map<Attribute, String> storedAttributes) {
        int n = exampleSet.size();
        Attributes attributes = exampleSet.getAttributes();
        int valuesSize = attributes.size();
        for (Example example : exampleSet) {
            IInstanceLabels storedValue = InstanceFactory.createInstanceLabels(example, storedAttributes);
            storedValue.put(Const.INDEX_CONTAINER, sampleCounter);
            samples.add(InstanceFactory.createVector(example));
            storedValues.add((T) storedValue);
            sampleCounter++;
        }
    }

    /**
     * Add new sample to the internal structure
     * @param values
     * @param storeValue 
     */
    @Override
    public void add(Vector values, T storeValue) {
        this.samples.add(values);
        storeValue.put(Const.INDEX_CONTAINER, sampleCounter++);
        this.storedValues.add(storeValue);
    }

    /**
     * This method returns a collection of {@code k} stored data values which
     * are closest to {@code values} according to some distance measure. Note
     * that the collection may not preserve proper order of nearest neighbors.
     * If the order is required please use the {@link #getNearestValueDistances(int, org.prules.dataset.IVector)
     * } and then use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k the number of neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @return
     */
    @Override
    public Collection<T> getNearestValues(int k, Vector values) {
        return getNearestValues(k, values, this.getIndex());
    }

    @Override
    public Collection<T> getNearestValues(int k, Vector values, IDataIndex index) {
        if (index.size() != samples.size()) {
            throw new IndexOutOfBoundsException("index has incorect size. It should has the same size as the number of samples");
        }
        List<T> result = new ArrayList<>(k);
        if (k > 1) {
            BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<>(k);
            for(int idx : index){
                Vector currentInstance = samples.get(idx);
                T second = storedValues.get(idx);
                double first = distanceEvaluator.evaluateDistance(currentInstance, values);
                DoubleObjectContainer<T> container = queue.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<>(first, second);
                } else {
                    container.setFirst(first);
                    container.setSecond(second);
                }
                queue.add(container);
            }
            while (!queue.isEmpty()) {
                DoubleObjectContainer<T> tupel = queue.poll();
                result.add(tupel.getSecond());
            }
            Collections.reverse(result);
        } else {            
            double minDist = Double.MAX_VALUE;
            T subResult = null;
            //for (Vector currentInstance : this.samples) {
            for(int i: index){                
                Vector currentInstance = samples.get(i);
                double dist = distanceEvaluator.evaluateDistance(currentInstance, values);
                if (dist < minDist) {
                    minDist = dist;
                    subResult = storedValues.get(i);
                }
            }
            result.add(subResult);
        }
        return result;
    }

    /**
     * This method returns a collection of data from the k nearest sample
     * points. This collection consists of Tupels containing the distance from
     * querrypoint to the samplepoint and in the second component the contained
     * value of the sample point. Note that the collection may not preserve
     * proper order of nearest neighbors. If the order is required please use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k the number of neighbours
     * @param values the coordinate of the querry point in the sample dimension
     * @return collection of stored values with associated distances
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Vector values) {
        return getNearestValueDistances(k, values, this.getIndex());
    }

    /**
     * This method returns a collection of data from the k nearest sample
     * points. This collection consists of Tupels containing the distance from
     * querrypoint to the samplepoint and in the second component the contained
     * value of the sample point. Note that the collection may not preserve
     * proper order of nearest neighbors. If the order is required please use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param k the number of neighbours
     * @param values the coordinate of the querry point in the sample dimension
     * @return collection of stored values with associated distances
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Vector values, IDataIndex index) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<>(k);
        for (int idx : index) {            
            Vector currentInstance = samples.get(idx);
            T second = storedValues.get(idx);
            double first = distanceEvaluator.evaluateDistance(currentInstance, values);
            DoubleObjectContainer<T> container = queue.getEmptyContainer();
            if (container == null) {
                container = new DoubleObjectContainer<>(first, second);
            } else {
                container.setFirst(first);
                container.setSecond(second);
            }
            queue.add(container);
        }
        List<DoubleObjectContainer<T>> results = new ArrayList<>(queue);
        Collections.sort(results);
        return results;
    }

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance. This collection consists of Tupels containing the
     * distance from query point to the sample point and in the second component
     * the label value of the sample point. Note that the collection may not
     * preserve proper order of nearest neighbors. If the order is required
     * please use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param withinDistance minimum distance
     * @param values the coordinate of the querry point in the sample dimension
     * @return ccollection of stored values with associated distances
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, Vector values) {
        return getNearestValueDistances(withinDistance, values, this.getIndex());
    }

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance. This collection consists of Tupels containing the
     * distance from query point to the sample point and in the second component
     * the label value of the sample point. Note that the collection may not
     * preserve proper order of nearest neighbors. If the order is required
     * please use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param withinDistance minimum distance
     * @param values the coordinate of the querry point in the sample dimension
     * @return ccollection of stored values with associated distances
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, Vector values, IDataIndex index) {
        List<DoubleObjectContainer<T>> queue = new ArrayList<>();        
        for (int idx : index) {           
            Vector currentInstance = samples.get(idx);
            T second = storedValues.get(idx);
            double currentDistance = distanceEvaluator.evaluateDistance(currentInstance, values);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<>(currentDistance, second));
            }
        }
        List<DoubleObjectContainer<T>> results = new ArrayList<>(queue);
        Collections.sort(results);
        return results;
    }

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance but at least k points. So the distance might be
     * enlarged if density is to low. This collection consists of Tupels
     * containing the distance from query point to the sample point and in the
     * second component the contained value of the sample point. Note that the
     * collection may not preserve proper order of nearest neighbors. If the
     * order is required please use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param withinDistance - max distance range
     * @param butAtLeastK - minimum number of nearest neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @return collection of stored values with associated distances
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, Vector values) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, values);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, values);
        }
        return result;
    }

    /**
     * This method returns a collection of data from all sample points inside
     * the specified distance but at least k points. So the distance might be
     * enlarged if density is to low. This collection consists of Tupels
     * containing the distance from query point to the sample point and in the
     * second component the contained value of the sample point. Note that the
     * collection may not preserve proper order of nearest neighbors. If the
     * order is required please use
     * {@link Arrays.sort(collection.toArray(new DoubleObjectContainer[res.size()]))}
     *
     * @param withinDistance - max distance range
     * @param butAtLeastK - minimum number of nearest neighbors
     * @param values the coordinate of the query point in the sample dimension
     * @return collection of stored values with associated distances
     */
    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, Vector values, IDataIndex index) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, values, index);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, values, index);
        }
        return result;
    }

    @Override
    public int size() {
        return samples.size();
    }

    @Override
    public T getStoredValue(int index) {
        return storedValues.get(index);
    }

    @Override
    public Vector getSample(int index) {
        return samples.get(index);
    }

    @Override
    public void remove(int n) {
        
        Vector v = samples.remove(n);
        storedValues.remove(n);
        for(long i=n; i<storedValues.size(); i++){
            storedValues.get((int)i).set(Const.INDEX_CONTAINER,i);
        }
        sampleCounter--;
    }

    @Override
    public Iterator<T> storedValueIterator() {
        return storedValues.iterator();
    }

    @Override
    public Iterator<Vector> samplesIterator() {
        return samples.iterator();
    }

    @Override
    public void setSample(int index, Vector sample, T storedValue) {
        samples.set(index, sample);
        storedValues.set(index, storedValue);
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

    @Override
    public PairContainer<Collection<T>, Collection<T>> getNearestNeighborsAndAnymies(int k, Vector values, T label) {        
        return getNearestNeighborsAndAnymies(k, values, label, this.getIndex());
    }

    @Override
    public PairContainer<Collection<T>, Collection<T>> getNearestNeighborsAndAnymies(int k, Vector values, T label, IDataIndex index) {
        List<T> resultPositive = new ArrayList<>(k);
        List<T> resultNegative = new ArrayList<>(k);
        BoundedPriorityQueue<DoubleObjectContainer<T>> queuePositive = new BoundedPriorityQueue<>(k);
        BoundedPriorityQueue<DoubleObjectContainer<T>> queueNegative = new BoundedPriorityQueue<>(k);
        for (int i : index) {
            Vector currentInstance = samples.get(i);
            T second = storedValues.get(i);
            double first = distanceEvaluator.evaluateDistance(currentInstance, values);
            if (second.getLabel()==label.getLabel()) {
                DoubleObjectContainer<T> container = queuePositive.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<>(first, second);
                } else {
                    container.setFirst(first);
                    container.setSecond(second);
                }
                queuePositive.add(container);
            } else {
                DoubleObjectContainer<T> container = queueNegative.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<>(first, second);
                } else {
                    container.setFirst(first);
                    container.setSecond(second);
                }
                queueNegative.add(container);
            }
        }
        while (!queuePositive.isEmpty()) {
            DoubleObjectContainer<T> tupel = queuePositive.poll();
            resultPositive.add(tupel.getSecond());
        }
        Collections.reverse(resultPositive);
        while (!queueNegative.isEmpty()) {
            DoubleObjectContainer<T> tupel = queueNegative.poll();
            resultNegative.add(tupel.getSecond());
        }
        Collections.reverse(resultNegative);
        return new PairContainer<>(resultPositive, resultNegative);
    }

    @Override
    public PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(int k, Vector values, T label) {
        return getNearestNeighborsAndAnymiesDistances(k, values, label, this.getIndex());
    }
    
    @Override
    public PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(int k, Vector values, T label, IDataIndex index) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queuePositive = new BoundedPriorityQueue<>(k);
        BoundedPriorityQueue<DoubleObjectContainer<T>> queueNegative = new BoundedPriorityQueue<>(k);
        for (int i : index){
            Vector currentInstance = samples.get(i);
            T second = storedValues.get(i);
            double first = distanceEvaluator.evaluateDistance(currentInstance, values);
            if (second.getLabel()==label.getLabel()) {
                DoubleObjectContainer<T> container = queuePositive.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<>(first, second);
                } else {
                    container.setFirst(first);
                    container.setSecond(second);
                }
                queuePositive.add(container);
            } else {
                DoubleObjectContainer<T> container = queueNegative.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<>(first, second);
                } else {
                    container.setFirst(first);
                    container.setSecond(second);
                }
                queueNegative.add(container);
            }
        }
        List<DoubleObjectContainer<T>> resultPositive = new ArrayList<>(k);
        List<DoubleObjectContainer<T>> resultNegative = new ArrayList<>(k);
        while (!queuePositive.isEmpty()) {
            DoubleObjectContainer<T> tupel = queuePositive.poll();
            resultPositive.add(tupel);
        }
        Collections.reverse(resultPositive);
        while (!queueNegative.isEmpty()) {
            DoubleObjectContainer<T> tupel = queueNegative.poll();
            resultNegative.add(tupel);
        }
        Collections.reverse(resultNegative);
        return new PairContainer<>( resultPositive, resultNegative);
    }

    @Override
    public PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, Vector values, T label) {
        return getNearestNeighborsAndAnymiesDistances(withinDistance, values, label, this.getIndex());
    }
    
    @Override
    public PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, Vector values, T label, IDataIndex index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, int butAtLeastK, Vector values, T label) {
        return getNearestNeighborsAndAnymiesDistances(withinDistance, butAtLeastK, values, label, this.getIndex());
    }
    
    @Override
    public PairContainer<Collection<DoubleObjectContainer<T>>, Collection<DoubleObjectContainer<T>>> getNearestNeighborsAndAnymiesDistances(double withinDistance, int butAtLeastK, Vector values, T label, IDataIndex index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

    @Override
    public IDataIndex getIndex() {        
        return new DataIndex(samples.size());        
    }
    
    @Override
    public DistanceMeasure getMeasure() {
        return this.distance;
    }

    private class Itr implements ListIterator<PairContainer<Vector, T>> {

        int iteratorState;
        int length = samples.size();

        public Itr() {
            iteratorState = -1;
        }

        public Itr(int id) {
            iteratorState = id;
        }

        @Override
        public boolean hasNext() {
            return iteratorState < length - 1;
        }

        @Override
        public PairContainer<Vector, T> next() {
            if (hasNext()) {
                iteratorState++;
                return new PairContainer<Vector, T>(samples.get(iteratorState), storedValues.get(iteratorState));
            }
            throw new NoSuchElementException();
        }

        @Override
        public PairContainer<Vector, T> previous() {
            if (hasPrevious()) {
                iteratorState--;
                return new PairContainer<Vector, T>(samples.get(iteratorState), storedValues.get(iteratorState));
            }
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasPrevious() {
            return iteratorState > 0;
        }

        @Override
        public int nextIndex() {
            if (hasNext()) {
                return iteratorState + 1;
            }
            return length;
        }

        @Override
        public int previousIndex() {
            if (hasPrevious()) {
                return iteratorState - 1;
            }
            return -1;
        }

        @Override
        public void set(PairContainer<Vector, T> e) {
            samples.set(iteratorState, e.getFirst());
            storedValues.set(iteratorState, e.getSecond());
        }

        @Override
        public void add(PairContainer<Vector, T> e) {
            samples.add(iteratorState, e.getFirst());
            storedValues.add(iteratorState, e.getSecond());
        }

        @Override
        public void remove() {
            samples.remove(iteratorState);
            storedValues.remove(iteratorState);            
        }
    }
}
