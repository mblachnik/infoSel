package com.rapidminer.ispr.operator.learner.feature.construction;

// List of MDS algorithms
public enum MdsAlgorithm {
	CLASSICAL_SCALING("Classical Scaling"), FULL_MDS("Full MDS"), PIVOT_MDS("Pivot MDS"), LANDMARK_MDS("Landmark MDS");

	private final String text;

	private MdsAlgorithm(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}

        /**
         * Converts string into appropriate Enum ignoring case
         * @param text name of ENUM
         * @return ENUM value
         */
	public static MdsAlgorithm fromString(String text) {

		if (text != null) {
			for (MdsAlgorithm mds : MdsAlgorithm.values()) {
				if (text.equalsIgnoreCase(mds.text)) {
					return mds;
				}
			}
		}
		throw new IllegalArgumentException("No constant with text " + text + " found");
	}
}
