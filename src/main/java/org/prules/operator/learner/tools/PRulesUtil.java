/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.RandomGenerator;

import java.util.*;

import org.prules.dataset.IInstanceLabels;
import org.prules.exceptions.IncorrectAttributeException;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.DoubleIntIntContainer;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;

/**
 * @author Marcin
 */
public class PRulesUtil {

    /**
     *
     */
    public static final String INSTANCES_WEIGHTS_NAME = "Instances Weights";

    /*
     public static ExampleSet SelectedInstancesExampleSetCreator(ExampleSet oryginalSet, boolean[] instancesOnOffIndex) {
    
     // create new exampleSet
     List<Attribute> attributes = Arrays.asList(oryginalSet.getExampleTable().getAttributes());
     ExampleTable inMemoryTrainingTable = new MemoryExampleTable(attributes, new OldInstanceSelectionDataRowReader(oryginalSet, instancesOnOffIndex));
    
     // set all regular attributes (remining are special attributes)
     List<Attribute> regularAttributes = new LinkedList<Attribute>();
     for (Attribute attribute : oryginalSet.getAttributes()) {
     regularAttributes.add(attribute);
     }
    
     // setting roles for special attributes
     ExampleSet selectedInstances = new SimpleExampleSet(inMemoryTrainingTable, regularAttributes);
     Iterator<AttributeRole> special = oryginalSet.getAttributes().specialAttributes();
     while (special.hasNext()) {
     AttributeRole role = special.next();
     selectedInstances.getAttributes().setSpecialAttribute(role.getAttribute(), role.getSpecialName());
     }
    
     return selectedInstances;
     }
    
     public static ExampleSet SelectedInstancesExampleSetCreator(ExampleSet oryginalSet, DataIndex instancesOnOffIndex) {
    
     // create new exampleSet
     List<Attribute> attributes = Arrays.asList(oryginalSet.getExampleTable().getAttributes());
     ExampleTable inMemoryTrainingTable = new MemoryExampleTable(attributes, new OldInstanceSelectionDataRowReader(oryginalSet, instancesOnOffIndex));
    
     // set all regular attributes (remining are special attributes)
     List<Attribute> regularAttributes = new LinkedList<Attribute>();
     for (Attribute attribute : oryginalSet.getAttributes()) {
     regularAttributes.add(attribute);
     }
    
     // setting roles for special attributes
     ExampleSet selectedInstances = new SimpleExampleSet(inMemoryTrainingTable, regularAttributes);
     Iterator<AttributeRole> special = oryginalSet.getAttributes().specialAttributes();
     while (special.hasNext()) {
     AttributeRole role = special.next();
     selectedInstances.getAttributes().setSpecialAttribute(role.getAttribute(), role.getSpecialName());
     }
    
     return selectedInstances;
     }
     */

    /**
     * @param size
     * @param randomGenerator
     * @return
     */
    public static int[] randomPermutation(int size, Random randomGenerator) {
        int[] idx = new int[size];
        for (int i = 1; i < size; i++) {
            idx[i] = i;
        }
        randomPermutation(idx, randomGenerator);
        return idx;
    }

    /**
     * @param idx             a table with elements to be permutated
     * @param randomGenerator
     */
    public static void randomPermutation(int[] idx, Random randomGenerator) {
        int size = idx.length;
        for (int i = size; i > 1; i--) {
            int k = randomGenerator.nextInt(i);
            int t = idx[k];
            idx[k] = idx[i - 1];
            idx[i - 1] = t;
        }
    }

    /**
     * @param size
     * @param numberOfInstancesToSelect
     * @param randomGenerator
     * @return
     */
    public static int[] randomSelection(int size, int numberOfInstancesToSelect, Random randomGenerator) {
        if (size < numberOfInstancesToSelect) {
            throw new IndexOutOfBoundsException();
        } else if (size == numberOfInstancesToSelect) {
            int[] out = new int[size];
            for (int i = 0; i < size; i++) {
                out[i] = i;
            }
            return out;
        } else {
            int[] idx = randomPermutation(size, randomGenerator);
            int[] out = new int[numberOfInstancesToSelect];
            for (int i = 0; i < numberOfInstancesToSelect; i++) {
                out[i] = idx[i];
            }
            Arrays.sort(out);
            return out;
        }
    }


