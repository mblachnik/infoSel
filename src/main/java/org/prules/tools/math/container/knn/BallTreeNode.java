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

import java.io.Serializable;

/**
 * The node for a ball tree.
 *
 * @param <T> This is the type of value with is stored with the points and retrieved on nearest
 *            neighbour search
 * @author Sebastian Land
 */
public class BallTreeNode<T> implements Serializable {

    private static final long serialVersionUID = 5250382342093166168L;
    private double[] center;
    private double radius;
    private T value;

    private BallTreeNode<T> leftChild;
    private BallTreeNode<T> rightChild;

    BallTreeNode(double[] center, double radius, T value) {
        this.center = center;
        this.radius = radius;
        this.value = value;
    }

    BallTreeNode<T> getLeftChild() {
        return leftChild;
    }

    void setLeftChild(BallTreeNode<T> leftChild) {
        this.leftChild = leftChild;
    }

    BallTreeNode<T> getRightChild() {
        return rightChild;
    }

    void setRightChild(BallTreeNode<T> rightChild) {
        this.rightChild = rightChild;
    }

    double[] getCenter() {
        return center;
    }

    double getRadius() {
        return radius;
    }


    public void replaceChild(BallTreeNode<T> replaceNode, BallTreeNode<T> replacementNode) {
        if (leftChild == replaceNode)
            leftChild = replacementNode;
        if (rightChild == replaceNode)
            rightChild = replacementNode;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (double centerDouble : center) {
            buffer.append(centerDouble).append("  ");
        }
        buffer.append("]  ");
        buffer.append(radius);
        return buffer.toString();
    }

    void setRadius(double radius) {
        this.radius = radius;
    }

    boolean isLeaf() {
        return getLeftChild() == null && getRightChild() == null;
    }

    boolean hasTwoChildren() {
        return (getLeftChild() != null && getRightChild() != null);
    }

    /**
     * This method returns the left child if existing or the right child
     * if left doesnt exist. If right is null either, then null is returned
     */
    BallTreeNode<T> getChild() {
        if (getLeftChild() != null) {
            return getLeftChild();
        } else {
            return getRightChild();
        }
    }

    void setChild(BallTreeNode<T> node) {
        if (!hasLeftChild()) {
            setLeftChild(node);
        } else {
            setRightChild(node);
        }
    }

    T getStoreValue() {
        return value;
    }

    boolean hasLeftChild() {
        return this.leftChild != null;
    }

    boolean hasRightChild() {
        return this.rightChild != null;
    }
}
