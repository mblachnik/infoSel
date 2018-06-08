/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.generalized;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Const;
import org.prules.operator.learner.PRulesModel;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.IntDoubleContainer;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.dataset.Vector;

/**
 *
 * @author Marcin
 */
public class GENNInstanceSelectionModel implements PRulesModel<ExampleSet> {

    private final DistanceMeasure measure;
    private final int k;
    private final double maxError;
    private final boolean relativeError;
    private Exception exception = null;
    private final boolean trainOnSubset;
    private final AbstractInstanceSelectorChain evaluator;

    public GENNInstanceSelectionModel(DistanceMeasure measure, int k, double maxError, boolean relativeError, boolean trainOnSubset, AbstractInstanceSelectorChain evaluator) {
        this.measure = measure;
        this.k = k;
        this.maxError = maxError;
        this.relativeError = relativeError;
        this.evaluator = evaluator;
        this.trainOnSubset = trainOnSubset;
    }

    @Override
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet;
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        ISPRGeometricDataCollection<IInstanceLabels> nearestNeighbors = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, inputExampleSet, measure);
        IDataIndex index = exampleSet.getIndex();
        index.setAllTrue();

        EditedExampleSet testSet = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet = new EditedExampleSet(exampleSet);

        DataIndex testIndex = testSet.getIndex();
        DataIndex trainingIndex = trainingSet.getIndex();

        testIndex.setAllFalse();

        int instanceIndex;
        double realLabel;
        double mean = 0;
        double variance = 0;
        double lowerBound = maxError;
        Attributes attributes = exampleSet.getAttributes();
        boolean numericalLabel = attributes.getLabel().isNumerical();
        boolean nominalLabel = attributes.getLabel().isNominal();

        Vector values = InstanceFactory.createVector(inputExampleSet);

        ExampleSet resultExample;
        for (Example example : exampleSet) {
            double predictedLabel;
            ISPRExample te = (ISPRExample) example;
            instanceIndex = te.getIndex();
            values.setValues(example);            
            Collection<IInstanceLabels> nearest = nearestNeighbors.getNearestValues(k, values);
            if (numericalLabel) {
                for (IInstanceLabels a : nearest) {
                    mean += a.getLabel();
                }
                mean = (nearest.isEmpty()) ? Double.NaN : mean / nearest.size();
                for (IInstanceLabels a : nearest) {
                    variance += (a.getLabel()- mean) * (a.getLabel()- mean);
                }
                variance /= nearest.size();
            }
            if (trainOnSubset){
                trainingIndex.setAllFalse();
                for (IInstanceLabels a : nearest) {
                    trainingIndex.set((int)a.getValueAsLong(Const.INDEX_CONTAINER),true);
                }
            } 
            try {
                testIndex.set(instanceIndex, true);
                trainingIndex.set(instanceIndex, false);
                resultExample = evaluator.executeInerModel(trainingSet, testSet);
            } catch (Exception e) {
                exception = e;
                return null;
            }

            predictedLabel = resultExample.getExample(0).getPredictedLabel();
            realLabel = example.getLabel();
            testIndex.set(instanceIndex, false);            
            trainingIndex.set(instanceIndex, true);
            if (numericalLabel) {
                double dif = Math.abs(realLabel - predictedLabel);

                if (relativeError) {
                    lowerBound = maxError * Math.sqrt(variance);
                }

                if (dif > lowerBound) {
                    index.set(instanceIndex, false);
                }
            } else if (nominalLabel) {
                if (predictedLabel != realLabel) {
                    index.set(instanceIndex, false);
                }
            }
        }
        exampleSet.setIndex(index);
        return exampleSet;
    }
    
     /**
     * 
     * @return
     */
    public boolean isException() {
        return exception != null;
    }

    /**
     * 
     * @return
     */
    public Exception getException() {
        return exception;
    }
}