    /* This method returns extracted list of all kind of attributess one may require ex. for creating new ExampleSet
     * If any of the input attributeLists is empty then it is not considered in the extraction process.
     * @param exampleSet input example set from witch the alltributes will be extracted
     * @param attributesList a list that would contain all the attributes that appear in the exampleSet (a concatenation of regularAttributesList and specialAttributes)
     * @param regularAttributesList a list that would contain just the regular attributes that appear in the exampleSet
     * @param specialAttributes a map that would contain just the mapping of the names and the corresponding attributes that appear in the exampleSet
     */

    /**
     * @param attributes
     * @param attributesList
     * @param regularAttributesList
     * @param specialAttributes
     */
    public static void extractAttributesAsList(Attributes attributes, List<Attribute> attributesList, List<Attribute> regularAttributesList, Map<Attribute, String> specialAttributes) {
        Iterator<Attribute> attributeIterator;
        attributeIterator = attributes.allAttributes();

        while (attributeIterator.hasNext()) {
            Attribute originalAttribute = attributeIterator.next();
            Attribute attribute = AttributeFactory.createAttribute(originalAttribute);
            attribute.clearTransformations();
            if (attributesList != null) {
                attributesList.add(attribute);
            }
            AttributeRole attributeRole = attributes.getRole(originalAttribute);
            if (attributeRole.isSpecial()) {
                if (specialAttributes != null) {
                    specialAttributes.put(attribute, attributeRole.getSpecialName());
                }
            } else if (regularAttributesList != null) {
                regularAttributesList.add(attribute);
            }

        }
    }

    /**
     * @param inputSet
     * @return
     */
    public static ExampleSet duplicateExampleSet(ExampleSet inputSet) {
        Attributes attributes = inputSet.getAttributes();
        List<Attribute> attributesList = new ArrayList<Attribute>(attributes.allSize());
        List<Attribute> regularAttributesList = new ArrayList<Attribute>(attributes.size());
        Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>(attributes.allSize() - attributes.size()); //HashMap of Special attributes this map includes a list of attributes and its roles. Data structure for duplicating attributes                
        PRulesUtil.extractAttributesAsList(attributes, attributesList, regularAttributesList, specialAttributes);

        //ExampleTable outputTable = new MemoryExampleTable(attributesList,
        //        new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'),
        //        inputSet.size());
        //ExampleSet outputSet = new SimpleExampleSet(outputTable, regularAttributesList, specialAttributes);
        ExampleSet outputSet = ExampleSets.from(attributesList).withBlankSize(inputSet.size()).withRoles(specialAttributes).build();
        Iterator<Example> inputSetIterator = inputSet.iterator();
        Iterator<Example> outputSetIterator = outputSet.iterator();

        while (inputSetIterator.hasNext() && outputSetIterator.hasNext()) {
            Example inputExample = inputSetIterator.next();
            Example outputExample = outputSetIterator.next();
            Iterator<Attribute> inputAttributeIterator = attributes.allAttributes();
            Iterator<Attribute> outputAttributeIterator = attributesList.iterator();
            while (inputAttributeIterator.hasNext() && outputAttributeIterator.hasNext()) {
                Attribute outputAttribute = outputAttributeIterator.next();
                Attribute inputAttribute = inputAttributeIterator.next();
                outputExample.setValue(outputAttribute, inputExample.getValue(inputAttribute));
            }
        }
        return outputSet;
    }

