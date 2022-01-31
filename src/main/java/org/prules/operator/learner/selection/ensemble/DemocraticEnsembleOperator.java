package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.*;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.RandomGenerator;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PRulesUtil;

import java.util.Arrays;
import java.util.List;

public class DemocraticEnsembleOperator extends AbstractISEnsembleOperator {
    public static final String PARAMETER_BINS = "Number of bins";
    public static final String PARAMETER_DEBUG = "Debug mode";
    int numberOfProjections = 5;
    int numberOfBins = 10;
    int currentBin = 0;
    double[] projection;
    List<Integer>[] bins;
    boolean doProjection = true;
    RandomGenerator randomGenerator;
    int currentIteration;
    int maxIterations;
    boolean debug = false;

    public DemocraticEnsembleOperator(OperatorDescription description) {
        super(description);
    }

    /**
     * This method is used to initialize method called processExamples. It is
     * called before the main loop of iterations starts. It is used for example
     * to initialize SplittedExampleSet etc.
     *
     * @param examploeSet
     * @throws com.rapidminer.operator.OperatorException
     */
    protected void initializeProcessExamples(ExampleSet examploeSet) throws OperatorException {
        randomGenerator = RandomGenerator.getRandomGenerator(this);
        numberOfProjections = getMaxIterations();
        numberOfBins = getParameterAsInt(PARAMETER_BINS);
        currentIteration = 0;
        currentBin = 0;
        maxIterations = numberOfBins * numberOfProjections;
        debug = getParameterAsBoolean(PARAMETER_DEBUG);
    }

    /**
     * Method responsible for preparing dataset used by processExample method.
     * This method is called in each iteration of the processExample method and
     * is responsible for diversity of the datasets used in the subprocess
     *
     * @param trainingSet
     * @return
     * @throws com.rapidminer.operator.OperatorException
     */
    protected ExampleSet preprocessingMainLoop(ExampleSet trainingSet) throws OperatorException {
        ExampleSet exampleSet = null;
        if (debug)
            LogService.getRoot().fine("Iteration: " + currentIteration);
        boolean check = false;
        do {
            if (check && debug)
                LogService.getRoot().fine("     Iteration: " + currentIteration + " Repeating subset search: all samples are from single class");
            exampleSet = generateNewSubset(trainingSet);
            currentBin++;
            if (currentBin >= numberOfBins) {
                currentBin = 0;
                doProjection = true;
            }
            currentIteration++;
            check = true;
        } while (PRulesUtil.isSingleLabel(exampleSet) && isNextIteration()); //If dataset consists of only single label then repeat with new subset,
        //or if we are out of projections and bins, then also break;
        return exampleSet;
    }

    @Override
    public boolean isNextIteration() {
        return currentIteration < maxIterations;
    }

    private ExampleSet generateNewSubset(ExampleSet trainingSet) {
        if (doProjection) {
            doProjection = false;
            int size = trainingSet.getAttributes().size();
            double[] vector = generateRandomVector(size);
            projection = PRulesUtil.project(trainingSet, vector);
            if (debug)
                LogService.getRoot().fine("     Iteration: " + currentIteration + " Projection params: " + Arrays.toString(vector));
            bins = PRulesUtil.discretizeFastEqFrequency(projection, numberOfBins);
        }
        SelectedExampleSet exampleSet = new SelectedExampleSet(trainingSet);
        IDataIndex index = exampleSet.getIndex();
        index.setAllFalse();
        bins[currentBin].forEach(i -> index.set(i, true));
        exampleSet.setIndex(index);
        return exampleSet;
    }

    private double[] generateRandomVector(int size) {
        double[] vector = new double[size];
        for (int i = 0; i < size; i++) {
            vector[i] = randomGenerator.nextDouble();
        }
        return vector;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_BINS, "Number of bins", 1, 10000, 10);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeBoolean(PARAMETER_DEBUG, "Debug mode", false);
        type.setExpert(true);
        types.add(type);
        return types;
    }
}
