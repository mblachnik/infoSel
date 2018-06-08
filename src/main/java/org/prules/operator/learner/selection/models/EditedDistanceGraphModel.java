/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.Const;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.similarity.DistanceEvaluator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.selection.models.tools.EmptyInstanceModifier;
import org.prules.operator.learner.selection.models.tools.InstanceModifier;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;
import org.prules.dataset.IInstancePrediction;
import org.prules.tools.math.similarity.IDistanceEvaluator;

/**
 * Class implementing Edited Distance Graph based algorithms
 * @author Marcin
 */
public class EditedDistanceGraphModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure distance;
    private final EditedDistanceGraphCriteria criteria;
    private final IISDecisionFunction loss;
    private final InstanceModifier modifier;
    private final IDistanceEvaluator distanceEvaluator;

    /**
     * Constructor of general Edited distance Graph models such as Gabriel editing
     * @param distance
     * @param criteria
     * @param loss
     * @param modifier instance modifier
     */
    public EditedDistanceGraphModel(DistanceMeasure distance, EditedDistanceGraphCriteria criteria, IISDecisionFunction loss, InstanceModifier modifier) {
        this.distance = distance;
        this.criteria = criteria;
        this.loss = loss;        
        if (modifier == null){
            this.modifier = new EmptyInstanceModifier();
        } else {
            this.modifier = modifier;
        }
        distanceEvaluator = new DistanceEvaluator(distance);
    }

    /**
     * Performs instance selection
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        int size = exampleSet.size();
        ISPRGeometricDataCollection<IInstanceLabels> samples;
        samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, distance);
//        ArrayList<double[]> samples = new ArrayList<double[]>(exampleSet.size());
//        ArrayList<Number> labels = new ArrayList<Number>(exampleSet.size());
        int numberOfAttrbutes = exampleSet.getAttributes().size();
        loss.init(samples);
//        for (Example ex : exampleSet) {
//            double[] values = new double[numberOfAttrbutes];
//            KNNTools.extractExampleValues(ex, values);
//            samples.add(values);
//            labels.add(ex.getLabel());
//        }      
        Vector vector = InstanceFactory.createVector(exampleSet);
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels labelB = InstanceFactory.createInstanceLabels();
        
        DataIndex indexA = new DataIndex(size);
        indexA.setAllFalse();
        for (int iA = 0; iA < size; iA++) {
            Vector sampleA = samples.getSample(iA);            
            double labelA = samples.getStoredValue(iA).getLabel();
            for (int iB = 0; iB < size; iB++) {
                if (iB == iA) continue;
                Vector sampleB = samples.getSample(iB);                
                labelB = samples.getStoredValue(iB);                                
                prediction.setLabel(labelA);
                instance.put(Const.VECTOR, sampleB);
                instance.put(Const.LABELS, labelB);
                instance.put(Const.PREDICTION, prediction);                
                if (loss.getValue(instance) > 0) {                    
                    boolean chk = true;
                    double dAB = distanceEvaluator.evaluateDistance(sampleA, sampleB);
                    for (int iC = 0; iC < size; iC++) {
                        if (iC == iA || iC == iB) continue;
                        Vector sampleC = samples.getSample(iC);
                        double dAC = distanceEvaluator.evaluateDistance(sampleA, sampleC);
                        double dBC = distanceEvaluator.evaluateDistance(sampleB, sampleC);
                        if (criteria.evaluate(dAB, dAC, dBC)) {
                            chk = false;
                            break;
                        }
                    }
                    if (chk) {
                        indexA.set(iA, true);
                        indexA.set(iB, true);
                    }                    
                }
            }
        }                
        return indexA;


/*
        EditedExampleSet exampleSetB = new EditedExampleSet(exampleSet);
        EditedExampleSet exampleSetC = new EditedExampleSet(exampleSet);
        DataIndex indexA = new DataIndex(size);
        indexA.setAllFalse();
        DataIndex indexB = exampleSetB.getIndex();
        DataIndex indexC = exampleSetC.getIndex();
        int counterA = 0;
        int counterB;
        for (Example exampleA : exampleSet) {
            double labelA = exampleA.getLabel();
            ISPRExample exampleAA = (ISPRExample) exampleA;
            int exampleAId = exampleAA.getIndex();
            indexB.set(exampleAId, false);
            indexC.set(exampleAId, false);
            counterB = counterA + 1;
            for (Example exampleB : exampleSetB) {
                if (exampleB.getLabel() != labelA) {
                    ISPRExample exampleBB = (ISPRExample) exampleB;
                    indexC.set(exampleBB.getIndex(), false);
                    boolean chk = true;
                    double dAB = distance.calculateDistance(exampleA, exampleB);
                    for (Example exampleC : exampleSetC) {
                        double dAC = distance.calculateDistance(exampleA, exampleC);
                        double dBC = distance.calculateDistance(exampleB, exampleC);
                        if (criteria.evaluate(dAB, dAC, dBC)) {
                            chk = false;
                            break;
                        }
                    }
                    if (chk) {
                        indexA.set(counterA, true);
                        indexA.set(counterB, true);
                    }
                    indexC.set(exampleBB.getIndex(), true);
                }
                counterB++;
            }
            indexC.set(exampleAId, true);
            counterA++;
        }
        */
    }
}
