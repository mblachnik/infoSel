/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;

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
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.RandomGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
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
     *
     * @param size
     * @param randomGenerator
     * @return
     */
    public static int[] randomPermutation(int size, Random randomGenerator) {
        int[] idx = new int[size];
        for (int i=1; i<size; i++){
            idx[i]=i;
        }
        for (int i=size; i>1; i--){
            int k = randomGenerator.nextInt(i);
            int t = idx[k];
            idx[k] = idx[i-1];               
            idx[i-1] = t;                                
        }                
        return idx;
    }

    /**
     *
     * @param size
     * @param randomGenerator     
     */
    public static void randomPermutation(int[] tab, Random randomGenerator) {
        int size = tab.length;
        for (int i=size; i>1; i--){
            int k = randomGenerator.nextInt(i);
            int t = tab[k];
            tab[k] = tab[i];               
            tab[i] = t;                                
        }                        
    }
    
    /**
     *
     * @param size
     * @param numberOfInstancesToSelect
     * @param randomGenerator
     * @return
     */
    public static int[] randomSelection(int size, int numberOfInstancesToSelect, Random randomGenerator) {
        if (size < numberOfInstancesToSelect) {
            throw new IndexOutOfBoundsException();
        } else {
            if (size == numberOfInstancesToSelect) {
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
    }

    /**
     *
     * @param counter
     * @return
     */
    public static int findMostFrequentValue(double[] counter) {
        int mostFrequentIndex = Integer.MIN_VALUE;
        double mostFrequentFrequency = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < counter.length; j++) {
            if (mostFrequentFrequency < counter[j]) {
                mostFrequentFrequency = counter[j];
                mostFrequentIndex = j;
            }
        }
        return mostFrequentIndex;
    }

    /**
     *
     * @param counter
     * @return
     */
    public static int findMostFrequentValue(int[] counter) {
        int mostFrequentIndex = Integer.MIN_VALUE;
        double mostFrequentFrequency = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < counter.length; j++) {
            if (mostFrequentFrequency < counter[j]) {
                mostFrequentFrequency = counter[j];
                mostFrequentIndex = j;
            }
        }
        return mostFrequentIndex;
    }

    /* This method returns extracted list of all kind of attributess one may require ex. for creating new ExampleSet
     * If any of the input attributeLists is empty then it is not considered in the extraction process.
     * @param exampleSet input example set from witch the alltributes will be extracted
     * @param attributesList a list that would contain all the attributes that appear in the exampleSet (a concatenation of regularAttributesList and specialAttributes)
     * @param regularAttributesList a list that would contain just the regular attributes that appear in the exampleSet
     * @param specialAttributes a map that would contain just the mapping of the names and the corresponding attributes that appear in the exampleSet
     */
    /**
     *
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
            } else {
                if (regularAttributesList != null) {
                    regularAttributesList.add(attribute);
                }
            }

        }
    }

    /**
     *
     * @param inputSet
     * @return
     */
    public static ExampleSet duplicateExampleSet(ExampleSet inputSet) {
        Attributes attributes = inputSet.getAttributes();
        List<Attribute> attributesList = new ArrayList<Attribute>(attributes.allSize());
        List<Attribute> regularAttributesList = new ArrayList<Attribute>(attributes.size());
        Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>(attributes.allSize() - attributes.size()); //HashMap of Special attributes this map includes a list of attributes and its roles. Data structure for duplicating attributes                
        PRulesUtil.extractAttributesAsList(attributes, attributesList, regularAttributesList, specialAttributes);

        ExampleTable outputTable = new MemoryExampleTable(attributesList,
                new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'),
                inputSet.size());
        ExampleSet outputSet = new SimpleExampleSet(outputTable, regularAttributesList, specialAttributes);

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
     *
     * @param exampleSets
     * @return
     */
    public static ExampleSet combineExampleSets(List<ExampleSet> exampleSets) {
        //First we create common set of attributes in case when one of prototypes set may contain different set of attributes
        int numberOfAllAttributes = exampleSets.get(0).getAttributes().allSize();
        int numberOfReularAttributes = exampleSets.get(0).getAttributes().size();
        int numberOfSpecialAttributes = numberOfAllAttributes - numberOfReularAttributes;
        int numberOfSamples = 0;
        HashMap<String, Attribute> attributesMap = new HashMap<String, Attribute>(numberOfAllAttributes);
        List<Attribute> attributesList = new ArrayList<Attribute>(numberOfAllAttributes);
        List<Attribute> regularAttributesList = new ArrayList<Attribute>(numberOfReularAttributes);
        Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>(numberOfSpecialAttributes);
        numberOfAllAttributes = numberOfReularAttributes = numberOfSpecialAttributes = 0;
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
                        numberOfReularAttributes++;
                    }
                }
            }
            numberOfSamples += imputSet.size();
        }
        ExampleTable outputTable = new MemoryExampleTable(attributesList, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), numberOfSamples);
        ExampleSet outputSet = new SimpleExampleSet(outputTable, regularAttributesList, specialAttributes);

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
                    outputExample.setValue(outputAttribute, inputExample.getValue(inputAttribute));
                }
            }

        }

        return outputSet;
    }


    /**
     * This method selects from the input exampleSet sampleSize samples, such that the number of selected samples is proportional to the number of classes. This method does not reorder samples so in case of stratified samples has to be randomized
     * @param exampleSet input exampleSet
     * @param sampleSize number of samples to select
     * @param randomGenerator randomGenerator (used when it is impossible to divide number of samples proportional to ocurence of the class labels)
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
        int[] classesCounter = new int[labelsMap.size()];
        //Because wectors are randomly ordered in the beginig by the abstract InstanceSelectionOperator we just need to select an appropriate subset
        //How many samples is from certain class
        for (Example instance : exampleSet) {
            int currentLabel = (int) instance.getLabel();
            classesCounter[currentLabel]++;
        }
        int sumClassCounterAfterResampling = 0;
        //Haw many samples we should obtain
        int[] classCounterAfterResampling = (int[]) classesCounter.clone();
        for (int i = 0; i < classesCounter.length; i++) {
            classCounterAfterResampling[i] = (int)(sampleSize * classesCounter[i] / (double)realTrainingSetSize); //Here realTrainingSetSize is double, otherwise we would have  problem when dividing
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
            } else {
                if (checkFirstAccess[currentLabel]){
                    checkFirstAccess[currentLabel] = false;
                    earlyStopCounter--;
                    if (earlyStopCounter==0) break;
                }
            }
            i++;
        }
        return index;
    }
    
    /**
     * This method selects from the input exampleSet sampleSize samples, such that the number of selected samples is proportional to the number of classes. This method does not reorder samples so in case of stratified samples has to be randomized
     * @param exampleSet input exampleSet
     * @param sampleSize number of samples to select
     * @param randomGenerator randomGenerator (used when it is impossible to divide number of samples proportional to ocurence of the class labels)
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
        int[] classCounterAfterResampling = (int[]) classesCounter.clone();
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
}
