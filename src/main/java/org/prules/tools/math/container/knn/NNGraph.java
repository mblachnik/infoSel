/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.Iterator;
import java.util.Set;
import org.prules.dataset.IInstanceLabels;
import org.prules.tools.math.container.DoubleIntContainer;

/**
 *
 * @author Marcin
 */
public class NNGraph extends NNGraphWithoutAssocuateUpdates implements INNGraph {

    public NNGraph(ISPRClassGeometricDataCollection<IInstanceLabels> samples, int k) {
        super(samples, k);
    }

    public NNGraph(NNGraph nnGraph) {
        super(nnGraph);
    }

    @Override
    public void remove(int nodeId) {
        super.remove(nodeId);
        for (DoubleIntContainer neighbor : neighbors.get(nodeId)) {
            int neighborID = neighbor.getSecond();
            Set<Integer> tmpAssociate = associates.get(neighborID);
            tmpAssociate.remove(nodeId);
            /*
            if (tmpAssociate.contains(nodeId)) {
                tmpAssociate.
                Iterator<Integer> associateIterator = tmpAssociate.iterator();
                while (associateIterator.hasNext()) {
                    Integer associate = associateIterator.next();
                    if (associate == nodeId) {
                        associateIterator.remove();
                        break;
                    }
                }
            }
*/
        }
    }
}
