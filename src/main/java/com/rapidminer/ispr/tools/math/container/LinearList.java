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
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.ispr.dataset.InstanceGenerator;
import com.rapidminer.ispr.dataset.SimpleInstance;
import com.rapidminer.ispr.dataset.StoredValuesHelper;
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

/**
 * This class is an implementation of the GeometricDataCollection interface,
 * which searches all datapoints linearly for the next k neighbours. Hence O(n)
 * computations are required for this operation.
 *
 * @author Sebastian Land
 * @param <T>
 */
public class LinearList<T extends IStoredValues> implements ISPRGeometricDataCollection<T>, RandomAccess {

    private static final long serialVersionUID = -746048910140779285L;
    DistanceMeasure distance;
    List<Instance> samples;
    List<T> storedValues;       
    private long index = 0;

    public LinearList(DistanceMeasure distance, int n) {
        this.distance = distance;
        samples = new ArrayList<>(n);
        storedValues = new ArrayList<>(n);                
    }

    public LinearList(List<Instance> samples, List<T> storedValues, DistanceMeasure distance) {
        this.distance = distance;
        assert samples.size() == storedValues.size();
        this.samples = samples;
        this.storedValues = storedValues;
        for (int i=0; i<storedValues.size(); i++){
            storedValues.get(i).setValue(StoredValuesHelper.INDEX, index);
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
            IStoredValues storedValue = StoredValuesHelper.createStoredValue(example, storedAttributes);
            storedValue.setValue(StoredValuesHelper.INDEX, index);
            samples.add(InstanceGenerator.generateInstance(example));
            storedValues.add((T)storedValue);            
            index++;
        }
    }
    
    @Override
    public void add(Instance values, T storeValue) {
        this.samples.add(values);
        storeValue.setValue(StoredValuesHelper.INDEX, index++);
        this.storedValues.add(storeValue);
    }

    @Override
    public Collection<T> getNearestValues(int k, Instance values) {
        Collection<T> result = new ArrayList<>(k);
        if (k > 1) {
            BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
            int i = 0;
            for (Instance currentInstance : this.samples) {                
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
            for (Instance currentInstance : this.samples) {                
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
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Instance values) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<>(k);
        int i = 0;
        for (Instance currentInstance : this.samples) {            
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
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, Instance values) {
        ArrayList<DoubleObjectContainer<T>> queue = new ArrayList<DoubleObjectContainer<T>>();
        int i = 0;
        for (Instance currentInstance : this.samples) {            
            double currentDistance = DistanceEvaluator.evaluateDistance(distance, currentInstance, values);
            if (currentDistance <= withinDistance) {
                queue.add(new DoubleObjectContainer<T>(currentDistance, storedValues.get(i)));
            }
            i++;
        }
        return queue;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, Instance values) {
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
    public Instance getSample(int index) {
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
    public Iterator<Instance> samplesIterator() {
        return samples.iterator();
    }

    @Override
    public void setSample(int index, Instance sample, T storedValue) {
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

    private class Itr implements ListIterator<PairContainer<Instance, T>> {

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
        public PairContainer<Instance, T> next() {
            if (hasNext()) {
                iteratorState++;
                return new PairContainer<Instance, T>(samples.get(iteratorState), storedValues.get(iteratorState));
            }
            throw new NoSuchElementException();
        }

        @Override
        public PairContainer<Instance, T> previous() {
            if (hasPrevious()) {
                iteratorState--;
                return new PairContainer<Instance, T>(samples.get(iteratorState), storedValues.get(iteratorState));
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
        public void set(PairContainer<Instance, T> e) {
            samples.set(iteratorState, e.getFirst());
            storedValues.set(iteratorState, e.getSecond());
        }

        @Override
        public void add(PairContainer<Instance, T> e) {
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
