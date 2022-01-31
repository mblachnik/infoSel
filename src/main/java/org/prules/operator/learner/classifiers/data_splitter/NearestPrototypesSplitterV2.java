package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.tools.math.BasicMath;
import org.prules.tools.math.container.PairContainer;

import java.util.*;

/**
 * This class implements a method for splitting the input dataset into subsets by searching for two nearest prototypes
 * from oposite class. So it takes as input the example set, and according to the prototpes position the input set is
 * decomposed into subsets which constitute a single batch. THe output exampleSet has new attribute with
 * role batch.
 */
public class NearestPrototypesSplitterV2 implements NearestPrototypesSplitter {
    double minFactor;
    int minSupport;
    DistanceMeasure distance;
    double[][] prototypes;
    double[] prototypeLabels;
    List<String> attributes;
    Map<Long,PiredTriple> selectedPairs;

    public NearestPrototypesSplitterV2(ExampleSet prototypeSet, DistanceMeasure distance, double minFactor, int minSupport) {
        this.minFactor = minFactor;
        this.minSupport = minSupport;
        this.distance = distance;
        //Prototypes are copied to avoid keeping reference to prototyps ExampleSet;
        int n = prototypeSet.size();
        Attributes atts = prototypeSet.getAttributes();
        int m = atts.size();
        prototypes = new double[n][m];
        prototypeLabels = new double[n];
        int i = 0, j = 0;
        for (Example e : prototypeSet) {
            j = 0;
            for (Attribute a : atts) {
                prototypes[i][j] = e.getValue(a);
                j++;
            }
            prototypeLabels[i] = e.getLabel();
            i++;
        }
        attributes = new ArrayList<>(m);
        for (Attribute a : atts) {
            attributes.add(a.getName());
        }
    }

