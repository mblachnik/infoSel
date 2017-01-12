/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.generalized;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.ValuesStoreFactory;
import com.rapidminer.ispr.dataset.Const;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.knn.KNNTools;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.IntDoubleContainer;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.dataset.IVector;
import com.rapidminer.ispr.tools.math.container.knn.KNNFactory;

/**
 *
 * @author Marcin
 */
public class GENNInstanceSelectionModel implements PRulesModel<ExampleSet> {

    private DistanceMeasure measure;
    private int k;
    private double maxError;
    private boolean relativeError;
    private Exception exception = null;
    private boolean trainOnSubset;
    private AbstractInstanceSelectorChain evaluator;

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
        ISPRGeometricDataCollection<IValuesStoreLabels> nearestNeighbors = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, inputExampleSet, measure);
        DataIndex index = exampleSet.getIndex();
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

        IVector values = ValuesStoreFactory.createVector(inputExampleSet);

        ExampleSet resultExample;
        for (Example example : exampleSet) {
            double predictedLabel;
            ISPRExample te = (ISPRExample) example;
            instanceIndex = te.getIndex();
            values.setValues(example);            
            Collection<IValuesStoreLabels> nearest = nearestNeighbors.getNearestValues(k, values);
            if (numericalLabel) {
                for (IValuesStoreLabels a : nearest) {
                    mean += a.getLabel();
                }
                mean = (nearest.isEmpty()) ? Double.NaN : mean / nearest.size();
                for (IValuesStoreLabels a : nearest) {
                    variance += (a.getLabel()- mean) * (a.getLabel()- mean);
                }
                variance /= nearest.size();
            }
            if (trainOnSubset){
                trainingIndex.setAllFalse();
                for (IValuesStoreLabels a : nearest) {
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
            trainingIndex.set(instanceIndex, false);
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
