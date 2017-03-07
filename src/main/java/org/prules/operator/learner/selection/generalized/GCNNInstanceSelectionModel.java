/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.generalized;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.operator.learner.PRulesModel;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;

/**
 *
 * @author Marcin
 */
public class GCNNInstanceSelectionModel implements PRulesModel<ExampleSet> {

    private final DistanceMeasure distance;    
    private final AbstractInstanceSelectorChain evaluator;
    private final boolean relativeError;
    private final double maxError;
    private final int k;
    private Exception exception = null;

    /**
     * 
     * @param distance
     * @param relativeError
     * @param maxError
     * @param k
     * @param evaluator
     */
    public GCNNInstanceSelectionModel(DistanceMeasure distance, boolean relativeError, double maxError, int k, AbstractInstanceSelectorChain evaluator) {
        this.distance = distance;        
        this.relativeError = relativeError;
        this.maxError = maxError;
        this.k = k;
        this.evaluator = evaluator;

    }

    /**
     * 
     * @param inputExampleSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet;
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        ISPRGeometricDataCollection<IInstanceLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, inputExampleSet, distance);
        EditedExampleSet testSet = new EditedExampleSet(exampleSet);
        EditedExampleSet selectedSet = new EditedExampleSet(exampleSet);
        EditedExampleSet examplesLeftSet = new EditedExampleSet(exampleSet);
        IDataIndex index         = exampleSet.getIndex();
        DataIndex testIndex     = testSet.getIndex();
        DataIndex selectedIndex = selectedSet.getIndex();
        DataIndex examplesLeftIndex = examplesLeftSet.getIndex();

        index.setAllFalse();
        testIndex.setAllFalse();
        selectedIndex.setAllFalse();
        examplesLeftIndex.setAllTrue();

        int i = 0;
        index.set(i, true);
        selectedIndex.set(i, true);
        examplesLeftIndex.set(i, false);
        Attribute attribue = exampleSet.getAttributes().getLabel();
        boolean numericalLabel = attribue.isNumerical();
        boolean nominalLabel = attribue.isNominal();
        boolean isModified;
        boolean update = false;
        do {            
            isModified = false;
            for (Example example : examplesLeftSet) {                
                
                ISPRExample queryExample = (ISPRExample) example;
                int exampleIndex = queryExample.getIndex();
                testIndex.set(exampleIndex, true);
                ExampleSet resultSet;
                try {
                    resultSet = evaluator.executeInerModel(selectedSet, testSet);
                } catch (Exception e) {
                    exception = e;
                    return null;
                }                                
                double predictedY = resultSet.getExample(0).getPredictedLabel();                
                double realY = example.getLabel();
                testIndex.set(exampleIndex, false);
                
                if (numericalLabel) {
                    double dif = Math.abs(realY - predictedY);
                    if (!relativeError) {
                        update = dif > maxError;
                    } else {
                        double var = 0;
                        double mean = 0;
                        Collection<IInstanceLabels> nearestNeighbors = KNNTools.returnKNearestNeighbors(example, samples, k);
                        for (IInstanceLabels x : nearestNeighbors) {
                            mean += x.getLabel();
                        }
                        mean /= nearestNeighbors.size(); //Warning here is size() instead of k because there may be less samples then k
                        for (IInstanceLabels x : nearestNeighbors) {
                            var += (x.getLabel()- mean) * (x.getLabel()- mean);
                        }
                        var /= nearestNeighbors.size();
                        double vdY = maxError * Math.sqrt(var);
                        update = dif > vdY;
                    }
                } else if (nominalLabel){
                    update = predictedY != realY;
                } else {
                    exception = new Exception("Unsupportet label type");
                }
                if (update) {                                        
                    index.set(exampleIndex, true);
                    selectedIndex.set(exampleIndex, true);
                    examplesLeftIndex.set(exampleIndex, false);
                    isModified = true;
                }
            }
        } while (isModified);
        exampleSet.setIndex(index);
        return exampleSet;
    }

    /**
     * 
     * @return
     */
    public boolean isException() {
        return exception != null;
    }

    /**
     * 
     * @return
     */
    public Exception getException() {
        return exception;
    }
}