    /**
     * @param exampleSets
     * @return
     */
    public static ExampleSet combineExampleSets(List<ExampleSet> exampleSets) {
        //First we create common set of attributes in case when one of prototypes set may contain different set of attributes
        int numberOfAllAttributes = exampleSets.get(0).getAttributes().allSize();
        int numberOfRegularAttributes = exampleSets.get(0).getAttributes().size();
        int numberOfSpecialAttributes = numberOfAllAttributes - numberOfRegularAttributes;
        int numberOfSamples = 0;
        Map<String, Attribute> attributesMap = new HashMap<String, Attribute>(numberOfAllAttributes);
        List<Attribute> attributesList = new ArrayList<Attribute>(numberOfAllAttributes);
        List<Attribute> regularAttributesList = new ArrayList<Attribute>(numberOfRegularAttributes);
        Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>(numberOfSpecialAttributes);
        numberOfAllAttributes = numberOfRegularAttributes = numberOfSpecialAttributes = 0;
        for (ExampleSet imputSet : exampleSets) {
            Attributes attributes = imputSet.getAttributes();
            Iterator<Attribute> attributeIterator = attributes.allAttributes();
            while (attributeIterator.hasNext()) {
                Attribute originalAttribute = attributeIterator.next();
                String attributeName = originalAttribute.getName();
                if (!attributesMap.containsKey(attributeName)) {
                    Attribute attribute = AttributeFactory.createAttribute(originalAttribute);
                    attribute.clearTransformations();
                    attributesList.add(attribute);
                    attributesMap.put(attributeName, attribute);
                    numberOfAllAttributes++;
                    AttributeRole attributeRole = attributes.getRole(originalAttribute);
                    if (attributeRole.isSpecial()) {
                        specialAttributes.put(attribute, attributeRole.getSpecialName());
                        numberOfSpecialAttributes++;
                    } else {
                        regularAttributesList.add(attribute);
                        numberOfRegularAttributes++;
                    }
                } else {
                    Attribute attribute = attributesMap.get(attributeName);
                    if (attribute.isNominal()) {
                        NominalMapping nominalMapping = attribute.getMapping();
                        NominalMapping nominalMappingOri = originalAttribute.getMapping();
                        List<String> nomStrOri = nominalMappingOri.getValues();
                        for (String s : nomStrOri) {
                            nominalMapping.mapString(s);
                        }
                    }
                }
            }
            numberOfSamples += imputSet.size();
        }
        //ExampleTable outputTable = new MemoryExampleTable(attributesList, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), numberOfSamples);
        //ExampleSet outputSet = new SimpleExampleSet(outputTable, regularAttributesList, specialAttributes);
        ExampleSet outputSet = ExampleSets.from(attributesList).withBlankSize(numberOfSamples).withRoles(specialAttributes).build();
        Iterator<Example> outputSetIterator = outputSet.iterator();

        for (ExampleSet inputSet : exampleSets) {
            Iterator<Example> inputSetIterator = inputSet.iterator();
            while (inputSetIterator.hasNext()) {
                Example inputExample = inputSetIterator.next();
                Example outputExample = outputSetIterator.next();
                Iterator<Attribute> inputAttributeIterator = inputSet.getAttributes().allAttributes();
                while (inputAttributeIterator.hasNext()) {
                    Attribute inputAttribute = inputAttributeIterator.next();
                    Attribute outputAttribute = attributesMap.get(inputAttribute.getName());
                    if (inputAttribute.isNumerical()) {
                        outputExample.setValue(outputAttribute, inputExample.getValue(inputAttribute));
                    } else {
                        outputExample.setValue(outputAttribute, inputExample.getValueAsString(inputAttribute));
                    }
                }
            }

        }

        return outputSet;
    }

