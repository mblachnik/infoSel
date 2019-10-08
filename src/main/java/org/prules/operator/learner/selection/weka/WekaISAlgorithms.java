/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.weka;

/**
 * @author Marcin
 */
public enum WekaISAlgorithms {

    BSE, CNN, DROP1, DROP2, DROP3, DROP4, DROP5, ENN, HMNE, HMNEI, ICF, MI, MSS, RNN, ENN_REG, CNN_REG, ICF_REG, MSS_REG, CCIS_REG;

    public static final String PARAMETER_IS_ALGORITHM = "IS_Algorithm";

    public static String[] IS_ALGORITHM_TYPES() {
        WekaISAlgorithms[] fields = WekaISAlgorithms.values();
        String[] names = new String[fields.length];
        int i = 0;
        for (WekaISAlgorithms value : fields) {
            names[i] = value.name();
            i++;
        }
        return names;
    }
}
