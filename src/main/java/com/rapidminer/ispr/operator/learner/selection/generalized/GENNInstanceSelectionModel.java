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
import com.rapidminer.ispr.operator.learner.PRulesModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.IntDoubleContainer;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;

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
        ISPRGeometricDataCollection<IntDoubleContainer> nearestNeighbors = KNNTools.initializeGeneralizedKNearestNeighbour(inputExampleSet, measure);
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

        double[] values = new double[attributes.size()];

        ExampleSet resultExample;
        for (Example example : exampleSet) {
            double predictedLabel;
            ISPRExample te = (ISPRExample) example;
            instanceIndex = te.getIndex();
            
            int i = 0;
            for (Attribute attribute : attributes) {
                values[i] = example.getValue(attribute);
                i++;
            }
            Collection<IntDoubleContainer> nearest = nearestNeighbors.getNearestValues(k, values);
            if (numericalLabel) {
                for (IntDoubleContainer a : nearest) {
                    mean += a.getSecond();
                }
                mean = (nearest.isEmpty()) ? Double.NaN : mean / nearest.size();
                for (IntDoubleContainer a : nearest) {
                    variance += (a.getSecond() - mean) * (a.getSecond() - mean);
                }
                variance /= nearest.size();
            }
            if (trainOnSubset){
                trainingIndex.setAllFalse();
                for (IntDoubleContainer a : nearest) {
                    trainingIndex.set(a.getFirst(),true);
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
