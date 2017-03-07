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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.dataset.Vector;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.NNGraph;

/**
 * Class implements ENN Vector selection algorithm
 *
 * @author Marcin
 */
public class Drop1InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int k;
    private final GeometricCollectionTypes knnType = GeometricCollectionTypes.LINEAR_SEARCH;        
    

    /**
     * Constructor for ENN instance selection model.
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors     
     */
    public Drop1InstanceSelectionModel(DistanceMeasure measure, int k) {
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
     * @param samples
     * @return - index of selected examples
     */    
    public IDataIndex selectInstances(ISPRClassGeometricDataCollection<IInstanceLabels> samples) { 
        /*
        IDataIndex index = samples.getIndex();
        IDataIndex indexOut = samples.getIndex();
        int size = samples.size();
        Collection<IInstanceLabels> res;
        for(int i = 0; i < size; i++){  
            System.out.println(i);
            int with = 0, without = 0;
            for (int j = 0; j < size; j++){
                if (j==i) continue;
                Vector v = samples.getSample(i);
                IInstanceLabels vl = samples.getStoredValue(i);
                index.set(i, false);                 
                res = samples.getNearestValues(k, v, index);
                double withoutLab = KNNTools.getMostFrequentValue(res);
                index.set(i, true);                
                res = samples.getNearestValues(k, v, index);
                double withLab = KNNTools.getMostFrequentValue(res);
                if (vl.getLabel() == withLab){
                    with++;
                }
                if (vl.getLabel() == withoutLab){
                    without++;
                }
            }
            if (without - with >= 0){
                index.set(i,false);
                indexOut.set(i, false);
            }
        }
          return indexOut; 
        */
        INNGraph nnGraph;
        nnGraph = new NNGraph(samples, k);
        //nnGraph.initialize();
        int sampleSize = samples.size();
        Vector vector;
        double realLabel;
        double predictedLabelWith = 0;
        double predictedLabelWithout = 0;
        IDataIndex index = samples.getIndex();
        int numberOfClasses = PRulesUtil.findUniqueLabels(samples).size();
        double[] classFreqCounterWith    = new double[numberOfClasses];        
        double[] classFreqCounterWithout    = new double[numberOfClasses];        
        IInstanceLabels labels;
        Iterator<IInstanceLabels> labelsIterator = samples.storedValueIterator();
        //Set<Integer> removedInstancesIDs = new HashSet<>(sampleSize);
        Collection<IInstanceLabels> res;
        while (labelsIterator.hasNext()) { //Iterate over samples
            int with = 0;
            int without = 0;
            labels = labelsIterator.next(); //read labels of the sample
            int queryInstanceID = (int) labels.getValueAsLong(Const.INDEX_CONTAINER);            
            int queryInstanceLabel = (int) labels.getLabel();            
            for (int associate : nnGraph.getAssociates(queryInstanceID)) { //Take associates of sample queryInstanceID
                //if (removedInstancesIDs.contains(associate)) continue; //If an instancse was already removed in Drop1 it is no longer considered in classification
                realLabel = samples.getStoredValue(associate).getLabel();
                Arrays.fill(classFreqCounterWith, 0);
                Arrays.fill(classFreqCounterWithout, 0);                
                int kTmp = 0;
                for (DoubleIntContainer neighbor : nnGraph.getNeighbors(associate)) { //go neighbors of associates, note that we have one more neighbor                    
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
                index.set(queryInstanceID, false);                
                nnGraph.remove(queryInstanceID);                                           
                //removedInstancesIDs.add(queryInstanceID);
            }            
        }
        
        return index;

    }
}
