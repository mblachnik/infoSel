package org.prules.operator.learner.prototype.model;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import org.prules.operator.learner.prototype.PrototypeTuple;
import org.prules.operator.learner.prototype.PrototypesEnsembleModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BasicNearestProtoModel extends AbstractNearestProtoModel {
    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Counter used to count frequency of prototypes pair
     */
    private HashMap<Long, int[]> countersMap;
    /**
     * Map of unique prototype tuples
     */
    private Map<Long, PrototypeTuple> selectedTuples;
    /**
     * Array of labels of training set for faster iteration
     */
    private double[] labels;
    /**
     * Size of biggest prototype tuple
     */
    private int biggestSize;
    /**
     * Id of smallest prototype tuple
     */
    private long smallestSizeId;
    /**
     * Factor of minimum data count of batch
     */
    private double minFactor;
    /**
     * Minimum data count of batch
     */
    private int minSupport;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * Constructor for model
     *
     * @param examples      - training examples
     * @param prototypes    - prototype examples
     * @param measureHelper - measure helper for retrieving distances between examples
     * @param minFactor     - percentage of biggest size of batch to be minimal size of smallest batch
     * @param minSupport    - min value from biggest size of batch
     * @throws OperatorException - on getInitializedMeasure in measureHelper from examples
     */
    public BasicNearestProtoModel(ExampleSet examples, ExampleSet prototypes, DistanceMeasureHelper measureHelper, double minFactor, int minSupport) throws OperatorException {
        super(examples, prototypes, measureHelper);
        this.minFactor = minFactor;
        this.minSupport = minSupport;
    }
    //</editor-fold>

    // <editor-fold desc="Set up stage" defaultState="collapsed" >
    @Override
    public void setup() {
        super.setup();
        //Init map of counters
        this.countersMap = new HashMap<>();
        //Init map of unique tuple
        this.selectedTuples = new HashMap<>();

        this.labels = new double[getExamples().size()];
    }
    //</editor-fold>

    //<editor-fold desc="Compute stage" defaultState="collapsed" >
    @Override
    void compute() {
        int exampleIndex = 0;
        for (Example example : getExamples()) {
            double[] valuesExample = new double[getAttributesExampleSet().size()];
            {
                int i = 0;
                for (Attribute attrName : getAttributesExampleSet()) {
                    valuesExample[i++] = example.getValue(attrName);
                }
            }
            PrototypeTuple prototypeTuple = createPrototypeTuple(exampleIndex, example, valuesExample);
            calculateTupleDist(exampleIndex, prototypeTuple);
            this.labels[exampleIndex] = example.getLabel();
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
        double[] prototypesDistances = getExample2ProtoDistances()[exampleIndex];
        double minDistanceNeighbor = Double.MAX_VALUE;
        double minDistanceEnemy = Double.MAX_VALUE;
        double minDistance = Double.MAX_VALUE;
        int prototypeIndex = 0;
        int neighborId = -1;
        int enemyId = -1;

        for (double[] prototype : getPrototypesAttributes()) {
            //Get distance
            double currentDistance = getDistanceMeasure().calculateDistance(valuesExample, prototype);
            //Store distances
            prototypesDistances[prototypeIndex] = currentDistance;
            if (getPrototypesLabel()[prototypeIndex] == example.getLabel()) {
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
            counter = new int[getLabelsNum()];
        }
        int label = (int) labels[exampleIndex];
        counter[label]++;
        countersMap.put(prototypeTuple.getPairId(), counter);
        if (getExamplesNearestTuples()[exampleIndex] != null) {
            getExamplesNearestTuples()[exampleIndex].set(prototypeTuple);
        } else {
            getExamplesNearestTuples()[exampleIndex] = new PrototypeTuple(prototypeTuple);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Optimize stage" defaultState="collapsed" >
    @Override
    void optimize() {
        int min = computeDistribution();
        //If minFactor is less restrictive then take minSupport (always take more restrictive rule)
        int minCounts = Math.max((int) (minFactor * biggestSize), minSupport);
        createUniqueTupleMap();

        while (min < minCounts) {
            int exampleIndex = 0;
            //For each training sample check if it belongs to least frequent pair, then update pair
            for (PrototypeTuple tuple : getExamplesNearestTuples()) {
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

                    double[] protoDistances = getExample2ProtoDistances()[exampleIndex];
                    double minDist = Double.MAX_VALUE;
                    PrototypeTuple tmpTuple = new PrototypeTuple();
                    //Identify two closest prototypes from existing pairs (new group/pair is not constructed)
                    for (Map.Entry<Long, PrototypeTuple> entry : selectedTuples.entrySet()) {
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

        int exampleIndex = 0;
        ExampleSet outputSet = getExamples();
        for (Example example : outputSet) {
            PrototypeTuple tuple = getExamplesNearestTuples()[exampleIndex];
            example.setValue(ATTRIBUTE_ID_PROTO_1, tuple.getPrototypeId1());
            example.setValue(ATTRIBUTE_ID_PROTO_2, tuple.getPrototypeId2());
            example.setValue(ATTRIBUTE_ID_PAIR, tuple.getPairId());
            exampleIndex++;
        }
    }

    /**
     * Method creates unique pairs map which contains unique pairs and corresponding prototype IDs
     */
    private void createUniqueTupleMap() {
        for (PrototypeTuple tuple : getExamplesNearestTuples()) {
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
        for (Map.Entry<Long, int[]> e : countersMap.entrySet()) {
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
    @Override
    public PrototypesEnsembleModel retrieveModel() {
        return new PrototypesEnsembleModel(getPrototypesAttributes(),
                labels, getPrototypeAttributeNames(), getDistanceMeasure(), selectedTuples);
    }

    @Override
    public ExampleSet retrieveOutputSet() {
        return getExamples();
    }
    //</editor-fold>
}
