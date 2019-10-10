/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.prules.operator.learner.feature.selection;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.operator.AbstractModel;

/**
 * @author Marcin
 */
public class FeatureSelectionModel extends AbstractModel {
    /**
     *
     */
    public static final long serialVersionUID = 1L;
    private AttributeWeights attributeWeights;

    /**
     * @param exampleSet
     * @param attributeWeights
     */
    public FeatureSelectionModel(ExampleSet exampleSet, AttributeWeights attributeWeights) {
        super(exampleSet);
        this.attributeWeights = attributeWeights;
    }

    @Override
    public ExampleSet apply(ExampleSet testSet) {
        AttributeWeightedExampleSet weightedSet = new AttributeWeightedExampleSet(testSet, attributeWeights);
        return weightedSet.createCleanClone();
    }
}
