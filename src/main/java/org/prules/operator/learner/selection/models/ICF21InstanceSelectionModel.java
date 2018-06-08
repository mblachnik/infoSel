/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.List;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.decisionfunctions.ISClassDecisionFunction;
import org.prules.operator.learner.selection.models.tools.DropBasicModel;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PredictionProblemType;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.NNGraphReachableCoverage;

/**
 * Class implements modified ICF instance selection algorithm. 
 * It is very similar to ICF algorithm, but the main difference is that instated of 
 * single pruning step here instances are removed iteratively one by one always 
 * recalculating coverage and reachability. Here the computational complexity is high n^3
 * It is identical to ICF2 algorithm but samples are removed in the same order as in Drop3 algorithm
 * @author Marcin
 */

public class ICF21InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private boolean algorithmType = true;

    /**
     * Constructor for Drop1 instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     */
    public ICF21InstanceSelectionModel(DistanceMeasure measure, int k) {
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
        boolean nextIteration;
        List<Integer> order;
        //do {
            //Create naturaln order
            nextIteration = false;
            order = DropBasicModel.orderSamplesByEnemies(nnGraph,-1);
            for (int i : order) {
                int reachable = nnGraph.getNeighbors(i).size();
                int coverage = nnGraph.getAssociates(i).size();
                if (reachable > coverage) {                    
                    nnGraph.remove(i);
                    nextIteration = true;
                }
            }          
        //} while (nextIteration);
        indexENN.setIndex(nnGraph.getIndex());
        return indexENN;
    }
}
