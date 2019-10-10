package org.prules.operator.learner.misc;

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
 * @author Marcin
 */
public class NearestPrototypesOperator extends Operator implements CapabilityProvider {

    private static final String PARAMETER_MIN_COUNT_FACTOR = "Min. counts factor";
    private static final String PARAMETER_MINIMUM_SUPPORT = "Min. support";

    /**
     * Input data - training set
     */
    private InputPort exampleSetInputPort = this.getInputPorts().createPort("example set");
    /**
     * Input data - prototypes
     */
    private InputPort prototypesInputPort = this.getInputPorts().createPort("prototypes");
    /**
     * example set with three additional attributes as described in class description
     */
    private OutputPort exampleSetOutputPort = this.getOutputPorts().createPort("example set");
    /**
     * Model representing prototypes and its relations
     */
    private OutputPort modelOutputPort = this.getOutputPorts().createPort("pairedModels");
    /**
     * Distance measure helper for creating appropriate distance measure
     */
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    /**
     * <p>
     * Creates a  NearestPrototypeBatch operator. S
     * </p>
     * <p>
     * NOTE: the preferred way for operator creation is using one of the factory
     * methods of {@link OperatorService}.
     * </p>
     *
     * @param description
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

    /**
     * Main method which performs all calculations
     *
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        //Set Logger
        //Logger logger = Logger.getLogger(NearestPrototypesOperator.class.getName());
        //Get data
        double minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        int minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        //boolean detectPureSubsets = getParameterAsBoolean(PARAMETER_DETECT_PURE_SUBSETS);        
        ExampleSet exampleSet = this.exampleSetInputPort.getDataOrNull(ExampleSet.class);
        ExampleSet prototypeSet = this.prototypesInputPort.getDataOrNull(ExampleSet.class);
        exampleSet = (ExampleSet) exampleSet.clone();
        //Create attributes
        Attribute idProto1 = AttributeFactory.createAttribute("ID_Proto_1", Ontology.NUMERICAL);
        Attribute idProto2 = AttributeFactory.createAttribute("ID_Proto_2", Ontology.NUMERICAL);
        Attribute idPair = AttributeFactory.createAttribute("ID_Proto_Pair", Ontology.NUMERICAL);
        //Add attributes to table
        exampleSet.getExampleTable().addAttribute(idProto1);
        exampleSet.getExampleTable().addAttribute(idProto2);
        exampleSet.getExampleTable().addAttribute(idPair);
        //Add attributes as Special
        exampleSet.getAttributes().setSpecialAttribute(idProto1, "id_pair_1");
        exampleSet.getAttributes().setSpecialAttribute(idProto2, "id_pair_2");
        exampleSet.getAttributes().setSpecialAttribute(idPair, Attributes.BATCH_NAME);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(exampleSet);
        //Main loop
        Attributes attributesExampleSet = exampleSet.getAttributes();
        Attributes attributesPrototypes = prototypeSet.getAttributes();
        int labelsNum = attributesExampleSet.getLabel().getMapping().size(); //Number of labels
        int numberOfAttributesInExampleSet = attributesExampleSet.size();
        double[] valuesExample = new double[numberOfAttributesInExampleSet];
        double[][] example2ProtoDistances = new double[exampleSet.size()][prototypeSet.size()];
        int exampleIndex = 0;
        PairedTuple[] examplesNearestPair = new PairedTuple[exampleSet.size()]; //Data structure used to store selected prototypes for given training sample. It contains three elements paired value, id_nearest_prototype, id_nearest_enemy
        Map<Long, int[]> counterMap = new HashMap<>(); //Counter used to count frequency of prototypes pair
        double[] labels = new double[exampleSet.size()]; //Here use use array to store labels as it is much faster then iterating over exampleSet
        double[][] prototypes = new double[prototypeSet.size()][numberOfAttributesInExampleSet];
        double[] prototypesLabel = new double[prototypeSet.size()];
        List<String> prototypeAttributeNames = new ArrayList<>(numberOfAttributesInExampleSet);
        int prototypeIndex = 0;
        int[] counts;
        //<editor-fold defaultstate="collapsed" desc="Create list of attributes">
        for (Attribute attr : attributesExampleSet) {
            prototypeAttributeNames.add(attr.getName());
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="For each prototype from the training set covert it into double[], such that the final set of prototypes if double[][]">
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
        //</editor-fold>        
        Map<Long, PairedTuple> selectedPairs = new HashMap<>();
        for (Example example : exampleSet) {
            //<editor-fold defaultstate="collapsed" desc="Extract values of example for the distance calculations">
            {
                int i = 0;
                for (Attribute attrName : attributesExampleSet) {
                    valuesExample[i++] = example.getValue(attrName);
                }
            }
            //</editor-fold>       
            //<editor-fold defaultstate="collapsed" desc="For each prototype calculate distances to example and store it in example2ProtoDistances matrix. ALso update counter for given pair">            
            double[] protoDistances = example2ProtoDistances[exampleIndex];
            prototypeIndex = 0;
            int neighborId = -1;
            int enemyId = -1;
            double minDistance = Double.MAX_VALUE;
            double minDistanceNeigbor = Double.MAX_VALUE;
            double minDistanceEnemy = Double.MAX_VALUE;
            for (double[] prototype : prototypes) {
                //Get distance                
                double currDistance = distance.calculateDistance(valuesExample, prototype);
                //Store distances
                protoDistances[prototypeIndex] = currDistance;
                if (prototypesLabel[prototypeIndex] == example.getLabel()) {
                    if (currDistance < minDistanceNeigbor) {
                        neighborId = prototypeIndex;
                        minDistanceNeigbor = currDistance;
                    }
                } else {
                    if (currDistance < minDistanceEnemy) {
                        enemyId = prototypeIndex;
                        minDistanceEnemy = currDistance;
                    }
                }
                if (currDistance < minDistance) minDistance = currDistance;
                prototypeIndex++;
            }
            long pair = neighborId < enemyId ? BasicMath.pair(neighborId, enemyId) : BasicMath.pair(enemyId, neighborId);
            //<editor-fold desc="Count class distribution for given pair">                                    
            int label = (int) labels[exampleIndex];
            if ((counts = counterMap.get(pair)) == null) {
                counts = new int[labelsNum];
                counterMap.put(pair, counts);
            }
            counts[label]++;
            //</editor-fold>                                                
            PairedTuple nearestPair;
            if (examplesNearestPair[exampleIndex] != null) {
                nearestPair = examplesNearestPair[exampleIndex];
            } else {
                nearestPair = new PairedTuple();
                examplesNearestPair[exampleIndex] = nearestPair;
            }
            nearestPair.paired = pair;
            nearestPair.protoId1 = neighborId;
            nearestPair.protoId2 = enemyId;
            //</editor-fold>                                    
            labels[exampleIndex] = example.getLabel();
            exampleIndex++;
        }
        //<editor-fold defaultstate="collapsed" desc="Determine maximum value of the counter, minimum value and indekx of the minimum value. Also examples which belong to a batch which is clear (all samples belong to a single class ) are added to pureSet variable.">
        int minCounts; //The the minimum avaliable number of counts for given pair of prototypes
        long minId = -1; //The id of the minimum value
        int min = Integer.MAX_VALUE; //Minimum value
        int max = 0;
        for (Entry<Long, int[]> e : counterMap.entrySet()) {
            counts = e.getValue();
            int sumi = 0;
            int maxi = 0;
            for (int c : counts) {
                sumi += c;
                if (c > maxi) {
                    maxi = c;
                }
            }
            //if (sumi - maxi == 0) pureSubset.add(e.getKey());
            if (sumi > max) {
                max = sumi;
            }
            if (sumi < min) {
                min = sumi;
                minId = e.getKey();
            }
        }
        //</editor-fold>
        minCounts = Math.max((int) (minFactor * max), minSupport); //If minFactor is less restrictive then take minSupport (always take more restrictive rule)
        //<editor-fold defaultstate="collapsed" desc="Creates selectedPairs map which contains unique pairs and corresponding prototype IDs">
        for (PairedTuple pairedTuple : examplesNearestPair) {
            if (!selectedPairs.containsKey(pairedTuple.paired)) {
                //If given pair does not exists in the map put it and preserve protoId order, such that protoId1 is smaller id than the protoId2
                PairedTuple tmpPair = new PairedTuple(pairedTuple);
                if (pairedTuple.protoId2 < pairedTuple.protoId1) {
                    tmpPair.protoId1 = pairedTuple.protoId2;
                    tmpPair.protoId2 = pairedTuple.protoId1;
                }
                //tmpPair.isPure = pureSubset.contains(pairedTuple.pairedTuple);
                selectedPairs.put(tmpPair.paired, tmpPair);
            }
        }
        //</editor-fold>
        // <editor-fold desc=" ${Searching for new nearest prototypes if current pair is not so common. Appears not enough often. Finish when all frequencies fulfills
        // the minCounts condition} ">
        while (min < minCounts) {
            exampleIndex = 0;
            //<editor-fold desc="For each training sample check if it belongs to least frequent pair, then update pair">
            for (PairedTuple pairs : examplesNearestPair) {
                //If sample with given exampleId belongs to the least frequent batch group then reassign it to other already existing group
                if (pairs.paired == minId) {
                    //If old counter is almost empty (has less then one element remove key)                
                    counts = counterMap.get(pairs.paired);
                    int label = (int) labels[exampleIndex];
                    //<editor-fold defaultstate="collapsed" desc="Get hits count for given sample. New we have a distribution of samples per class">
                    if (Arrays.stream(counts).sum() <= 1) {
                        counterMap.remove(pairs.paired);
                    } else { //Otherwise decrease counter
                        counts[label]--;
                    }
                    //</editor-fold>
                    double[] protoDistances = example2ProtoDistances[exampleIndex];
                    double minDist = Double.MAX_VALUE;
                    long minPair = -1;
                    //<editor-fold defaultstate="collapsed" desc="Identify two closest prototypes from existing pairs (new group/pair is not constructed)">
                    for (Entry<Long, PairedTuple> entry : selectedPairs.entrySet()) {
                        if (entry.getKey() != minId) {
                            PairedTuple pairedTuple = entry.getValue();
                            double dist = protoDistances[pairedTuple.protoId1] + protoDistances[pairedTuple.protoId2];
                            long pair = pairedTuple.paired;
                            if (dist < minDist) {
                                minDist = dist;
                                minPair = pair;
                            }
                        }
                    }
                    //</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="Update selectedPair">
                    PairedTuple tmpPaired = selectedPairs.get(minPair);
                    if (prototypesLabel[tmpPaired.protoId1] == label) {
                        pairs.set(minPair, tmpPaired.protoId1, tmpPaired.protoId2);
                    } else {
                        pairs.set(minPair, tmpPaired.protoId2, tmpPaired.protoId1);
                    }
                    //</editor-fold>
                    //Increase counter of a new pair
                    counts = counterMap.get(pairs.paired);
                    counts[label]++;
                }
                exampleIndex++;
            }
            selectedPairs.remove(minId);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Again determine minID and min counts.  Warning if we find a subset of examples which all belong to a single class we keep that subset and store it in pureSubset set. In later stage if any example falls into this pair then they will be automatically classified to the majority class.">
            min = Integer.MAX_VALUE;
            max = -1;
            minId = -1;
            for (Entry<Long, int[]> e : counterMap.entrySet()) {
                //if (pureSubset.contains(e.getKey()) || ignoredPairsSet.contains(e.getKey())) continue;
                counts = e.getValue();
                int sumi = 0;
                int maxi = 0;
                for (int c : counts) {
                    sumi += c;
                    if (c > maxi) {
                        maxi = c;
                    }
                }
                if (sumi > max) {
                    max = sumi;
                }
                if (sumi < min) {
                    min = sumi;
                    minId = e.getKey();
                }
            }
            //</editor-fold>
        }
        // </editor-fold>

        exampleIndex = 0;
        ExampleSet outputSet = exampleSet;
        for (Example example : exampleSet) {
            PairedTuple paired = examplesNearestPair[exampleIndex];
//            if (!selectedPairs.containsKey(paired.paired)) {
//                //If given pair does not exists in the map put it and preserve protoId order, such that protoId1 is smaller id than the protoId2
//                PairedTriple tmpPair = new PairedTriple(paired);
//                if (paired.protoId2 < paired.protoId1) {
//                    tmpPair.protoId1 = paired.protoId2;
//                    tmpPair.protoId2 = paired.protoId1;
//                }                
//                selectedPairs.put(tmpPair.paired, tmpPair);
//            }
            example.setValue(idProto1, paired.protoId1);
            example.setValue(idProto2, paired.protoId2);
            example.setValue(idPair, paired.paired);
            exampleIndex++;
        }
        PrototypesEnsembleModel model = new PrototypesEnsembleModel(prototypes, labels, prototypeAttributeNames, distance, selectedPairs);
        //Return data
        this.exampleSetOutputPort.deliver(outputSet);
        this.modelOutputPort.deliver(model);
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

        ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_COUNT_FACTOR, "Factor indicating minimum number of instances in a single batch. It is multiplayed by the max counts.", 0, 1, 0.1);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMUM_SUPPORT, "Minimum number of samples in a single batch. It it has lower number of samples it will be removed and the samples will be redistributed into another batches", 0, Integer.MAX_VALUE, 20);
        types.add(type);
        //type = new ParameterTypeBoolean(PARAMETER_DETECT_PURE_SUBSETS,"Detect pure subsets and keep them (for examples falling into this pair label will be determined without training a model) ",false);
        //types.add(type);        
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }

    static class PairedTuple {

        long paired = -1;
        int protoId1 = -1;
        int protoId2 = -1;
        boolean isPure = false;

        public PairedTuple(long paired, int protoId1, int protoId2) {
            set(paired, protoId1, protoId2, false);
        }

        public PairedTuple(long paired, int protoId1, int protoId2, boolean isPure) {
            set(paired, protoId1, protoId2, isPure);
        }

        PairedTuple(PairedTuple pair) {
            this.set(pair);
        }

        PairedTuple() {
        }

        final void set(PairedTuple pair) {
            this.paired = pair.paired;
            this.protoId1 = pair.protoId1;
            this.protoId2 = pair.protoId2;
            this.isPure = pair.isPure;
        }

        final void set(long paired, int protoId1, int protoId2) {
            this.paired = paired;
            this.protoId1 = protoId1;
            this.protoId2 = protoId2;
            this.isPure = false;
        }

        final void set(long paired, int protoId1, int protoId2, boolean isPure) {
            this.paired = paired;
            this.protoId1 = protoId1;
            this.protoId2 = protoId2;
            this.isPure = isPure;
        }
    }
}
