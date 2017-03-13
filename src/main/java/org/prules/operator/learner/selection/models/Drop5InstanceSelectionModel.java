/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import org.prules.operator.learner.selection.models.tools.DropBasicModel;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.Const;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.NNGraphWithoutAssocuateUpdates;

/**
 * Class implements Drop5 instance selection algorithm for details see Wilson,
 * Martinez, Reduction Techniques for Instance-Based Learning Algorithms,
 * Machine Learning, 38, 257–286, 2000.
 *
 * @author Marcin
 */
public class Drop5InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     */
    public Drop5InstanceSelectionModel(DistanceMeasure measure, int k) {
        this.measure = measure;
        this.k = k;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     * performed
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
     *
     * @param samples
     * @return - index of selected examples
     */
    public IDataIndex selectInstances(ISPRClassGeometricDataCollection<IInstanceLabels> samples) {
        //Reorder samples according to distance to nearest enymy
        List<Integer> order;
        List<DoubleIntContainer> sampleOrderList = new ArrayList<>(samples.size());
        INNGraph nnGraph;
        nnGraph = new NNGraphWithoutAssocuateUpdates(samples, k);
        order = DropBasicModel.orderSamplesByEnemies(nnGraph);
        //Execute DropModel
        order = DropBasicModel.execute(nnGraph, order);
        //Prepare results
        int oldSize;
        do {
            oldSize = order.size();
            sampleOrderList.clear();
            for (int i : order) {
                List<DoubleIntContainer> enemies = nnGraph.getEnemies(i);
                double distance = Double.POSITIVE_INFINITY;
                if (!enemies.isEmpty()) {
                    distance = enemies.get(0).getFirst();
                }
                sampleOrderList.add(new DoubleIntContainer(-distance, i));
            }
            Collections.sort(sampleOrderList);
            order.clear();
            for (DoubleIntContainer i : sampleOrderList) {
                order.add(i.getSecond());
            }
            order = DropBasicModel.execute(nnGraph, order);
        } while (oldSize > order.size());
        boolean[] binIndex = new boolean[samples.size()];
        Arrays.fill(binIndex, false);
        for (int i : order) {
            binIndex[i] = true;
        }
        return new DataIndex(binIndex);
    }
}
