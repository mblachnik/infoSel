/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.clustering;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.optimization.Prototype;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.tools.Ontology;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public class ISPRClusterModelTools {

    /**
     * Creates a Map<Integer, String> which maps cluster number into cluster
     * name in a form of {
     *
     * @see Attributes.CLUSTER_NAME} _\#cluster_number
     * @param c - number of clusters
     */
    public static Map<Integer, String> prepareClusterNamesMap(int c) {
        Map<Integer, String> clusterNamesMap = new HashMap<Integer, String>(c); //Map of nominal values of Cluster attribute        
        for (int i = 0; i < c; i++) {
            String clusterName = Attributes.CLUSTER_NAME + "_" + i;
            clusterNamesMap.put(i, clusterName);
        }
        return clusterNamesMap;
    }

    /**
     * This method creates ExampleSet from a collection of {
     *
     * @param clusterNames a map which maps prototype id on its name (label)
     * @see Prototype} objects. It is used to convert cluster centers into
     * ExampleSet, because prototypes are represented as double[] they have to
     * be mapped into appropriate attribute names, so a list of attributes is
     * required.
     * @param codebooks - collection of {
     * @see Prototype}
     * @param referenceAttributes - list of attributes mapping appropriate
     * attribute names and types on prototypes exampleSet
     * @return - exampleSet containing prototypes
     */
    public static ExampleSet prepareCodebooksExampleSet(Collection<Prototype> codebooks, Map<Integer, String> clusterNames, Attributes referenceAttributes) {
        List<Attribute> attributes = new ArrayList<>(referenceAttributes.size());
        for (Attribute a : referenceAttributes) {
            a = AttributeFactory.createAttribute(a);
            attributes.add(a);
        }
        Attribute codebookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
        NominalMapping codebookLabelsNames = new PolynominalMapping(clusterNames);
        codebookLabels.setMapping(codebookLabelsNames);
        attributes.add(codebookLabels);
        ExampleTable codebooksTable = new MemoryExampleTable(attributes, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), codebooks.size());
        ExampleSet codebooksSet = new SimpleExampleSet(codebooksTable, attributes);
        codebooksSet.getAttributes().setLabel(codebookLabels);
        Iterator<Prototype> codebookIterator = codebooks.iterator();
        Iterator<Example> codebookExampleIterator = codebooksSet.iterator();
        //Rewrite codebooks to codebooks ExampleSet
        Attributes codebookAttributes = codebooksSet.getAttributes();
        int codebookIndex = 0;
        while (codebookIterator.hasNext()) {
            Prototype codebook = codebookIterator.next();
            Example codebookExample = codebookExampleIterator.next();
            int i = 0;
            for (Attribute a : codebookAttributes) {
                codebookExample.setValue(a, codebook.getValues()[i]);
                i++;
            }
            codebookExample.setLabel(codebookIndex);
            codebookIndex++;
        }
        return codebooksSet;
    }
    
     
}
