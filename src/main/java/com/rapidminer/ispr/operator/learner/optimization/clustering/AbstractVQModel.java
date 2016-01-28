package com.rapidminer.ispr.operator.learner.optimization.clustering;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.PRulesModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base model for any Vector Quantization models. It extends PRulesModel, and implements basics of any VQ algorithm
 * It is used to extract data from the input example set to speedup the calculations. The class extracts current example and store
 * it as both object of Example type and as double[] - exampleValues. Similarly it provides number of attributes and number of 
 * prototypes, as well as prototypes represented as double[][] matrix
 * @author Marcin
 */
public abstract class AbstractVQModel implements PRulesModel<ExampleSet> {

    ExampleSet prototypes; //Set of prototypes
    private Example example; //Current example from the example set
    double[] exampleValues; //double values retrived from the example set
    Attributes prototypeAttributes; //List of attributes
    List<Attribute> trainingAttributes; //List of attrubutes
    private int attributesSize, numberOfPrototypes; //Number of prototypes and number of attributes
    double[][] prototypeValues; //matrix representing prototypes position. It if finally converted into prototypes

    /**
     * Constructor which requires initialization of prototypes/codebooks
     * @param prototypes
     */
    public AbstractVQModel(ExampleSet prototypes) {
        prototypeAttributes = prototypes.getAttributes();
        this.prototypes = prototypes;
        attributesSize = prototypeAttributes.size();
        numberOfPrototypes = prototypes.size();
    }

    /**
     * Execute the training process with training data as an input
     * @param trainingSet - input training data
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet trainingSet) {
        Attributes tmpTrainingAttributes = trainingSet.getAttributes();
        trainingAttributes = new ArrayList<Attribute>(tmpTrainingAttributes.size());
        //Caching codebooks for faster optimization
        prototypeValues = new double[numberOfPrototypes][attributesSize];
        int i = 0, j = 0;
        for (Example p : prototypes) {
            j = 0;
            for (Attribute a : prototypeAttributes) {
                prototypeValues[i][j] = p.getValue(a);
                j++;
            }
            i++;
        }
        //Reordering attributes for 
        for (Attribute a : prototypeAttributes) {
            trainingAttributes.add(tmpTrainingAttributes.get(a.getName()));
        }
        exampleValues = new double[prototypeAttributes.size()];
        do {
            for (Example trainingExample : trainingSet) {
                this.example = trainingExample;
                j = 0;
                for (Attribute attribute : trainingAttributes) {
                    exampleValues[j] = trainingExample.getValue(attribute);
                    j++;
                }
                update();
            }
        } while (nextIteration());

        i = 0;
        for (Example p : prototypes) {
            j = 0;
            for (Attribute a : prototypeAttributes) {
                p.setValue(a, prototypeValues[i][j]);
                j++;
            }
            i++;
        }
        return prototypes;
    }

    /**
     * Returns current example from the training set 
     * @return
     */
    protected Example getCurrentExample() {
        return example;
    }

    /**
     * Returns current example in the form from the training set 
     * @return
     */
    protected double[] getCurrentExampleValues() {
        return exampleValues;
    }

    /**
     * Returns the number of attributes
     * @return
     */
    protected int getAttributesSize() {
        return attributesSize;
    }

    /**
     * Returns the number of prototypes
     * @return
     */
    protected int getNumberOfPrototypes() {
        return numberOfPrototypes;
    }

    /**
     * returns a row vector (double[]) of i'th prototype values
     * @param i
     * @return
     */
    protected double[] getPrototypeValues(int i) {
        return prototypeValues[i];
    }

    /**
     * The VQ algorithm is executed in the main loop and in each iteration of the main loop it iterates over an entry example set. 
     * This method is used to check if new main iteration should be executed. It allows to check whether the number of iterations has expired 
     * or appropriate convergence has been achieved. When it return true then next main iteration will be executed, if false then the algorithm
     * stops and returns prototypes. In the body of this method also adaptation parameter alpha can be updated (reduced)
     * @return true if next iteration should be performed
     */
    public abstract boolean nextIteration();

    /**
     * called for every new instance when iteration over examples from example set. In the body of this method the codebook update rule 
     * should be performed
     */
    public abstract void update();

    /**
     *This is an extra method which can be used in the body of nextIteration method to perform learning rate update.
     * @param learningRate
     * @param currentIteration
     * @param iterations
     * @param initialLearningRate
     * @return
     */
    public static double learingRateUpdateRule(double learningRate, int currentIteration, int iterations, double initialLearningRate) {
        //learningRate = learningRate * Math.exp(-(double)currentIteration/(double)iterations);
        learningRate = learningRate / (1 + Math.sqrt(iterations) / (double) iterations + learningRate);
        return learningRate;
    }
}