/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.weka;

import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.tools.DataIndex;
import java.util.Vector;
import main.core.algorithm.Algorithm;
import org.prules.operator.learner.tools.IDataIndex;

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
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        try {
            isAlgorithm.allSteps();
            Vector<Integer> vIndex = isAlgorithm.getOutputDatasetIndex();
            IDataIndex index = exampleSet.getIndex();
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
