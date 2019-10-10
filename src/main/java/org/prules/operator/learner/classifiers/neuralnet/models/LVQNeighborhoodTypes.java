/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.neuralnet.models;

/**
 * @author Marcin
 */
public enum LVQNeighborhoodTypes {
    EUCLIDEAN, GAUSSIAN;

    private static final String[] typeNames;

    static {
        LVQNeighborhoodTypes[] fields = LVQNeighborhoodTypes.values();
        typeNames = new String[fields.length];
        int i = 0;
        for (LVQNeighborhoodTypes value : fields) {
            typeNames[i] = value.name();
            i++;
        }
    }

    public static String[] typeNames() {
        return typeNames;
    }
}
