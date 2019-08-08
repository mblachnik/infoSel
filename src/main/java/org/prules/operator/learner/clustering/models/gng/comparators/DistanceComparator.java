package org.prules.operator.learner.clustering.models.gng.comparators;

import java.util.Comparator;

import org.prules.operator.learner.clustering.gng.NeuronNode;


/**
 * Created by Łukasz Migdałek on 2016-08-03.
 */
public class DistanceComparator implements Comparator<NeuronNode> {

	@Override
	public int compare(NeuronNode neuronNode, NeuronNode n) {
		if (neuronNode.getDist() < n.getDist()) {
			return -1;
		} else if (neuronNode.getDist() == n.getDist()) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public boolean equals(Object o) {
		return false;
	}
}
