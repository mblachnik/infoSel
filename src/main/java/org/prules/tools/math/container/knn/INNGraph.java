/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleIntContainer;

/**
 *
 * @author Marcin
 */
public interface INNGraph {

    /**
     * Returns set of associate samples that are these samples which one of
     * neighbors is instance with nodeId
     *
     * @param nodeId
     * @return
     */
    Set<DoubleIntContainer> getAssociates(int nodeId);

    /**
     * Returns list containing ordered indexes of enemies samples. The elements
     * are sorted from the nearest enemy to the farthest enemy This elements are
     * iteratively updated such that after removal of a sample an instance new
     * enemies are determined
     *
     * @param nodeId
     * @return list of pair containing distance and id of enemies
     */
    List<DoubleIntContainer> getEnemies(int nodeId);

    IDataIndex getIndex();

    /**
     * Returns list containing ordered indexes of neighbor samples. The elements
     * are sorted from the most nearest neighbor to the farthest neighbor
     *
     * @param nodeId
     * @return list of pairs containing distance and the id of nearest neighbor
     */
    List<DoubleIntContainer> getNeighbors(int nodeId);

    void initialize();

    void remove(int nodeId);      
    
    ISPRClassGeometricDataCollection<IInstanceLabels> getSamples();
    
    int getK();
    
    int size();
}
