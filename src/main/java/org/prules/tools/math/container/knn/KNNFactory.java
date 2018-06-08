/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.ExampleSet;
import org.prules.dataset.Const;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.HashMap;
import java.util.Map;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;
import weka.core.matrix.IntVector;

/**
 *
 * @author Marcin
 */
public class KNNFactory {

    public static ISPRGeometricDataCollection<IInstanceLabels> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, DistanceMeasure measure) {
        Map<Attribute, String> storedAttributes = new HashMap<>();

        storedAttributes.put(exampleSet.getAttributes().getLabel(), Const.LABEL);
        storedAttributes.put(exampleSet.getAttributes().getId(), Const.ID);
        storedAttributes.put(exampleSet.getAttributes().getCluster(), Const.CLUSTER);
        storedAttributes.put(exampleSet.getAttributes().getWeight(), Const.WEIGHT);
        AttributeRole noiseRole = exampleSet.getAttributes().getRole(Const.NOISE);
        if (noiseRole != null) {
            storedAttributes.put(noiseRole.getAttribute(), Const.NOISE);
        }
        return initializeKNearestNeighbourFactory(type, exampleSet, storedAttributes, measure);
    }

    /**
     * Returns nearest neighbor data structure
     *
     * @param type - type of the structure
     * @param exampleSet - dataset
     * @param storedAttributes - which attributes to store
     * @param measure - distance measure
     * @return
     */
    public static ISPRGeometricDataCollection<IInstanceLabels> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, Map<Attribute, String> storedAttributes, DistanceMeasure measure) {
        ISPRGeometricDataCollection samples = null;
        switch (type) {
            case LINEAR_SEARCH:
                samples = new LinearList(exampleSet, storedAttributes, measure);
                break;
            case CACHED_LINEAR_SEARCH:
                samples = new SimpleNNCachedLineraList(exampleSet, storedAttributes, measure);
                break;
            case BALL_TREE_SEARCH:
                samples = new BallTree(exampleSet, storedAttributes, measure);
                break;
            case KD_TREE_SEARCH:
                samples = new KDTree(exampleSet, storedAttributes, measure);
                break;
        }
        return samples;
    }

    /**
     * Returns nearest neighbor data structure
     *
     * @param type - type of the structure
     * @param exampleSet - dataset
     * @param attribute - attribute to store
     * @param storedValueName - name in the store
     * @param measure - distance measure
     * @return
     */
    public static ISPRGeometricDataCollection<IInstanceLabels> initializeKNearestNeighbourFactory(GeometricCollectionTypes type, ExampleSet exampleSet, Attribute attribute, String storedValueName, DistanceMeasure measure) {
        Map<Attribute, String> map = new HashMap<>();
        map.put(attribute, storedValueName);
        return initializeKNearestNeighbourFactory(type, exampleSet, map, measure);
    }

}
