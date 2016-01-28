/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools.genetic;

/**
 *
 * @author Marcin
 */
public class test {
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        LinearNaturalBinaryCoding a = new LinearNaturalBinaryCoding(0, 4);
        Chromosome c = new Chromosome(10);
        GeneticSplitter g = new GeneticSplitter();
        g.addSplit(0, 4, a);
        g.addSplit(5, 9, a);
        double[] val = {1,4};
        g.code(c, val);
        for (int i=0; i<val.length; i++){
            System.out.println("Zakodowano "+val[i]);
        }
        for (int i = 0; i < c.chromosome.length; i++) {
            System.out.print(" "+c.chromosome[i]);            
        }
        System.out.println("");
        val = g.decode(c);
        for (int i=0; i<val.length; i++){
            System.out.println("Zdekodowano "+val[i]);
        }
    }
    
}
