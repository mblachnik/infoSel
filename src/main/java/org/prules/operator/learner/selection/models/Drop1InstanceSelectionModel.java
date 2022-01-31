/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import org.prules.operator.learner.selection.models.tools.DropBasicModel;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

import java.util.ArrayList;
import java.util.List;

import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.NNGraph;

/**
 * Class implements Drop1 instance selection algorithm
 * for details see Wilson, Martinez, Reduction Techniques for Instance-Based
 * Learning Algorithms, Machine Learning, 38, 257–286, 2000.
 *
 * @author Marcin
 */
public class Drop1InstanceSelectionModel extends AbstractInstanceSelectorModel {

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
    public Drop1InstanceSelectionModel(DistanceMeasure measure, int k) {
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
        INNGraph nnGraph = new NNGraph(samples, k);
        //Create naturaln order
        List<Integer> order = new ArrayList<>(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            order.add(i);
        }
        order = DropBasicModel.execute(nnGraph, order);
        IDataIndex index = samples.getIndex();
        index.setAllFalse();
        for (int i : order) {
            index.set(i, true);
        }
        return index;
    }
}
