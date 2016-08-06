/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */ 
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.ispr.dataset.InstanceGenerator;
import com.rapidminer.ispr.dataset.StoredValuesHelper;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.tools.EmptyInstanceModifier;
import com.rapidminer.ispr.operator.learner.selection.models.tools.InstanceModifier;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;

/**
 * Implementation of IB2 algorithm
 * @author Marcin
 */
public class IB2InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private DistanceMeasure distance;
    private IISDecisionFunction loss;
    private final InstanceModifier modifier;

    /**
     * Constructor for IB2 algorithm - algorithm is similar to CNN except it preforms only single iteration
     * @param distance     - distance measure
     * @param loss - decision function
     * @param modifier - allows for instance modification on the fly, for example by random noise. Can be null.
     */
    public IB2InstanceSelectionModel(DistanceMeasure distance, IISDecisionFunction loss, InstanceModifier modifier) {
        this.distance = distance;
        this.loss = loss;
        if (modifier==null){
            this.modifier = new EmptyInstanceModifier();
        } else {
            this.modifier = modifier;
        }
    }

    /**
     * Performs instance selection
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);
        loss.init(exampleSet, distance);

        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();
        selectedIndex.setAllFalse();
        int i = 0;
        selectedIndex.set(i, true);
        trainingIndex.set(i, false);
        ISPRGeometricDataCollection<IStoredValues> nn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,selectedSet, distance);

        int attributeSize = exampleSet.getAttributes().size();
        Instance values = InstanceGenerator.generateInstance(exampleSet);
        for (Example currentInstance : trainingSet) {
            values.setValues(currentInstance);
            Collection<IStoredValues> result = nn.getNearestValues(1, modifier.modify(values));
            double realLabel = currentInstance.getLabel();
            double predictedLabel = result.iterator().next().getLabel();
            if (loss.getValue(realLabel, predictedLabel, values) > 0) {                
                i = ((ISPRExample) currentInstance).getIndex();
                selectedIndex.set(i, true);
                trainingIndex.set(i, false);                
                nn.add((Instance)values.clone(), StoredValuesHelper.createStoredValue(currentInstance));
            }
        }
        return selectedIndex;        
    }
}
