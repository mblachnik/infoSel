/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * Class implementing Gabriel Editing algorithm
 * @author Marcin
 */
public class GEInstanceSelectionModel extends AbstractInstanceSelectorModel implements EditedDistanceGraphCriteria {

    private AbstractInstanceSelectorModel model;

    /**
     * Constructor for Gabriel Editing algorithm
     * @param distance - distance function
     * @param IISDecisionFunction - loss function
     */
    public GEInstanceSelectionModel(DistanceMeasure distance, IISDecisionFunction loss) {
        this.model = new EditedDistanceGraphModel(distance,this, loss);
    }

    /**
     * Performs instance selection
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet inputExampleSet) {
        return model.selectInstances(inputExampleSet);        
    }

    /**
     * Evaluation function to check inequality 
     * @param a distance between samples 1 and 3
     * @param b distance between samples 1 and 2
     * @param c distance between samples 2 and 3
     * @return 
     */
    @Override
    public boolean evaluate(double a, double b, double c) {        
        a *= a;
        b *= b;
        c *= c;
        return a > b + c;
    }
  
}
