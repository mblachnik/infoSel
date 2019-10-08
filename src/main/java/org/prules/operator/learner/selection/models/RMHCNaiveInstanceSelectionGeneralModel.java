/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.*;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.tools.math.container.knn.KNNTools;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Naive implementation of RMHC algorithm. Here instead of binary coding of
 * instances we use a table of int, and each instance is represented as its id.
 * When performing a change in the example set instead of bit mutation a single
 * new prototype is determined. This requires more memory, but is much simpler
 * and intuitive to understand. Mutating a bit infact is equivalent to selecting
 * new instance. In binary coding the problem is that not all bits are equally
 * likely to represent certain value (instance) for the value of 128 the oldest
 * bit is set to 1 only once over 128 cases so it makes a problem for mutation
 * because it is high chance 127/128 that the selected instance will be out of
 * range. In natural integer coding this problem does not appear
 *
 * @author Marcin
 */
public class RMHCNaiveInstanceSelectionGeneralModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int numberOfPrototypes;
    private final RandomGenerator randomGenerator;
    private final int iterations;
    private final IISDecisionFunction loss;

    /**
     * Constructor of RMHC algorithm implementation. Here instead of binary coding of
     * instances we use a table of int, and each instance is represented as its
     * id. When performing a change in the example set instead of bit mutation a
     * single new prototype is determined. This requires more memory, but is
     * much simpler and intuitive to understand. Mutating a bit infact is
     * equivalent to selecting new instance. In binary coding the problem is
     * that not all bits are equally likely to represent certain value
     * (instance) for the value of 128 the oldest bit is set to 1 only once over
     * 128 cases so it makes a problem for mutation because it is high chance
     * 127/128 that the selected instance will be out of range. In natural
     * integer coding this problem does not appear
     *
     * @param measure
     * @param populationSize
     * @param iterations
     * @param randomGenerator
     * @param loss
     */
    public RMHCNaiveInstanceSelectionGeneralModel(DistanceMeasure measure, int populationSize, int iterations, RandomGenerator randomGenerator, IISDecisionFunction loss) {
        this.measure = measure;
        this.randomGenerator = randomGenerator;
        this.numberOfPrototypes = populationSize;
        this.iterations = iterations;
        this.loss = loss;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     *                   performed
     * @return - index of selected examples
     */
    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        //loss.init(exampleSet);
        //TODO make use of the loss function, because it is not used now
        //TODO Update the code, because now it repeates the iterations if selected instance is out of range (based on bit encoding)
        // and this may not be good. Moreover it may happen that certain instance is selected more then once,
        // so the final number of samples mey not be equal to the number of prototypes which should be selected
        //initialization
        loss.init(exampleSet, measure);
        int[] bestSelectedInstances;
        IDataIndex index = exampleSet.getIndex();
        EditedExampleSet workingSet = new EditedExampleSet(exampleSet);
        DataIndex indexWorking = workingSet.getIndex();
        int size = exampleSet.size();

        //choosing initial set of prototypes
        int[] selectedInstances = new int[this.numberOfPrototypes]; //Array contains indexes of slected instances
        Set<Integer> setOfWorkingSetIdx = new TreeSet<>();
        int instanceId;
        for (int i = 0; i < numberOfPrototypes; i++) {
            do {
                instanceId = randomGenerator.nextInt(size);
            } while (setOfWorkingSetIdx.contains(instanceId));
            selectedInstances[i] = instanceId;
            setOfWorkingSetIdx.add(instanceId);
        }


        //Building kNN with initial set of prototypes        
        bestSelectedInstances = new int[numberOfPrototypes];
        System.arraycopy(selectedInstances, 0, bestSelectedInstances, 0, numberOfPrototypes);

        indexWorking.setAllFalse();
        for (int j = 0; j < selectedInstances.length; j++) {
            indexWorking.set(selectedInstances[j], true);
        }
        ISPRGeometricDataCollection<IInstanceLabels> kNN = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, workingSet, measure);
        loss.init(kNN);
        double errorRateBest = Double.MAX_VALUE;
        int selectedInstanceToChangeId = 0; //a value in range 0-selectedInstances.length-1. It is index of element of selectedInstances array which we are going to change
        int kNNInstanceIdCurrent = selectedInstances[selectedInstanceToChangeId]; //index of example in training set - recent value
        int kNNInstanceIdCandidate = selectedInstances[selectedInstanceToChangeId]; //index of example in training set - which we evaluate if it will be better then instanceIdCurrent
        Vector kNNInstanceValues = kNN.getSample(selectedInstanceToChangeId);
        IInstanceLabels kNNInstanceLabel = kNN.getStoredValue(selectedInstanceToChangeId);
        Vector vector = InstanceFactory.createVector(exampleSet);
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);
        Instance instance = InstanceFactory.createEmptyInstance();
        IInstanceLabels label = InstanceFactory.createInstanceLabels();

        for (int i = 0; i < iterations; i++) {
            double errorRate = 0;
            int q = 0;
            for (Example testExample : exampleSet) {
                vector.setValues(testExample);
                label.set(testExample);
                //double predictedLabel = KNNTools.predictOneNearestNeighbor(testExample, kNN);                
                double predictedLabel = KNNTools.predictOneNearestNeighbor(vector, kNN);
                prediction.setLabel(predictedLabel);
                instance.put(Const.VECTOR, vector);
                instance.put(Const.LABELS, label);
                instance.put(Const.PREDICTION, prediction);
                double lossVal = loss.getValue(instance);
//                if (lossVal > 0){
//                    System.out.println(i+" "+q + " " + selectedInstances[0] + " " + selectedInstances[1] + " " + selectedInstances[2]);                    
//                }
                q++;
                errorRate += lossVal;
            }
            System.out.println("Error:" + errorRate);
            if (errorRate < errorRateBest) {
                errorRateBest = errorRate;
                //kNN is upToDate so we don't need to do anything
                bestSelectedInstances[selectedInstanceToChangeId] = kNNInstanceIdCandidate;
//                System.arraycopy(selectedInstances, 0, bestSelectedInstances, 0, numberOfPrototypes);
            } else { //We have to restore old current Vector and Label in place of candidate
                //If the previous change was unsuccessful then restore values from before mutation
                //Restore values of kNN
                //IVector vectorToChange = kNN.getSample(selectedInstanceToChangeId);
                kNN.setSample(selectedInstanceToChangeId, kNNInstanceValues, kNNInstanceLabel);
                selectedInstances[selectedInstanceToChangeId] = kNNInstanceIdCurrent;
                //Restore values of selectedInstances                
            }
            //Choose which prototype to change            
            selectedInstanceToChangeId = randomGenerator.nextInt(numberOfPrototypes);
            //Get new prototype id and assure that the value is different from the previous one
            kNNInstanceIdCurrent = selectedInstances[selectedInstanceToChangeId];
            kNNInstanceValues = kNN.getSample(selectedInstanceToChangeId);
            kNNInstanceLabel = kNN.getStoredValue(selectedInstanceToChangeId);
            do {
                kNNInstanceIdCandidate = randomGenerator.nextInt(size);
            } while (kNNInstanceIdCandidate == kNNInstanceIdCurrent);
            //Update information in selectedInstances
            selectedInstances[selectedInstanceToChangeId] = kNNInstanceIdCandidate;
            Example example = exampleSet.getExample(kNNInstanceIdCandidate);
            Vector vectorCandidate = InstanceFactory.createVector(example);
            IInstanceLabels labelCandidate = InstanceFactory.createInstaceLabels(example);
            kNN.setSample(selectedInstanceToChangeId, vectorCandidate, labelCandidate);

            //display(kNN, selectedInstances, exampleSet);
        }
        System.out.println("BestErrorRate = " + errorRateBest);
        index.setAllFalse();
        for (int bestSelectedInstance : bestSelectedInstances) {
            index.set(bestSelectedInstance, true);
        }
        return index;
    }

    private void display(ISPRGeometricDataCollection<Number> kNN, int[] si, SelectedExampleSet es) {
        for (int i = 0; i < si.length; i++) {
            double[] values = kNN.getSample(i).getValues();
            int in = si[i];
            System.out.println("Index = " + in);
            System.out.print(" [");

            for (double value : values) {
                System.out.print(value + " ; ");
            }
            System.out.print("] ");
            Iterator it = es.iterator();
            ISPRExample ex;
            do {

                ex = (ISPRExample) it.next();
            } while (ex.getIndex() != in);
            KNNTools.extractExampleValues(ex, values);
            double id = ex.getId();
            String ids = es.getAttributes().getId().getMapping().mapIndex((int) id);
            in = ex.getIndex();
            System.out.println("Index = " + in);
            System.out.print("  " + ids + " [");
            for (double value : values) {
                System.out.print(value + " ; ");
            }
            System.out.print("] ");
            System.out.println();
        }
        System.out.println("------------------------------------------------");
    }
}
