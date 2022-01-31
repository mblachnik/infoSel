package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.tools.math.BasicMath;

import java.util.*;

/**
 * This class implements a method for splitting the input dataset into subsets by searching for two nearest prototypes
 * from oposite class. So it takes as input the example set, and according to the prototpes position the input set is
 * decomposed into subsets which constitute a single batch. THe output exampleSet has new attribute with
 * role batch.
 *
 */
public class NearestPrototypesSplitterV1 implements NearestPrototypesSplitter {

    double minFactor;
    int minSupport;
    DistanceMeasure distance;
    double[][] prototypes;
    double[] prototypeLabels;
    List<String> attributes;
    Map<Long, PiredTriple>    selectedPairs;

    public NearestPrototypesSplitterV1(ExampleSet prototypeSet, DistanceMeasure distance, double minFactor, int minSupport) {
        this.minFactor = minFactor;
        this.minSupport = minSupport;
        this.distance = distance;
        //Prototypes are copied to avoid keeping reference to prototyps ExampleSet;
        int n = prototypeSet.size();
        Attributes atts = prototypeSet.getAttributes();
        int m = atts.size();
        prototypes = new double[n][m];
        prototypeLabels = new double[n];
        int i=0,j=0;
        for (Example e : prototypeSet){
            j=0;
            for(Attribute a : atts){
                prototypes[i][j] = e.getValue(a);
                j++;
            }
            prototypeLabels[i] = e.getLabel();
            i++;
        }
        attributes = new ArrayList<>(m);
        for(Attribute a : atts){
            attributes.add(a.getName());
        }
    }

