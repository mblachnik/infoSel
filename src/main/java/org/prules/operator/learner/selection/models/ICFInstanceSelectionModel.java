/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Map;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.decisionfunctions.ISClassDecisionFunction;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.operator.learner.tools.PredictionProblemType;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.NNGraphReachableCoverage;

/**
 * Class implements Drop1 instance selection algorithm for details see Wilson,
 * Martinez, Reduction Techniques for Instance-Based Learning Algorithms,
 * Machine Learning, 38, 257–286, 2000.
 *
 * @author Marcin
 */
public class ICFInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;
    private final int maxIterations;

    /**
     * Constructor for ICF instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     */
    public ICFInstanceSelectionModel(DistanceMeasure measure, int k) {
        this(measure, k, Integer.MAX_VALUE);
    }

    /**
     * Constructor for ICF instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param maxIterations - maximum number of iterations of the main loop. THe
     * number of iterations depends on the data distribution and in extreme
     * cases it can remove single instance in single iteration that may be very
     * time
     */
    public ICFInstanceSelectionModel(DistanceMeasure measure, int k, int maxIterations) {
        this.measure = measure;
        this.k = k;
        this.maxIterations = maxIterations;
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
        IDataIndex index = nnGraph.getIndex();
        boolean nextIteration;
        int iterations = 0;
        Map<Double, Integer> classCountMap = PRulesUtil.countClassFrequency(samplesSelected);
        do {
            //Create naturaln order
            nextIteration = false;
            for (int i : index) {
                int reachable = nnGraph.getNeighbors(i).size();
                int coverage = nnGraph.getAssociates(i).size();
                if (reachable > coverage) {
                    IInstanceLabels tmpILabel = samplesSelected.getStoredValue(i);
                    double tmpLabel = tmpILabel.getLabel();
                    int classCount = classCountMap.get(tmpLabel);
                    if ( classCount > 1) {
                        index.set(i, false);
                        nextIteration = true;
                        classCount --;
                        classCountMap.put(tmpLabel,classCount);
                    }
                }
            }
            if (nextIteration) {
                nnGraph.calculateGraph();
            }
            iterations++;
        } while (nextIteration && iterations < maxIterations);
        indexENN.setIndex(index);
        return indexENN;
    }
}
