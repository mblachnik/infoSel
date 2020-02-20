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
public class NNGraphWithoutAssocuateUpdates implements INNGraph {

    int k;
    Map<Integer, Set<Integer>> associates;
    Map<Integer, List<DoubleIntContainer>> neighbors;
    Map<Integer, List<DoubleIntContainer>> enemies;
    ISPRClassGeometricDataCollection<IInstanceLabels> samples;
    IDataIndex index;

    public NNGraphWithoutAssocuateUpdates(ISPRClassGeometricDataCollection<IInstanceLabels> samples, int k) {
        this.k = k;
        this.samples = samples;
        index = samples.getIndex();
        index.setAllTrue();
        init();
        calculateGraph();
    }

    public NNGraphWithoutAssocuateUpdates(NNGraphWithoutAssocuateUpdates nnGraph) {
        this.k = nnGraph.k;
        this.samples = nnGraph.samples;
        this.index = nnGraph.index;
        this.associates = nnGraph.associates;
        this.neighbors = nnGraph.neighbors;
        this.enemies = nnGraph.enemies;
    }

    private void init() {
        associates = new HashMap(samples.size());
        neighbors = new HashMap(samples.size());
        enemies = new HashMap(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            associates.put(i, new HashSet<>(k+1));
            //neighbors.put(i, new LinkedList<DoubleIntContainer>());
            //enemies.put(i, new LinkedList<DoubleIntContainer>());
            neighbors.put(i, new ArrayList<>(k+1));
            enemies.put(i, new ArrayList<>(k+1));
        }
    }

    @Override
    public IDataIndex getIndex() {
        return index;
    }

    /**
     * Method creates a nearest neighbor graph. It is called from the
     * constructor so it is not necesary to run it after creation of an obiecjt.
     * It can be run if anything in the data samples get changed
     */
    @Override
    public final void calculateGraph() {
        //Fillin associates
        //sampleIterator = samples.samplesIterator();     
        for (int i = 0; i < samples.size(); i++) {
            associates.get(i).clear();
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
        return neighbors.get(nodeId);
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
        return associates.get(nodeId);
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

    /**
     * Removes sample nodId and recalculate the graph, such that the removal
     * changes the graph structure
     *
     * @param nodeId
     */
    @Override
    public void remove(int nodeId) {
        //Turn of the instance
        index.set(nodeId, false);        
        //Get the associate elements which points to the instance being deleted (nodeId)
        for (int associate : associates.get(nodeId)) {            
            //Recalculate its neighbors when instance nodeId will be deleted
            calculateGraphForInstance(associate);
        }
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

    public void calculateGraphForInstance(int id) {
        Vector vector;
        PairContainer<Collection<DoubleObjectContainer<IInstanceLabels>>, Collection<DoubleObjectContainer<IInstanceLabels>>> resAll;
        List<DoubleObjectContainer<IInstanceLabels>> resCombined;
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
        resCombined = new ArrayList<>(resAll.getFirst());
        resEnemies = new ArrayList<>(resAll.getSecond());
        Collections.sort(resEnemies);
        resCombined.addAll(resEnemies);
        //resTmp = (DoubleObjectContainer<IInstanceLabels>[])resCombined.toArray(new DoubleObjectContainer<?>[resCombined.size()]);
        Collections.sort(resCombined);
        int kTmp = 0;
        neighbors.get(currentInstanceID).clear();
        for (DoubleObjectContainer<IInstanceLabels> container : resCombined) {
            IInstanceLabels lab = container.getSecond();
            double distance = container.getFirst();
            int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);
            if (kTmp < k) {
                associates.get(neighborID).add(currentInstanceID);
            }
            neighbors.get(currentInstanceID).add(new DoubleIntContainer(distance, neighborID));            
            kTmp++;
            if (kTmp > k) {
                break; //We brak the loop when we take k+1 samples.
            }
        }
        //resTmp = (DoubleObjectContainer<IInstanceLabels>[])resEnemies.toArray(new DoubleObjectContainer<?>[resEnemies.size()]);
        //Arrays.sort(resTmp);
        //Collections.sort(resEnemies); //The sorting was moved to the begining becouse sorting already sorted values is more efficient, and we sort the resCombined
        enemies.get(currentInstanceID).clear();
        for (DoubleObjectContainer<IInstanceLabels> container : resEnemies) {
            int enemyID = (int) container.getSecond().getValueAsLong(Const.INDEX_CONTAINER);
            double distance = container.getFirst();
            enemies.get(currentInstanceID).add(new DoubleIntContainer(distance, enemyID));
        }
    }
}
