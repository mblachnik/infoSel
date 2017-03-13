/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.IntIntContainer;
import org.prules.tools.math.container.knn.INNGraph;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;

/**
 *
 * @author Marcin
 */
public class DropBasicModel {

    public static List execute(INNGraph nnGraph, List<Integer> order) {
        ISPRClassGeometricDataCollection<IInstanceLabels> samples = nnGraph.getSamples();
        //IDataIndex index = samples.getIndex();
        int numberOfClasses = PRulesUtil.findUniqueLabels(samples).size();
        int with;
        int without;
        List<Integer> selected = new ArrayList(order.size());
        
        //Iterator<IInstanceLabels> labelsIterator = samples.storedValueIterator();
        //Set<Integer> removedInstancesIDs = new HashSet<>(sampleSize);
        Collection<IInstanceLabels> res;
        for (int i : order) {
            //while (labelsIterator.hasNext()) { //Iterate over samples
            IntIntContainer withWithout = improvement(nnGraph,numberOfClasses,i);
            with = withWithout.getFirst();
            without = withWithout.getSecond();
            //If the drop1 rule is fullfield
            if (without - with >= 0) {                
                    //index.set(queryInstanceID, false);                
                    nnGraph.remove(i);
                    //removedInstancesIDs.add(queryInstanceID);                
            } else {
                selected.add(i);
            }
        }
        return selected;
    }

    public static IntIntContainer improvement(INNGraph nnGraph, int numberOfClasses, int i) {
        int with = 0;
        int without = 0;
        double realLabel;
        double predictedLabelWith = 0;
        double predictedLabelWithout = 0;
        double[] classFreqCounterWith = new double[numberOfClasses];
        double[] classFreqCounterWithout = new double[numberOfClasses];
        int k = nnGraph.getK();
        ISPRClassGeometricDataCollection<IInstanceLabels> samples;
        samples = nnGraph.getSamples();
        IInstanceLabels labels;
        labels = samples.getStoredValue(i); //read labels of the sample
        int queryInstanceID = (int) labels.getValueAsLong(Const.INDEX_CONTAINER);        
        for (DoubleIntContainer associatePair : nnGraph.getAssociates(queryInstanceID)) { //Take associates of sample queryInstanceID
            int associate = associatePair.getSecond();
            realLabel = samples.getStoredValue(associate).getLabel();
            Arrays.fill(classFreqCounterWith, 0);
            Arrays.fill(classFreqCounterWithout, 0);
            int kTmp = 0;
            for (DoubleIntContainer neighbor : nnGraph.getNeighbors(associate)) { //go neighbors of associates, note that we have one more neighbor                    
                labels = samples.getStoredValue(neighbor.getSecond()); //get labels of samples associated to i
                int labelDeterminedByNeighbor = (int) labels.getLabel();
                if (kTmp < k) {
                    classFreqCounterWith[labelDeterminedByNeighbor]++;
                }
                if (neighbor.getSecond() != queryInstanceID) {
                    classFreqCounterWithout[labelDeterminedByNeighbor]++;
                }
                kTmp++;
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
        return new IntIntContainer(with,without);
    }
    
    public static List<Integer> orderSamplesByEnemies(INNGraph nnGraph){
        return orderSamplesByEnemies(nnGraph, 1);
    }
    
    public static List<Integer> orderSamplesByEnemies(INNGraph nnGraph, int direction){
        List<DoubleIntContainer> sampleOrderList = new ArrayList<>(nnGraph.size());
        IDataIndex indexAfterRemoval = nnGraph.getIndex();
        for (int i : indexAfterRemoval) {
            List<DoubleIntContainer> enemies = nnGraph.getEnemies(i);
            double distance = Double.POSITIVE_INFINITY;
            if (!enemies.isEmpty()) {
                distance = direction * enemies.get(0).getFirst();
            }
            sampleOrderList.add(new DoubleIntContainer(-distance, i));
        }
        Collections.sort(sampleOrderList);
        //Prepare samples order
        List<Integer> order = new ArrayList<>(indexAfterRemoval.getLength());
        for (DoubleIntContainer i : sampleOrderList) {
            order.add(i.getSecond());
        }
        return order;
    }
    
//    public static void orderSamples(INNGraph nnGraph, IDataIndex index, List<Integer> order){
//        List<DoubleIntContainer> sampleOrderList = new ArrayList<>(index.size());
//        IDataIndex indexAfterRemoval = index.getIndex();
//        for (int i : indexAfterRemoval) {
//            List<DoubleIntContainer> enemies = nnGraph.getEnemies(i);
//            double distance = Double.POSITIVE_INFINITY;
//            if (!enemies.isEmpty()) {
//                distance = enemies.get(0).getFirst();
//            }
//            sampleOrderList.add(new DoubleIntContainer(-distance, i));
//        }
//        Collections.sort(sampleOrderList);
//        //Prepare samples order
//        order = new ArrayList<>(index.size());
//        for (DoubleIntContainer i : sampleOrderList) {
//            order.add(i.getSecond());
//        }
//    }
}
