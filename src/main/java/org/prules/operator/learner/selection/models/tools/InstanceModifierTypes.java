/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.tools;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public enum InstanceModifierTypes {
    NONE("No modification"), GAUSSIAN_NOISE("Gaussian noise");

    private static final Map<String, InstanceModifierTypes> description2ModifierMap = new HashMap<>();

    static {
        for (InstanceModifierTypes value : InstanceModifierTypes.values()) {
            description2ModifierMap.put(value.getDescription(), value);
        }
    }

    private final String description;

    InstanceModifierTypes(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static String[] getDescriptions() {
        InstanceModifierTypes[] values = InstanceModifierTypes.values();
        String[] tab = new String[values.length];
        int i = 0;
        for (InstanceModifierTypes value : values) {
            tab[i] = value.getDescription();
            i++;
        }
        return tab;
    }

    public static InstanceModifierTypes getEnumOf(String description) {
        return description2ModifierMap.get(description);
    }        
}