    /**
     * This method selects from the input exampleSet sampleSize samples, such
     * that the number of selected samples is proportional to the number of
     * classes. This method does not reorder samples so in case of stratified
     * samples has to be randomized
     *
     * @param exampleSet      input exampleSet
     * @param sampleSize      number of samples to select
     * @param randomGenerator randomGenerator (used when it is impossible to
     *                        divide number of samples proportional to ocurence of the class labels)
     * @return data index
     */
    public static DataIndex stratifiedSelectionOfFirstSamplesFromEachClass(ExampleSet exampleSet, int sampleSize, RandomGenerator randomGenerator) {
        int realTrainingSetSize = exampleSet.size();
        Attribute labels = exampleSet.getAttributes().getLabel();
        if (sampleSize > realTrainingSetSize || labels == null || !labels.isNominal()) {
            return null;
        }
        DataIndex index = new DataIndex(realTrainingSetSize);
        index.setAllFalse();

        NominalMapping labelsMap = labels.getMapping();
        int[] classesCounter = new int[labelsMap.size()];
        //Because wectors are randomly ordered in the beginig by the abstract InstanceSelectionOperator we just need to select an appropriate subset
        //How many samples is from certain class
        for (Example instance : exampleSet) {
            int currentLabel = (int) instance.getLabel();
            classesCounter[currentLabel]++;
        }
        int sumClassCounterAfterResampling = 0;
        //Haw many samples we should obtain
        int[] classCounterAfterResampling = classesCounter.clone();
        for (int i = 0; i < classesCounter.length; i++) {
            classCounterAfterResampling[i] = (int) (sampleSize * classesCounter[i] / (double) realTrainingSetSize); //Here realTrainingSetSize is double, otherwise we would have  problem when dividing
            sumClassCounterAfterResampling += classCounterAfterResampling[i];
        }
        //If because of rounding finall number of samples is smaller then desired the add one for each missing class
        int numberOfClasses = labelsMap.size();
        while (sampleSize - sumClassCounterAfterResampling > 0) { //When 0 the desired sampleSize is equal to those whech will be selected
            int j = randomGenerator.nextInt(numberOfClasses);
            if (classesCounter[j] - classCounterAfterResampling[j] > 0) { //We need to check this incase of selected random class has not enought samples in the training set
                classCounterAfterResampling[j]++;
                sumClassCounterAfterResampling++;
            }
        }
        int i = 0;
        //Select instancess
        boolean[] checkFirstAccess = new boolean[labelsMap.size()];
        Arrays.fill(checkFirstAccess, true);
        int earlyStopCounter = labelsMap.size();
        for (Example instance : exampleSet) {
            int currentLabel = (int) instance.getLabel();
            if (classCounterAfterResampling[currentLabel] > 0) {
                index.set(i, true);
                classCounterAfterResampling[currentLabel]--;
            } else if (checkFirstAccess[currentLabel]) {
                checkFirstAccess[currentLabel] = false;
                earlyStopCounter--;
                if (earlyStopCounter == 0) {
                    break;
                }
            }
            i++;
        }
        return index;
    }

    /**
     * This method selects from the input exampleSet sampleSize samples, random
     * sample preserving class labels distribution
     *
     * @param exampleSet      input exampleSet
     * @param sampleSize      number of samples to select
     * @param randomGenerator randomGenerator (used when it is impossible to
     *                        divide number of samples proportional to ocurence of the class labels)
     * @return data index
     */
    public static DataIndex stratifiedSelection(ExampleSet exampleSet, int sampleSize, RandomGenerator randomGenerator) {
        int realTrainingSetSize = exampleSet.size();
        Attribute labels = exampleSet.getAttributes().getLabel();
        if (sampleSize > realTrainingSetSize || labels == null || !labels.isNominal()) {
            return null;
        }
        DataIndex index = new DataIndex(realTrainingSetSize);
        index.setAllFalse();

        NominalMapping labelsMap = labels.getMapping();
        int numClasses = labelsMap.size();
        int i;
        //Initialize classIndexer. It contains list of samples from given class
        List<Integer>[] classesIndexer = new List[numClasses];
        for (i = 0; i < numClasses; i++) {
            classesIndexer[i] = new ArrayList<Integer>(realTrainingSetSize / numClasses);
        }
        //Fillin classIndexer with samples id which belong to given class
        i = 0;
        for (Example instance : exampleSet) {
            int currentLabel = (int) instance.getLabel();
            classesIndexer[currentLabel].add(i++);
        }
        //Get class labels distribution
        int[] classCounter = new int[numClasses];
        for (i = 0; i < numClasses; i++) {
            classCounter[i] = classesIndexer[i].size();
        }
        //randomize each list
        for (i = 0; i < numClasses; i++) {
            int n = classCounter[i];
            for (int j = 0; j < n; j++) {
                int k = randomGenerator.nextInt(n);
                int vj = classesIndexer[i].get(j);
                int vk = classesIndexer[i].get(k);
                classesIndexer[i].set(k, vj);
                classesIndexer[i].set(j, vk);
            }
        }
        //Calculate samples distribution after resampling
        int sumClassCounterAfterResampling = 0;
        int[] classCounterAfterResampling = classCounter.clone();
        for (i = 0; i < numClasses; i++) {
            classCounterAfterResampling[i] = (int) (sampleSize * classCounter[i] / (double) realTrainingSetSize); //Here realTrainingSetSize is double, otherwise we would have  problem when dividing
            sumClassCounterAfterResampling += classCounterAfterResampling[i];
        }
        //NOTE that sumClassCounterAfterResampling < sampleSize
        //If this condition is true, add an extra sample to a random class. Repeat this procedure until  sumClassCounterAfterResampling == sampleSize
        while (sampleSize - sumClassCounterAfterResampling > 0) { //When 0 the desired sampleSize is equal to those whech will be selected
            int j = randomGenerator.nextInt(numClasses);
            if (classCounter[j] - classCounterAfterResampling[j] > 0) { //We need to check this incase of selected random class has not enought samples in the training set
                classCounterAfterResampling[j]++;
                sumClassCounterAfterResampling++;
            }
        }
        //Select instancess        
        for (i = 0; i < numClasses; i++) {
            //For i'th class get number of samples to be selected, and generate appropriate number of random ints
            for (int j = 0; j < classCounterAfterResampling[i]; j++) {
                //Select appropriate samples
                int ii = classesIndexer[i].get(j);
                index.set(ii, true);
            }
        }
        return index;
    }

