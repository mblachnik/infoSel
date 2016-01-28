package com.rapidminer.ispr.operator.learner.tools.genetic;

/**
 *
 * @author Marcin
 */
public class IntNaturalBinaryCoding implements BinaryCoding{
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
private final int min,max;

    /**
     * 
     * @param min
     * @param max
     */
    public IntNaturalBinaryCoding(int min, int max) {
        this.min = min;
        this.max = max;
    }
    
    /**
     * 
     * @param bits
     * @param startBit
     * @param endBit
     * @return
     */
    @Override
    public double decode(boolean[] bits, int startBit, int endBit) {
        double val = 0;
        int k = 0;
        int dx = endBit - startBit;
        for(int i=startBit; i<=endBit; i++){
            double d = bits[i] ? 1.0 : 0.0;
            val += d*Math.pow(2,k);
            k++;
        }
        val = this.min + val;
        return val;
    }    
    
    /**
     * 
     * @param val
     * @param bits
     * @param startBit
     * @param lastBit
     */
    @Override
    public void code(double val, boolean[] bits, int startBit, int lastBit){        
        int dx = lastBit - startBit;
        val = val - this.min;        
        int bitVal = (int)val;
        for(int i=startBit; i<=lastBit; i++){            
            int r = bitVal % 2;            
            bits[i] = r == 1;
            bitVal /= 2;
        }        
    }
    
    @Override
    public void code(double val, boolean[] bits){
        code(val, bits, 0, bits.length-1);
    }
    
    @Override
    public double decode(boolean[] bits){
        return decode(bits,0,bits.length-1);
    }
        
}

