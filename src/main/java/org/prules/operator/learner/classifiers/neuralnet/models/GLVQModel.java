package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.tools.math.BasicMath;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Marcin
 */
public class GLVQModel extends AbstractLVQModel {

    private final DistanceMeasure measure; //Distance measure
    private final int iterations; //TOtal number of iterations
    private int currentIteration; //Iteration id
    private double alpha; //Learning rate    
    private double time; //Time used in sigmoidal cost function evaluation
    private final double initialAlpha; // Initial value of alpha, here alpha is not changing    
    private final double timeMax = 10;
    private final double timeMin = 1;
    private final boolean debug; //In debug mode the true cost function is calculated
    private double costValue; //The value of the cost function
    private final List<Double> costValues; //List of cost Function values
    private final List<Double> learningRateValues; //List of learning rate values
    private final List<Double> timeRateValues; //List of time (lambda) rate values
    private final LearningRateUpdateRule learningRateUpdateRule; //THe update rule of the learning rate
    private int numberOfUpdates; //The number of times the update function was executed before nextIteration was executed
    private double tmpFactor = 0;
    /**
     *
     * @param prototypes
     * @param iterations
     * @param measure
     * @param alpha
     * @param debug - wether to use debug mode. In debug mode the algorithm also calculates the cost function which can be time consuming
     * @throws OperatorException
     */
    public GLVQModel(ExampleSet prototypes, int iterations,
            DistanceMeasure measure, double alpha) throws OperatorException {
        super(prototypes);
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alpha = alpha;
        this.time = 1;
        this.initialAlpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);        
        this.costValues = new ArrayList<>(iterations);
        this.debug = false;
        learningRateUpdateRule = new HyperbolicLearningRateUpdateRule();
        learningRateValues = new ArrayList<>(iterations);
        timeRateValues = new ArrayList<>(iterations);
        addStoredValue(LEARNING_RATE_KEY, learningRateValues);
        addStoredValue(LAMBDA_RATE_KEY, timeRateValues);        
    }

    /**
     *
     */
    @Override
    public void update() {

        double dist, minDistCorrect = Double.MAX_VALUE, minDistIncorrect = Double.MAX_VALUE;
        int selectedPrototypeCorrect = 0;
        int selectedPrototypeIncorrect = 0;
        int i = 0;

        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            double protoLabel = prototypeLabels[i];
            if (dist < minDistCorrect && exampleLabel == protoLabel) {
                minDistCorrect = dist;
                selectedPrototypeCorrect = i;
            }
            if (dist < minDistIncorrect && exampleLabel != protoLabel) {
                minDistIncorrect = dist;
                selectedPrototypeIncorrect = i;
            }
            i++;
        }

        double denominator = minDistCorrect + minDistIncorrect;
        denominator = denominator == 0 ? 1e-10 : denominator;
        double mu = (minDistCorrect - minDistIncorrect) / denominator;
        double currentCostValue = BasicMath.sigmoid(mu * time);
        costValue += currentCostValue;
        
        double dif_df_mu = currentCostValue * time * (1 - currentCostValue);
        double factorCorrect = minDistIncorrect / (denominator * denominator);
        double factorIncorrect = minDistCorrect / (denominator * denominator);
        tmpFactor += 2 * alpha * dif_df_mu * factorCorrect;
        for (i = 0; i < getAttributesSize(); i++) {
            double trainValue = exampleValues[i];
            double valueCorrect = prototypeValues[selectedPrototypeCorrect][i];
            double valueIncorrect = prototypeValues[selectedPrototypeIncorrect][i];
            valueCorrect += 2 * alpha * dif_df_mu * factorCorrect * (trainValue - valueCorrect);
            valueIncorrect -= 2 * alpha * dif_df_mu * factorIncorrect * (trainValue - valueIncorrect);
            prototypeValues[selectedPrototypeCorrect][i] = valueCorrect;
            prototypeValues[selectedPrototypeIncorrect][i] = valueIncorrect;
        }
        numberOfUpdates++;
    }

    /**
     * Returns true if the algorithm should perform the next iteration step
     *
     * @return
     */
    @Override
    public boolean nextIteration(ExampleSet trainingSet) {
        currentIteration++;
        //time *= 1.1;
        
        if (debug) {
            costValues.add(calcCostFunction(trainingSet));
        } else {
            costValues.add(costValue/numberOfUpdates);
            timeRateValues.add(tmpFactor/numberOfUpdates);
            learningRateValues.add(alpha);        
        }
        costValue = 0;
        numberOfUpdates = 0;
        tmpFactor = 0;
        time = Math.min(time + (timeMax - timeMin) / iterations, timeMax);
        alpha = learningRateUpdateRule.update(alpha);
        return currentIteration < iterations;
    }

    /**
     * Cost function. Note that the cost normally the cost function should be executed in each iteration, 
     * but this is very time consuming because we have to two times iterate over the dataset. 
     * Normally instead we use cost function value calculated trivially but it 
     * doesn't give us the true value of the cost function because the prototypes 
     * position changes after presenting each training sample (because of SGD)
     * This is the true cost function
     * @param trainingSet
     * @return 
     */
    private double calcCostFunction(ExampleSet trainingSet) {
        double[] tmpExampleValues = new double[getAttributesSize()];
        double tmpExampleLabel;
        double costValue = 0;
        for (Example e : trainingSet) {
            int j = 0;
            for (Attribute attribute : trainingAttributes) {
                tmpExampleValues[j] = e.getValue(attribute);
                j++;
            }
            tmpExampleLabel = e.getLabel();
            double dist, minDistCorrect = Double.MAX_VALUE, minDistIncorrect = Double.MAX_VALUE;
            int i = 0;
            for (double[] prototype : prototypeValues) {
                dist = measure.calculateDistance(prototype, tmpExampleValues);
                double protoLabel = prototypeLabels[i];
                if (dist < minDistCorrect && tmpExampleLabel == protoLabel) {
                    minDistCorrect = dist;
                }
                if (dist < minDistIncorrect && tmpExampleLabel != protoLabel) {
                    minDistIncorrect = dist;
                }
                i++;
            }

            double denominator = minDistCorrect + minDistIncorrect;
            denominator = denominator == 0 ? 1e-10 : denominator;
            double mu = (minDistCorrect - minDistIncorrect) / denominator;
            costValue += BasicMath.sigmoid(mu * time);
        }
        return costValue/trainingSet.size();
    }

    /**
     * Returns total number of iterations (maximum number of iterations)
     *
     * @return
     */
    @Override
    public int getMaxIterations() {
        return iterations;
    }

    /**
     * Returns current iteration
     *
     * @return
     */
    @Override
    public int getIteration() {
        return currentIteration;
    }

    /**
     * Returns the value of cost function
     *
     * @return
     */
    @Override
    public double getCostFunctionValue() {
        if (costValues.size()>0)
            return costValues.get(costValues.size()-1);
        return -1;
    }

    /**
     * Returns list of cost function values
     *
     * @return
     */
    @Override
    public List<Double> getCostFunctionValues() {
        return costValues;
    }
}
