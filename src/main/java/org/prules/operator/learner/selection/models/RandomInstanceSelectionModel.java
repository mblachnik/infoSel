/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.parameter.ParameterType;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.PRulesUtil;
import com.rapidminer.tools.RandomGenerator;
import java.util.List;
import java.util.Random;
import org.prules.operator.learner.tools.IDataIndex;

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
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {

        if (sampleSize > exampleSet.size()) return null;
        IDataIndex index = exampleSet.getIndex();
        index.setAllFalse();

        if (stratifiedSelection) {
            //index = PRulesUtil.stratifiedSelectionOfFirstSamplesFromEachClass(exampleSet, sampleSize, randomGenerator);
            index = PRulesUtil.stratifiedSelection(exampleSet, sampleSize, randomGenerator);
        } else {               
            int[] ints = PRulesUtil.randomPermutation(exampleSet.size(), randomGenerator);     
            for (int i = 0; i < sampleSize; i++) {                
                index.set(ints[i], true);
            }
        }
        return index;
    }
    


}
