/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.weka;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import java.util.Vector;
import main.core.algorithm.Algorithm;

/**
 *
 * @author Marcin
 */
public class WekaISModel extends AbstractInstanceSelectorModel {

    Algorithm isAlgorithm;

    public WekaISModel(Algorithm isAlgorithm) {
        this.isAlgorithm = isAlgorithm;
    }

    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        try {
            isAlgorithm.allSteps();
            Vector<Integer> vIndex = isAlgorithm.getOutputDatasetIndex();
            DataIndex index = exampleSet.getIndex();
            index.setAllFalse();
            for (int i : vIndex) {
                index.set(i, true);
            }
            return index;
        } catch (Exception ex) {
            return null;
        }
    }

}
