package org.prules.operator.learner.prototype;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.tools.math.BasicMath;

import java.util.*;
import java.util.Map.Entry;

/**
 * The class implements NearestPrototypeBatchOperator. It takes the prototypes
 * and for each training vector it identifies two closest prototypes from opposite classes.
 * This allows to mark all training vectors with appropriate pair of prototypes.
 * This allows to split the training data along the decision boundary. As an output it adds
 * three new attributes to the training data: batch which indicates data subsets
 * which belong to a single pair, and two additional attributes ID_Proto_1 and ID_Proto_2
 * which indicates respectively index of closest prototype from same class label and opposite
 * class label respectively. This operator also returns PrototypeEnsembleModel which
 * contains information on prototypes and its labels and additional information require to finally build ensemble etc.
 *
 * @author Marcin, Paweł
 */
public class NearestPrototypesOperator extends Operator implements CapabilityProvider {
    //<editor-fold desc="Static data" defaultState="collapsed" >
    private static final String PARAMETER_MIN_COUNT_FACTOR = "Min. counts factor";
    private static final String PARAMETER_MINIMUM_SUPPORT = "Min. support";
    private static final String NAME_ATTRIBUTE_ID_1 = "ID_Proto_1";
    private static final String NAME_ATTRIBUTE_ID_2 = "ID_Proto_2";
    private static final String NAME_ATTRIBUTE_ID_PAIR = "ID_Proto_Pair";
    private static final String PORT_INPUT_EXAMPLE = "example set";
    private static final String PORT_INPUT_PROTOTYPES = "prototypes";
    static final String PORT_OUTPUT_PROTOTYPES = "example set";
    static final String PORT_OUTPUT_TUPLES = "tuplesModel";
    private static final Attribute ATTRIBUTE_ID_PROTO_1 = AttributeFactory.createAttribute(NAME_ATTRIBUTE_ID_1, Ontology.NUMERICAL);
    private static final Attribute ATTRIBUTE_ID_PROTO_2 = AttributeFactory.createAttribute(NAME_ATTRIBUTE_ID_2, Ontology.NUMERICAL);
    private static final Attribute ATTRIBUTE_ID_PAIR = AttributeFactory.createAttribute(NAME_ATTRIBUTE_ID_PAIR, Ontology.POLYNOMINAL);
    private static final String FACTOR_DESCRIPTION = "Factor indicating minimum number of instances in a single batch. It is multiplied by the max counts.";
    private static final String MINIMUM_NUMBER_SUPPORT_DESCRIPTION = "Minimum number of samples in a single batch. It it has lower number of samples it will be removed and the samples will be redistributed into another batches";
    //</editor-fold>

    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Input data - training set
     */
    private final InputPort exampleSetInputPort = this.getInputPorts().createPort(PORT_INPUT_EXAMPLE);
    /**
     * Input data - prototypes
     */
    private final InputPort prototypesInputPort = this.getInputPorts().createPort(PORT_INPUT_PROTOTYPES);
    /**
     * example set with three additional attributes as described in class description
     */
    private final OutputPort exampleSetOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_PROTOTYPES);
    /**
     * Model representing prototypes and its relations
     */
    private final OutputPort modelOutputPort = this.getOutputPorts().createPort(PORT_OUTPUT_TUPLES);
    /**
     * Distance measure helper for creating appropriate distance measure
     */
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
    /**
     * Factor of minimum data count of batch
     */
    private double minFactor;
    /**
     * Minimum data count of batch
     */
    private int minSupport;
    /**
     * Training data set
     */
    private ExampleSet exampleSet;
    /**
     * Distance measure data
     */
    private DistanceMeasure distanceMeasure;
    /**
     * Attributes of training set
     */
    private Attributes attributesExampleSet;
    /**
     * Counter used to count frequency of prototypes pair
     */
    private HashMap<Long, int[]> countersMap;
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
     * Array of labels of training set for faster iteration
     */
    private double[] labels;
    /**
     * Values of prototypes Values
     */
    private double[][] prototypes;
    /**
     * Array of labels of prototypes set for faster iteration
     */
    private double[] prototypesLabel;
    /**
     * Array of nearest tuples
     */
    private PrototypeTuple[] examplesNearestTuples;
    /**
     * Map of unique prototype tuples
     */
    private Map<Long, PrototypeTuple> selectedTuples;
    /**
     * Size of biggest prototype tuple
     */
    private int biggestSize;
    /**
     * Id of smallest prototype tuple
     */
    private long smallestSizeId;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * <p>
     * Creates a  NearestPrototypeBatch operator. S
     * </p>
     * <p>
     * NOTE: the preferred way for operator creation is using one of the factory
     * methods of {@link OperatorService}.
     * </p>
     *
     * @param description Operator description
     */
    public NearestPrototypesOperator(OperatorDescription description) {
        super(description);
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
            try {
                measureType = measureHelper.getSelectedMeasureType();
            } catch (UndefinedParameterError ignored) {
            }
            switch (capability) {
                case BINOMINAL_ATTRIBUTES:
                case POLYNOMINAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
                case NUMERICAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                            || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
                case MISSING_VALUES:
                case BINOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, exampleSetInputPort));
        prototypesInputPort.addPrecondition(new DistanceMeasurePrecondition(prototypesInputPort, this));
        prototypesInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
            try {
                measureType = measureHelper.getSelectedMeasureType();
            } catch (UndefinedParameterError ignored) {
            }
            switch (capability) {
                case BINOMINAL_ATTRIBUTES:
                case POLYNOMINAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
                case NUMERICAL_ATTRIBUTES:
                    return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                            || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                            || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
                case MISSING_VALUES:
                case BINOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, prototypesInputPort));
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);
        getTransformer().addGenerationRule(modelOutputPort, PrototypesEnsembleModel.class);
    }
    //</editor-fold>

    // <editor-fold desc="Set up stage" defaultState="collapsed" >

    /**
     * Sets up configuration variables for process computation
     *
     * @throws OperatorException
     */
    private void setup() throws OperatorException {
        minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        //base examples
        exampleSet = this.exampleSetInputPort.getDataOrNull(ExampleSet.class);
        //Prototypes data set
        ExampleSet prototypeSet = this.prototypesInputPort.getDataOrNull(ExampleSet.class);
        exampleSet = (ExampleSet) exampleSet.clone();
        //Add attributes to table
        exampleSet.getExampleTable().addAttribute(ATTRIBUTE_ID_PROTO_1);
        exampleSet.getExampleTable().addAttribute(ATTRIBUTE_ID_PROTO_2);
        exampleSet.getExampleTable().addAttribute(ATTRIBUTE_ID_PAIR);
        //Add attributes as Special
        exampleSet.getAttributes().setSpecialAttribute(ATTRIBUTE_ID_PROTO_1, NAME_ATTRIBUTE_ID_1);
        exampleSet.getAttributes().setSpecialAttribute(ATTRIBUTE_ID_PROTO_2, NAME_ATTRIBUTE_ID_2);
        exampleSet.getAttributes().setSpecialAttribute(ATTRIBUTE_ID_PAIR, Attributes.BATCH_NAME);
        //Get distance measures
        distanceMeasure = measureHelper.getInitializedMeasure(exampleSet);
        //Attributes of prototypes set
        attributesExampleSet = exampleSet.getAttributes();
        Attributes attributesPrototypes = prototypeSet.getAttributes();
        //Arrays and maps
        labelsNum = attributesExampleSet.getLabel().getMapping().size();
        int numberOfAttributesInExampleSet = attributesExampleSet.size();
        example2ProtoDistances = new double[exampleSet.size()][prototypeSet.size()];
        //Init table of neatest tuples
        examplesNearestTuples = new PrototypeTuple[exampleSet.size()];
        labels = new double[exampleSet.size()];
        prototypes = new double[prototypeSet.size()][numberOfAttributesInExampleSet];
        prototypesLabel = new double[prototypeSet.size()];
        prototypeAttributeNames = new ArrayList<>(numberOfAttributesInExampleSet);
        //Create list of attributes
        for (Attribute attr : attributesExampleSet) {
            prototypeAttributeNames.add(attr.getName());
        }
        //For each prototype from the training set convert it into double[],
        // such that the final set of prototypes if double[][]
        int prototypeIndex = 0;
        for (Example prototypeExample : prototypeSet) {
            double[] prototype = prototypes[prototypeIndex];
            int i = 0;
            for (String attrName : prototypeAttributeNames) {
                Attribute prototypeAttribute = attributesPrototypes.get(attrName);
                prototype[i++] = prototypeExample.getValue(prototypeAttribute);
            }
            prototypesLabel[prototypeIndex] = prototypeExample.getLabel();
            prototypeIndex++;
        }
        //Init map of counters
        countersMap = new HashMap<>();
        //Init map of unique tuple
        selectedTuples = new HashMap<>();
    }
    //</editor-fold>

    //<editor-fold desc="Compute stage" defaultState="collapsed" >

    /**
     * Computes PrototypeTuples for each Example in exampleSet
     */
    private void compute() {
        int exampleIndex = 0;
        for (Example example : exampleSet) {
            double[] valuesExample = new double[attributesExampleSet.size()];
            {
                int i = 0;
                for (Attribute attrName : attributesExampleSet) {
                    valuesExample[i++] = example.getValue(attrName);
                }
            }
            PrototypeTuple prototypeTuple = createPrototypeTuple(exampleIndex, example, valuesExample);
            calculateTupleDist(exampleIndex, prototypeTuple);
            labels[exampleIndex] = example.getLabel();
            exampleIndex++;
        }
    }

    /**
     * Method to calculate for each prototype distances to example and store it in example2ProtoDistances matrix
     *
     * @param exampleIndex  Id of training element for what we compute {@link PrototypeTuple}
     * @param example       Training element for what we compute {@link PrototypeTuple}
     * @param valuesExample Values of example Values
     * @return {@link PrototypeTuple}
     */
    private PrototypeTuple createPrototypeTuple(int exampleIndex, Example example, double[] valuesExample) {
        double[] prototypesDistances = example2ProtoDistances[exampleIndex];
        double minDistanceNeighbor = Double.MAX_VALUE;
        double minDistanceEnemy = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        int prototypeIndex = 0;
        int neighborId = -1;
        int enemyId = -1;

        for (double[] prototype : prototypes) {
            //Get distance
            double currentDistance = distanceMeasure.calculateDistance(valuesExample, prototype);
            //Store distances
            prototypesDistances[prototypeIndex] = currentDistance;
            if (prototypesLabel[prototypeIndex] == example.getLabel()) {
                if (currentDistance < minDistanceNeighbor) {
                    neighborId = prototypeIndex;
                    minDistanceNeighbor = currentDistance;
                }
            } else {
                if (currentDistance < minDistanceEnemy) {
                    enemyId = prototypeIndex;
                    minDistanceEnemy = currentDistance;
                }
            }
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
            }
            prototypeIndex++;
        }
        return new PrototypeTuple(neighborId, enemyId);
    }

    /**
     * Method to calculate counters for created  {@link PrototypeTuple}
     *
     * @param exampleIndex   Index of training element
     * @param prototypeTuple Tuple to be counted
     */
    private void calculateTupleDist(int exampleIndex, PrototypeTuple prototypeTuple) {
        int[] counter = countersMap.get(prototypeTuple.getPairId());
        if (counter == null) {
            counter = new int[labelsNum];
        }
        int label = (int) labels[exampleIndex];
        counter[label]++;
        countersMap.put(prototypeTuple.getPairId(), counter);
        if (examplesNearestTuples[exampleIndex] != null) {
            examplesNearestTuples[exampleIndex].set(prototypeTuple);
        } else {
            examplesNearestTuples[exampleIndex] = new PrototypeTuple(prototypeTuple);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Optimize stage" defaultState="collapsed" >

    /**
     * Method to optimize computed results
     */
    private void optimize() {
        int min = computeDistribution();
        //If minFactor is less restrictive then take minSupport (always take more restrictive rule)
        int minCounts = Math.max((int) (minFactor * biggestSize), minSupport);
        createUniqueTupleMap();

        while (min < minCounts) {
            int exampleIndex = 0;
            //For each training sample check if it belongs to least frequent pair, then update pair
            for (PrototypeTuple tuple : examplesNearestTuples) {
                //If sample with given exampleId belongs to the least frequent batch group,
                // then reassign it to other already existing group
                if (tuple.getPairId() == smallestSizeId) {
                    //If old counter is almost empty (has less then one element remove key)
                    int[] counter = countersMap.get(tuple.getPairId());
                    int label = (int) labels[exampleIndex];
                    //Get hits count for given sample. New we have a distribution of samples per class
                    if (Arrays.stream(counter).sum() <= 1) {
                        countersMap.remove(tuple.getPairId());
                    } else { //Otherwise decrease counter
                        counter[label]--;
                    }

                    double[] protoDistances = example2ProtoDistances[exampleIndex];
                    double minDist = Double.MAX_VALUE;
                    PrototypeTuple tmpTuple = new PrototypeTuple();
                    //Identify two closest prototypes from existing pairs (new group/pair is not constructed)
                    for (Entry<Long, PrototypeTuple> entry : selectedTuples.entrySet()) {
                        if (entry.getKey() != smallestSizeId) {
                            PrototypeTuple prototypeTuple = entry.getValue();
                            double dist = protoDistances[prototypeTuple.getPrototypeId1()] + protoDistances[prototypeTuple.getPrototypeId2()];
                            if (dist < minDist) {
                                minDist = dist;
                                tmpTuple.set(prototypeTuple);
                            }
                        }
                    }
                    //Increase counter of a new pair
                    tuple.set(tmpTuple);
                    counter = countersMap.get(tmpTuple.getPairId());
                    counter[label]++;
                    countersMap.put(tmpTuple.getPairId(), counter);
                }
                exampleIndex++;
            }
            selectedTuples.remove(smallestSizeId);

            //Again determine minID and min counts.
            // Warning if we find a subset of examples which all belong to a single class we keep that subset
            // and store it in pureSubset set. In later stage if any example falls into this pair then they
            // will be automatically classified to the majority class.
            min = computeDistribution();
        }
    }

    /**
     * Method creates unique pairs map which contains unique pairs and corresponding prototype IDs
     */
    private void createUniqueTupleMap() {
        for (PrototypeTuple tuple : examplesNearestTuples) {
            //If given pair does not exists in the map put it
            if (!selectedTuples.containsKey(tuple.getPairId())) {
                selectedTuples.put(tuple.getPairId(), new PrototypeTuple(tuple));
            }
        }
    }

    /**
     * Method to Determine maximum value of the counter, minimum value and index of the minimum value.
     * Also examples which belong to a batch which is clear (all samples belong to a single class)
     * are added to pureSet variable.
     *
     * @return int minimal size
     */
    private int computeDistribution() {
        //The the minimum available number of counts for given pair of prototypes
        int min = Integer.MAX_VALUE;
        smallestSizeId = -1;
        biggestSize = -1;
        for (Entry<Long, int[]> e : countersMap.entrySet()) {
            int[] counter = e.getValue();
            int sumi = 0;
            int maxi = 0;
            for (int c : counter) {
                sumi += c;
                if (c > maxi) {
                    maxi = c;
                }
            }
            if (sumi > biggestSize) {
                biggestSize = sumi;
            }
            if (sumi < min) {
                min = sumi;
                smallestSizeId = e.getKey();
            }
        }
        return min;
    }
    //</editor-fold>

    //<editor-fold desc="Delivery stage" defaultState="collapsed" >

    /**
     * Method run at the end, delivers computed data from operator
     */
    private void deliver() {
        int exampleIndex = 0;
        ExampleSet outputSet = exampleSet;
        for (Example example : outputSet) {
            PrototypeTuple tuple = examplesNearestTuples[exampleIndex];
            example.setValue(ATTRIBUTE_ID_PROTO_1, tuple.getPrototypeId1());
            example.setValue(ATTRIBUTE_ID_PROTO_2, tuple.getPrototypeId2());
            example.setValue(ATTRIBUTE_ID_PAIR, tuple.getPairId());
            exampleIndex++;
        }
        PrototypesEnsembleModel model = new PrototypesEnsembleModel(prototypes, labels, prototypeAttributeNames, distanceMeasure, selectedTuples);
        //Return data
        this.exampleSetOutputPort.deliver(outputSet);
        this.modelOutputPort.deliver(model);
    }
    //</editor-fold>

    //<editor-fold desc="Operator methods" defaultState="collapsed" >

    /**
     * Main method which performs all calculations
     *
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        setup();
        compute();
        optimize();
        deliver();
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (UndefinedParameterError ignored) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            case BINOMINAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_COUNT_FACTOR, FACTOR_DESCRIPTION, 0, 1, 0.1);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMUM_SUPPORT, MINIMUM_NUMBER_SUPPORT_DESCRIPTION, 0, Integer.MAX_VALUE, 20);
        types.add(type);
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
    //</editor-fold>

    //<editor-fold desc="PrototypeTuple Class" defaultState="collapsed" >
    static class PrototypeTuple {
        /**
         * Id of pair
         */
        private long pairId;
        /**
         * Smaller id of pair
         */
        private int prototypeId1;
        /**
         * Seconds Id of pair(bigger)
         */
        private int prototypeId2;

        /**
         * Base Constructor
         * Class will self decide which number is smaller
         *
         * @param prototypeId1 id of first prototype
         * @param prototypeId2 id of second prototype
         */
        PrototypeTuple(int prototypeId1, int prototypeId2) {
            if (prototypeId1 < prototypeId2) {
                this.prototypeId1 = prototypeId1;
                this.prototypeId2 = prototypeId2;
            } else {
                this.prototypeId1 = prototypeId2;
                this.prototypeId2 = prototypeId1;
            }
            this.pairId = BasicMath.pair(this.prototypeId1, this.prototypeId2);
        }

        /**
         * Copy constructor
         *
         * @param tuple other {@link PrototypeTuple}
         */
        PrototypeTuple(PrototypeTuple tuple) {
            this.prototypeId1 = tuple.prototypeId1;
            this.prototypeId2 = tuple.prototypeId2;
            this.pairId = tuple.pairId;
        }

        PrototypeTuple() {
            this.prototypeId1 = -1;
            this.prototypeId2 = -1;
            this.pairId = -1;
        }

        /**
         * Method to set data from other tuple
         *
         * @param tuple from which to copy fields
         */
        final void set(PrototypeTuple tuple) {
            this.set(tuple.getPrototypeId1(), tuple.getPrototypeId2());
        }

        /**
         * Method to set data from specific Ids,
         * will self decide which number is smaller and generate pairId
         *
         * @param prototypeId1 first id
         * @param prototypeId2 second id
         */
        public void set(int prototypeId1, int prototypeId2) {
            if (prototypeId1 < prototypeId2) {
                this.prototypeId1 = prototypeId1;
                this.prototypeId2 = prototypeId2;
            } else {
                this.prototypeId1 = prototypeId2;
                this.prototypeId2 = prototypeId1;
            }
            this.pairId = BasicMath.pair(this.prototypeId1, this.prototypeId2);
        }

        /**
         * Getter for pair Id
         *
         * @return long
         */
        long getPairId() {
            return pairId;
        }

        /**
         * Getter for smaller prototype
         *
         * @return int
         */
        int getPrototypeId1() {
            return prototypeId1;
        }

        /**
         * Getter for bigger prototype
         *
         * @return int
         */
        int getPrototypeId2() {
            return prototypeId2;
        }

        /**
         * Method for faster printing Class data
         *
         * @return string with object summary
         */
        @Override
        public String toString() {
            return "PrototypeTuple{" +
                    "pairId=" + pairId +
                    ", protoId1=" + prototypeId1 +
                    ", protoId2=" + prototypeId2 +
                    '}';
        }
    }
    //</editor-fold>
}
