/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.container.Tupel;
import com.rapidminer.tools.math.container.BoundedPriorityQueue;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;

/**
 *    The algorithm implementation follows the Randy Wilson source code, 
 *   randy@axon.cs.byu.edu, http://axon.cs.byu.edu/~randy
 *  Computer Science Department, Brigham Young University, Provo, UT 84602
 *  July 8, 1996 - May 1, 1997.
 *   IB3 [Aha, 1991, 1992]. Allows only 'acceptable' instances to
 *   remain in the training set.  Uses the following algorithm:
 *
 *    For each instance t in T
 *      Let n be the nearest 'acceptable' instance in S to t
 *      (if no acceptable instances in S, let n be random instance in S)
 *      if class(n) != class(t) then add t to S.
 *      For each instance s in S
 *        if s is at least as close to t as n is
 *	   then update the classification record of S
 *	   and remove s from S if its classification record is
 *	  
 * @author Marcin
 */
public class IB3InstanceSelectionModel extends AbstractInstanceSelectorModel {
/*   IB3 [Aha, 1991, 1992]. Allows only 'acceptable' instances to
     remain in the training set.  Uses the following algorithm:

     For each instance t in T
       Let n be the nearest 'acceptable' instance in S to t
       (if no acceptable instances in S, let n be random instance in S)
       if class(n) != class(t) then add t to S.
       For each instance s in S
         if s is at least as close to t as n is
	   then update the classification record of S
	   and remove s from S if its classification record is
	  */
    DistanceMeasure measure;
    int k;
    double upperInterval, lowerInterval;
    transient double[] tmpClassFrequency; //temporal variable declared here to avoid realocation
    int numClasses;
    RandomGenerator randomGenerator;
    //IISDecisionFunction loss;

    /**
     * Constructor of the IB3InstanceSelectionModel
     * @param measure - distance measure function
     * @param k - number of nearest neighbors
     * @param upperInterval - upper interval of acceptance
     * @param lowerInterval - lower interval of acceptance
     * @param randomGenerator - if dataset should be randomized before performing instance selection
     */
    //public IB3InstanceSelectionModel(DistanceMeasure measure, int k, double upperInterval, double lowerInterval, RandomGenerator randomGenerator, IISDecisionFunction loss){
    public IB3InstanceSelectionModel(DistanceMeasure measure, int k, double upperInterval, double lowerInterval, RandomGenerator randomGenerator){
        this.upperInterval = upperInterval;
        this.lowerInterval = lowerInterval;
        this.measure = measure;
        this.k = k;
        this.randomGenerator = randomGenerator;
        //this.loss = loss;
    }
        
