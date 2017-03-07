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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import com.rapidminer.datatable.SimpleDataTable;
import com.rapidminer.datatable.SimpleDataTableRow;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.VectorDense;
import org.prules.dataset.Const;
import com.rapidminer.tools.math.container.BoundedPriorityQueue;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;

/**
 * This class is an implementation of a Ball-Tree for organizing
 * multidimensional datapoints in a fashion supporting the search for nearest
 * neighbours. This is only working well in low to middle number of dimensions.
 * Since the building of the tree is very expensiv, in most cases a linear
 * search strategy will outperform the ballTree in overall performance.
 *
 * @param <T> This is the type of value with is stored with the points and
 * retrieved on nearest neighbour search
 *
 * @author Sebastian Land
 */
public class BallTree<T extends IInstanceLabels> implements ISPRGeometricDataCollection<T> {

    private static final long serialVersionUID = 2954882147712365506L;
    private BallTreeNode<T> root;
    private int k;
    private double dimensionFactor;
    private DistanceMeasure distance;
    private int size = 0;
    private List<T> values = new ArrayList<>();
    private long index = 0;

    public BallTree(DistanceMeasure distance) {
        this.distance = distance;
    }

    public BallTree(ExampleSet exampleSet, Map<Attribute, String> storedValuesAttribute, DistanceMeasure distance) {
        this(distance);
        initialize(exampleSet, storedValuesAttribute);
    }

    /**
     * Initialize data structure
     *
     * @param exampleSet
     * @param storedValuesAttribute
     */
    @Override
    public final void initialize(ExampleSet exampleSet, Map<Attribute, String> storedValuesAttribute) {
        Attributes attributes = exampleSet.getAttributes();
        int valuesSize = attributes.size();
        for (Example example : exampleSet) {
            double[] exampleValues = new double[valuesSize];
            int i = 0;
            for (Attribute attribute : attributes) {
                exampleValues[i] = example.getValue(attribute);
                i++;
            }
            IInstanceLabels labelValue = InstanceFactory.createInstanceLabels(example, storedValuesAttribute);
            this.add(InstanceFactory.createVector(exampleValues), (T)labelValue);
        }
    }

