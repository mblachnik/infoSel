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
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.tools.Ontology;
import org.prules.dataset.Vector;

import java.util.*;

/**
 * A set of static method used for clustering algorithms
 *
 * @author Marcin
 */
public class IS_ClusterModelTools {

    /**
     * Creates a Map<Integer, String> which maps cluster number into cluster
     * name in a form of {
     *
     * @param c - number of clusters
     * @see com.rapidminer.example.Attributes.CLUSTER_NAME} _\#cluster_number
     */
    static Map<Integer, String> prepareClusterNamesMap(int c) {
        Map<Integer, String> clusterNamesMap = new HashMap<Integer, String>(c); //Map of nominal values of Cluster attribute        
        for (int i = 0; i < c; i++) {
            String clusterName = Attributes.CLUSTER_NAME + "_" + i;
            clusterNamesMap.put(i, clusterName);
        }
        return clusterNamesMap;
    }

    /**
     * This method creates ExampleSet from a collection of {@link Vector} used to represent codeBooks posiotions.
     * Example usage - Fuzzy C-means
     *
     * @param clusterNames        a map which maps prototype id on its name (label)
     * @param codeBooks           - collection of {
     * @param referenceAttributes - list of attributes mapping appropriate
     *                            attribute names and types on prototypes exampleSet
     * @return - exampleSet containing prototypes
     * @see Vector} objects. It is used to convert cluster centers into
     * ExampleSet, because prototypes are represented as double[] they have to
     * be mapped into appropriate attribute names, so a list of attributes is
     * required.
     * @see Vector}
     */
    public static ExampleSet prepareCodeBooksExampleSet(Collection<Vector> codeBooks, Map<Integer, String> clusterNames, Attributes referenceAttributes) {
        List<Attribute> attributes = new ArrayList<>(referenceAttributes.size());
        for (Attribute a : referenceAttributes) {
            a = AttributeFactory.createAttribute(a);
            attributes.add(a);
        }
        Attribute codeBookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
        NominalMapping codeBookLabelsNames = new PolynominalMapping(clusterNames);
        codeBookLabels.setMapping(codeBookLabelsNames);
        attributes.add(codeBookLabels);


        //ExampleTable codeBooksTable = new MemoryExampleTable(attributes, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), codeBooks.size());
        //ExampleSet codeBooksSet = new SimpleExampleSet(codeBooksTable, attributes);
        ExampleSet codeBooksSet = ExampleSets.from(attributes).withBlankSize(codeBooks.size()).build();
        codeBooksSet.getAttributes().setLabel(codeBookLabels);
        Iterator<Vector> codeBookIterator = codeBooks.iterator();
        Iterator<Example> codeBookExampleIterator = codeBooksSet.iterator();
        //Rewrite codeBooks to codeBooks ExampleSet
        Attributes codeBookAttributes = codeBooksSet.getAttributes();
        int codeBookIndex = 0;
        while (codeBookIterator.hasNext()) {
            Vector codeBook = codeBookIterator.next();
            Example codeBookExample = codeBookExampleIterator.next();
            int i = 0;
            for (Attribute a : codeBookAttributes) {
                codeBookExample.setValue(a, codeBook.getValues()[i]);
                i++;
            }
            codeBookExample.setLabel(codeBookIndex);
            codeBookIndex++;
        }
        return codeBooksSet;
    }
}
