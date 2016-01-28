/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
//import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.ispr.operator.learner.tools.genetic.RandomGenerator;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;

/**
 * Class implements MonteCarlo instance selection. It simply randomly selects a
 * subset of intances, evaluates its performance on 1NN algorithm, and repeats
 * such procedure for "iterations" times selecting the best subset of instances
 *
 * @author Marcin
 */
public class MCInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int numberOfPrototypes;
    private final RandomGenerator randomGenerator;
    private final int iterations;
    IISDecisionFunction loss;

    /**
     * Constructor of MonteCarlo instance selection. It simply randomly selects
     * a subset of intances, evaluates its performance on 1NN algorithm, and
     * repeats such procedure for "iterations" times selecting the best subset
     * of instances
     *
     * @param measure - distance measure
     * @param populationSize - number of samples to select
     * @param iterations - number of iterations
     * @param randomGenerator - random number generator
     * @param loss - decision function
     */
    public MCInstanceSelectionModel(DistanceMeasure measure, int populationSize, int iterations, RandomGenerator randomGenerator, IISDecisionFunction loss) {
        this.measure = measure;
        this.randomGenerator = randomGenerator;
        this.numberOfPrototypes = populationSize;
        this.iterations = iterations;
        this.loss = loss;
    }

    /**
     * Performs instance selection
     *
     * @param inputExampleSet - example set for which instance selection will be
     * performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet;
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        loss.init(exampleSet, measure);
        int size = exampleSet.size();
        EditedExampleSet workingSet = new EditedExampleSet(exampleSet);
        DataIndex indexWorking = workingSet.getIndex();

        Attributes attributes = exampleSet.getAttributes();
        double[] values = new double[attributes.size()];
        double errorRateBest = Double.MAX_VALUE;
        DataIndex bestIndex = null;
        for (int i = 0; i < iterations; i++) {
            indexWorking.setAllFalse();
            for (int j = 0; j < numberOfPrototypes; j++) {
                int id = randomGenerator.nextInteger(size);
                indexWorking.set(id, true);
            }

            double errorRate = 0;
            ISPRGeometricDataCollection<Number> kNN = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, workingSet, measure);
            //GeometricDataCollection<Integer> kNN = KNNTools.initializeKNearestNeighbour(workingSet, measure);
            for (Example ex : exampleSet) {
                KNNTools.extractExampleValues(ex, values);
                //loss.setPredictedLabel(KNNTools.predictOneNearestNeighbor(exampleSet, values, measure));
                double predictedLabel = KNNTools.predictOneNearestNeighbor(ex, kNN);
                double realLabel = ex.getLabel();
                errorRate += loss.getValue(realLabel, predictedLabel, ex);
                //acc += KNNTools.predictOneNearestNeighbor(exampleSet, values, measure) == ex.getLabel() ? 1 : 0;
                //acc += KNNTools.predictNearestNeighbor(exampleSet, values, ex.getLabel(), measure) ? 1 : 0;
            }
            if (errorRate < errorRateBest) {
                errorRateBest = errorRate;
                bestIndex = new DataIndex(indexWorking);
            }
        }
        if (bestIndex == null) {
            throw new NullPointerException("Something went wrong, please check the number of iterations or other parameters");
        }
        return bestIndex;
    }

}
