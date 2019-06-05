/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2010 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.prules.operator.learner.classifiers;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.VectorDense;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.operator.learner.tools.PRulesUtil;
import com.rapidminer.tools.Tools;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

/**
 * An implementation of a knn model.
 *
 * @author Sebastian Land
 * @param <T>
 *
 */
public class IS_KNNClassificationModel<T extends Serializable> extends PredictionModel {

    private static final long serialVersionUID = -6292869962412072573L;
    private final int k;
    private final int size;
    private int attributesNumber;
    private final ISPRGeometricDataCollection<T> samples;
    private boolean useCovariance;
    private HashMap<Integer, double[][]> covarianceMatrix;
    private final VotingType weightedNN;
    private PredictionType predictionType;
    private boolean generateID = false;
    private final List<String> trainingAttributeNames;

    /**
     *Constructor of kNN prediction model.
     * 
     * 
     * @param trainingSet - training data set (used to extract attribute information)
     * @param samples - the nearest neighbor structure
     * @param k - number of nearest neighbors
     * @param weightedNN - if we wont to specify particular weighting scheme     
     * @param predictionType - type of prediction model: regression/classification/clustering
     */        
    public IS_KNNClassificationModel(ExampleSet trainingSet, ISPRGeometricDataCollection<T> samples, int k, VotingType weightedNN, PredictionType predictionType) {
        super(trainingSet, ExampleSetUtilities.SetsCompareOption.EQUAL, ExampleSetUtilities.TypesCompareOption.EQUAL);
        this.k = k;
        this.size = trainingSet.size();
        this.samples = samples;
        this.weightedNN = weightedNN;
        Attributes attributes = trainingSet.getAttributes();
        trainingAttributeNames = new ArrayList<>(attributes.size());
        for (Attribute a : attributes) {
            trainingAttributeNames.add(a.getName());
        }
        this.useCovariance = false;
        this.predictionType = predictionType;
    }
    
    @Override    
    //@SuppressWarnings("unchecked")
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) {
        // building attribute order from trainingset
        Attributes attributes = exampleSet.getAttributes();
        attributesNumber = attributes.size();        
        List<Attribute> orderedAttributes = PRulesUtil.reorderAttributesByName(attributes, trainingAttributeNames);
        Vector instance = InstanceFactory.createVector(exampleSet.getExample(0));
        for (Example example : exampleSet) {
            // reading values
            instance.setValues(example);
            double[] counter;
            int mostFrequentIndex;
            switch (predictionType) {
                case Classification:
                    // counting frequency of labels
                    counter = new double[predictedLabel.getMapping().size()];
                    KNNTools.doNNVotes(counter, instance, (ISPRGeometricDataCollection<IInstanceLabels>) samples, k, weightedNN);

                    // finding most frequent class
                    mostFrequentIndex = KNNTools.getMostFrequentValue(counter);
                    // setting prediction
                    example.setValue(predictedLabel, mostFrequentIndex);
                    // setting confidence
                    for (int index = 0; index < counter.length; index++) {
                        example.setConfidence(predictedLabel.getMapping().mapIndex(index), counter[index]);
                    }
                    break;
                case Clustering:
                    // counting frequency of labels
                    counter = new double[predictedLabel.getMapping().size()];
                    KNNTools.doNNVotes(counter, instance, (ISPRGeometricDataCollection<IInstanceLabels>) samples, k, weightedNN);

                    // finding most frequent class
                    mostFrequentIndex = KNNTools.getMostFrequentValue(counter);
                    // setting prediction
                    example.setValue(predictedLabel, mostFrequentIndex);                                        
                    break;
                case Regression:
                    double predictedValue = KNNTools.getRegVotes(instance, (ISPRGeometricDataCollection<IInstanceLabels>) samples, k, weightedNN);
                    // setting prediction
                    example.setValue(predictedLabel, predictedValue);
                    break;
            }
        }
        return exampleSet;
    }

    /*
     *
     *
     * @param updateSet @throws OperatorException
     *
     * @Override public void update(ExampleSet updateSet) throws OperatorException { Attribute label =
     * updateSet.getAttributes().getLabel(); // check if exampleset header is correct if (label.isNominal()) { Attributes
     * attributes = updateSet.getAttributes();
     *
     * int valuesSize = attributes.size(); for (Example example : updateSet) { double[] values = new double[valuesSize]; int i =
     * 0; for (Attribute attribute : attributes) { values[i] = example.getValue(attribute); i++; } int labelValue = (int)
     * example.getValue(label); samples.add(values, labelValue); } } }
     *
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(weightedNN.toString());
        buffer.append(k).append("-Nearest Neighbour model for ").append(predictionType).append(".").append(Tools.getLineSeparator());
        buffer.append("The model contains ").append(size).append(" examples with ").append(attributesNumber).append(" dimensions of the following classes:");
        buffer.append(Tools.getLineSeparator());
        if (predictionType == PredictionType.Classification) {
            for (String value : getTrainingHeader().getAttributes().getLabel().getMapping().getValues()) {
                buffer.append("  ").append(value).append(Tools.getLineSeparator());
            }
        }
        if (predictionType == PredictionType.Clustering) {
            for (String value : getTrainingHeader().getAttributes().getCluster().getMapping().getValues()) {
                buffer.append("  ").append(value).append(Tools.getLineSeparator());
            }
        }
        return buffer.toString();
    }

    public ISPRGeometricDataCollection<T> getSamples() {
        return samples;
    }

    public boolean isUseCovariance() {
        return useCovariance;
    }

    public void setUseCovariance(boolean useCovariance) {
        this.useCovariance = useCovariance;
    }

    public void setCovarianceMatrix(HashMap<Integer, double[][]> covarianceMatrix) {
        this.covarianceMatrix = covarianceMatrix;
    }

    public HashMap<Integer, double[][]> getCovarianceMatrix() {
        return this.covarianceMatrix;
    }

    public PredictionType getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(PredictionType predictionType) {
        this.predictionType = predictionType;
    }

    public boolean isGenerateID() {
        return generateID;
    }

    public void setGenerateID(boolean generateID) {
        this.generateID = generateID;
    }
}
