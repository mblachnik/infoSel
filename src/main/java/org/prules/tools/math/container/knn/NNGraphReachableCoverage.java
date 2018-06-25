/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.DoubleIntContainer;
import org.prules.tools.math.container.DoubleObjectContainer;
import org.prules.tools.math.container.PairContainer;

/**
 *
 * @author Marcin
 */
public class NNGraphReachableCoverage implements INNGraph {

    int k;
    Map<Integer, Set<Integer>> coverage;
    Map<Integer, List<DoubleIntContainer>> reachable;
    Map<Integer, List<DoubleIntContainer>> enemies;
    Map<Integer, Set<Integer>> enemyAssociate;
    ISPRClassGeometricDataCollection<IInstanceLabels> samples;
    IDataIndex index;

    public NNGraphReachableCoverage(ISPRClassGeometricDataCollection<IInstanceLabels> samples) {
        this.k = samples.size();
        this.samples = samples;
        index = samples.getIndex();
        index.setAllTrue();
        init();
        calculateGraph();
    }

    public NNGraphReachableCoverage(NNGraphReachableCoverage nnGraph) {
        this.k = nnGraph.k;
        this.samples = nnGraph.samples;
        this.index = nnGraph.index;
        this.coverage = nnGraph.coverage;
        this.reachable = nnGraph.reachable;
        this.enemies = nnGraph.enemies;
    }

    private void init() {
        //DoubleObjectContainer<IInstanceLabels>[] resTmp;        
        coverage = new HashMap(samples.size());
        reachable = new HashMap(samples.size());
        enemies = new HashMap(samples.size());
        enemyAssociate = new HashMap(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            coverage.put(i, new HashSet<>(k+1));
            //reachable.put(i, new LinkedList<DoubleIntContainer>());
            reachable.put(i, new ArrayList<>(k+1));
            //enemies.put(i, new LinkedList<DoubleIntContainer>());
            enemies.put(i, new ArrayList<>(k+1));
            enemyAssociate.put(i, new HashSet<>(k+1));
        }
    }

    /**
     * Returns reference to internal index which identifies which samples are
     * considered when initialize is called.
     *
     * @return
     */
    @Override
    public IDataIndex getIndex() {
        return index;
    }

    @Override
    public final void calculateGraph() {        
        //The other elements are cleared in calculateGraphForInstance
         for (int i = 0; i < samples.size(); i++) {
            coverage.get(i).clear();
            enemyAssociate.get(i).clear();
         }
        for (int id : index) {
            calculateGraphForInstance(id);
        }
    }

    /**
     * Returns list containing ordered indexes of neighbor samples. The elements
     * are sorted from the most nearest neighbor to the farthest neighbor
     *
     * @param nodeId
     * @return list of pairs containing distance and the id of nearest neighbor
     */
    @Override
    public List<DoubleIntContainer> getNeighbors(int nodeId) {
        return reachable.get(nodeId);
    }

    /**
     * Returns set of associate samples that are these samples which one of
     * neighbors is instance with nodeId
     *
     * @param nodeId
     * @return
     */
    @Override
    public Set<Integer> getAssociates(int nodeId) {
        return coverage.get(nodeId);
    }

    /**
     * Returns list containing ordered indexes of enemies samples. The elements
     * are sorted from the nearest enemy to the farthest enemy This elements are
     * iteratively updated such that after removal of a sample an instance new
     * enemies are determined
     *
     * @param nodeId
     * @return list of pair containing distance and id of enemies
     */
    @Override
    public List<DoubleIntContainer> getEnemies(int nodeId) {
        return enemies.get(nodeId);
    }

    @Override
    public ISPRClassGeometricDataCollection<IInstanceLabels> getSamples() {
        return samples;
    }

    @Override
    public int getK() {
        return k;
    }

    @Override
    public int size() {
        return samples.size();
    }

    @Override
    public void remove(int nodeId) {
        //Turn of the instance
        index.set(nodeId, false);     
        
        //DoubleObjectContainer<IInstanceLabels>[] resTmp;
        //Get the associate elements which pointo to the instance being deleted (nodeId)                
        for (int enemyID  : enemyAssociate.get(nodeId)) {            
            //Recalculate its neighbors when instance nodeId will be deleted
            if (enemyID >= 0){            
                calculateGraphForInstance(enemyID);               
            }
        }        
        for (Entry<Integer,Set<Integer>> e : enemyAssociate.entrySet()){
            e.getValue().remove(nodeId);
        }
        
        for (Entry<Integer,Set<Integer>> e : coverage.entrySet()){
            e.getValue().remove(nodeId);
        }
    }

    
    protected void calculateGraphForInstance(int id) {
        Vector vector;
        PairContainer<Collection<DoubleObjectContainer<IInstanceLabels>>, Collection<DoubleObjectContainer<IInstanceLabels>>> resAll;
        List<DoubleObjectContainer<IInstanceLabels>> resReachable;
        List<DoubleObjectContainer<IInstanceLabels>> resEnemies;
        vector = samples.getSample(id);
        IInstanceLabels currentInstanceLabels = samples.getStoredValue(id);
        int currentInstanceID = (int) currentInstanceLabels.getValueAsLong(Const.INDEX_CONTAINER);
        if (index.get(id)) { //If the sample which we analyze is on then switch it off for a moment, and than switch it on again
            index.set(id, false);
            resAll = samples.getNearestNeighborsAndAnymiesDistances(k + 1, vector, currentInstanceLabels, index);
            index.set(id, true);
        } else { //If it is already off just search for nearest neighbor   
            resAll = samples.getNearestNeighborsAndAnymiesDistances(k + 1, vector, currentInstanceLabels, index);
        }
        resReachable = new ArrayList<>(resAll.getFirst());
        resEnemies = new ArrayList<>(resAll.getSecond());
        //resTmp = (DoubleObjectContainer<IInstanceLabels>[])resCombined.toArray(new DoubleObjectContainer<?>[resCombined.size()]);
        Collections.sort(resReachable);
        Collections.sort(resEnemies);
        double distanceEnemy = Double.POSITIVE_INFINITY;
        int enemyID = -1;
        enemies.get(currentInstanceID).clear();
        if (resEnemies.size() > 0){
            distanceEnemy = resEnemies.get(0).getFirst();
            enemyID = (int)resEnemies.get(0).getSecond().getValueAsLong(Const.INDEX_CONTAINER);
            enemies.get(currentInstanceID).add(new DoubleIntContainer(distanceEnemy, enemyID));            
        }
        reachable.get(currentInstanceID).clear();        
        for (DoubleObjectContainer<IInstanceLabels> container : resReachable) {
            IInstanceLabels lab = container.getSecond();
            double distance = container.getFirst();
            if (distance < distanceEnemy) {
                int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);
                reachable.get(currentInstanceID).add(new DoubleIntContainer(distance, neighborID));
                coverage.get(neighborID).add(currentInstanceID);
                enemyAssociate.get(enemyID).add(currentInstanceID);
            } else {
                break;
            }
        }                 
    }
}
