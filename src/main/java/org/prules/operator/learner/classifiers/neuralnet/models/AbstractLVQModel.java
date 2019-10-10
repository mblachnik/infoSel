package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.operator.learner.PRulesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin
 */
public abstract class AbstractLVQModel implements PRulesModel<ExampleSet> {

    public static final String LEARNING_RATE_KEY = "LearningRate";
    public static final String LAMBDA_RATE_KEY = "LambdaRate";

    private ExampleSet prototypes;
    private Example example; //Current example from the example set
    double[] exampleValues; //double values retrieved from the example set
    double exampleLabel; //Current label
    private Attributes prototypeAttributes; //List of attributes
    List<Attribute> trainingAttributes;
    private final int attributesSize, numberOfPrototypes; //Number of prototypes and number of attributes
    double[][] prototypeValues;
    double[] prototypeLabels;
    private Map<String, Object> storedValues;

    /**
     * @param prototypes
     */
    public AbstractLVQModel(ExampleSet prototypes) {
        prototypeAttributes = prototypes.getAttributes();
        this.prototypes = prototypes;
        attributesSize = prototypeAttributes.size();
        numberOfPrototypes = prototypes.size();
        storedValues = new HashMap<>();
    }

    /**
     * @param trainingSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet trainingSet) {
        Attributes tmpTrainingAttributes = trainingSet.getAttributes();
        trainingAttributes = new ArrayList<>(tmpTrainingAttributes.size());
        //Caching codeBooks for faster optimization
        prototypeValues = new double[numberOfPrototypes][attributesSize];
        prototypeLabels = new double[numberOfPrototypes];
        int i = 0, j = 0;
        for (Example p : prototypes) {
            j = 0;
            for (Attribute a : prototypeAttributes) {
                prototypeValues[i][j] = p.getValue(a);
                j++;
            }
            prototypeLabels[i] = p.getLabel();
            i++;
        }
        //Reordering attributes for 
        for (Attribute a : prototypeAttributes) {
            trainingAttributes.add(tmpTrainingAttributes.get(a.getName()));
        }
        beforeTraining(trainingSet);

        exampleValues = new double[prototypeAttributes.size()];
        do {
            for (Example trainingExample : trainingSet) {
                this.example = trainingExample;
                j = 0;
                for (Attribute attribute : trainingAttributes) {
                    exampleValues[j] = trainingExample.getValue(attribute);
                    j++;
                }
                exampleLabel = trainingExample.getLabel();
                update();
            }
        } while (nextIteration(trainingSet));

        afterTraining(trainingSet);

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
     * Returns total number of iterations (maximum number of iterations)
     *
     * @return
     */
    abstract public int getMaxIterations();

    /**
     * Returns current iteration
     *
     * @return
     */
    abstract public int getIteration();

    /**
     * Returns the value of cost function
     *
     * @return
     */
    abstract public double getCostFunctionValue();

    /**
     * Returns list of cost function values
     *
     * @return
     */
    abstract public List<Double> getCostFunctionValues();

    /**
     * @return
     */
    protected Example getCurrentExample() {
        return example;
    }

    /**
     * @return
     */
    protected double[] getCurrentExampleValues() {
        return exampleValues;
    }

    /**
     * @return
     */
    protected int getAttributesSize() {
        return attributesSize;
    }

    /**
     * @return
     */
    protected int getNumberOfPrototypes() {
        return numberOfPrototypes;
    }

    /**
     * @param i
     * @return
     */
    protected double[] getPrototypeValues(int i) {
        return prototypeValues[i];
    }

    /**
     * @param i
     * @return
     */
    protected double getPrototypeLabel(int i) {
        return prototypeLabels[i];
    }

    /**
     * @return
     */
    abstract boolean nextIteration(ExampleSet trainingSet);

    /**
     * Method executed before the training starts.
     *
     * @param trainingSet
     */
    public void beforeTraining(ExampleSet trainingSet) {
    }

    /**
     * Method executed then the main loop of the LVQ algorithm is finished.
     *
     * @param trainingSet
     */
    public void afterTraining(ExampleSet trainingSet) {
    }

    /**
     *
     */
    abstract void update();

    public final Object getStoredValue(String key) {
        return storedValues.get(key);
    }

    final void addStoredValue(String key, Object value) {
        storedValues.put(key, value);
    }
}
