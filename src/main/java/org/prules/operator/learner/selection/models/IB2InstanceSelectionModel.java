/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.*;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.Collection;

/**
 * Implementation of IB2 algorithm
 *
 * @author Marcin
 */
public class IB2InstanceSelectionModel extends AbstractInstanceSelectorModel {

    private DistanceMeasure distance;
    private IISDecisionFunction loss;

    /**
     * Constructor for IB2 algorithm - algorithm is similar to CNN except it
     * preforms only single iteration
     *
     * @param distance - distance measure
     * @param loss     - decision function
     * @param modifier - allows for instance modification on the fly, for
     *                 example by random noise. Can be null.
     */
    public IB2InstanceSelectionModel(DistanceMeasure distance, IISDecisionFunction loss) {
        this.distance = distance;
        this.loss = loss;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     *                   performed
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
        ISPRGeometricDataCollection<IInstanceLabels> nn = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, selectedSet, distance);

        int attributeSize = exampleSet.getAttributes().size();
        Vector vector = InstanceFactory.createVector(exampleSet);
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels label = InstanceFactory.createInstanceLabels();
        for (Example currentInstance : trainingSet) {
            vector.setValues(currentInstance);
            Collection<IInstanceLabels> result = nn.getNearestValues(1, vector);
            label.set(currentInstance);
            double predictedLabel = result.iterator().next().getLabel();
            prediction.setLabel(predictedLabel);
            instance.put(Const.VECTOR, vector);
            instance.put(Const.LABELS, label);
            instance.put(Const.PREDICTION, prediction);
            if (loss.getValue(instance) > 0) {
                i = ((ISPRExample) currentInstance).getIndex();
                selectedIndex.set(i, true);
                trainingIndex.set(i, false);
                nn.add((Vector) vector.clone(), InstanceFactory.createInstaceLabels(currentInstance));
            }
        }
        return selectedIndex;
    }
}