    @Override
    public void add(Vector values, T storeValue) {
        storeValue.put(Const.INDEX_CONTAINER, index);
        this.size++;
        this.values.add(storeValue);
        if (root == null) {
            root = new BallTreeNode<T>(values.getValues(), 0, storeValue);

            // setting dimension
            k = values.getValues().length;
            dimensionFactor = Math.sqrt(Math.PI) / Math.pow(gammaFunction(k / 2), 1d / k);
        } else {
            double totalAncestorIncrease = 0;
            double bestVolumeIncrease = Double.POSITIVE_INFINITY;
            BallTreeNode<T> bestNode = null;  // this node will be made child of new node
            int bestNodeIndex = 0;
            int bestSide = -1; // -1 left, 1 right 
            BallTreeNode<T> currentNode = root;
            LinkedList<BallTreeNode<T>> ancestorList = new LinkedList<BallTreeNode<T>>();
            while (true) {
                // calculate ancestor increase if added to this current node
                double deltaAncestorIncrease = getVolumeIncludingPoint(currentNode, values.getValues()) - getVolume(currentNode);
                totalAncestorIncrease += deltaAncestorIncrease;

                // calculate new Volume if added as left or right child of current
                double leftVolume = getNewVolume(currentNode, currentNode.getLeftChild(), values.getValues());
                double rightVolume = getNewVolume(currentNode, currentNode.getRightChild(), values.getValues());
                // check if adding as left node is best position till now
                double minVolume = Math.min(leftVolume, rightVolume);
                if (minVolume + totalAncestorIncrease < bestVolumeIncrease) {
                    bestVolumeIncrease = minVolume + totalAncestorIncrease;
                    bestNode = currentNode;
                    bestSide = Double.compare(leftVolume, rightVolume);
                    bestNodeIndex = ancestorList.size();
                }

                // adding next father
                ancestorList.add(currentNode);

                // check for termination
                if (currentNode.isLeaf()) {
                    break;
                }

                // search for better child
                if (currentNode.hasTwoChilds()) {
                    BallTreeNode<T> leftChild = currentNode.getLeftChild();
                    double deltaVLeft = getVolumeIncludingPoint(leftChild, values.getValues()) - getVolume(leftChild);
                    BallTreeNode<T> rightChild = currentNode.getRightChild();
                    double deltaVRight = getVolumeIncludingPoint(rightChild, values.getValues()) - getVolume(rightChild);
                    BallTreeNode<T> betterChild = (deltaVLeft < deltaVRight) ? leftChild : rightChild;
                    currentNode = betterChild;
                } else {
                    // or use single if only one present
                    currentNode = currentNode.getChild();
                }
            }

            // now adding as specified child from bestFather
            BallTreeNode<T> newNode = new BallTreeNode<T>(values.getValues(), 0, storeValue);
            if (bestSide < 0) {
                newNode.setChild(bestNode.getLeftChild());
                bestNode.setLeftChild(newNode);
            } else {
                newNode.setChild(bestNode.getRightChild());
                bestNode.setRightChild(newNode);
            }

            // setting radius of new node
            if (!newNode.isLeaf()) {
                newNode.setRadius(distance.calculateDistance(values.getValues(), newNode.getChild().getCenter()) + newNode.getChild().getRadius());
            }

            // correcting radius of all ancestors
            ListIterator<BallTreeNode<T>> iterator = ancestorList.listIterator(bestNodeIndex + 1);
            while (iterator.hasPrevious()) {
                BallTreeNode<T> ancestor = iterator.previous();
                if (ancestor.hasTwoChilds()) {
                    BallTreeNode<T> leftChild = ancestor.getLeftChild();
                    BallTreeNode<T> rightChild = ancestor.getRightChild();
                    ancestor.setRadius(Math.max(rightChild.getRadius() + distance.calculateDistance(rightChild.getCenter(), ancestor.getCenter()),
                            leftChild.getRadius() + distance.calculateDistance(leftChild.getCenter(), ancestor.getCenter())));
                } else {
                    BallTreeNode<T> child = ancestor.getChild();
                    ancestor.setRadius(distance.calculateDistance(ancestor.getCenter(), child.getCenter()) + child.getRadius());
                }
            }
        }
    }

    /**
     * Returns the volume of the ball if the new node is added as child of
     * father and new father of child with center as center. Child might be
     * null, then the radius is 0
     */
    private double getNewVolume(BallTreeNode<T> father, BallTreeNode<T> child, double[] center) {
        if (child == null) {
            return 0;
        }
        return Math.pow((distance.calculateDistance(center, child.getCenter()) + child.getRadius()) * dimensionFactor, k);
    }

    @Override
    public Collection<T> getNearestValues(int k, Vector values) {
        BoundedPriorityQueue<DoubleObjectContainer<BallTreeNode<T>>> priorityQueue = getNearestNodes(k, values.getValues());
        LinkedList<T> neighboursList = new LinkedList<>();
        for (DoubleObjectContainer<BallTreeNode<T>> tupel : priorityQueue) {
            neighboursList.add((tupel.getSecond()).getStoreValue());
        }
        return neighboursList;
    }