    /**
     * This method is responsible for identifing best maching pair of prototypes for each training example. As a result
     * this method returns two arguments wrapped within the PairContainer. These are PrototypesEnsembleModel - which contains information on selected pair of prototypes (prototypes ID etc)
     * and the second element of the PairContainer is exampleSet with all necessary attributes (the most important is batch atribute)
     * which is used to iterate and train individual prediction model for each batch value
     *
     * @return - pair containing PrototypesEnsembeleModel and ExampleSet with the batch attribute
     * @throws OperatorException
     */
    @Override
    public ExampleSet split(ExampleSet exampleSet) throws OperatorException {
        distance.init(exampleSet);
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
        //Main loop
        Attributes attributesExampleSet = exampleSet.getAttributes();
        int labelsNum = attributesExampleSet.getLabel().getMapping().size(); //Numer of labels
        int numberOfAttributesInExampleSet = attributesExampleSet.size();
        double[] valuesExample = new double[numberOfAttributesInExampleSet];
        double[][] example2ProtoDistances = new double[exampleSet.size()][prototypes.length];
        PiredTriple[] examplesNearestPair = new PiredTriple[exampleSet.size()]; //Data structure used to store selected prototypes for given training sample. It contains three elements pired value, id_nearest_prototype, id_nearest_enemy
        Map<Long, PairContainer<int[], List<Integer>>> piredTrainingSetAndCounterMap = new HashMap<>(); //A map which holds counter used to count frequency of prototypes for given pair, but it also holds training samples associated with given pair
        int[] labels = new int[exampleSet.size()]; //Here use use array to store labels as it is much facter then iterating over exampleSet
        Map<String, Integer> prototypeAttributeNames2PosMap = new HashMap<>(numberOfAttributesInExampleSet);
        List<String> prototypeAttributeNames = new ArrayList<>(numberOfAttributesInExampleSet);
        int[] counts;
        //<editor-fold defaultstate="collapsed" desc="Reorder values of prototypes so they much the order of the input trainingSet ">
        boolean attrIndexSame = true;
        int attrIndex = 0;
        for (Attribute attr : attributesExampleSet) {
            String attrName = attr.getName();
            prototypeAttributeNames2PosMap.put(attrName, attrIndex);
            prototypeAttributeNames.add(attrName);
            if (!attrName.equals(attributes.get(attrIndex))) {
                attrIndexSame = false;
            }
            attrIndex++;
        }
        if (!attrIndexSame) {
            double[] newPrototype = new double[attributes.size()];
            for (int prototypeIndex = 0; prototypeIndex < prototypes.length; prototypeIndex++) {
                double[] prototype = prototypes[prototypeIndex];
                for (int j = 0; j < attributes.size(); j++) {
                    String attrName = attributes.get(j);
                    int newId = prototypeAttributeNames2PosMap.get(attrName);
                    newPrototype[newId] = prototype[j];
                }
                System.arraycopy(newPrototype, 0, prototype, 0, prototype.length);
            }
            attributes = prototypeAttributeNames;
        }
        //</editor-fold>
        selectedPairs = new HashMap<>();
        int exampleIndex = 0;
        for (Example example : exampleSet) {
            labels[exampleIndex] = (int) example.getLabel();
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
            int prototypeIndex = 0;
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
                if (prototypeLabels[prototypeIndex] == example.getLabel()) {
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

            long pair = getPair(neighborId, enemyId);
            examplesNearestPair[exampleIndex] = new PiredTriple(pair, neighborId, enemyId);
            //<editor-fold desc="Count class distribution for given pair">
            int label = labels[exampleIndex];
            PairContainer<int[], List<Integer>> container = piredTrainingSetAndCounterMap.computeIfAbsent(pair, (k) -> new PairContainer<int[], List<Integer>>(new int[labelsNum], new ArrayList<Integer>()));
            counts = container.getFirst();
            counts[label]++;
            //</editor-fold>
            container.getSecond().add(exampleIndex); //We add the pair to the list associated with given pair.
            //</editor-fold>
            exampleIndex++;
        }


        // <editor-fold desc=" ${Searching for new nearest prototypes if current pair is not so common. Appears not enough often. Finish when all frequences fulfills the minCOunts condition} ">
        MinResults mins = new  MinResults();
        calcMinSupport(piredTrainingSetAndCounterMap, mins);

        //TODO dodać warunek stopy gdyby kryterium nie zostało spełnione
        if (minFactor>0 && minSupport>0) {
            while ((mins.min < mins.minCounts || mins.singleClassBatch) && (piredTrainingSetAndCounterMap.size() > 1)) { //
                reassignSmallesBatch(piredTrainingSetAndCounterMap, mins.minPairId, example2ProtoDistances, labels, examplesNearestPair);
                calcMinSupport(piredTrainingSetAndCounterMap, mins);
            }
        }
        for (Map.Entry<Long, PairContainer<int[], List<Integer>>> res : piredTrainingSetAndCounterMap.entrySet()) {
            int exampleId = res.getValue().getSecond().get(0);
            PiredTriple pt = examplesNearestPair[exampleId];
            selectedPairs.put(res.getKey(),pt);
        }
        // </editor-fold>

        exampleIndex = 0;
        ExampleSet outputSet = exampleSet;
        for (Example example : exampleSet) {
            PiredTriple pired = examplesNearestPair[exampleIndex];
            example.setValue(idProto1, pired.protoId1);
            example.setValue(idProto2, pired.protoId2);
            example.setValue(idPair, pired.pired);
            exampleIndex++;
        }
        //Return data
        return outputSet;
    }

    private void reassignSmallesBatch(Map<Long, PairContainer<int[], List<Integer>>> piredTrainingSetAndCounterMap, long pair, double[][] example2ProtoDistances, int[] labels, PiredTriple[] examplesNearestPair) {
        List<Integer> idSmallestBatch = piredTrainingSetAndCounterMap.remove(pair).getSecond();
        LogService.getRoot().finest("Pair being removed: " + pair);
        Set<Long> pairs = piredTrainingSetAndCounterMap.keySet();
        for (int exampleIndex : idSmallestBatch) {
            double[] protoDistances = example2ProtoDistances[exampleIndex];
            long minPair = -1;
            int minProtoId1 = -1;
            int minProtoId2 = -1;
            double minDist = Double.MAX_VALUE;
            for (long newPair : pairs) {
                int exampleNewPairId = piredTrainingSetAndCounterMap.get(newPair).getSecond().get(0); //Take the first id of an instance from the colsest pair
                int protoId1 = examplesNearestPair[exampleNewPairId].protoId1;
                int protoId2 = examplesNearestPair[exampleNewPairId].protoId2;
                double dist = protoDistances[protoId1] + protoDistances[protoId2];
                if (dist < minDist) {
                    minDist = dist;
                    minPair = newPair;
                    minProtoId1 = protoId1;
                    minProtoId2 = protoId2;
                }
            }
            //Update piredTrainingSetAndCounterMap
            LogService.getRoot().finest("");
            PairContainer<int[], List<Integer>> bestMatch = piredTrainingSetAndCounterMap.get(minPair);
            bestMatch.getSecond().add(exampleIndex);
            int label = labels[exampleIndex];
            bestMatch.getFirst()[label]++;
            //Update examplesNearestPair
            examplesNearestPair[exampleIndex].pired = minPair;
            examplesNearestPair[exampleIndex].protoId1 = minProtoId1;
            examplesNearestPair[exampleIndex].protoId2 = minProtoId2;
        }
    }

    /**
     * Calculate pairing function for given pair of prototypes. Before calculating the pairing function the ids are reordered in according to the value of the prototypes
     *
     * @param id1 id of first prototype
     * @param id2 id od the second prototype
     * @return value of the pairing function
     */
    @Override
    public long getPair(int id1, int id2) {
        long out = id1 < id2 ? BasicMath.pair((long) id1, (long) id2) : BasicMath.pair((long) id2, (long) id1);
        return out;
    }

    /**
     * This function is used to find the pair which has the smalles support to check if given pair should be reassigned to other pairs
     *
     * @param piredTrainingSetAndCounterMap
     * @param results                  - results table. To avoid allocating new tables we use the one entered here
     * @return reference to resultsTable
     */
    private  MinResults calcMinSupport(Map<Long, PairContainer<int[], List<Integer>>> piredTrainingSetAndCounterMap,  MinResults results) {
        int minCounts; //The the minimum avaliable number of counts for given pair of prototypes
        long minPairId = -1; //The id of the minimum value
        int min = Integer.MAX_VALUE; //Minimum value
        int max = 0; //maximum number of elements in a particular subspace
        int maxId = -1;
        int minId = -1;
        int[] counts;
        boolean isSingleClassBatch = false;
        for (Map.Entry<Long, PairContainer<int[], List<Integer>>> e : piredTrainingSetAndCounterMap.entrySet()) {
            counts = e.getValue().getFirst();
            int sumi = 0; //Cumulative sum of elements in counts
            int maxi = 0; //THe highest number of elements in class c
            int mini = Integer.MAX_VALUE;
            int maxiId = 0;
            int miniId = 0;
            for (int i=0; i<counts.length; i++){
                int c = counts[i];
                sumi += c;
                if (c > maxi) {
                    maxi = c;
                    maxiId = i;
                }
                if(c < mini && c > 0) {
                    mini = c;
                    miniId = i;
                }
            }
            if (mini == maxi){ //Scheck if batch containse elements only of a single class
                minPairId = e.getKey();
                LogService.getRoot().info("A batch (" + minPairId + ") containes alements of a single class");
                isSingleClassBatch = true;
                break;
            }
            //if (sumi - maxi == 0) pureSubset.add(e.getKey());
            if (sumi > max) { //Store the maximum number of examples for given pair
                max = sumi;
                maxId = maxiId;
            }
            if (sumi < min) { //store minimum number of examples for given pair, and get the id of the smalles group
                min = sumi;
                minId = miniId;
                minPairId = e.getKey();
            }
        }

        minCounts = (int) (minFactor * max) > minSupport ? (int) (minFactor * max) : minSupport; //If minFactor is less restrictive then take minSupport (always take more restrictive rule)
        results.minCounts = minCounts;
        results.minPairId = minPairId;
        results.min = min;
        results.max = max;
        results.singleClassBatch = isSingleClassBatch;
        return results;
    }

    /**
     * Returns prototypes position in a form of double matrix. Each row is a single prototype. It is ued to speed up calculations
     *
     * @return
     */
    @Override
    public double[][] getPrototypes() {
        return prototypes;
    }

    /**
     * Returns prototypeLables not that these are double values which were obtained from the prototypes input set.
     * It is ued to speed up training, but should be used with care becouse other datasets may have different order of mappings double->label
     *
     * @return
     */
    @Override
    public double[] getPrototypeLabels() {
        return prototypeLabels;
    }

    /**
     * Returns attributes order used when calculating {@link #getAttributes() getAttributes}. According to the elements order in the list prototypes are ordered
     *
     * @return
     */
    @Override
    public List<String> getAttributes() {
        return attributes;
    }

    /**
     * Returns selectedPairs obtained after evaluating  {@link #split(ExampleSet) split}
     *
     * @return piredMap
     */
    @Override
    public Map<Long,PiredTriple> getSelectedPairs() {
        return selectedPairs;
    }

    /**
     * Returns the value of relative minimum number of samples which is required in given batch
     *
     * @return
     */
    @Override
    public double getMinFactor() {
        return minFactor;
    }

    /**
     * Set the value of relative minimum number of samples which is required in given batch
     *
     * @param minFactor
     */
    @Override
    public void setMinFactor(double minFactor) {
        this.minFactor = minFactor;
    }

    /**
     * Returns minimum number of samples which is required to create a batch, if batch contain smaller number than it is rejected and connected to another batch
     *
     * @return minimum support
     */
    @Override
    public int getMinSupport() {
        return minSupport;
    }

    /**
     * Sets minimum number of samples which is required to create a batch, if batch contain smaller number than it is rejected and connected to another batch
     *
     * @param minSupport
     */
    @Override
    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
    }

    /**
     * Returns the distance measure used in the calculations
     *
     * @return
     */
    @Override
    public DistanceMeasure getDistance() {
        return distance;
    }

    /**
     * Sets the distance measure used in the calculations.
     *
     * @param distance
     */
    @Override
    public void setDistance(DistanceMeasure distance) {
        this.distance = distance;
    }


    class MinResults {
        public int minCounts;
        public long minPairId;
        public int min;
        public int max;
        public boolean singleClassBatch;
    }
}