    /**
     * This method is responsible for identifing best maching pair of prototypes for each training example. As a result
     * this method returns two arguments wrapped within the PairContainer. These are PrototypesEnsembleModel - which contains information on selected pair of prototypes (prototypes ID etc)
     * and the second element of the PairContainer is exampleSet with all necessary attributes (the most important is batch atribute)
     * which is used to iterate and train individual prediction model for each batch value
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
        Map<Long, int[]> counterMap = new HashMap<>(); //Counter used to count frequency of prototypes pair
        int[] labels = new int[exampleSet.size()]; //Here use use array to store labels as it is much facter then iterating over exampleSet
        Map<String,Integer> prototypeAttributeNames2PosMap = new HashMap<>(numberOfAttributesInExampleSet);
        List<String> prototypeAttributeNames = new ArrayList<>(numberOfAttributesInExampleSet);
        int[] counts;
        //<editor-fold defaultstate="collapsed" desc="Reorder values of prototypes so they much the order of the input trainingSet ">
        boolean attrIndexSame = true;
        int attrIndex = 0;
        for (Attribute attr : attributesExampleSet) {
            String attrName = attr.getName();
            prototypeAttributeNames2PosMap.put(attrName,attrIndex);
            prototypeAttributeNames.add(attrName);
            if (!attrName.equals(attributes.get(attrIndex))){
                attrIndexSame = false;
            }
            attrIndex++;
        }
        if (!attrIndexSame) {
            for (int prototypeIndex = 0; prototypeIndex < prototypes.length; prototypeIndex++) {
                double[] prototype = prototypes[prototypeIndex];
                double[] newPrototype = new double[prototype.length];
                for (int j = 0; j < attributes.size(); j++) {
                    String attrName = attributes.get(j);
                    int newId = prototypeAttributeNames2PosMap.get(attrName);
                    newPrototype[newId] = prototype[j];
                }
                prototypes[prototypeIndex] = newPrototype;
            }
            attributes = prototypeAttributeNames;
        }
        //</editor-fold>
        selectedPairs = new HashMap<>();
        int exampleIndex = 0;
        for (Example example : exampleSet) {
            labels[exampleIndex] = (int)example.getLabel();
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
                    if (currDistance < minDistanceNeigbor){
                        neighborId = prototypeIndex;
                        minDistanceNeigbor = currDistance;
                    }
                } else {
                    if (currDistance < minDistanceEnemy){
                        enemyId = prototypeIndex;
                        minDistanceEnemy = currDistance;
                    }
                }
                if (currDistance < minDistance) minDistance = currDistance;
                prototypeIndex++;
            }
            long pair = getPair(neighborId,enemyId);
            //<editor-fold desc="Count class distribution for given pair">
            int label = labels[exampleIndex];
            counts = counterMap.computeIfAbsent(pair,(k)->new int[labelsNum]);
            counts[label]++;
            //</editor-fold>
            PiredTriple nearestPair;
            if (examplesNearestPair[exampleIndex] != null) {
                nearestPair = examplesNearestPair[exampleIndex];
            } else {
                nearestPair = new PiredTriple();
                examplesNearestPair[exampleIndex] = nearestPair;
            }
            nearestPair.pired = pair;
            nearestPair.protoId1 = neighborId;
            nearestPair.protoId2 = enemyId;
            //</editor-fold>
            exampleIndex++;
        }
        //<editor-fold defaultstate="collapsed" desc="Determine maximum value of the counter, minimum value and indekx of the minimum value. Also examples which belong to a batch which is clear (all samples belong to a single class ) are added to pureSet variable.">
        int minCounts; //The the minimum avaliable number of counts for given pair of prototypes
        long minId = -1; //The id of the minimum value
        int min = Integer.MAX_VALUE; //Minimum value
        int max = 0; //maximum number of elements in a particular subspace
        for (Map.Entry<Long, int[]> e : counterMap.entrySet()) {
            counts = e.getValue();
            int sumi = 0; //Cumulative sum of elements in counts
            int maxi = 0; //THe highest number of elements in class c
            for (int c : counts) {
                sumi += c;
                if (c > maxi) {
                    maxi = c;
                }
            }
            //if (sumi - maxi == 0) pureSubset.add(e.getKey());
            if (sumi > max) { //Store the maximum number of examples for given pair
                max = sumi;
            }
            if (sumi < min) { //store minimum number of examples for given pair, and get the id of the smalles group
                min = sumi;
                minId = e.getKey();
            }
        }
        //</editor-fold>
        minCounts = (int) (minFactor * max) > minSupport ? (int) (minFactor * max) : minSupport; //If minFactor is less restrictive then take minSupport (always take more restrictive rule)
        //<editor-fold defaultstate="collapsed" desc="Creates selectedPairs map which contains unique pairs and corresponding prototype IDs">
        for (PiredTriple pired : examplesNearestPair) {
            if (!selectedPairs.containsKey(pired.pired)) {
                //If given pair does not exists in the map put it and preserve protoId order, such that protoId1 is smaller id than the protoId2
                PiredTriple tmpPair = new PiredTriple(pired);
                if (pired.protoId2 < pired.protoId1) {
                    tmpPair.protoId1 = pired.protoId2;
                    tmpPair.protoId2 = pired.protoId1;
                }
                //tmpPair.isPure = pureSubset.contains(pired.pired);
                selectedPairs.put(tmpPair.pired, tmpPair);
            }
        }
        //</editor-fold>
        // <editor-fold desc=" ${Searching for new nearest prototypes if current pair is not so common. Appears not enough often. Finish when all frequences fulfills the minCOunts condition} ">
        while (min < minCounts) {
            exampleIndex = 0;
            //<editor-fold desc="For each training sample check if it belongs to least frequent pair, then update pair">
            for (PiredTriple pairs : examplesNearestPair) {
                //If sample with given exampleId belongs to the least frequent batch group then reassigne it to other olready existing group
                if (pairs.pired == minId) {
                    //If old counter is almost empty (has less then one element remove key)
                    counts = counterMap.get(pairs.pired);
                    int label = (int) labels[exampleIndex];
                    //<editor-fold defaultstate="collapsed" desc="Get hits count for given sample. New we have a distribution of samples per class">
                    if (Arrays.stream(counts).sum() <= 1) {
                        counterMap.remove(pairs.pired);
                    } else { //Otherwise decrease counter
                        counts[label]--;
                    }
                    //</editor-fold>
                    double[] protoDistances = example2ProtoDistances[exampleIndex];
                    double minDist = Double.MAX_VALUE;
                    long minPair = -1;
                    //<editor-fold defaultstate="collapsed" desc="Identify two closest prototypes from existing pairs (new group/pair is not constructed)">
                    for (Map.Entry<Long, PiredTriple> entry : selectedPairs.entrySet()) {
                        if (entry.getKey() != minId) {
                            PiredTriple piredTriple = entry.getValue();
                            double dist = protoDistances[piredTriple.protoId1] + protoDistances[piredTriple.protoId2];
                            long pair = piredTriple.pired;
                            if (dist < minDist) {
                                minDist = dist;
                                minPair = pair;
                            }
                        }
                    }
                    //</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="Update selectedPair">
                    PiredTriple tmpPired = selectedPairs.get(minPair);
                    if (prototypeLabels[tmpPired.protoId1] == label) {
                        pairs.set(minPair, tmpPired.protoId1, tmpPired.protoId2);
                    } else {
                        pairs.set(minPair, tmpPired.protoId2, tmpPired.protoId1);
                    }
                    //</editor-fold>
                    //Increase counter of a new pair
                    counts = counterMap.get(pairs.pired);
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
            for (Map.Entry<Long, int[]> e : counterMap.entrySet()) {
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
        ExampleSet outputSet =  exampleSet;
        for (Example example : exampleSet) {
            PiredTriple pired = examplesNearestPair[exampleIndex];
//            if (!selectedPairs.containsKey(pired.pired)) {
//                //If given pair does not exists in the map put it and preserve protoId order, such that protoId1 is smaller id than the protoId2
//                PiredTriple tmpPair = new PiredTriple(pired);
//                if (pired.protoId2 < pired.protoId1) {
//                    tmpPair.protoId1 = pired.protoId2;
//                    tmpPair.protoId2 = pired.protoId1;
//                }
//                selectedPairs.put(tmpPair.pired, tmpPair);
//            }
            example.setValue(idProto1, pired.protoId1);
            example.setValue(idProto2, pired.protoId2);
            example.setValue(idPair, pired.pired);
            exampleIndex++;
        }
        //Return data
        return outputSet;
    }

    /**
     * Calculate pairing function for given pair of prototypes. Before calculating the pairing function the ids are reordered in according to the value of the prototypes
     * @param id1 id of first prototype
     * @param id2 id od the second prototype
     * @return value of the pairing function
     */
    public long getPair(int id1, int id2){
        long out = id1 < id2 ? BasicMath.pair((long) id1, (long) id2) : BasicMath.pair((long) id2, (long) id1);
        return out;
    }

