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
 * The node for a KD tree.
 *
 * @param <T> This is the type of value with is stored with the points and retrieved on nearest
 *            neighbour search
 * @author Sebastian Land
 */
public class KDTreeNode<T> implements Serializable {

    private static final long serialVersionUID = -4204535347268139613L;
    private T storeValue;
    private double[] values;
    private KDTreeNode<T> lesserChild;
    private KDTreeNode<T> greaterChild;
    private int comparisionDimension;

    KDTreeNode(double[] values, T storeValue, int comparisionDimension) {
        this.values = values;
        this.storeValue = storeValue;
        this.comparisionDimension = comparisionDimension;
    }

    KDTreeNode<T> getNearChild(double[] compare) {
        if (compare[comparisionDimension] < values[comparisionDimension])
            return lesserChild;
        else
            return greaterChild;
    }

    KDTreeNode<T> getFarChild(double[] compare) {
        if (compare[comparisionDimension] >= values[comparisionDimension])
            return lesserChild;
        else
            return greaterChild;
    }

    boolean hasNearChild(double[] compare) {
        if (compare[comparisionDimension] < values[comparisionDimension])
            return lesserChild != null;
        else
            return greaterChild != null;
    }

    boolean hasFarChild(double[] compare) {
        if (compare[comparisionDimension] >= values[comparisionDimension])
            return lesserChild != null;
        else
            return greaterChild != null;
    }

    void setChild(KDTreeNode<T> node) {
        if (node.getValues()[comparisionDimension] < values[comparisionDimension])
            lesserChild = node;
        else
            greaterChild = node;
    }

    T getStoreValue() {
        return storeValue;
    }

    public KDTreeNode getLesserChild() {
        return lesserChild;
    }

    public void setLesserChild(KDTreeNode<T> leftChild) {
        this.lesserChild = leftChild;
    }

    public KDTreeNode<T> getGreaterChild() {
        return greaterChild;
    }

    public void setGreaterChild(KDTreeNode<T> rightChild) {
        this.greaterChild = rightChild;
    }

    public double[] getValues() {
        return values;
    }

    double getCompareValue() {
        return values[comparisionDimension];
    }

    int getCompareDimension() {
        return comparisionDimension;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < comparisionDimension; i++) {
            buffer.append(values[i]).append("  ");
        }
        buffer.append("[");
        buffer.append(values[comparisionDimension]);
        buffer.append("]  ");
        for (int i = comparisionDimension + 1; i < values.length; i++) {
            buffer.append(values[i]).append("  ");
        }
        return buffer.toString();
    }
}
