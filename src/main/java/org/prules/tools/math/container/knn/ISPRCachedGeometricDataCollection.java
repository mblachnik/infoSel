/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import com.rapidminer.tools.container.Tupel;
import java.io.Serializable;
import java.util.Collection;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.dataset.IInstanceLabels;

/**
 *
 * @author Marcin
 * @param <T>
 */
public interface ISPRCachedGeometricDataCollection<T extends Serializable> extends ISPRGeometricDataCollection<T> {

    Collection<DoubleObjectContainer<T>> getNearestValueDistances(int k, int index);

    Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int index);

    Collection<DoubleObjectContainer<T>> getNearestValueDistances(double withinDistance, int butAtLeastK, int index);

    Collection<T> getNearestValues(int k, int index);
}
