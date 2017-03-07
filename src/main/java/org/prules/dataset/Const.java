/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.dataset;

import org.prules.operator.learner.weighting.Ontology;

/**
 *
 * @author Marcin
 */
public class Const {
    /**
     * Used to store label values.  Type double
     */
    public static final String LABEL = "label"; 
    /**
     * Used to store cluster assignment.  Type double
     */
    public static final String CLUSTER = "cluster";
    /**
     * Used for naming element which stores example weight. Type double
     */
    public static final String WEIGHT = "weight";
    /**
     * Used for naming element which stores example id. Type int
     */    
    public static final String ID = "id";
    /**
     * Used for naming element which store noise level for given example. Type double
     */
    public static final String NOISE = Ontology.ATTRIBUTE_NOISE;
    /**
     * Used for naming element which stores distance values. Type double
     */    
    public static final String DISTANCE = "distance";
    /**
     * Used for naming element which stores internal index of parent data structure. Type long
     */
    public static final String INDEX_CONTAINER = "index_container";
    /**
     * Used for naming element which stores index in example set. Usually = INDEX_CONTAINER but could be different.  Type long
     */
    public static final String INDEX_EXAMPLESET = "index_exampleset";
    /**
     * Used for naming element which stores example prediction. Type {@see IValuesStorePrediction}
     */
    public static final String PREDICTION = "prediction";
    /**
     * Used for naming element which stores example prediction confidence. Type double[]
     */
    public static final String CONFIDENCE = "confidence";    
    /**
     * Used for naming element which stores example vector values. Type {@see IVector}
     */
    public static final String VECTOR = "vector";
    /**
     * Used for naming element which stores information about given implementation of {@see StoredValues}
     */
    public static final String DESCRIPTION = "description";
    /**
     * Used for naming element which stores comment
     */
    public static final String COMMENT = "comment";
    /**
     * Used for naming element which stores annotations. Not used
     */
    public static final String ANNOTATION = "annotation";
    /**
     * Used for naming element which stores labeling part of an instance. Equivalent to special attributes in RapidMiner. Type {@see IValuesStoreLabels}
     */
    public static final String LABELS = "labels";
    
}
