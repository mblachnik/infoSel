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
import org.prules.operator.learner.tools.PredictionProblemType;
import org.prules.tools.math.container.knn.*;

import java.util.*;

/**
 * Class implements Drop4 instance selection algorithm
 * for details see Wilson, Martinez, Reduction Techniques for Instance-Based
 * Learning Algorithms, Machine Learning, 38, 257–286, 2000.
 *
 * @author Marcin
 */
public class Drop4InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k       - number of nearest neighbors
     */
    public Drop4InstanceSelectionModel(DistanceMeasure measure, int k) {
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
        List<Integer> order;
        Set<Double> labels = new HashSet<>(5);
        Iterator<IInstanceLabels> labelIterator = samples.storedValueIterator();
        while (labelIterator.hasNext()) {
            double label = labelIterator.next().getLabel();
            labels.add(label);
        }
        int numberOfClassews = labels.size();
        //RUN ENN
        ENNInstanceSelectionModel ennModel = new ENNInstanceSelectionModel(measure, k, new ISClassDecisionFunction(), false);
        IDataIndex index = ennModel.selectInstances(samples, PredictionProblemType.CLASSIFICATION);
        //Check  samples affect classification and analyze instances which should be removed if they affect classification accuracy        
        nnGraph = new NNGraphWithoutAssociateUpdates(samples, k);
        IDataIndex indexNeg = new DataIndex(index);
        indexNeg.negate();

        for (int i : indexNeg) {
            double improve = DropBasicModel.improvement(nnGraph, numberOfClassews, i);
            if (improve < 0) { //If without instance "i" system classifies worse then with it keep instance i
                index.set(i, true);
            }
        }
        //Prune samples
        ISPRClassGeometricDataCollection<IInstanceLabels> samplesSelected;
        samplesSelected = KNNTools.takeSelected(samples, index); //Here we remove useless samples      
        nnGraph = new NNGraphWithoutAssociateUpdates(samplesSelected, k);
        //Reorder samples according to distance to nearest enemy
        order = DropBasicModel.orderSamplesByEnemies(nnGraph, -1); //Order from the furthest to the nearest
        //Execute DropModel
        order = DropBasicModel.execute(nnGraph, order);
        //Prepare results
        boolean[] binIndex = new boolean[samplesSelected.size()];
        Arrays.fill(binIndex, false);
        for (int i : order) {
            binIndex[i] = true;
        }
        index.setIndex(new DataIndex(binIndex));
        return index;
    }
}
