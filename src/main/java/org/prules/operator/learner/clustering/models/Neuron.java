package org.prules.operator.learner.clustering.models;

/**
 * Created by Łukasz Migdałek on 2016-07-16.
 */
public class Neuron implements Comparable<Neuron> {
	private int index;
	private double dist;

	public Neuron(int index, double dist) {
		this.index = index;
		this.dist = dist;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		this.dist = dist;
	}

	@Override
	public int compareTo(Neuron o) {
		if (this.dist < o.dist) {
			return -1;
		} else if (this.dist == o.dist) {
			return 0;
		} else {
			return 1;
		}
	}

}
