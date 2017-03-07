/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleObjectContainer;

/**
 *
 * @author Marcin
 */
public class NNGraphKUL {

    int kLower;
    int kUpper;
    Map<Integer, Set<Integer>> associatesKUpper;
    Map<Integer, Set<Integer>> associatesKLower;
    Map<Integer, List<Integer>> neighbors;
    ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples;
    IDataIndex index;

    public NNGraphKUL(ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples, int k) {                
        this(samples, -1, k);
    }
    
    public NNGraphKUL(ISPRGeometricDataCollectionWithIndex<IInstanceLabels> samples, int kLower, int kUpper) {        
        if (kLower > kUpper){
            int tmp = kUpper;
            kUpper = kLower;
            kLower = tmp;
        }
        this.kLower = kLower;
        this.kUpper = kUpper;
        this.samples = samples;
        index = samples.getIndex();
        index.setAllTrue();
        initialize();
    }

    public IDataIndex getIndex(){
        return index;
    }
    
    public final void initialize() {
        Iterator<Vector> sampleIterator;
        Vector vector;
        Collection<DoubleObjectContainer<IInstanceLabels>> res;

        associatesKUpper = new HashMap(samples.size());
        if (kLower>0){                    
            associatesKLower = new HashMap(samples.size());
        }
        neighbors = new HashMap(samples.size());
        for (int i = 0; i < samples.size(); i++) {            
            associatesKUpper.put(i, new HashSet<Integer>());
            neighbors.put(i, new ArrayList<Integer>(kLower+1));
            if (kLower>0){  
                associatesKLower.put(i, new HashSet<Integer>());
            }
        }
        //Fillin associates
        //sampleIterator = samples.samplesIterator();                
        for (int id : index) {
            vector = samples.getSample(id);
            int currentInstanceID = (int)samples.getStoredValue(id).getValueAsLong(Const.INDEX_CONTAINER);
            index.set(id, false);
            res = samples.getNearestValueDistances(kUpper, vector, index);
            index.set(id, true);
            List<DoubleObjectContainer<IInstanceLabels>> resTmp = new ArrayList<>(res);
            Collections.sort(resTmp);
//            Collections.sort(resTmp,new Comparator<DoubleObjectContainer<IInstanceLabels>> {          
        //});
            int kTmp = 0;
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) {
                IInstanceLabels lab = container.getSecond();
                int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);
                associatesKUpper.get(neighborID).add(currentInstanceID);
                if (kTmp < kLower){  
                    associatesKLower.get(neighborID).add(currentInstanceID);
                }
                neighbors.get(currentInstanceID).add(neighborID);
                kTmp++;
            }            
        }
    }

    public List<Integer> getNeighbors(int nodeId) {
        return neighbors.get(nodeId);
    }

    public Set<Integer> getAssociates(int nodeId) {
        return associatesKUpper.get(nodeId);
    }

    public void remove(int nodeId) {        
        //Turn of the instance
        index.set(nodeId,false);                           
        Collection<DoubleObjectContainer<IInstanceLabels>> res;        
        //Get the associate elements which pointo to the instance being deleted (nodeId)
        for (int associate : associatesKLower.get(nodeId)) {
            //Recalculate its neighbors when instance nodeId will be deleted
            Vector vector = samples.getSample(associate);     
            index.set(associate, false); //Switch off current instance
            res = samples.getNearestValueDistances(kUpper, vector, index);
            index.set(associate, true); //Switch on current instance
            DoubleObjectContainer<IInstanceLabels> resTmp[] = (DoubleObjectContainer<IInstanceLabels>[])res.toArray(new DoubleObjectContainer<?>[res.size()]);            //Sort neighnors according to the nearest order
            Arrays.sort(resTmp);                                        
            neighbors.get(associate).clear(); //Clear associates of current instance
            int kTmp = 0;
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) { //Recreate neighbors
                IInstanceLabels lab = container.getSecond();
                int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);                
                associatesKUpper.get(neighborID).add(associate); //Add info that current point (associate) is belongs to asspociates of nearest neighbors of current instance
                if (kTmp < kLower){
                    associatesKLower.get(neighborID).add(associate); //Add info that current point (associate) is belongs to asspociates of nearest neighbors of current instance
                }
                neighbors.get(associate).add(neighborID);  //Add new neighbors
                kTmp++;
            }            
        }
        Set<Integer> reminingAssociates = new HashSet(associatesKUpper.get(nodeId));
        reminingAssociates.removeAll(associatesKLower.get(nodeId));
        for (int associate : reminingAssociates) {
            //Recalculate its neighbors when instance nodeId will be deleted
            Vector vector = samples.getSample(associate);     
            index.set(associate, false); //Switch off current instance
            res = samples.getNearestValueDistances(kUpper, vector, index);
            index.set(associate, true); //Switch on current instance
            DoubleObjectContainer<IInstanceLabels> resTmp[] = (DoubleObjectContainer<IInstanceLabels>[])res.toArray(new DoubleObjectContainer<?>[res.size()]);            //Sort neighnors according to the nearest order
            Arrays.sort(resTmp);                                                    
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) { //Recreate neighbors
                IInstanceLabels lab = container.getSecond();
                int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);                
                associatesKUpper.get(neighborID).add(associate); //Add info that current point (associate) is belongs to asspociates of nearest neighbors of current instance                
                neighbors.get(associate).add(neighborID);  //Add new neighbors
            }            
        }
        for (Integer neighbor : neighbors.get(nodeId)){
            associatesKUpper.get(neighbor).remove(nodeId);
            if (kLower > 0){
                associatesKLower.get(neighbor).remove(nodeId);
            }
        }
    }
}
