/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.Iterator;
import org.prules.dataset.IInstanceLabels;
import org.prules.tools.math.container.DoubleIntContainer;

/**
 *
 * @author Marcin
 */
public class NNGraph extends NNGraphWithoutAssocuateUpdates implements INNGraph {
    

    public NNGraph(ISPRClassGeometricDataCollection<IInstanceLabels> samples, int k) {
        super(samples,k);        
    }

    public NNGraph(NNGraph nnGraph) {
        super(nnGraph);        
    }
    
    @Override
    public void remove(int nodeId) {
        super.remove(nodeId);        
        for (DoubleIntContainer neighbor : neighbors.get(nodeId)) {
            Iterator<DoubleIntContainer> associateIterator = associates.get(neighbor.getSecond()).iterator();
            while (associateIterator.hasNext()) {
                DoubleIntContainer associate = associateIterator.next();
                if (associate.getSecond() == nodeId) {
                    associateIterator.remove();
                    break;
                }
            }
        }
    }        
}