    /**
     * This method selects from the input exampleSet sampleSize samples, such
     * that the number of selected samples is proportional to the number of
     * classes. This method does not reorder samples so in case of stratified
     * samples has to be randomized
     *
     * @param exampleSet      input exampleSet
     * @param sampleSize      number of samples to select
     * @param randomGenerator randomGenerator (used when it is impossible to
     *                        divide number of samples proportional to ocurence of the class labels)
     * @return data index
     */
    public static DataIndex stratifiedSelection(ExampleSet exampleSet, int sampleSize, int[] order, RandomGenerator randomGenerator) {
        int realTrainingSetSize = exampleSet.size();
        Attribute labels = exampleSet.getAttributes().getLabel();
        if (sampleSize > realTrainingSetSize || labels == null || !labels.isNominal() || order.length != realTrainingSetSize) {
            return null;
        }
        DataIndex index = new DataIndex(sampleSize);
        index.setAllFalse();

        NominalMapping labelsMap = labels.getMapping();
        int[] classesCounter = new int[labelsMap.size()];
        //Because wectors are randomly ordered in the beginig by the abstract InstanceSelectionOperator we just need to select an appropriate subset
        //How many samples is from certain class
        for (Example instance : exampleSet) {
            int currentLabel = (int) instance.getLabel();
            classesCounter[currentLabel]++;
        }
        int sumClassCounterAfterResampling = 0;
        //Haw many samples we should obtain
        int[] classCounterAfterResampling = classesCounter.clone();
        for (int i = 0; i < classesCounter.length; i++) {
            classCounterAfterResampling[i] = sampleSize * classesCounter[i] / realTrainingSetSize;
            sumClassCounterAfterResampling += classCounterAfterResampling[i];
        }
        //If because of rounding finall number of samples is smaller then desired the add one for each missing class
        int numberOfClasses = labelsMap.size();
        while (sampleSize - sumClassCounterAfterResampling > 0) { //When 0 the desired sampleSize is equal to those whech will be selected
            int j = randomGenerator.nextInt(numberOfClasses);
            if (classesCounter[j] - classCounterAfterResampling[j] > 0) { //We need to check this incase of selected random class has not enought samples in the training set
                classCounterAfterResampling[j]++;
                sumClassCounterAfterResampling++;
            }
        }
        int i = 0;
        //Select instancess
        for (Example instance : exampleSet) {
            int currentLabel = (int) instance.getLabel();
            if (classCounterAfterResampling[currentLabel] > 0) {
                index.set(i, true);
                classCounterAfterResampling[currentLabel]--;
            }
            i++;
        }
        return index;
    }

    /**
     * Method change the order of attributes to the given by the list of
     * attribute names First exampleSet is cloned, then the order of attributes
     * is adjusted according to the list
     *
     * @param exampleSet     input example set
     * @param attributeNames list of attribute names
     * @return
     */
    public static ExampleSet reorderAttributesByName(ExampleSet exampleSet, List<String> attributeNames) {
        ExampleSet newExampleSet = (ExampleSet) exampleSet.clone();
        Attributes originalAttributeNames = exampleSet.getAttributes();
        Attributes newAttributeNames = newExampleSet.getAttributes();
        newAttributeNames.clearRegular();
        for (String attributeName : attributeNames) {
            newAttributeNames.addRegular(originalAttributeNames.get(attributeName));
        }
        return newExampleSet;
    }

