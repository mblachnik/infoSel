/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.dataset.Instance;
import com.rapidminer.ispr.dataset.InstanceGenerator;
import com.rapidminer.ispr.dataset.StoredValuesHelper;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Iterator;
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
     *Constructor of RMHC algorithm implementation. Here instead of binary coding of
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
     * performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        //loss.init(exampleSet);
        //TODO make use of the loss function, because it is not used now
        //TODO Update the code, because now it repeates the iterations if selected instance is out of range (based on bit enciding) and this may not be good. Moreover it may happen that certain instance is selected more then once, so the final number of samples mey not be equalt to the number of prototypes which should be selected 
        //initialization
        loss.init(exampleSet, measure);
        int[] bestSelectedInstances;        
        DataIndex index = exampleSet.getIndex();
        EditedExampleSet workingSet = new EditedExampleSet(exampleSet);
        DataIndex indexWorking = workingSet.getIndex();
        int size = exampleSet.size();

        //choosing initial set of prototypes
        int[] selectedInstances = new int[this.numberOfPrototypes];
        TreeSet<Integer> usedValues = new TreeSet<>();
        int instanceId;
        for (int i = 0; i < numberOfPrototypes; i++) {
            do {
                instanceId = randomGenerator.nextInt(size);
            } while (usedValues.contains(instanceId));
            selectedInstances[i] = instanceId;
            usedValues.add(instanceId);
        }

        //Building kNN with initial set of prototypes        
        bestSelectedInstances = new int[numberOfPrototypes];
        System.arraycopy(selectedInstances, 0, bestSelectedInstances, 0, numberOfPrototypes);

        indexWorking.setAllFalse();
        for (int j = 0; j < selectedInstances.length; j++) {
            indexWorking.set(selectedInstances[j], true);
        }
        ISPRGeometricDataCollection<IStoredValues> kNN = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, workingSet, measure);
        loss.init(kNN);
        double errorRateBest = Double.MAX_VALUE;
        int prototypeToChangeId = 0;
        int curentInstanceId = selectedInstances[prototypeToChangeId];
        int newInstanceId = selectedInstances[prototypeToChangeId];
        Example example;
        Instance values = InstanceGenerator.generateInstance(exampleSet);
        for (int i = 0; i < iterations; i++) {
            double errorRate = 0;
            for (Example testExample : exampleSet) {
                values.setValues(testExample);
                //double predictedLabel = KNNTools.predictOneNearestNeighbor(testExample, kNN);
                double predictedLabel = KNNTools.predictOneNearestNeighbor(values, kNN);
                double realLabel = testExample.getLabel();
                errorRate += loss.getValue(realLabel, predictedLabel, values);
            }
            //System.out.println("ER=" + errorRate);
            if (errorRate < errorRateBest) {
                errorRateBest = errorRate;
                //Set the new values of 
                bestSelectedInstances[prototypeToChangeId] = newInstanceId;
                //System.arraycopy(selectedInstances, 0, bestSelectedInstances, 0, numberOfPrototypes);
            } else {
                //If the previous change was unseccesfull then restore values from before mutatio
                //Restore values of kNN
                values = kNN.getSample(prototypeToChangeId);
                example = exampleSet.getExample(curentInstanceId);                
                kNN.setSample(prototypeToChangeId, values, StoredValuesHelper.createStoredValue(example));
                //Restore values of selectedInstances
                selectedInstances[prototypeToChangeId] = curentInstanceId;
            }
            //Choose which prototype to change            
            prototypeToChangeId = randomGenerator.nextInt(numberOfPrototypes);
            //Get new prototype id and assure that the value is different from the previous one
            curentInstanceId = selectedInstances[prototypeToChangeId];
            do {
                newInstanceId = randomGenerator.nextInt(size);
            } while (newInstanceId == curentInstanceId);
            //Update information in selectedInstances
            selectedInstances[prototypeToChangeId] = newInstanceId;
            //Update information in kNN
            values = kNN.getSample(prototypeToChangeId);
            example = exampleSet.getExample(newInstanceId);            
            kNN.setSample(prototypeToChangeId, values, StoredValuesHelper.createStoredValue(example));

            //disp(kNN, selectedInstances, exampleSet);
        }
        index.setAllFalse();
        for (int j = 0; j < bestSelectedInstances.length; j++) {
            index.set(bestSelectedInstances[j], true);
        }
        return index;
    }

    private void disp(ISPRGeometricDataCollection<Number> kNN, int[] si, SelectedExampleSet es) {
        for (int i = 0; i < si.length; i++) {
            double[] values = kNN.getSample(i).getValues();
            int in = si[i];
            System.out.println("Indeks = " + in);
            System.out.print(" [");
            
            for (int j = 0; j < values.length; j++) {
                System.out.print(values[j] + " ; ");
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
            System.out.println("Indeks = " + in);
            System.out.print("  " + ids + " [");
            for (int j = 0; j < values.length; j++) {
                System.out.print(values[j] + " ; ");
            }
            System.out.print("] ");
            System.out.println("");
        }
        System.out.println("------------------------------------------------");
    }

}