    @Override
    public Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, Vector values) {
        BoundedPriorityQueue<DoubleObjectContainer<BallTreeNode<T>>> priorityQueue = getNearestNodes(k, values.getValues());
        LinkedList<DoubleObjectContainer<T>> neighboursList = new LinkedList<>();
        for (DoubleObjectContainer<BallTreeNode<T>> tupel : priorityQueue) {
            boolean add = neighboursList.add(new DoubleObjectContainer<T>(tupel.getFirst(), tupel.getSecond().getStoreValue()));
        }
        return neighboursList;

    }

    private BoundedPriorityQueue<DoubleObjectContainer<BallTreeNode<T>>> getNearestNodes(int k, double[] values) {
        Stack<BallTreeNode<T>> nodeStack = new Stack<>();
        Stack<Integer> sideStack = new Stack<>();
        // first doing initial search for nearest Node
        traverseTree(nodeStack, sideStack, root, values);
        // creating data structure for finding k nearest values
        BoundedPriorityQueue<DoubleObjectContainer<BallTreeNode<T>>> priorityQueue = new BoundedPriorityQueue<>(k);

        // now work on stack
        while (!nodeStack.isEmpty()) {
            // put top element into priorityQueue
            BallTreeNode<T> currentNode = nodeStack.pop();
            Integer currentSide = sideStack.pop();
            DoubleObjectContainer<BallTreeNode<T>> currentTupel = new DoubleObjectContainer<>(distance.calculateDistance(currentNode.getCenter(), values), currentNode);
            priorityQueue.add(currentTupel);
            // now check if far children has to be regarded
            if (currentNode.hasTwoChilds()) {
                BallTreeNode<T> otherChild = (currentSide < 0) ? currentNode.getRightChild() : currentNode.getLeftChild();
                if (!priorityQueue.isFilled()
                        || priorityQueue.peek().getFirst() + otherChild.getRadius()
                        > distance.calculateDistance(values, otherChild.getCenter())) {
                    // if needs to be checked, traverse tree to not visited leaf
                    traverseTree(nodeStack, sideStack, otherChild, values);
                }
            }
            // go on, until stack is empty
        }
        return priorityQueue;
    }

    private void traverseTree(Stack<BallTreeNode<T>> stack, Stack<Integer> sideStack, BallTreeNode<T> root, double[] values) {
        BallTreeNode<T> currentNode = root;
        stack.push(currentNode);
        while (!currentNode.isLeaf()) {
            if (currentNode.hasTwoChilds()) {
                double distanceLeft = distance.calculateDistance(currentNode.getLeftChild().getCenter(), values);
                double distanceRight = distance.calculateDistance(currentNode.getRightChild().getCenter(), values);
                currentNode = (distanceLeft < distanceRight) ? currentNode.getLeftChild() : currentNode.getRightChild();
                sideStack.push(Double.compare(distanceLeft, distanceRight));
            } else {
                currentNode = currentNode.getChild();
                sideStack.push(0);
            }
            stack.push(currentNode);
        }
        sideStack.push(0);
    }

    private double getVolumeIncludingPoint(BallTreeNode node, double[] point) {
        return Math.pow(Math.max(node.getRadius(), distance.calculateDistance(point, node.getCenter())) * dimensionFactor, k);
    }

    private double getVolume(BallTreeNode node) {
        return Math.pow(node.getRadius() * dimensionFactor, k);
    }

    private double gammaFunction(int n) {
        double result = 1;
        for (int i = 2; i < n; i++) {
            result *= i;
        }
        return result;
    }

    public SimpleDataTable getVisualization() {
        SimpleDataTable table = new SimpleDataTable("BallTree", new String[]{"x", "y", "radius"});
        fillTable(table, root);
        return table;
    }

    private void fillTable(SimpleDataTable table, BallTreeNode<T> node) {
        table.add(new SimpleDataTableRow(new double[]{node.getCenter()[0], node.getCenter()[1], node.getRadius()}));
        if (node.hasLeftChild()) {
            fillTable(table, node.getLeftChild());
        }
        if (node.hasRightChild()) {
            fillTable(table, node.getRightChild());
        }
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
    public Vector getSample(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void remove(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Iterator<T> storedValueIterator() {
        return values.iterator();
    }

    @Override
    public Iterator<Vector> samplesIterator() {
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
