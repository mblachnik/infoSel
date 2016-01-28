/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools.genetic;

import com.rapidminer.ispr.tools.math.container.DoubleDoubleContainer;
import com.rapidminer.tools.container.Pair;

/**
 *
 * @author Marcin
 */
public class Chromosome {

    boolean[] chromosome;
    //GeneticSplitter splitter;
    RandomGenerator random;
    DoubleDoubleContainer[] bitMutationProbabilities; //This parameter describes the chance that the certain bit will be mutated according to its value. For example we may prefare the mutation from 1 to 0 and reduce the probability of mutation from 0 to 1. Now it is possible. The first element describes the mutation from 1 to 0, and the second from 0 to 1

    /**
     *
     * @param size
     */
    public Chromosome(int size) {
        chromosome = new boolean[size];
        bitMutationProbabilities = new DoubleDoubleContainer[size];
        for (int i = 0; i < bitMutationProbabilities.length; i++) {
            bitMutationProbabilities[i] = new DoubleDoubleContainer(1.0, 1.0);
        }
    }

    /**
     *
     * @param chromosome
     */
    public Chromosome(Chromosome chromosome) {
        this.chromosome = chromosome.chromosome.clone();
        this.random = chromosome.random;
        bitMutationProbabilities = chromosome.bitMutationProbabilities.clone();
    }

    public void setBitMutationProbabilities(DoubleDoubleContainer[] bitMutationProbabilities) {
        this.bitMutationProbabilities = bitMutationProbabilities;
    }
    
    public void setBitMutationProbabilities(int bitId, DoubleDoubleContainer bitMutationProbabilities) {
        this.bitMutationProbabilities[bitId] = bitMutationProbabilities;
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean mutateBit(int id) {
        boolean chk = false;
        double mutationProb = chromosome[id] ? bitMutationProbabilities[id].getFirst() : bitMutationProbabilities[id].getSecond();
        double val = random.nextDouble();
        if (val < mutationProb) {
            chromosome[id] = !chromosome[id];
            chk = true;
        }
        return chk;
    }
    
    /**
     * This method mutates single bit of the chromosome. The bit is identified by random. 
     */
    public void mutateBit(){
        int id;
        do{
            id = random.nextInteger(this.chromosome.length);
        } while( !mutateBit(id));
    }

    /**
     * This method assure mutation of a signle bit in range from start to end bit
     * @param startBit - first bit
     * @param endBit - last bit 
     */
    public void mutateBit(int startBit, int endBit) {
        int id;
        int range = endBit - startBit;
        do{
            id = random.nextInteger(range);
        } while( !mutateBit(startBit + id));
    }
    
    /**
     *
     * @param id
     */
    public void setBit(int id) {
        chromosome[id] = true;
    }

    /**
     *
     * @param id
     */
    public void resetBit(int id) {
        chromosome[id] = false;
    }

    /**
     *
     * @param id
     * @param val
     */
    public void setBit(int id, boolean val) {
        chromosome[id] = val;
    }

    /**
     *
     * @param id
     * @return
     */
    public boolean getBit(int id) {
        return chromosome[id];
    }

    /**
     *
     * @return
     */
    public boolean[] getChromosome() {
        return chromosome;
    }

    /**
     *
     * @param random
     */
    public void setRandomGenerator(RandomGenerator random) {
        this.random = random;
    }

    /**
     *
     */
    public void randomize() {        
        for (int i = 0; i < chromosome.length; i++) {
            mutateBit(random.nextInteger(this.chromosome.length));
        }
    }

    public void randomize(int firstBit, int lastBit) {
        int range = lastBit - firstBit;
        for (int i = 0; i < range; i++) {
            int id = random.nextInteger(range);
            mutateBit(firstBit + id);
        }
    }
}
