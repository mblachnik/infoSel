/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.tools.EmptyInstanceModifier;
import org.prules.operator.learner.selection.models.tools.InstanceModifier;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.dataset.Vector;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;
import org.prules.tools.math.container.knn.KNNTools;

/**
 * Class implements repeated ENN algorithm (RENN). It repeats ENN algorithm
 * until any instance can be marked for removal
 *
 * @author Marcin
 */
public class RENNWithModifierInstanceSelectionModel extends AbstractInstanceSelectorModel {

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
     * @param maxIterations - maximum number of iterations. This algorithm in worst case can have n^3 complexity, by setting max iteration you can break the process earlier reaching n^2*mazIterations complexity. Number of loops depends on data distribution
     * @param modifier - instance modifier, if null it does nothing, one can set here an instance modifier which on the fly changes instance considered for removal
     */
    public RENNWithModifierInstanceSelectionModel(DistanceMeasure measure, int k, IISDecisionFunction loss, int maxIterations, InstanceModifier modifier) {
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
        //DATA STRUCTURE PREPARATION        
        ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples = (ISPRGeometricDataCollectionWithIndex<IInstanceLabels>) KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, measure);
        int numberOfClasses = exampleSet.getAttributes().getLabel().getMapping().size();
        loss.init(samples);        
        //ENN EDITTING
        Vector values;
        IInstanceLabels label;
        int[] counter = new int[numberOfClasses];
        IDataIndex mainIndex = exampleSet.getIndex();
        int maxIterations = this.maxIterations < 0 ? Integer.MAX_VALUE : this.maxIterations;   
        int loopCounter = 0; 
        boolean breakLoop;
        do {
            breakLoop = true;
            int instanceIndex = 0;
            IDataIndex index = mainIndex.getIndex();
            Iterator<Vector> iteratorSamples = samples.samplesIterator();
            Iterator<IInstanceLabels> iteratorLabels = samples.storedValueIterator();
            IDataIndex samplesIndex = samples.getIndex();
            samplesIndex.setAllTrue();
            while (iteratorSamples.hasNext() && iteratorLabels.hasNext()) {
                Arrays.fill(counter, 0);
                Collection<IInstanceLabels> res;                
                values = iteratorSamples.next();
                label  = iteratorLabels.next();
                values = modifier.modify(values);
                samplesIndex.set(instanceIndex,false);
                res = samples.getNearestValues(k, values, samplesIndex);
                samplesIndex.set(instanceIndex,true);
                double sum = 0;
                for (IInstanceLabels i : res) {
                    counter[(int)i.getLabel()]++;
                    sum++;
                }
                //counter[(int) label.getLabel()] --; //here we have to subtract distanceRate because we took k+1 neighbours 					            
                //sum--; //here we have to subtract because nearest neighbors includ itself, see line above
                int mostFrequent = KNNTools.getMostFrequentValue(counter);
                if (label.getLabel() != mostFrequent) {
                    index.set(instanceIndex, false);
                    breakLoop = false;
                }
                instanceIndex++;
            }                        
            mainIndex.setIndex(index);            
            for (int i = index.size() - 1; i > -1; i--) {
                if (!index.get(i)) {
                    samples.remove(i);
                }
            }
        loopCounter++;
        } while (!(breakLoop || loopCounter >= maxIterations));
        return mainIndex;
    }
}
