/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.selection.models.tools.ELH;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Vector;
import org.prules.dataset.VectorDense;

/**
 * Class implements ELH instance selection algorithm
 * @author Marcin
 */
public class ELHInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private DistanceMeasure measure;
    private ELH elh = null;
    private DataIndex selectedIndex = null;        

    /**
     * Constructor for ELH instance selection algorithm
     * @param measure     
     */
    public ELHInstanceSelectionModel(DistanceMeasure measure) {
        this.measure = measure;
    }

    
    
      /**
     * Performs instance selection
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {

        EditedExampleSet selectedSet;
        if (selectedIndex == null){
            selectedSet = new EditedExampleSet(exampleSet);
            selectedIndex = selectedSet.getIndex();
        } else {
            selectedSet = new EditedExampleSet(exampleSet,selectedIndex);
        }
        //Attributes attributes = exampleSet.getAttributes();
        //DataIndex index = exampleSet.getIndex();        

        //DATA STRUCTURE PREPARATION
        ISPRGeometricDataCollection<IInstanceLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,exampleSet, measure);
        int numberOfExamples = exampleSet.size();
        int m = selectedSet.size(); //Number of instances in training set
        int labels = exampleSet.getAttributes().getLabel().getMapping().size();
        elh = new ELH(m);
        int x = 0;
        double[] values = new double[exampleSet.getAttributes().size()];        
        for (Example e : exampleSet) {
            KNNTools.extractExampleValues(e, values);            
            int predictedLabel = (int)KNNTools.predictOneNearestNeighbor(InstanceFactory.createVector(values), samples);            
            int realLabel = (int) e.getLabel();
            x += predictedLabel != realLabel ? 1 : 0;
        }
        
        
        boolean doElh = m > 1;
        boolean doRepeat = true;

        while (doElh) {
            int oldM = m;

            for (int i : selectedIndex) {
                if (m == 1) {
                    break;
                }
                int x2 = 0;
                selectedIndex.set(i, false);
                samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,selectedSet, measure);
                for (Example e : exampleSet) {
                    KNNTools.extractExampleValues(e, values);            
                    int predictedLabel = (int)KNNTools.predictOneNearestNeighbor(InstanceFactory.createVector(values), samples);                                
                    int realLabel = (int) e.getLabel();
                    x2 += predictedLabel != realLabel ? 1 : 0;
                }
                if (elh.cost(m-1,numberOfExamples,x2,labels) <= elh.cost(m,numberOfExamples,x,labels)){
                    m = m - 1;
                    x = x2;
                    //index.set(i,false);
                } else {
                    selectedIndex.set(i, true);
                }
            }
            doElh = oldM > m && doRepeat;

        }
        return selectedIndex;        
    }
    
    public DataIndex getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(DataIndex selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
}
