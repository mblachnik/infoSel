/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import org.prules.operator.learner.tools.PredictionProblemType;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @author Marcin
 */
public enum GeometricCollectionTypes {

    LINEAR_SEARCH(PredictionProblemType.ANY, "Linear search"),
    CACHED_LINEAR_SEARCH(PredictionProblemType.ANY, "Cached linear search"),
    BALL_TREE_SEARCH(PredictionProblemType.ANY, "Ball tree"),
    KD_TREE_SEARCH(PredictionProblemType.ANY, "KD tree");
    private final PredictionProblemType type;
    private final String friendlyName;

    public static String[] getFriendlyNames(PredictionProblemType type) {
        EnumSet<GeometricCollectionTypes> es;
        es = EnumSet.allOf(GeometricCollectionTypes.class);

        ArrayList<String> namesList = new ArrayList<String>(es.size());
        int i = 0;

        for (GeometricCollectionTypes x : es) {
            if (x.type.checkCompatibility(type)) {
                namesList.add(x.getFriendlyName());
            }
        }
        return namesList.toArray(new String[0]);
    }

    GeometricCollectionTypes(PredictionProblemType type, String friendlyName) {
        this.type = type;
        this.friendlyName = friendlyName;
    }

    public PredictionProblemType getType() {
        return type;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public static GeometricCollectionTypes valueOfFriendlyName(String name) {
        if (name != null) {
            for (GeometricCollectionTypes x : GeometricCollectionTypes.values()) {
                if (x.getFriendlyName().equals(name)) {
                    return x;
                }
            }
        }
        throw new IllegalArgumentException();
    }
}