    /**
     * Returns prototypes position in a form of double matrix. Each row is a single prototype. It is ued to speed up calculations
     * @return
     */
    public double[][] getPrototypes() {
        return prototypes;
    }

    /**
     * Returns prototypeLables not that these are double values which were obtained from the prototypes input set.
     * It is ued to speed up training, but should be used with care becouse other datasets may have different order of mappings double->label
     * @return
     */
    public double[] getPrototypeLabels() {
        return prototypeLabels;
    }

    /**
     * Returns attributes order used when calculating {@link #getAttributes() getAttributes}. According to the elements order in the list prototypes are ordered
     * @return
     */
    public List<String> getAttributes() {
        return attributes;
    }

    /**
     * Returns selectedPairs obtained after evaluating  {@link #split(ExampleSet) split}
     * @return piredMap
     */
    public Map<Long, PiredTriple> getSelectedPairs() {
        return selectedPairs;
    }

    /**
     * Returns the value of relative minimum number of samples which is required in given batch
     * @return
     */
    public double getMinFactor() {
        return minFactor;
    }

    /**
     * Set the value of relative minimum number of samples which is required in given batch
     * @param minFactor
     */
    public void setMinFactor(double minFactor) {
        this.minFactor = minFactor;
    }

    /**
     *  Returns minimum number of samples which is required to create a batch, if batch contain smaller number than it is rejected and connected to another batch
     * @return minimum support
     */
    public int getMinSupport() {
        return minSupport;
    }

    /**
     * Sets minimum number of samples which is required to create a batch, if batch contain smaller number than it is rejected and connected to another batch
     * @param minSupport
     */
    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
    }

    /**
     * Returns the distance measure used in the calculations
     * @return
     */
    public DistanceMeasure getDistance() {
        return distance;
    }

    /**
     * Sets the distance measure used in the calculations.
     * @param distance
     */
    public void setDistance(DistanceMeasure distance) {
        this.distance = distance;
    }



}


