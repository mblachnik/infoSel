/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.decisionfunctions.ISClassDecisionFunction;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PredictionProblemType;
import org.prules.tools.math.container.knn.*;

/**
 * Class implements Drop1 instance selection algorithm for details see Wilson,
 * Martinez, Reduction Techniques for Instance-Based Learning Algorithms,
 * Machine Learning, 38, 257–286, 2000.
 *
 * @author Marcin
 */
public class ICF2InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private boolean algorithmType = true;

    /**
     * Constructor for Drop1 instance selection model.
     *
     * @param measure - distance measure
     * @param k       - number of nearest neighbors
     */
    public ICF2InstanceSelectionModel(DistanceMeasure measure, int k) {
        this.measure = measure;
        this.k = k;
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
        Attributes attributes = exampleSet.getAttributes();
        IDataIndex index = exampleSet.getIndex();
        //DATA STRUCTURE PREPARATION        
        ISPRClassGeometricDataCollection<IInstanceLabels> samples;
        samples = (ISPRClassGeometricDataCollection<IInstanceLabels>) KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);
        return selectInstances(samples);
    }

    /**
     * Performs instance selection
     * <p>
     * This method implements to true algorithm, while the one with ExampleSet
     * as input calls that one to perform instance selection
     *
     * @param samples
     * @return - index of selected examples
     */
    public IDataIndex selectInstances(ISPRClassGeometricDataCollection<IInstanceLabels> samples) {
        ENNInstanceSelectionModel ennModel = new ENNInstanceSelectionModel(measure, k, new ISClassDecisionFunction(), false);
        IDataIndex indexENN = ennModel.selectInstances(samples, PredictionProblemType.CLASSIFICATION);
        ISPRClassGeometricDataCollection<IInstanceLabels> samplesSelected;
        samplesSelected = KNNTools.takeSelected(samples, indexENN); //Here we remove useless samples      

        INNGraph nnGraph = new NNGraphReachableCoverage(samplesSelected);
        IDataIndex index = nnGraph.getIndex();
        IDataIndex myIndex = new DataIndex(index);
        boolean nextIteration;
        //do {
        //Create natural order
        nextIteration = false;

        for (int i : index) {
            int reachable = nnGraph.getNeighbors(i).size();
            int coverage = nnGraph.getAssociates(i).size();
            if (reachable > coverage) {
                myIndex.set(i, false);
                nnGraph.remove(i);
                nextIteration = true;
            }
        }
        //} while (nextIteration);
        indexENN.setIndex(index);
        return indexENN;
    }
}
