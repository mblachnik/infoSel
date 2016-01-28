/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.tools.math.container;

import com.rapidminer.ispr.operator.learner.tools.TaskType;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 *
 * @author Marcin
 */
public enum GeometricCollectionTypes {

    LINEAR_SEARCH(TaskType.ANY, "Linear search"),
    CACHED_LINEAR_SEARCH(TaskType.ANY, "Cached linear search"),
    BALL_TREE_SEARCH(TaskType.ANY, "Ball tree"),
    KD_TREE_SEARCH(TaskType.ANY, "KD tree");
    private final TaskType type;
    private final String friendlyName;

    public static String[] getFriendlyNames(TaskType type) {
        EnumSet<GeometricCollectionTypes> es;
        es = EnumSet.allOf(GeometricCollectionTypes.class);
        
        ArrayList<String> namesList = new ArrayList<String>(es.size());
        int i = 0;

        for (GeometricCollectionTypes x : es) {
            if(x.type.checkCompatability(type)){
                namesList.add(x.getFriendlyName());            
            }
        }        
        return namesList.toArray(new String[0]);
    }

    GeometricCollectionTypes(TaskType type, String friendlyName) {
        this.type = type;
        this.friendlyName = friendlyName;
    }

    public TaskType getType() {
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
