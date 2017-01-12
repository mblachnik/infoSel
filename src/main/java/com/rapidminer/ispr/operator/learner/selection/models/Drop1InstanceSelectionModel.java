/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.tools.Associates;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.knn.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.IntDoubleContainer;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;

/**
 *
 * @author Marcin
 */
public class Drop1InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private DistanceMeasure distance;
    private int k;
    private int classNum;

    /**
     *
     * @param distance
     * @param randomGenerator
     */
    public Drop1InstanceSelectionModel(DistanceMeasure distance) {
        this.distance = distance;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);
        classNum = trainingSet.getAttributes().getLabel().getMapping().size();

        ISPRGeometricDataCollection<IValuesStoreLabels> knn = null;// = KNNTools.initializeGeneralizedKNearestNeighbour(exampleSet, distance);
        Associates associates = KNNTools.findAssociatedInstances(exampleSet, knn, k);

        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();

        return selectedIndex;        
    }

    private int Improvement(double[] values, double label, DataIndex p, int[] as, boolean includePruned, ISPRGeometricDataCollection<IValuesStoreLabels> samples, int k) {
        /*  Returns the number of additional associates of 'p'
         that would be classified correctly (by the instances in the subset)
         if 'p' were removed.  If 'includePruned' is true, then we count
         both remaining instances and pruned instances in determining the
         change in accuracy.  If 'includePruned' is false, we count only
         non-pruned instances.
         Returns 0 if the same number would be classified correctly, 1 if
         one more would be, -1 if one more would be misclassified, etc.
         */
        //Neighbor i;
        int id = 0;
        int correctWith = 0, correctWithout = 0;
        if (p == null) {
            return 0;
        }
        /* For each associate 'i' of 'p', see if the classification of i
         by all k+1 nearest neighbors of 'i' except for 'p' is at least
         as good as the classification by the first k nearest neighbors
         of 'i'.  Note that if 'p' is i's k+1-th nearest neighbor, both
         classifications will be equal, which will add either 1 to both
         counters or 0 to both counters, resulting in a net difference of
         0.  This is good, because we really shouldn't be considering an
         associate of p that is only an associate because of the k+1-th nn. */
        
        int[] a = null;// = as.get(p.get(k));
        
        int j,i = 0;        
        double[] votes = new double[classNum];
        while((j = a[i])>=0 && i<a.length){            
            if (p.get(j) || includePruned){
                KNNTools.doNNVotes(votes, ValuesStoreFactory.createVector(values), samples, k, VotingType.MAJORITY);
                double newLabelA = PRulesUtil.findMostFrequentValue(votes);
                if(label == newLabelA){
                    correctWith++;
                }
                KNNTools.doNNVotes(votes, ValuesStoreFactory.createVector(values), samples, k, VotingType.MAJORITY);
                double newLabelB = PRulesUtil.findMostFrequentValue(votes);
                if(label == newLabelB){
                    correctWithout++;
                }                
            }
        }
        return correctWithout - correctWith;
    } /* Improvement */

}
