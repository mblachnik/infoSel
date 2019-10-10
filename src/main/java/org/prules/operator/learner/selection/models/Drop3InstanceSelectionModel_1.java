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
import org.prules.operator.learner.selection.models.tools.DropBasicModel;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.knn.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class implements Drop3 instance selection algorithm
 * for details see Wilson, Martinez, Reduction Techniques for Instance-Based
 * Learning Algorithms, Machine Learning, 38, 257–286, 2000.
 * A small difference to the other implementation. Here we call ENN directly, and in the other one ENN is called with "samples" as input
 *
 * @author Marcin
 */
public class Drop3InstanceSelectionModel_1 extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k       - number of nearest neighbors
     */
    public Drop3InstanceSelectionModel_1(DistanceMeasure measure, int k) {
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
        //DATA STRUCTURE PREPARATION        
        ENNInstanceSelectionModel model = new ENNInstanceSelectionModel(measure, k, new ISClassDecisionFunction(), false);
        IDataIndex index = model.selectInstances(exampleSet);
        exampleSet.setIndex(index);
        //
        ISPRClassGeometricDataCollection<IInstanceLabels> samples;
        samples = (ISPRClassGeometricDataCollection<IInstanceLabels>) KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);
        index.setIndex(selectInstances(samples));
        return index;
    }

    /**
     * Performs instance selection Note that access is restricted to package,
     * this is becouse IDataIndex selectInstances(SelectedExampleSet exampleSet)
     * must be called This method includes execution of ENN which prunes noisy
     * instances
     *
     * @param samples
     * @return - index of selected examples
     */
    public IDataIndex selectInstances(ISPRClassGeometricDataCollection<IInstanceLabels> samples) {
        INNGraph nnGraph;
        nnGraph = new NNGraphWithoutAssociateUpdates(samples, k);

        //Reorder samples according to distance to nearest enymy
        IDataIndex tmpIndex = samples.getIndex();
        List<DoubleIntContainer> sampleOrderList = new ArrayList<>(samples.size());
        List<Integer> order = new ArrayList<>(samples.size());
        IDataIndex index = samples.getIndex();
        for (int i : index) {
            List<DoubleIntContainer> enemies = nnGraph.getEnemies(i);
            double distance = Double.POSITIVE_INFINITY;
            if (!enemies.isEmpty()) {
                distance = enemies.get(0).getFirst();
            }
            sampleOrderList.add(new DoubleIntContainer(-distance, i));
        }
        Collections.sort(sampleOrderList);
        for (DoubleIntContainer i : sampleOrderList) {
            order.add(i.getSecond());
        }
//        for (int i : index) {
//            order.add(i);
//        }
        //Execute DropModel       

        order = DropBasicModel.execute(nnGraph, order);
        //Prepare results
        boolean[] binIndex = new boolean[samples.size()];
        Arrays.fill(binIndex, false);
        for (int i : order) {
            binIndex[i] = true;
        }
        return new DataIndex(binIndex);
    }
}
