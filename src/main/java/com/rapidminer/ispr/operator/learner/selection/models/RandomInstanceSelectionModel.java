/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.tools.RandomGenerator;

/**
 *
 * @author Marcin
 */
public class RandomInstanceSelectionModel extends AbstractInstanceSelectorModel {
    private final int sampleSize;
    private final boolean stratifiedSelection;
    private final RandomGenerator randomGenerator;

    public RandomInstanceSelectionModel(int sampleSize, boolean stratifiedSelection, RandomGenerator randomGenerator) {
        this.sampleSize = sampleSize;
        this.stratifiedSelection = stratifiedSelection;
        this.randomGenerator = randomGenerator;
    }
    
    

    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {

        if (sampleSize > exampleSet.size()) return null;
        DataIndex index = exampleSet.getIndex();
        index.setAllFalse();

        if (stratifiedSelection) {
            index = PRulesUtil.stratifiedSelection(exampleSet, sampleSize, randomGenerator);
        } else {            
            for (int i = 0; i < sampleSize; i++) {                
                index.set(i, true);
            }
        }
        return index;
    }

}
