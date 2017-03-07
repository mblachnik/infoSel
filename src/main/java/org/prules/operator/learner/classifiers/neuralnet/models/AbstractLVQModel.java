package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.operator.learner.PRulesModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Marcin
 */
public abstract class AbstractLVQModel implements PRulesModel<ExampleSet>{

    ExampleSet prototypes;
    private Example example; //Current example from the example set
    double[] exampleValues; //double values retrived from the example set
    double exampleLabel;
    Attributes prototypeAttributes; //List of attributes
    List<Attribute> trainingAttributes;
    private final int attributesSize, numberOfPrototypes; //Number of prototypes and number of attributes
    double[][] prototypeValues;
    double[] prototypeLabels;

    /**
     * 
     * @param prototypes
     */
    public AbstractLVQModel(ExampleSet prototypes) {
        prototypeAttributes = prototypes.getAttributes();
        this.prototypes = prototypes;
        attributesSize = prototypeAttributes.size();
        numberOfPrototypes = prototypes.size();
    }

    /**
     * 
     * @param trainingSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet trainingSet) {
        Attributes tmpTrainingAttributes = trainingSet.getAttributes();
        trainingAttributes = new ArrayList<Attribute>(tmpTrainingAttributes.size());
        //Caching codebooks for faster optimization
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
     * 
     * @return
     */
    protected Example getCurrentExample() {
        return example;
    }

    /**
     * 
     * @return
     */
    protected double[] getCurrentExampleValues() {
        return exampleValues;
    }

    /**
     * 
     * @return
     */
    protected int getAttributesSize() {
        return attributesSize;
    }

    /**
     * 
     * @return
     */
    protected int getNumberOfPrototypes() {
        return numberOfPrototypes;
    }

    /**
     * 
     * @param i
     * @return
     */
    protected double[] getPrototypeValues(int i) {
        return prototypeValues[i];
    }

    /**
     * 
     * @param i
     * @return
     */
    protected double getPrototypeLabel(int i) {
        return prototypeLabels[i];
    }

    /**
     * 
     * @return
     */
    abstract boolean nextIteration();

    /**
     * 
     */
    abstract void update();

}