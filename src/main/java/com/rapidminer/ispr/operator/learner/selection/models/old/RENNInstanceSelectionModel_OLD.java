/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.ISClassDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.ENNInstanceSelectionModel;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class RENNInstanceSelectionModel_OLD implements PRulesModel<ExampleSet> {

    private int k;
    private DistanceMeasure measure;

    /**
     *
     * @param measure
     * @param k
     */
    public RENNInstanceSelectionModel_OLD(DistanceMeasure measure, int k) {
        this.k = k;
        this.measure = measure;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet exampleSet) {
        int initSize, tInitSize = exampleSet.size();
        IISDecisionFunction loss = new ISClassDecisionFunction();
        
        ENNInstanceSelectionModel m = new ENNInstanceSelectionModel(measure, k, loss);
        do {
            initSize = tInitSize;
            exampleSet = m.run(exampleSet);
        } while ((tInitSize = exampleSet.size()) != initSize);
        return exampleSet;
    }
}
