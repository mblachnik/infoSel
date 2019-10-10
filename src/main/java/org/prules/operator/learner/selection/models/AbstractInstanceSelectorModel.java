/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.PRulesModel;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;

/**
 * Abstract class which simplifies implementation of Instance selection algorithms
 *
 * @author Marcin
 */
public abstract class AbstractInstanceSelectorModel implements PRulesModel<ExampleSet> {

    /**
     * Method inherited from PRulesModel. Initialize model execution - wraps
     * ExampleSet by SelectedExampleSet class which allows for instance
     * selection and executes selectInstances method which is directly implements
     * instance selection algorithm
     *
     * @param inputExampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet.clone();
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        IDataIndex index = selectInstances(exampleSet);
        //UWAGA tak trzeba zrobić bo może się zdarzyć że któryś z algorytmów selekcji sam wybierze wektory i wówczas nie będzie można zrobić setIndex bo nie będą się zgadzały indeksy
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet.clone();
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        exampleSet.setIndex(index);
        return exampleSet;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    public abstract IDataIndex selectInstances(SelectedExampleSet exampleSet);

    /**
     * This method can be override to return {@link ISPRGeometricDataCollection} model. If this kind of model is
     * used or build inside instance selection algorithm. Because often such model is
     * used and already build so it is not necesary to rebuild it again. This may reduce memory
     * and computational footprint.
     *
     * @return
     */
    public ISPRGeometricDataCollection<IInstanceLabels> getModel() {
        return null;
    }
}
