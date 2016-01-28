/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;

import java.util.Arrays;

/**
 *
 * @author Marcin
 */
public class Associates {
    int[][] index;
    int[] sizes;
    int n, k;
    public Associates(int n, int k){
        this.n = n;
        this.k = k;
        index = new int[n][k];
        sizes = new int[n];
        for(int[] i : index){
            Arrays.fill(i, -1);
        }
    }
    
    public void set(int i, int j, int value){
        index[i][j] = value;
    }    
    
    public int get(int i, int j){
        return index[i][j];
    }
    
    public int[] get(int i){
        return index[i];
    }
    
    public void add(int i, int value){
        index[i][sizes[i]] = value;
        sizes[i]++;
    }
    
}
