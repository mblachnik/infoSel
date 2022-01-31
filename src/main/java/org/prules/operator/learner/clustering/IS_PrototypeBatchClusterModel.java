/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import org.prules.operator.learner.clustering.models.AbstractBatchModel;
import org.prules.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.clustering.ClusterModel;
import com.rapidminer.tools.Ontology;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Information Selection IOObject used for cluster assigment for all type of clustering algorithms based on prototypes which use batch method for obtaining the prototypes
 * @author Marcin
 */
public class IS_PrototypeBatchClusterModel extends IS_ClusterModel {

    final Map<Integer, String> clusterNames;
    final AbstractBatchModel model;
    final boolean addPartitionMatrix;
    final List<String> attributeNames;
    final boolean addCluster;
    final int numberOfClusters;
    final List<Double> costFunctionValues;

    public IS_PrototypeBatchClusterModel(AbstractBatchModel model, Map<Integer, String> clusterNamesMap, ExampleSet exampleSet, int k, boolean addCluster, boolean addClusterAsLabel, boolean removeUnknown, boolean addPartitionMatrix, List<Double> costFunctionValues) {
        super(exampleSet, k, addClusterAsLabel, removeUnknown);
        this.clusterNames = clusterNamesMap;
        this.model = model;
        this.addPartitionMatrix = addPartitionMatrix;
        Attributes attributes = exampleSet.getAttributes();
        attributeNames = new ArrayList<>(attributes.size());
        for (Attribute a : attributes) {
            attributeNames.add(a.getName());
        }
        this.addCluster = addCluster;
        numberOfClusters = clusterNames.size();
        this.costFunctionValues = costFunctionValues;
    }

    /**
     * Adds result attributes to the input trainingSet which store clustering
     * results. These new atrributes are: cluster or label role with cluster
     * assigments and attributes with partition matrix
     *
     * @param trainingSet
     * @return
     */
    @Override
    public ExampleSet apply(ExampleSet trainingSet) {
        return apply(trainingSet, false);
    }

    /**
     * Adds result attributes to the input trainingSet which store clustering
     * results. These new atrributes are: cluster or label role with cluster
     * assigments and attributes with partition matrix The setClusterAssignments
     * derives from RapidMiner clusterModel support.
     *
     * @param trainingSet
     * @param setClusterAssignments - true idicates that the input data will be
     * rein
     * @return
     */
    @Override
    public ExampleSet apply(ExampleSet trainingSet, boolean setClusterAssignments) {
        int[] labels = getClusterAssignments(PRulesUtil.reorderAttributesByName(trainingSet, attributeNames));
        if (setClusterAssignments) {
            setClusterAssignments(labels, trainingSet);
        }
        if (addCluster) {
            int c = clusterNames.size();
            Attributes attributes = trainingSet.getAttributes();
            ArrayList<Attribute> partitionMatrixAttributes = null;
            Iterator<double[]> partitionMatrixIterator = null;
            if (addPartitionMatrix) {
                partitionMatrixAttributes = new ArrayList<>(c); //partition matrix + cluster attribute
                for (int i = 0; i < c; i++) {
                    Attribute attribute = AttributeFactory.createAttribute(Attributes.CONFIDENCE_NAME + "(" + clusterNames.get(i) + ")", Ontology.NUMERICAL);
                    partitionMatrixAttributes.add(attribute);
                }
                trainingSet.getExampleTable().addAttributes(partitionMatrixAttributes);
                int i = 0;
                for (Attribute attribute : partitionMatrixAttributes) {
                    attributes.addRegular(attribute);
                    attributes.setSpecialAttribute(attribute, Attributes.CONFIDENCE_NAME + "_" + clusterNames.get(i));
                    i++;
                }
                partitionMatrixIterator = model.getPartitionMatrix().iterator();
            }
            //Cluster labels        
            String labelName;
            if (isAddingLabel()) {
                labelName = Attributes.LABEL_NAME;
            } else {
                labelName = Attributes.CLUSTER_NAME;
            }
            Attribute trainingSetLabels = AttributeFactory.createAttribute(labelName, Ontology.NOMINAL);
            NominalMapping labelsNames = new PolynominalMapping(clusterNames);
            trainingSetLabels.setMapping(labelsNames);
            trainingSetLabels.setDefault(Double.NaN);
            trainingSet.getExampleTable().addAttribute(trainingSetLabels);
            Iterator<Example> exampleIterator = trainingSet.iterator();
            if (isAddingLabel()) {
                attributes.setLabel(trainingSetLabels);
            } else {
                attributes.setCluster(trainingSetLabels);
            }
            int i, j = 0;
            while (exampleIterator.hasNext()) {
                Example example = exampleIterator.next();
                i = 0;
                if (addPartitionMatrix) {
                    double[] partitionMatrix = partitionMatrixIterator.next();
                    for (Attribute attribute : partitionMatrixAttributes) {
                        example.setValue(attribute, partitionMatrix[i]);
                        i++;
                    }
                }
                example.setValue(trainingSetLabels, labels[j]);
                j++;
            }
        }
        return trainingSet;
    }

    @Override
    public int[] getClusterAssignments(ExampleSet trainingSet) {
        model.resetPartitionMatrix(trainingSet);
        model.updatePartitionMatrix(trainingSet);
        //Iterator<Example> trainingSetIterator = trainingSet.iterator();
        Iterator<double[]> partitionMatrixIterator = model.getPartitionMatrix().iterator();
        int[] results = new int[trainingSet.size()];
        int j = 0;
        //while (trainingSetIterator.hasNext() && partitionMatrixIterator.hasNext()) {
        while (partitionMatrixIterator.hasNext()) {
            double[] partitionMatrixEntry = partitionMatrixIterator.next();
            //Example example = trainingSetIterator.next();
            double best = -1;
            for (int i = 0; i < getNumberOfClusters(); i++) {
                double curValue = partitionMatrixEntry[i];
                if (curValue > best) {
                    best = curValue;
                    results[j] = i;
                }
            }
            j++;

        }
        return results;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Model info:\n")
                .append(super.toString())
                .append("\n")
                .append("Cost function:");
        costFunctionValues.forEach(x->builder.append("  ").append(x).append("\n"));
        return builder.toString();
    }
}
