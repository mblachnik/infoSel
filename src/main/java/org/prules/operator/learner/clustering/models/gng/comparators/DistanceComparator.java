package org.prules.operator.learner.clustering.models.gng.comparators;

import org.prules.operator.learner.clustering.gng.NeuronNode;

import java.util.Comparator;


/**
 * Created by Łukasz Migdałek on 2016-08-03.
 */
public class DistanceComparator implements Comparator<NeuronNode> {

    @Override
    public int compare(NeuronNode neuronNode, NeuronNode n) {
        return Double.compare(neuronNode.getDist(), n.getDist());
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
