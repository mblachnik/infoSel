/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.operator.learner.tools.PredictionProblemType;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.Map;

/**
 * Class implements repeated ENN algorithm (RENN). It repeats ENN algorithm
 * until any instance can be marked for removal
 *
 * @author Marcin
 */
public class RENNInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final int k;
    private final DistanceMeasure measure;
    private final IISDecisionFunction loss;
    private final int maxIterations;

    /**
     * Constructor of repeated ENN algorithm (RENN). It repeats ENN algorithm
     * until any instance can be marked for removal
     *
     * @param measure       - distance measure
     * @param k             - number of nearest neighbors
     * @param loss          - loss function
     * @param maxIterations - maximum number of iterations. This algorithm in
     *                      worst case can have n^3 complexity, by setting max iteration you can
     *                      break the process earlier reaching n^2*mazIterations complexity. Number
     *                      of loops depends on data distribution
     */
    public RENNInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss, int maxIterations) {
        this.k = k;
        this.measure = measure;
        this.loss = loss;
        this.maxIterations = maxIterations;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     *                   performed
     * @return - index of selected examples
     */
    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples = (ISPRGeometricDataCollectionWithIndex<IInstanceLabels>) KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        loss.init(samples);
        //ENN EDITING
        IDataIndex mainIndex = exampleSet.getIndex();
        int maxIterations = this.maxIterations < 0 ? Integer.MAX_VALUE : this.maxIterations;
        int loopCounter = 0;
        boolean breakLoop;
        ENNInstanceSelectionModel enn = new ENNInstanceSelectionModel(measure, k, loss, false);
        loss.init(samples);
        Map<Double, Integer> counter = PRulesUtil.countClassFrequency(samples);
        breakLoop = false;
        do {
            //Execute ENN
            IDataIndex index = enn.selectInstances(samples, PredictionProblemType.CLASSIFICATION, false);
            if (index.getLength() != index.size()) { //If any instance was deleted                                                
                //Remove the instances marked for removal
                for (int i = index.size() - 1; i > -1; i--) { //We have to iterate backwards because otherwise indexes wouldn't much
                    if (!index.get(i)) {
                        //First we check how many instances from given class we have
                        IInstanceLabels labels = samples.getStoredValue(i);
                        double label = labels.getLabel();
                        int count = counter.get(label);
                        //If only one instance per class is left and it should be deleted (the last one), than we ignore all the changes and break the loop
                        //This may happen in in balanced problems
                        if (count == 1) {
                            breakLoop = true;
                            break;
                        } else {
                            count--;
                            counter.put(label, count);
                        }
                        samples.remove(i);
                    }
                }
                if (!breakLoop) {
                    //Apply last iteration only if after removal at least one instance per class will be present
                    mainIndex.setIndex(index);
                }
            } else {
                breakLoop = true;
            }
            loopCounter++;
        } while (!(breakLoop || loopCounter >= maxIterations));
        return mainIndex;
    }
}
