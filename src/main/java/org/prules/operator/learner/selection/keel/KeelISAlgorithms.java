/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.keel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public enum KeelISAlgorithms {

    CCIS;

    public static final String PARAMETER_IS_ALGORITHM = "IS_Algorithm";

    public static String[] IS_ALGORITHM_TYPES() {
        KeelISAlgorithms[] fields = KeelISAlgorithms.values();
        String[] names = new String[fields.length];
        int i = 0;
        for (KeelISAlgorithms value : fields) {
            names[i] = value.name();
            i++;
        }
        return names;
    }

    
}
