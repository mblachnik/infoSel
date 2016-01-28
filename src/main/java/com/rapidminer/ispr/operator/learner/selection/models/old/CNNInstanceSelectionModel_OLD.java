/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class CNNInstanceSelectionModel_OLD implements PRulesModel {

    private DistanceMeasure distance;
    private RandomGenerator randomGenerator;

    /**
     * 
     * @param distance
     * @param randomGenerator
     */
    public CNNInstanceSelectionModel_OLD(DistanceMeasure distance, RandomGenerator randomGenerator) {
        this.distance = distance;
        this.randomGenerator = randomGenerator;
    }

    /**
     * 
     * @param inputExampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet;
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);


        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();
        selectedIndex.setAllFalse();
        int i = randomGenerator.nextInt(exampleSet.size());
        selectedIndex.set(i, true);
        //trainingIndex.set(i, false);
        boolean isModiffied = true;
        boolean isIncorrectlyClassified;
        int attributeSize = exampleSet.getAttributes().size();
        double[] firstValues = new double[attributeSize];
        while (isModiffied) {
            isModiffied = false;
            for (Example firstInstance : trainingSet) {
                KNNTools.extractExampleValues(firstInstance, firstValues);
                //isIncorrectlyClassified = !KNNTools.predictNearestNeighbor(selectedSet, firstValues, firstInstance.getLabel(), distance);
                isIncorrectlyClassified = KNNTools.predictOneNearestNeighbor(selectedSet, firstValues, distance) != firstInstance.getLabel() ? true : false;
                if (isIncorrectlyClassified) {
                    i = ((ISPRExample) firstInstance).getIndex();
                    selectedIndex.set(i, true);
                    trainingIndex.set(i, false);
                    isModiffied = true;
                }
                //isModiffied |= isIncorrectlyClassified;
            }
        }
        exampleSet.setIndex(selectedIndex);

        return exampleSet;
    }
    
}
