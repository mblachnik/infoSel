/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.Const;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.selection.models.decisionfunctions.ISClassDecisionFunction;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.NNGraphWithoutAssocuateUpdates;

/**
 * Class implements ENN Vector selection algorithm
 *
 * @author Marcin
 */
public class Drop3InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;        
    

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors     
     */
    public Drop3InstanceSelectionModel(DistanceMeasure measure, int k) {
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
        ENNInstanceSelectionModel model = new ENNInstanceSelectionModel(measure, k, new ISClassDecisionFunction(), false);
        IDataIndex indexENN = model.selectInstances(exampleSet);
        index.setIndex(indexENN);
        //
        ISPRClassGeometricDataCollection<IInstanceLabels> samples;
        samples = (ISPRClassGeometricDataCollection<IInstanceLabels>) KNNFactory.initializeKNearestNeighbourFactory(knnType, exampleSet, measure);                   
        return selectInstances(samples);
    }
    
    /**
     * Performs instance selection
     * Note that access is restricted to package, this is becouse IDataIndex selectInstances(SelectedExampleSet exampleSet)  must be called
     * This method includes execution of ENN which prunes noisy instances
     * @param samples
     * @return - index of selected examples
     */    
    IDataIndex selectInstances(ISPRClassGeometricDataCollection<IInstanceLabels> samples) {         
        IDataIndex tmpIndex = samples.getIndex();
        List<DoubleIntContainer> sampleOrderList = new ArrayList<>(samples.size());
        INNGraph nnGraph;
        nnGraph = new NNGraphWithoutAssocuateUpdates(samples, k);
        for(int i : tmpIndex){
            List<DoubleIntContainer> enemies = nnGraph.getEnemies(i);
            double distance = Double.POSITIVE_INFINITY;
            if (!enemies.isEmpty()){
                distance = enemies.get(0).getFirst();
            }
            sampleOrderList.add(new DoubleIntContainer(distance, i));
        }
        Collections.sort(sampleOrderList);                
        double realLabel;
        double predictedLabelWith = 0;
        double predictedLabelWithout = 0;       
        
        IDataIndex indexOut = samples.getIndex();         
        int numberOfClasses = PRulesUtil.findUniqueLabels(samples).size();
        double[] classFreqCounterWith = new double[numberOfClasses];
        double[] classFreqCounterWithout = new double[numberOfClasses];
        IInstanceLabels labels;
        
        for (int idx = sampleOrderList.size()-1; idx > -1; idx--) { //Iterate over samples
            DoubleIntContainer element = sampleOrderList.get(idx);
            labels = samples.getStoredValue(element.getSecond());
            //int idx = 0;
            int with = 0;
            int without = 0;            
            int queryInstanceID = (int) labels.getValueAsLong(Const.INDEX_CONTAINER);            
            int queryInstanceLabel = (int) labels.getLabel();            
            for (int associate : nnGraph.getAssociates(queryInstanceID)) { //Take associates of sample i               
                List<DoubleIntContainer> neighbors = nnGraph.getNeighbors(associate);
                if (neighbors.isEmpty()) continue;
                realLabel = samples.getStoredValue(associate).getLabel();
                Arrays.fill(classFreqCounterWith, 0);
                 Arrays.fill(classFreqCounterWithout, 0);  
                int kTmp = 1;                
                for (DoubleIntContainer neighbor : neighbors) { //go neighbors of associates, note that we have one more neighbor                    
                    labels = samples.getStoredValue(neighbor.getSecond()); //get labels of samples associated to i
                    int labelDeterminedByNeighbor = (int) labels.getLabel();
                    if (kTmp < k){
                        classFreqCounterWith[labelDeterminedByNeighbor]++;
                    }
                    if (neighbor.getSecond() != queryInstanceID) {                        
                        classFreqCounterWithout[labelDeterminedByNeighbor]++;                        
                    }
                    kTmp ++;
                }                
                predictedLabelWithout = PRulesUtil.findMostFrequentValue(classFreqCounterWithout);                             
                predictedLabelWith = PRulesUtil.findMostFrequentValue(classFreqCounterWith);    
                if (predictedLabelWith == realLabel) {
                    with++;
                }
                if (predictedLabelWithout == realLabel) {
                    without++;
                }
            }
            //If the drop1 rule is fullfield
            if (without - with >= 0) {
                indexOut.set(queryInstanceID, false);                
                nnGraph.remove(queryInstanceID);                                           
            }            
        }
        //IMPORTANT the output index is corted according to output data structure order!!!
        //it is important as we reorder the elements in samples
        return indexOut;
    }
}
