/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.ispr.dataset.InstanceGenerator;
import com.rapidminer.ispr.dataset.StoredValuesHelper;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.tools.InstanceModifier;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;

/**
 * Class implements Condenced NN instance selection algorithm.
 *
 * @author Marcin
 */
public class CNNInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure distance;
    private final IISDecisionFunction loss;
    private ISPRGeometricDataCollection<IStoredValues> model;
    private InstanceModifier modifier;

    /**
     * Constructor of Condensed NN instance selection algorithms
     *
     * @param distance - distance function
     * @param loss - loss function     
     */
    public CNNInstanceSelectionGeneralModel(DistanceMeasure distance, IISDecisionFunction loss) {
        this.distance = distance;
        this.loss = loss;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     * performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        loss.init(exampleSet, distance);
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);

        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();
        selectedIndex.setAllFalse();
        int i = 0;
        selectedIndex.set(i, true);
        trainingIndex.set(i, false);
        ISPRGeometricDataCollection<IStoredValues> nn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, selectedSet, distance);
        boolean isModiffied = true;
        int attributeSize = exampleSet.getAttributes().size();
        Instance instance = InstanceGenerator.generateInstance(trainingSet);

        while (isModiffied) {
            isModiffied = false;
            for (Example firstInstance : trainingSet) {
                instance.setValues(firstInstance);                
                Collection<IStoredValues> result = nn.getNearestValues(1, instance);
                double realLabel = firstInstance.getLabel();
                double predictedLabel = result.iterator().next().getLabel();
                if (loss.getValue(realLabel, predictedLabel, instance) > 0) {
                    i = ((ISPRExample) firstInstance).getIndex();
                    selectedIndex.set(i, true);
                    trainingIndex.set(i, false);
                    nn.add((Instance)instance.clone(), StoredValuesHelper.createStoredValue(firstInstance));
                    isModiffied = true;
                }
            }
        }
        model = nn;
        return selectedIndex;
    }

    @Override
    public ISPRGeometricDataCollection<IStoredValues> getModel() {
        return model;
    }       
}
