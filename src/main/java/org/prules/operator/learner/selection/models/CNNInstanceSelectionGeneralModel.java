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
 * Class implements Condensed NN instance selection algorithm.
 *
 * @author Marcin
 */
public class CNNInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure distance;
    private final IISDecisionFunction loss;
    private ISPRGeometricDataCollection<IInstanceLabels> model;

    /**
     * Constructor of Condensed NN instance selection algorithms
     *
     * @param distance - distance function
     * @param loss     - loss function
     */
    public CNNInstanceSelectionGeneralModel(DistanceMeasure distance, IISDecisionFunction loss) {
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
        loss.init(exampleSet, distance);
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);

        DataIndex trainingIndex = trainingSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();
        selectedIndex.setAllFalse();
        int i = 0;
        selectedIndex.set(i, true);
        trainingIndex.set(i, false);
        ISPRGeometricDataCollection<IInstanceLabels> nn = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, selectedSet, distance);
        boolean isModified = true;
        int attributeSize = exampleSet.getAttributes().size();
        Vector vector = InstanceFactory.createVector(trainingSet);
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels label = InstanceFactory.createInstanceLabels();

        while (isModified) {
            isModified = false;
            for (Example firstInstance : trainingSet) {
                vector.setValues(firstInstance);
                Collection<IInstanceLabels> result = nn.getNearestValues(1, vector);
                double predictedLabel = result.iterator().next().getLabel();
                label.set(firstInstance);
                prediction.setLabel(predictedLabel);
                instance.put(Const.VECTOR, vector);
                instance.put(Const.LABELS, label);
                instance.put(Const.PREDICTION, prediction);
                if (loss.getValue(instance) > 0) {
                    i = ((ISPRExample) firstInstance).getIndex();
                    selectedIndex.set(i, true);
                    trainingIndex.set(i, false);
                    nn.add((Vector) vector.clone(), InstanceFactory.createInstaceLabels(firstInstance));
                    isModified = true;
                }
            }
        }
        model = nn;
        return selectedIndex;
    }

    @Override
    public ISPRGeometricDataCollection<IInstanceLabels> getModel() {
        return model;
    }
}
