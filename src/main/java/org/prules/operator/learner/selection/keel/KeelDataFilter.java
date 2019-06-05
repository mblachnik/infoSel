/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.keel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

/**
 * This class is used to combine ISPR with Keel project.
 * In Keel project we obtain already filtered dataset, but we don't know index of filtered samples. 
 * The KeelDataFilter is used to identify which samples were removed. The filter first 
 * creates a map which stores input data as strings such that each sample is 
 * represented as text including class label.
 * Then, when running filterSamples we again convert each sample of the dataset returned by keel into text, and we try to 
 * find this sample in "map" variable. When we find it we remove this sample from map. At the end the map contains a list of 
 * samples which were rejected by keel algorithm. Unfortunatelly this introduces overhead 
 * in computation but otherwise we will not be able to recognize which sample was removed and which not.
 * 
 * @author Marcin
 */
public class KeelDataFilter {

    Map<String, ArrayList<Integer>> map;
    int n, m;
    int numberOfMisses;    

    public KeelDataFilter(double[][] trainingSet, int[] labels) {
        numberOfMisses = 0;
        n = trainingSet.length;
        m = n > 0 ? trainingSet[0].length : -1;
        map = new HashMap<>(n); //Map used to identify identical rows - here we have List<Integer> becouse two rows may be identical, each with different id        
        for (int i = 0; i < n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < m; j++) {
                double v = trainingSet[i][j];
                sb.append(v).append(";");                
            }
            int l = labels[i];
            sb.append("|").append(l);
            //This part is to identify selected samples
            String s = sb.toString();
            if (map.containsKey(s)) { //If given row already exist
                map.get(s).add(i);   //Add new id             
            } else {
                ArrayList<Integer> li = new ArrayList<>(); //Create new list and add ID
                li.add(i);
                map.put(s, li);
            }
        }
    }

    public IDataIndex filterSamples(double[][] trainingSet, int[] labels) {
        IDataIndex index = new DataIndex(n);
        for (int i = 0; i < trainingSet.length; i++) {
            //Extract row as string
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < m; j++) {
                double v = trainingSet[i][j];
                sb.append(v).append(";");                
            }
            int label = labels[i];
            sb.append("|").append(label);
            
            String s = sb.toString();
            //Check if map contains the string
            ArrayList<Integer> list = map.get(s);
            if (list != null) {
                if (!list.isEmpty()) {
                    list.remove(0); //Remove first occurance
                }                //We do not remove from map 
                //if (l.isEmpty()) { //If list is empty remove from map
                //    map.remove(s);
                //}
            } else { //The number of times the row didn't appear 
                numberOfMisses++;
            }
        }        
        for (Map.Entry<String, ArrayList<Integer>> val : map.entrySet()) {
            for (int j : val.getValue()) {
                index.set(j, false);
            }
        }        
        return index;
    }
    
    public int getNumberOfMisses() {
        return numberOfMisses;
    }
}
