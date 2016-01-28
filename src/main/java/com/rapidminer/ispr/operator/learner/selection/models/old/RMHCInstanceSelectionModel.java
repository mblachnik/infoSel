/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.genetic.BinaryCoding;
import com.rapidminer.ispr.operator.learner.tools.genetic.Chromosome;
import com.rapidminer.ispr.operator.learner.tools.genetic.GeneticSplitter;
import com.rapidminer.ispr.operator.learner.tools.genetic.IntNaturalBinaryCoding;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
//import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.ispr.operator.learner.tools.genetic.RandomGenerator;


/**
 *
 * @author Marcin
 */
public class RMHCInstanceSelectionModel extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int numberOfPrototypes;
    private final RandomGenerator randomGenerator;
    private final int iterations;
    private final IISDecisionFunction loss;

    /**
     *
     * @param measure
     * @param populationSize
     * @param iterations
     * @param randomGenerator
     * @param loss
     */
    public RMHCInstanceSelectionModel(DistanceMeasure measure, int populationSize, int iterations, RandomGenerator randomGenerator, IISDecisionFunction loss) {
        this.measure = measure;
        this.randomGenerator = randomGenerator;
        this.numberOfPrototypes = populationSize;
        this.iterations = iterations;
        this.loss = loss;
    }

    /**
     *
     * @param exampleSet
     * @return
     */
    @Override
    public DataIndex selectInstances(SelectedExampleSet exampleSet) {
        //loss.init(exampleSet);
        //TODO make use of the loss function, because it is not used now
        //TODO Update the code, because now it repeates the iterations if selected instance is out of range (based on bit enciding) and this may not be good. Moreover it may happen that certain instance is selected more then once, so the final number of samples mey not be equalt to the number of prototypes which should be selected 
        int size = exampleSet.size();
        double bitsD = Math.log(size) / Math.log(2.0);
        int bitsPerProto = (int) Math.ceil(bitsD);
        int totalBits = bitsPerProto * numberOfPrototypes;
        //Preparing chromosome - it reserves sveral bits in chromosome
        Chromosome chromosome = new Chromosome(totalBits);
        Chromosome bestChromosome = chromosome;
        chromosome.setRandomGenerator(randomGenerator);        
        //Here we divide the binary representation basede on number of features 
        //we wont to encode. For that purpose we use spliter. Because we can have different
        //number of bits for different features, and they may be encoded using different 
        //coding, so we define independent codding for each feature. Here we have 
        //fixed number of features and each feature use same codding so below we do that
        GeneticSplitter splitter = new GeneticSplitter();
        BinaryCoding coding = new IntNaturalBinaryCoding(0, size - 1);
        for (int i = 0; i < numberOfPrototypes; i++) {
            int splitMin = i * bitsPerProto;
            int splitMax = (i + 1) * bitsPerProto - 1;
            int splitterId = splitter.addSplit(splitMin, splitMax, coding);                                        
            int instanceId = randomGenerator.nextInteger(size);
            splitter.code(splitterId, instanceId, chromosome);                                        
        }
        
        double[] idx,bestIdx;        

        Attributes attributes = exampleSet.getAttributes();
        double[] values = new double[attributes.size()];
        double bestAcc = -1;
        DataIndex bestIndex = null;
        EditedExampleSet workingSet = new EditedExampleSet(exampleSet);
        DataIndex index = workingSet.getIndex();        
        for (int i = 0; i < iterations; i++) {
            index.setAllFalse();
            idx = splitter.decode(chromosome);
            double acc = 0;
            //System.out.println("Generation");
            for (int j = 0; j < idx.length; j++) {
                //idx[j] = idx[j] >= size ? size - 1 : idx[j];
                //System.out.println((int)idx[j]);
                index.set((int) idx[j], true);
            }
            //GeometricDataCollection<Integer> kNN = KNNTools.initializeKNearestNeighbour(workingSet, measure);
            for (Example ex : exampleSet) {
                int k = 0;
                for (Attribute a : attributes) {
                    values[k] = ex.getValue(a);
                    k++;
                }
                //acc += KNNTools.predictNearestNeighbor(workingSet, values, ex.getLabel(), measure) ? 1 : 0;
                acc += KNNTools.predictOneNearestNeighbor(workingSet, values, measure) == ex.getLabel() ? 1 : 0;
            }
            //
            if (acc > bestAcc) {
                bestAcc = acc;
                bestIndex = new DataIndex(index);
                bestChromosome = chromosome;
                bestIdx = idx;
            }
            chromosome = bestChromosome;
            //Choose which prototype to change
            int splitterId = randomGenerator.nextInteger(numberOfPrototypes);                                        
            //Read the id of current prototype
            int instanceId, id = (int) splitter.decode(splitterId, chromosome);
            //Get new prototype id and assure that the value is different from the previous one
            do{
                instanceId = randomGenerator.nextInteger(size);                
            }while(instanceId == id);
            splitter.code(splitterId, instanceId, chromosome);            
        }
        if (bestIndex == null) {
            throw new NullPointerException("Something went wrong, please check the number of iterations or other parameters");
        }
        return bestIndex;
    }
}
