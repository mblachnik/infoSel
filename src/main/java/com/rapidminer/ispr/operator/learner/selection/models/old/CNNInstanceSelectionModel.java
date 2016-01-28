/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;

/**
 *
 * @author Marcin
 */
public class CNNInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private DistanceMeasure distance;
    private IISDecisionFunction loss; 

    /**
     * 
     * @param distance
     * @param randomGenerator
     */
    public CNNInstanceSelectionModel(DistanceMeasure distance, IISDecisionFunction loss) {
        this.distance = distance;   
        this.loss = loss;
    }

    /**
     * 
     * @param inputExampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
                
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);


        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = exampleSet.getIndex();
        selectedIndex.setAllFalse();
        int i = 0;
        selectedIndex.set(i, true);        
        trainingIndex.set(i, false);
        ISPRGeometricDataCollection<Number> nn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,selectedSet, distance); 
        boolean isModiffied = true;
        int attributeSize = exampleSet.getAttributes().size();
        double[] firstValues = new double[attributeSize];
        
        while (isModiffied) {
            isModiffied = false;
            for (Example firstInstance : trainingSet) {
                KNNTools.extractExampleValues(firstInstance, firstValues);
                Collection<Number> result = nn.getNearestValues(1, firstValues);
                int realLabel = (int)firstInstance.getLabel();                
                int predictedLabel = result.iterator().next().intValue();                
                if (realLabel != predictedLabel) {
                    i = ((ISPRExample) firstInstance).getIndex();
                    selectedIndex.set(i, true);
                    trainingIndex.set(i, false);                    
                    nn.add(firstValues.clone(), realLabel);                    
                    isModiffied = true;
                }
            }
        }
        return selectedIndex;
    }
    
}