    /**
     * Performs instance selection
     * @param exampleSet - example set for which instance selection will be performed
     * @return - index of selected examples
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        EditedExampleSet selectedSet      = new EditedExampleSet(exampleSet);
        EditedExampleSet trainingSet      = new EditedExampleSet(exampleSet);
        EditedExampleSet notAcceptableSet =  new EditedExampleSet(exampleSet);
        //loss.init(exampleSet, measure);
        Attribute labelAttribute = exampleSet.getAttributes().getLabel();
        NominalMapping labelMapping = labelAttribute.getMapping();
        numClasses = labelMapping.size();           
        int numInstances = exampleSet.size();
        double[] classCounter       = new double[numClasses]; //class counter
        double[] classMinConfidence = new double[numClasses]; //class confidence        
                 tmpClassFrequency  = new double[numClasses];
        double[] instanceSuccessCounter = new double[numInstances];
        double[] instanceTrialCounter   = new double[numInstances];
        double[] distCache              = new double[numInstances];

        DataIndex trainingIndex      = trainingSet.getIndex();
        DataIndex selectedIndex      = selectedSet.getIndex();
        DataIndex notAcceptableIndex = notAcceptableSet.getIndex();        
        selectedIndex.setAllFalse();
        notAcceptableIndex.setAllFalse();
        //Set first instance as selected
        int i = 0,j,labelID, cn = 1;
        selectedIndex.set(i, true);
        trainingIndex.set(i, false);                        
        instanceSuccessCounter[i] = 1;
        instanceTrialCounter[i]   = 1;                
        //Initialize class and confidence freq
        for (labelID=0; labelID<numClasses; labelID++) {            
            classCounter[labelID] = 0.0;            
            classMinConfidence[labelID] = minConfidence(classCounter[labelID], cn, upperInterval);
        }  
        labelID = (int)selectedSet.getExample(i).getLabel();
        classCounter[labelID] = 1.0;
        classMinConfidence[labelID] = minConfidence(classCounter[labelID], cn, upperInterval);
        
        int attributeSize = exampleSet.getAttributes().size();
        double[] exampleValues         = new double[attributeSize]; //cache for example values
        double[] selectedExampleValues = new double[attributeSize]; //cache for example values of SelectedSet               
        BoundedPriorityQueue<Tupel<Double, Integer>> nearestAcceptable = new BoundedPriorityQueue<Tupel<Double, Integer>>(k);
        //BoundedPriorityQueue<Tupel<Double, Integer>> nearestOther      = new BoundedPriorityQueue<Tupel<Double, Integer>>(k);
        double maxDist;        
        //For each instance in trainingSet
        for (Example example : trainingSet) {
            //Initialization
            j = ((ISPRExample) example).getIndex();
            nearestAcceptable.clear();
            notAcceptableIndex.setAllFalse();
            KNNTools.extractExampleValues(example, exampleValues);                        
            for (labelID = 0; labelID<numClasses; labelID++) {                
                classMinConfidence[labelID] = maxConfidence(classCounter[labelID], cn, upperInterval);
            }            
            //Search for nearest acceptable instances in selectedSet. If a selected instance is not acceotable then it marked as a member of notAcceptableSet 
            for (Example selectedExample : selectedSet) {
                KNNTools.extractExampleValues(selectedExample, selectedExampleValues);
                double dist = measure.calculateDistance(exampleValues, selectedExampleValues);                
                i = ((ISPRExample) selectedExample).getIndex();
                distCache[i] = dist;
                labelID = (int)selectedExample.getLabel();
                if ((instanceTrialCounter[i] > 0) && (minConfidence(instanceSuccessCounter[i], instanceTrialCounter[i], upperInterval) > classMinConfidence[labelID])) {
                    nearestAcceptable.add(new Tupel<Double,Integer>(dist, labelID));
                } else {                    
                    notAcceptableIndex.set(i,true);
                    //nearestOther.add(new Tupel<Double,Integer>(dist, labelID));
                }                
            }
            //If number of acceptable instances is smaller then "k", then we search by random in notAcceptableSet and add as many as necesarry to meet "k" neighbors
            int otherSize;
            Tupel<Double,Integer> other;
            //while ((nearestAcceptable.size()<k) && (other = nearestOther.poll()) != null) {                
            while ((nearestAcceptable.size()<k) && (otherSize = notAcceptableSet.size()) > 0) {                
                int id = randomGenerator.nextInt(otherSize);
                Example otherExample = notAcceptableSet.getExample(id);
                other = new Tupel<Double,Integer>(distCache[i],(int)otherExample.getLabel());
                nearestAcceptable.add(other);
            }
            //Predicting class label (similar to IB2)
            Tupel<Double,Double> result = predictLabelAndMaxDist(nearestAcceptable);            
            double predictedLabel = result.getSecond();
            if (example.getLabel() != predictedLabel) {                
                selectedIndex.set(j, true);
            } else {
                selectedIndex.set(j, false);
            }
            
            //Recalculation of class confidence
            labelID = (int)example.getLabel();
            classCounter[labelID]++;            
            cn++;
            for (labelID = 0; labelID < numClasses; labelID++) {                
                classMinConfidence[labelID] = minConfidence(classCounter[labelID], cn, lowerInterval);
            }
            maxDist = result.getFirst();
            //From selectedSet we chack if any could be removed by applying the lower bound of confdence interval
            for (Example selectedExample : selectedSet){
                i = ((ISPRExample) selectedExample).getIndex();                
                //Only the "k" nearest neighbors are considered
                if (distCache[i] <= maxDist){
                    instanceTrialCounter[i]++;
                    if(selectedExample.getLabel() == example.getLabel()){
                        instanceSuccessCounter[i]++;
                    }                        
                    //The confidence is recalculated
                    if(maxConfidence(instanceSuccessCounter[i],instanceTrialCounter[i],lowerInterval) < classMinConfidence[(int)selectedExample.getLabel()]){
                        selectedIndex.set(i,false);
                    }
                }                    
            }

        }
        //Final analysis
        //First recalculate the class confidence
        for (labelID = 0; labelID<numClasses; labelID++) {
            classMinConfidence[labelID] = minConfidence(classCounter[labelID], cn, upperInterval);            
        }
        //Then the inequality is verified to check if any selectedExample could be removed
        for (Example ex : selectedSet) {
            i = ((ISPRExample) ex).getIndex();
            int label = (int)ex.getLabel();
            if (minConfidence(instanceSuccessCounter[i], instanceTrialCounter[i], upperInterval) <= classMinConfidence[label]) {
                selectedIndex.set(i, false);
            }
        }
        return selectedIndex;        
    }

    private Tupel<Double,Double> predictLabelAndMaxDist(BoundedPriorityQueue<Tupel<Double, Integer>> nearestAcceptable) {                
        double maxDist = -1;  
        //Clear the classFrequency cache
        Arrays.fill(tmpClassFrequency, 0, numClasses, 0);
        //Calculate the class frequency of "k" nearest neighbors
        for (Tupel<Double, Integer> tupel : nearestAcceptable) {
            double dist = tupel.getFirst();
            if (dist > maxDist) {
                maxDist = dist;
            }
            int tmpLabel = tupel.getSecond();
            tmpClassFrequency[tmpLabel]++;            
        }
        double mostFreqValue = -1;
        double mostFreqClass = -1;
        //find the most frequent class
        for(int i=0; i<numClasses; i++) {
            double value = tmpClassFrequency[i];
            if (mostFreqValue < value){
                mostFreqValue = value;
                mostFreqClass = i;
            }
        }                
        return new Tupel<Double,Double>(maxDist,mostFreqClass);
    }

    /**
     * Returns high end of confidence interval, given:     
     * @param y the number of successes
     * @param n the number of trials
     * @param z the confidence level
     * @return 
     */
    private double maxConfidence(double y, double n, double z) {
        if (n == 0.0) {
            return 1;
        } else {
            double frequency = y / n;
            double z2 = z * z;
            double n2 = n * n;
            double numerator, denominator, val;
            val = z * Math.sqrt((frequency * (1.0 - frequency) / n) + z2 / (4 * n2));
            numerator = frequency + z2 / (2 * n) + val;
            denominator = 1.0 + z2 / n;
            return (numerator / denominator);
        }
    }

    /**
     * Returns low end of confidence interval, given:     
     * @param y the number of successes
     * @param n the number of trials
     * @param z the confidence level
     * @return 
     */
    private double minConfidence(double y, double n, double z) {        
        if (n == 0.0) {
            return 0;
        } else {
            double frequency = y / n;
            double z2 = z * z;
            double n2 = n * n;
            double numerator, denominator, val;
            val = z * Math.sqrt((frequency * (1.0 - frequency) / n) + z2 / (4 * n2));
            numerator = frequency + z2 / (2 * n) - val;
            denominator = 1.0 + z2 / n;
            return (numerator / denominator);
        }
    }
}
