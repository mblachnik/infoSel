/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.InstanceFactory;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.tools.EmptyInstanceModifier;
import org.prules.operator.learner.selection.models.tools.InstanceModifier;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.dataset.Vector;

/**
 * Class implements repeated ENN algorithm (RENN). It repeats ENN algorithm
 * until any instance can be marked for removal
 *
 * @author Marcin
 */
public class RENNInstanceSelectionModelOld extends AbstractInstanceSelectorModel {

    private final int k;
    private final DistanceMeasure measure;
    private final IISDecisionFunction loss;
    private final InstanceModifier modifier;
    private final int maxIterations;

    /**
     * Constructor of repeated ENN algorithm (RENN). It repeats ENN algorithm
     * until any instance can be marked for removal
     *
     * @param measure - distance measure
     * @param k - number of nearest neighbors
     * @param loss - loss function
     * @param modifier - instance modifier, if null it does nothing, one can set here an instance modifier which on the fly changes instance considered for removal
     */
    public RENNInstanceSelectionModelOld(DistanceMeasure measure, int k, IISDecisionFunction loss, int maxIterations ,InstanceModifier modifier) {
        this.k = k;
        this.measure = measure;
        this.loss = loss;
        this.maxIterations = maxIterations;        
        if (modifier == null){
            this.modifier = new EmptyInstanceModifier();
        } else {
            this.modifier = modifier;
        }
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     * performed
     * @return - index of selected examples
     */
    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attributes = exampleSet.getAttributes();
        Attribute label = attributes.getLabel();
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollection<IInstanceLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        loss.init(samples);
        int numberOfClasses = label.getMapping().size();
        int maxIterations = this.maxIterations < 0 ? Integer.MAX_VALUE : this.maxIterations;        
        //ENN EDITTING
        Vector values = InstanceFactory.createVector(exampleSet);
        int[] counter = new int[numberOfClasses];
        IDataIndex mainIndex = exampleSet.getIndex();
        boolean breakLoop = false;
        int loopCounter = 0;
        do {
            int instanceIndex = 0;
            IDataIndex index = exampleSet.getIndex();
            for (Example example : exampleSet) {
                Arrays.fill(counter, 0);
                Collection<IInstanceLabels> res;                
                values.setValues(example);
                values = modifier.modify(values);
                res = samples.getNearestValues(k + 1, values);
                double sum = 0;
                for (IInstanceLabels i : res) {
                    counter[(int)i.getLabel()]++;
                    sum++;
                }
                counter[(int) example.getLabel()] --; //here we have to subtract distanceRate because we took k+1 neighbours 					            
                sum--; //here we have to subtract because nearest neighbors includ itself, see line above
                int mostFrequent = PRulesUtil.findMostFrequentValue(counter);
                if (example.getLabel() != mostFrequent) {
                    index.set(instanceIndex, false);
                    breakLoop = true;
                }
                instanceIndex++;
            }            
            exampleSet.setIndex(index);
            mainIndex.setIndex(index);
            for (int i = index.getFullLength() - 1; i > -1; i--) {
                if (!index.get(i)) {
                    samples.remove(i);
                }
            }
            loopCounter++;
        } while (!breakLoop || loopCounter < maxIterations);
        return mainIndex;
    }
}
