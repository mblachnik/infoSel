package org.prules.operator.learner.clustering.models.gng.comparators;

import java.util.Comparator;

import org.prules.operator.learner.clustering.gng.NeuronNode;

/**
 * Created by Łukasz Migdałek on 2016-08-03.
 */
public class LocalErrorComparator implements Comparator<NeuronNode> {
	@Override
	public int compare(NeuronNode neuronNode, NeuronNode n) {
		if (neuronNode.getLocalError() < n.getLocalError()) {
			return -1;
		} else if (neuronNode.getLocalError() == n.getLocalError()) {
			return 0;
		} else {
			return 1;
		}
	}
}
