/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools.genetic;

import java.util.ArrayList;

/**
 *
 * @author Marcin
 */
public class GeneticSplitter {

    ArrayList<Splitter> splitter;    

    /**
     * 
     */
    public GeneticSplitter() {
        splitter = new ArrayList<Splitter>();
    }

    /**
     * 
     * @param min
     * @param max
     * @param coding
     * @return
     */
    public int addSplit(int min, int max, BinaryCoding coding) {
        Splitter s = new Splitter();
        s.cooding = coding;
        s.min = min;
        s.max = max;
        splitter.add(s);        
        return splitter.size()-1;
    }
    
    public int getSplitterID(Splitter spliter) {        
        return splitter.indexOf(spliter);
    }
    
    public Splitter getSplitter(int spliter) {        
        return splitter.get(spliter);
    }
    
    public Splitter removeSplitter(int spliter) {                
        return splitter.remove(spliter);
    }
    

    /**
     * 
     * @param id
     * @param chromosome
     * @return
     */
    public double decode(int id, Chromosome chromosome) {
        Splitter s = splitter.get(id);
        if (chromosome.chromosome.length > s.max || s.min >= 0) {
            return s.cooding.decode(chromosome.chromosome, s.min, s.max);
        } else {
            throw new IndexOutOfBoundsException("Splitter range out of chromosome length");
        }
    }

    /**
     * 
     * @param id
     * @param val
     * @param chromosome
     */
    public void code(int id, double val, Chromosome chromosome) {
        Splitter s = splitter.get(id);
        if (chromosome.chromosome.length > s.max || s.min >= 0) {
            s.cooding.code(val, chromosome.chromosome, s.min, s.max);
        } else {
            throw new IndexOutOfBoundsException("Splitter range out of chromosome length");
        }
    }

    /**
     * 
     * @param chromosome
     * @return
     */
    public double[] decode(Chromosome chromosome) {
        double[] vals = new double[splitter.size()];
        int i = 0;
        for (Splitter s : splitter) {
            if (chromosome.chromosome.length > s.max || s.min >= 0) {
                vals[i] = s.cooding.decode(chromosome.chromosome, s.min, s.max);
                i++;
            } else {
                throw new IndexOutOfBoundsException("Splitter range out of chromosome length");
            }
        }
        return vals;
    }

    /**
     * 
     * @param chromosome
     * @param vals
     */
    public void code(Chromosome chromosome, double[] vals) {
        int i = 0;
        for (Splitter s : splitter) {
            if (chromosome.chromosome.length > s.max || s.min >= 0) {
                s.cooding.code(vals[i], chromosome.chromosome, s.min, s.max);
                i++;
            } else {
                throw new IndexOutOfBoundsException("Splitter range out of chromosome length");
            }
        }
    }

    /**
     * 
     * @return
     */
    public int getSize() {
        return splitter.size();
    }
}