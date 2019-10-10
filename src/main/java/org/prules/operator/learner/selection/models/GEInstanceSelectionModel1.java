/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.IDataIndex;

/**
 * Class implementing Gabriel Editing algorithm
 *
 * @author Marcin
 */
public class GEInstanceSelectionModel1 extends AbstractInstanceSelectorModel implements EditedDistanceGraphCriteria {

    private AbstractInstanceSelectorModel model;

    /**
     * Constructor for Gabriel Editing algorithm
     *
     * @param distance - distance function
     * @param loss     - loss function
     * @param modifier - how the input instance is modified
     */
    public GEInstanceSelectionModel1(DistanceMeasure distance, IISDecisionFunction loss) {
        this.model = new EditedDistanceGraphModel(distance, this, loss);
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public IDataIndex selectInstances(SelectedExampleSet inputExampleSet) {
        return model.selectInstances(inputExampleSet);
    }

    /**
     * Evaluation function to check inequality
     *
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