    /**
     * Method changes the order of attributes to the given by the list of
     * attribute names
     *
     * @param attributes     list of attributes
     * @param attributeNames list of attribute names (the order is important)
     * @return
     */
    public static List<Attribute> reorderAttributesByName(Attributes attributes, List<String> attributeNames) {
        List<Attribute> listOfAttributes = new ArrayList<>(attributes.size());
        for (String attributeName : attributeNames) {
            listOfAttributes.add(attributes.get(attributeName));
        }
        return listOfAttributes;
    }

    public static Set<Double> findUniqueLabels(ISPRGeometricDataCollection<IInstanceLabels> samples) {
        Iterator<IInstanceLabels> tmpLabelIterator = samples.storedValueIterator();
        Set<Double> uniqueLabels = new HashSet<>();
        while (tmpLabelIterator.hasNext()) {
            uniqueLabels.add(tmpLabelIterator.next().getLabel());
        }
        return uniqueLabels;
    }

    public static Map<Double, Integer> countClassFrequency(ISPRGeometricDataCollection<IInstanceLabels> samples) {
        Iterator<IInstanceLabels> tmpLabelIterator = samples.storedValueIterator();
        Map<Double, Integer> map = new HashMap<>();
        while (tmpLabelIterator.hasNext()) {
            double label = tmpLabelIterator.next().getLabel();
            if (map.containsKey(label)) {
                int counter = map.get(label);
                counter++;
                map.put(label, counter);
            } else {
                map.put(label, 1);
            }
        }
        return map;
    }

    public static double[] project(ExampleSet set, double[] vector) {
        Attributes attrs = set.getAttributes();
        int m = attrs.size();
        int n = set.size();
        assert m == vector.length : "Internal error. Incorect vector size when performing projection";
        for (Attribute a : attrs) {
            if (!a.isNumerical()) {
                LogService.getRoot().info("Incorect attribute type");
                throw new IncorrectAttributeException("Attribute: " + a.getName() + "must be numeric");
            }
        }

        double[] projection = new double[n];
        int j = 0;
        for (Example ex : set) {
            int i = 0;
            for (Attribute a : attrs) {
                projection[j] += ex.getValue(a) * vector[i];
                i++;
            }
            projection[j] /= m;
            j++;
        }
        return projection;
    }

    /**
     * Discretize of input double array into given number of bins, trying yto keep equal number of examples in all beens. This implementation is fast that is sort + linear in time, but when the number of unique values in the input data is significantly lower then the size of the input data, than there can be inconsistancy in the size of each bean.
     *
     * @param values - input array to be discretized
     * @param bins   - number of bins
     * @return array of lists. Each index in the array corresponds to given unique bin, and the list contains all elements which belong to this bin
     */
    public static List<Integer>[] discretizeFastEqFrequency(double[] values, int bins) {
        List<DoubleIntContainer> list = new ArrayList<>(values.length);
        int i = 0;
        for (double d : values) {
            list.add(new DoubleIntContainer(d, i));
            i++;
        }
        Collections.sort(list, (x, y) -> (x.first > y.first) ? 1 : (x.first == y.first) ? 0 : -1);
        int batch = values.length / bins;
        i = 0;
        int counter = 0;
        double oldValue = Double.NaN;
        //int[] res = new int[values.length];
        List<Integer>[] res = new List[bins];
        res[0] = new LinkedList<Integer>();
        for (DoubleIntContainer c : list) {
            if (c.first != oldValue) {
                oldValue = c.first;
                if (counter >= batch && i + 1 < bins) {
                    counter = 0;
                    i++;
                    res[i] = new LinkedList<Integer>();
                }
            }
            counter++;
            res[i].add(c.second);
        }
        return res;
    }

    public static boolean isSingleLabel(ISPRClassGeometricDataCollection<IInstanceLabels> samples) {
        Iterator<IInstanceLabels> iterator = samples.storedValueIterator();
        boolean out = true;
        if (iterator.hasNext()) {
            double oldLabel = iterator.next().getLabel();
            while (iterator.hasNext()) {
                if (iterator.next().getLabel() != oldLabel) {
                    out = false;
                    break;
                }
            }
        }
        return out;
    }

    public static boolean isSingleLabel(ExampleSet samples) {
        if (samples.size()==0) return true;
        boolean out = true;
        double oldLabel = samples.getExample(0).getLabel();
        for (Example ex : samples) {
            if (ex.getLabel() != oldLabel) {
                out = false;
                break;
            }
        }
        return out;
    }
}
