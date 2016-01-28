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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;

import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * This class is an implementation of the GeometricDataCollection interface,
 * which searches all datapoints linearly for the next k neighbours. Hence O(n)
 * computations are required for this operation.
 *
 * @author Sebastian Land
 *
 * @param <T> This is the type of value with is stored with the points and
 * retrieved on nearest neighbour search
 */
public class MyLinearList<T extends Serializable> implements ISPRGeometricDataCollection<T>, RandomAccess {

    private static final long serialVersionUID = -746048910140779285L;
    DistanceMeasure distance;
    ArrayList<double[]> samples;
    ArrayList<T> storedValues;

    public MyLinearList(DistanceMeasure distance, int n) {
        this.distance = distance;
        samples = new ArrayList<double[]>(n);
        storedValues = new ArrayList<T>(n);
    }

    public MyLinearList(ArrayList<double[]> samples, ArrayList<T> storedValues, DistanceMeasure distance) {
        this.distance = distance;
        assert samples.size() == storedValues.size();
        this.samples = samples;
        this.storedValues = storedValues;
    }

    @Override
    public void add(double[] values, T storeValue) {
        this.samples.add(values);
        this.storedValues.add(storeValue);
    }

    @Override
    public Collection<T> getNearestValues(int k, double[] values) {
        Collection<T> result = new ArrayList<T>(k);
        if (k > 1) {
            BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
            int i = 0;
            for (double[] sample : this.samples) {
                T second = storedValues.get(i);
                double first = distance.calculateDistance(sample, values);
                DoubleObjectContainer<T> container = queue.getEmptyContainer();
                if (container == null) {
                    container = new DoubleObjectContainer<T>(first, second);
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

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, double[] values) {
        BoundedPriorityQueue<DoubleObjectContainer<T>> queue = new BoundedPriorityQueue<DoubleObjectContainer<T>>(k);
        int i = 0;
        for (double[] sample : this.samples) {
            T second = storedValues.get(i);
            double first = distance.calculateDistance(sample, values);
            DoubleObjectContainer<T> container = queue.getEmptyContainer();
            if (container == null) {
                container = new DoubleObjectContainer<T>(first, second);
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

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, double[] values) {
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
    public double[] getSample(int index) {
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
    public Iterator<double[]> samplesIterator() {
        return samples.iterator();
    }

    @Override
    public void setSample(int index, double[] sample, T storedValue) {
        samples.set(index, sample);
        storedValues.set(index, storedValue);
    }

    private class Itr implements ListIterator<PairContainer<double[], T>> {

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
        public PairContainer<double[], T> next() {
            if (hasNext()) {
                iteratorState++;
                return new PairContainer<double[], T>(samples.get(iteratorState), storedValues.get(iteratorState));
            }
            throw new NoSuchElementException();
        }

        @Override
        public PairContainer<double[], T> previous() {
            if (hasPrevious()) {
                iteratorState--;
                return new PairContainer<double[], T>(samples.get(iteratorState), storedValues.get(iteratorState));
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
        public void set(PairContainer<double[], T> e) {
            samples.set(iteratorState, e.getFirst());
            storedValues.set(iteratorState, e.getSecond());
        }

        @Override
        public void add(PairContainer<double[], T> e) {
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
