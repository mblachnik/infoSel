/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import com.rapidminer.tools.math.container.BoundedPriorityQueue;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

/**
 * This class is an implementation of a KD-Tree for organizing multidimensional
 * datapoints in a fashion supporting the search for nearest neighbours. This is
 * only working well in low dimensions.
 *
 * @author Sebastian Land
 *
 * @param <T> This is the type of value with is stored with the points and
 * retrieved on nearest neighbour search
 */
public class KDTree<T extends IInstanceLabels> implements ISPRGeometricDataCollection<T> {

    private static final long serialVersionUID = -8531805333989991725L;
    private KDTreeNode<T> root;
    private int k; // the number of dimensions
    private DistanceMeasure distance;
    private int size = 0;
    private ArrayList<T> values = new ArrayList<T>();
    long index = 0;

    public KDTree(DistanceMeasure distance, int numberOfDimensions) {
        this.k = numberOfDimensions;
        this.distance = distance;
    }

    public KDTree(ExampleSet exampleSet, Map<Attribute,String> storedValuesAttribute, DistanceMeasure distance) {
        this(distance,exampleSet.size());
        initialize(exampleSet, storedValuesAttribute);
    }
     
    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedValuesAttribute     
     */
    @Override
    public final void initialize(ExampleSet exampleSet, Map<Attribute,String> storedValuesAttribute) {
        Attributes attributes = exampleSet.getAttributes();
        int valuesSize = attributes.size();
        for (Example example : exampleSet) {
            double[] exampleValues = new double[valuesSize];
            int i = 0;
            for (Attribute attribute : attributes) {
                exampleValues[i] = example.getValue(attribute);
                i++;
            }
            IInstanceLabels labelValue = InstanceFactory.createInstanceLabels(example,storedValuesAttribute);
            this.add(InstanceFactory.createVector(exampleValues),(T)labelValue);
        }
    }
    
    @Override
    public void add(Vector values, T storeValue) {
        storeValue.put(Const.INDEX_CONTAINER, index++);
        this.size++;
        this.values.add(storeValue);
        if (root == null) {
            this.root = new KDTreeNode<T>(values.getValues(), storeValue, 0);
        } else {
            int currentDimension = 0;
            int depth = 0;
            KDTreeNode<T> currentNode = root;
            KDTreeNode<T> childNode;
            // running through tree until empty leaf found: Add new node with given values
            while (true) {
                childNode = currentNode.getNearChild(values.getValues());
                if (childNode == null) {
                    break;
                } else {
                    currentNode = childNode;
                    depth++;
                    currentDimension = depth % k;
                }
            }
            currentNode.setChild(new KDTreeNode<T>(values.getValues(), storeValue, currentDimension));
        }
    }

    @Override
    public Collection<T> getNearestValues(int k, Vector values) {
        BoundedPriorityQueue<DoubleObjectContainer<KDTreeNode<T>>> priorityQueue = getNearestNodes(k, values.getValues());
        LinkedList<T> neighboursList = new LinkedList<T>();
        for (DoubleObjectContainer<KDTreeNode<T>> tupel : priorityQueue) {
            neighboursList.add(tupel.getSecond().getStoreValue());
        }
        return neighboursList;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Vector values) {
        BoundedPriorityQueue<DoubleObjectContainer<KDTreeNode<T>>> priorityQueue = getNearestNodes(k, values.getValues());
        LinkedList<DoubleObjectContainer<T>> neighboursList = new LinkedList<DoubleObjectContainer<T>>();
        for (DoubleObjectContainer<KDTreeNode<T>> tupel : priorityQueue) {
            neighboursList.add(new DoubleObjectContainer<T>(tupel.getFirst(), tupel.getSecond().getStoreValue()));
        }
        return neighboursList;
    }

    private BoundedPriorityQueue<DoubleObjectContainer<KDTreeNode<T>>> getNearestNodes(int k, double[] values) {
        Stack<KDTreeNode<T>> nodeStack = new Stack<KDTreeNode<T>>();
        // first doing initial search for nearest Node
        nodeStack = traverseTree(nodeStack, root, values);

        // creating data structure for finding k nearest values
        BoundedPriorityQueue<DoubleObjectContainer<KDTreeNode<T>>> priorityQueue = new BoundedPriorityQueue<DoubleObjectContainer<KDTreeNode<T>>>(k);

        // now work on stack
        while (!nodeStack.isEmpty()) {
            // put top element into priorityQueue
            KDTreeNode<T> currentNode = nodeStack.pop();
            DoubleObjectContainer<KDTreeNode<T>> currentTupel = new DoubleObjectContainer<KDTreeNode<T>>(distance.calculateDistance(currentNode.getValues(), values), currentNode);
            priorityQueue.add(currentTupel);
            // now check if far children has to be regarded
            if (!priorityQueue.isFilled()
                    || priorityQueue.peek().getFirst() > currentNode.getCompareValue() - values[currentNode.getCompareDimension()]) {
                // if needs to be checked, traverse tree to nearest leaf
                if (currentNode.hasFarChild(values)) {
                    traverseTree(nodeStack, currentNode.getFarChild(values), values);
                }
            }

            // go on, until stack is empty
        }
        return priorityQueue;
    }

    private Stack<KDTreeNode<T>> traverseTree(Stack<KDTreeNode<T>> stack, KDTreeNode<T> root, double[] values) {
        KDTreeNode<T> currentNode = root;
        stack.push(currentNode);
        while (currentNode.hasNearChild(values)) {
            currentNode = currentNode.getNearChild(values);
            stack.push(currentNode);
        }
        return stack;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, Vector values) {
        throw new RuntimeException("Not supported method");
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, Vector values) {
        throw new RuntimeException("Not supported method");
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getStoredValue(int index) {
        return values.get(index);
    }

    @Override
    public void remove(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Vector getSample(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }    
    
    @Override
    public Iterator<T> storedValueIterator(){
        return values.iterator();
    }
        
    @Override
    public Iterator<Vector> samplesIterator(){
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setSample(int index, Vector sample, T storedValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Count how many unique appears in the storedValue structure
     *
     * @return number of unique values
     */
    @Override
    public int numberOfUniquesOfStoredValues() {
        Set<T> uniqueValues = new HashSet<>();
        for (T value : values) {
            uniqueValues.add(value);
        }
        return uniqueValues.size();
    }
    
    @Override
    public DistanceMeasure getMeasure() {
        return this.distance;
    }
}
