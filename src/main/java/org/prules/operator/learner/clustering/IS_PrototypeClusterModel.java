/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.VectorDense;
import org.prules.operator.learner.classifiers.VotingType;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.Ontology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;

/**
 * Information Selection IOObject used for cluster assigment for all type of clustering algorithms based on prototypes
 * @author Marcin
 */
public class IS_PrototypeClusterModel extends IS_ClusterModel {

    private static final long serialVersionUID = -6292869962412072573L;
    final Map<Integer, String> clusterNamesMap;
    final ISPRGeometricDataCollection<IInstanceLabels> samples;
    final VotingType weightedNN;
    final List<String> trainingAttributeNames;
    final int numberOfClusters;
    final boolean addCluster;

    /**
     * Constructor of prototype based clustering model.
     *
     * @param trainingSet - training data set (used to extract attribute
     * information)
     * @param model - clustering model. An ISPRGemoetricDataCollection structure
     * for efficient search for nearest prototype
     * @param numberOfClusters - number of clusters
     * @param addClusterAsLabel - required by RM ClusterModel - type of
     * attribute which should have labels
     * @param clusterNamesMap - hashMap of pairs cluster id, cluster name
     * @param addCluster - if true then new attribute containing clustering
     * results will be added. If false the algorithm can be used to identify
     * cluster centers only
     */
    public IS_PrototypeClusterModel(ExampleSet trainingSet, ISPRGeometricDataCollection<IInstanceLabels> model, int numberOfClusters, Map<Integer, String> clusterNamesMap, boolean addClusterAsLabel, boolean addCluster) {
        super(trainingSet, numberOfClusters, addClusterAsLabel, false);
        samples = model;
        weightedNN = VotingType.MAJORITY;
        Attributes attributes = trainingSet.getAttributes();
        trainingAttributeNames = new ArrayList<>(attributes.size());
        for (Attribute a : attributes) {
            trainingAttributeNames.add(a.getName());
        }
        this.clusterNamesMap = clusterNamesMap;
        this.numberOfClusters = clusterNamesMap.size();
        this.addCluster = addCluster;
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
     * @param setClusterAssignments
     * @return
     */
    @Override
    public ExampleSet apply(ExampleSet trainingSet, boolean setClusterAssignments) {
        int[] labels = getClusterAssignments(trainingSet);
        if (setClusterAssignments) {
            setClusterAssignments(labels, trainingSet);
        }
        if (addCluster) {
            int c = clusterNamesMap.size();
            Attributes attributes = trainingSet.getAttributes();
            //Cluster labels        
            String labelName;
            if (isAddingLabel()) {
                labelName = Attributes.LABEL_NAME;
            } else {
                labelName = Attributes.CLUSTER_NAME;
            }
            Attribute trainingSetLabels = AttributeFactory.createAttribute(labelName, Ontology.NOMINAL);
            NominalMapping labelsNames = new PolynominalMapping(clusterNamesMap);
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
                example.setValue(trainingSetLabels, labels[j]);
                j++;
            }
        }
        return trainingSet;
    }

    /**
     * This method determines which example from exampleSet belongs to which
     * cluster based on .
     *
     * @param exampleSet - dataset which we want to cluster
     * @return array containing assignments of given example to given cluster.
     * Clusters are numbered 0 to k-1
     */
    @Override
    public int[] getClusterAssignments(ExampleSet exampleSet) {
        int[] predictions = new int[exampleSet.size()];
        Attributes attributes = exampleSet.getAttributes();
        int attributesNumber = trainingAttributeNames.size();        
        int j = 0;
        double[] counter;
        counter = new double[numberOfClusters];

        List<Attribute> orderedAttributes = PRulesUtil.reorderAttributesByName(attributes, trainingAttributeNames);
        Vector instance = InstanceFactory.createVector(new double[exampleSet.getAttributes().size()]);
        for (Example example : exampleSet) {
            // reading values
            instance.setValues(example, orderedAttributes);
            int mostFrequentIndex;
            // counting frequency of labels
            Arrays.fill(counter, 0);
            KNNTools.doNNVotes(counter,instance, samples, 1, weightedNN);
            // finding most frequent class
            mostFrequentIndex = KNNTools.getMostFrequentValue(counter);
            // setting prediction
            predictions[j] = mostFrequentIndex;
            j++;
        }
        return predictions;
    }
}
