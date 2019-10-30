package org.prules.operator.learner.prototype.model;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import org.prules.operator.learner.prototype.PrototypeTuple;
import org.prules.operator.learner.prototype.PrototypesEnsembleModel;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNearestProtoModel {
    //<editor-fold desc="Static data" defaultState="collapsed" >
    private static final String NAME_ATTRIBUTE_ID_1 = "ID_Proto_1";
    private static final String NAME_ATTRIBUTE_ID_2 = "ID_Proto_2";
    private static final String NAME_ATTRIBUTE_ID_PAIR = "ID_Proto_Pair";
    static final Attribute ATTRIBUTE_ID_PROTO_1 = AttributeFactory.createAttribute(NAME_ATTRIBUTE_ID_1, Ontology.NUMERICAL);
    static final Attribute ATTRIBUTE_ID_PROTO_2 = AttributeFactory.createAttribute(NAME_ATTRIBUTE_ID_2, Ontology.NUMERICAL);
    static final Attribute ATTRIBUTE_ID_PAIR = AttributeFactory.createAttribute(NAME_ATTRIBUTE_ID_PAIR, Ontology.POLYNOMINAL);
    //</editor-fold>

    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Distance measure data
     */
    private DistanceMeasure distanceMeasure;
    /**
     * Training data set
     */
    private ExampleSet examples;
    /**
     * Training data set
     */
    private ExampleSet prototypes;
    /**
     * Attributes of training set
     */
    private Attributes attributesExampleSet;

    /**
     * Attributes of prototypes set
     */
    private List<String> prototypeAttributeNames;
    /**
     * Number of label
     */
    private int labelsNum;
    /**
     * Map example to Prototype distance
     */
    private double[][] example2ProtoDistances;

    /**
     * Values of prototypes Values
     */
    private double[][] prototypesAttributes;
    /**
     * Array of labels of prototypes set for faster iteration
     */
    private double[] prototypesLabel;
    /**
     * Array of nearest tuples
     */
    private PrototypeTuple[] examplesNearestTuples;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * Constructor for AbstractNearestProtoModel
     *
     * @param examples      - training examples
     * @param prototypes    - prototype examples
     * @param measureHelper - measure helper for retrieving distances between examples
     * @throws OperatorException - on getInitializedMeasure in measureHelper from examples
     */
    public AbstractNearestProtoModel(ExampleSet examples, ExampleSet prototypes, DistanceMeasureHelper measureHelper) throws OperatorException {
        //Copy data
        this.examples = (ExampleSet) examples.clone();
        this.prototypes = (ExampleSet) prototypes.clone();
        //Get distance measures
        this.distanceMeasure = measureHelper.getInitializedMeasure(this.examples);
    }
    //</editor-fold>

    public void process() {
        setup();
        compute();
        optimize();
    }

    public void setup() {
        //Add attributes to table
        this.examples.getExampleTable().addAttribute(ATTRIBUTE_ID_PROTO_1);
        this.examples.getExampleTable().addAttribute(ATTRIBUTE_ID_PROTO_2);
        this.examples.getExampleTable().addAttribute(ATTRIBUTE_ID_PAIR);
        //Add attributes as Special
        this.examples.getAttributes().setSpecialAttribute(ATTRIBUTE_ID_PROTO_1, NAME_ATTRIBUTE_ID_1);
        this.examples.getAttributes().setSpecialAttribute(ATTRIBUTE_ID_PROTO_2, NAME_ATTRIBUTE_ID_2);
        this.examples.getAttributes().setSpecialAttribute(ATTRIBUTE_ID_PAIR, Attributes.BATCH_NAME);
        //Attributes of prototypes set
        this.attributesExampleSet = this.examples.getAttributes();
        Attributes attributesPrototypes = prototypes.getAttributes();
        //Arrays and maps
        this.labelsNum = this.attributesExampleSet.getLabel().getMapping().size();
        int numberOfAttributesInExampleSet = this.attributesExampleSet.size();
        this.example2ProtoDistances = new double[examples.size()][prototypes.size()];
        //Init table of neatest tuples
        this.examplesNearestTuples = new PrototypeTuple[examples.size()];
        this.prototypesAttributes = new double[prototypes.size()][numberOfAttributesInExampleSet];
        this.prototypesLabel = new double[prototypes.size()];
        this.prototypeAttributeNames = new ArrayList<>(numberOfAttributesInExampleSet);
        //Create list of attributes
        for (Attribute attr : this.attributesExampleSet) {
            this.prototypeAttributeNames.add(attr.getName());
        }
        //For each prototype from the training set convert it into double[],
        // such that the final set of prototypes if double[][]
        int prototypeIndex = 0;
        for (Example prototypeExample : prototypes) {
            double[] prototype = prototypesAttributes[prototypeIndex];
            int i = 0;
            for (String attrName : prototypeAttributeNames) {
                Attribute prototypeAttribute = attributesPrototypes.get(attrName);
                prototype[i++] = prototypeExample.getValue(prototypeAttribute);
            }
            this.prototypesLabel[prototypeIndex] = prototypeExample.getLabel();
            prototypeIndex++;
        }

    }

    //<editor-fold desc="Abstract methods" defaultState="collapsed" >

    /**
     * Computes PrototypeTuples for each Example in exampleSet
     */
    abstract void compute();

    /**
     * Method to optimize computed results
     */
    abstract void optimize();

    /**
     * Method run at the end, delivers computed model
     *
     * @return PrototypesEnsembleModel
     */
    public abstract PrototypesEnsembleModel retrieveModel();

    /**
     * Method to return output set
     *
     * @return ExampleSet
     */
    public abstract ExampleSet retrieveOutputSet();
    //</editor-fold>

    //<editor-fold desc="Package-private getters" defaultState="collapsed" >
    public ExampleSet getExamples() {
        return examples;
    }

    Attributes getAttributesExampleSet() {
        return attributesExampleSet;
    }

    double[][] getPrototypesAttributes() {
        return prototypesAttributes;
    }

    DistanceMeasure getDistanceMeasure() {
        return distanceMeasure;
    }

    double[] getPrototypesLabel() {
        return prototypesLabel;
    }

    int getLabelsNum() {
        return labelsNum;
    }

    PrototypeTuple[] getExamplesNearestTuples() {
        return examplesNearestTuples;
    }

    void setExamplesNearestTuples(PrototypeTuple[] examplesNearestTuples) {
        this.examplesNearestTuples = examplesNearestTuples;
    }

    double[][] getExample2ProtoDistances() {
        return example2ProtoDistances;
    }

    List<String> getPrototypeAttributeNames() {
        return prototypeAttributeNames;
    }
    //</editor-fold>
}
