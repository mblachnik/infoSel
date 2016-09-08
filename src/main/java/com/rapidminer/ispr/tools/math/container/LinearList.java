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
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.dataset.VectorDense;
import com.rapidminer.ispr.dataset.Const;
import com.rapidminer.ispr.tools.math.similarity.DistanceEvaluator;
import java.util.ArrayList;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IVector;

/**
 * This class is an implementation of the GeometricDataCollection interface,
 * which searches all datapoints linearly for the next k neighbours. Hence O(n)
 * computations are required for this operation.
 *
 * @author Sebastian Land
 * @param <T>
 */
public class LinearList<T extends IValuesStoreLabels> implements ISPRGeometricDataCollection<T>, RandomAccess {

    private static final long serialVersionUID = -746048910140779285L;
    DistanceMeasure distance;
    List<IVector> samples;
    List<T> storedValues;       
    private long index = 0;

    public LinearList(DistanceMeasure distance, int n) {
        this.distance = distance;
        samples = new ArrayList<>(n);
        storedValues = new ArrayList<>(n);                
    }

    public LinearList(List<IVector> samples, List<T> storedValues, DistanceMeasure distance) {
        this.distance = distance;
        assert samples.size() == storedValues.size();
        this.samples = samples;
        this.storedValues = storedValues;
        for (int i=0; i<storedValues.size(); i++){
            storedValues.get(i).put(Const.INDEX_CONTAINER, index);
            index++;
        }
    }
    
    public LinearList(ExampleSet exampleSet, Map<Attribute,String> storedValuesAttribute, DistanceMeasure distance) {        
        this(distance,exampleSet.size());
        initialize(exampleSet, storedValuesAttribute);
    }

    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedAttributes     
     */
    @Override
    public final void initialize(ExampleSet exampleSet, Map<Attribute,String> storedAttributes) {
        int n = exampleSet.size();       
        Attributes attributes = exampleSet.getAttributes();
        int valuesSize = attributes.size();        
        for (Example example : exampleSet) {            
            IValuesStoreLabels storedValue = ValuesStoreFactory.createValuesStoreLabels(example, storedAttributes);
            storedValue.put(Const.INDEX_CONTAINER, index);
            samples.add(ValuesStoreFactory.createVector(example));
            storedValues.add((T)storedValue);            
            index++;
        }
    }
    
    @Override
    public void add(IVector values, T storeValue) {
        this.samples.add(values);
        storeValue.put(Const.INDEX_CONTAINER, index++);
        this.storedValues.add(storeValue);
    }

    @Override
    public Collection<T> getNearestValues(int k, IVector values) {
        Collection<T> result = new ArrayList<>(k);
        if (k > 1) {
            BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<>(k);
            int i = 0;
            for (IVector currentInstance : this.samples) {                
                T second = storedValues.get(i);
                double first = DistanceEvaluator.evaluateDistance(distance,currentInstance, values);
                DoubleObjectContainer<T> container = queue.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<>(first, second);
                } else {
                    container.setFirst(first);
                    container.setSecond(second);
                }
                queue.add(container);
                i++;
            }
            for (DoubleObjectContainer<T> tupel : queue) {
                result.add(tupel.getSecond());
            }
        } else {
            int i = 0;
            double minDist = Double.MAX_VALUE;
            T subResult = null;
            for (IVector currentInstance : this.samples) {                
                double dist = DistanceEvaluator.evaluateDistance(distance,currentInstance, values);
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

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, IVector values) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<>(k);
        int i = 0;
        for (IVector currentInstance : this.samples) {            
            T second = storedValues.get(i);
            double first = DistanceEvaluator.evaluateDistance(distance, currentInstance, values);
            DoubleObjectContainer<T> container = queue.getEmptyContainer();
            if (container == null) {
                container = new DoubleObjectContainer<>(first, second);
            } else {
                container.setFirst(first);
                container.setSecond(second);
            }
            queue.add(container);
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, IVector values) {
        ArrayList<DoubleObjectContainer<T>> queue = new ArrayList<DoubleObjectContainer<T>>();
        int i = 0;
        for (IVector currentInstance : this.samples) {            
            double currentDistance = DistanceEvaluator.evaluateDistance(distance, currentInstance, values);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<T>(currentDistance, storedValues.get(i)));
            }
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, IVector values) {
        Collection<DoubleObjectContainer<T>> result = getNearestValueDistances(withinDistance, values);
        if (result.size() < butAtLeastK) {
            return getNearestValueDistances(butAtLeastK, values);
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
    public IVector getSample(int index) {
        return samples.get(index);
    }

    @Override
    public void remove(int n) {
        samples.remove(n);
        storedValues.remove(n);
    }

    @Override
    public Iterator<T> storedValueIterator() {
        return storedValues.iterator();
    }

    @Override
    public Iterator<IVector> samplesIterator() {
        return samples.iterator();
    }

    @Override
    public void setSample(int index, IVector sample, T storedValue) {
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

    private class Itr implements ListIterator<PairContainer<IVector, T>> {

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
        public PairContainer<IVector, T> next() {
            if (hasNext()) {
                iteratorState++;
                return new PairContainer<IVector, T>(samples.get(iteratorState), storedValues.get(iteratorState));
            }
            throw new NoSuchElementException();
        }

        @Override
        public PairContainer<IVector, T> previous() {
            if (hasPrevious()) {
                iteratorState--;
                return new PairContainer<IVector, T>(samples.get(iteratorState), storedValues.get(iteratorState));
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
        public void set(PairContainer<IVector, T> e) {
            samples.set(iteratorState, e.getFirst());
            storedValues.set(iteratorState, e.getSecond());
        }

        @Override
        public void add(PairContainer<IVector, T> e) {
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
