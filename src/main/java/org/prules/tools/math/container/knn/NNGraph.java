/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public class NNGraph implements INNGraph {

    int k;
    Map<Integer, Set<Integer>> associates;
    Map<Integer, List<DoubleIntContainer>> neighbors;
    Map<Integer, List<DoubleIntContainer>> enemies;    
    ISPRClassGeometricDataCollection<IInstanceLabels> samples;
    IDataIndex index;

    public NNGraph(ISPRClassGeometricDataCollection<IInstanceLabels> samples, int k) {
        this.k = k;
        this.samples = samples;
        index = samples.getIndex();
        index.setAllTrue();
        initialize();
    }

    @Override
    public IDataIndex getIndex() {
        return index;
    }

    @Override
    public final void initialize() {
        Iterator<Vector> sampleIterator;
        Vector vector;
        PairContainer<Collection<DoubleObjectContainer<IInstanceLabels>>, Collection<DoubleObjectContainer<IInstanceLabels>>> resAll;
        Collection<DoubleObjectContainer<IInstanceLabels>> resCombined;
        Collection<DoubleObjectContainer<IInstanceLabels>> resAnymies;
        associates = new HashMap(samples.size());
        neighbors = new HashMap(samples.size());
        enemies = new HashMap(samples.size());
        for (int i = 0; i < samples.size(); i++) {
            associates.put(i, new HashSet<Integer>());
            neighbors.put(i, new ArrayList<DoubleIntContainer>(k + 1));
            enemies.put(i, new ArrayList<DoubleIntContainer>(k + 1));
        }
        //Fillin associates
        //sampleIterator = samples.samplesIterator();                
        for (int id : index) {
            vector = samples.getSample(id);
            IInstanceLabels currentInstanceLabels = samples.getStoredValue(id);
            int currentInstanceID = (int) currentInstanceLabels.getValueAsLong(Const.INDEX_CONTAINER);
            index.set(id, false);
            resAll = samples.getNearestNeighborsAndAnymiesDistances(k + 1, vector, currentInstanceLabels, index);
            index.set(id, true);
            resCombined = resAll.getFirst();
            resAnymies = resAll.getSecond();
            resCombined.addAll(resAnymies);
            DoubleObjectContainer<IInstanceLabels> resTmp[] = (DoubleObjectContainer<IInstanceLabels>[]) resCombined.toArray(new DoubleObjectContainer<?>[resCombined.size()]);            //Sort neighnors according to the nearest order
            Arrays.sort(resTmp);
//            Collections.sort(resTmp,new Comparator<DoubleObjectContainer<IInstanceLabels>> {          
            //});
            int kTmp = 0;
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) {
                IInstanceLabels lab = container.getSecond();
                double distance = container.getFirst();
                int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);
                if (kTmp < k) {
                    associates.get(neighborID).add(currentInstanceID);
                }
                neighbors.get(currentInstanceID).add(new DoubleIntContainer(distance, neighborID));
                if (kTmp > k) {
                    break; //We brak the loop when we take k+1 samples.
                }
                kTmp++;
            }
            resTmp = (DoubleObjectContainer<IInstanceLabels>[]) resAnymies.toArray(new DoubleObjectContainer<?>[resAnymies.size()]);            //Sort neighnors according to the nearest order
            Arrays.sort(resTmp);
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) {
                int enemyID = (int) container.getSecond().getValueAsLong(Const.INDEX_CONTAINER);
                double distance = container.getFirst();
                enemies.get(currentInstanceID).add(new DoubleIntContainer(distance, enemyID));
            }
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

    @Override
    public void remove(int nodeId) {
        //Turn of the instance
        index.set(nodeId, false);

        PairContainer<Collection<DoubleObjectContainer<IInstanceLabels>>, Collection<DoubleObjectContainer<IInstanceLabels>>> resAll;
        Collection<DoubleObjectContainer<IInstanceLabels>> resCombined;
        Collection<DoubleObjectContainer<IInstanceLabels>> resEnemies;
        //Get the associate elements which pointo to the instance being deleted (nodeId)
        for (int associate : associates.get(nodeId)) {
            //Recalculate its neighbors when instance nodeId will be deleted
            Vector vector = samples.getSample(associate);
            IInstanceLabels vectorLabels = samples.getStoredValue(associate);
            index.set(associate, false); //Switch off current instance
            resAll = samples.getNearestNeighborsAndAnymiesDistances(k + 1, vector, vectorLabels, index);
            index.set(associate, true); //Switch on current instance
            resCombined = resAll.getFirst();
            resEnemies = resAll.getSecond();
            resCombined.addAll(resEnemies);
            DoubleObjectContainer<IInstanceLabels> resTmp[] = (DoubleObjectContainer<IInstanceLabels>[]) resCombined.toArray(new DoubleObjectContainer<?>[resCombined.size()]);            //Sort neighnors according to the nearest order
            Arrays.sort(resTmp);
            neighbors.get(associate).clear(); //Clear associates of current instance
            int kTmp = 0;
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) { //Recreate neighbors
                IInstanceLabels lab = container.getSecond();
                int neighborID = (int) lab.getValueAsLong(Const.INDEX_CONTAINER);
                double distance = container.getFirst();
                if (kTmp < k) {
                    associates.get(neighborID).add(associate); //Add info that current point (associate) is belongs to asspociates of nearest neighbors of current instance
                }
                neighbors.get(associate).add(new DoubleIntContainer(distance, neighborID));  //Add new neighbors
                if (kTmp > k) {
                    break; //Here we break becouse we already have k+1 neighbors
                }
                kTmp++;
            }
            enemies.get(associate).clear();
            resTmp = (DoubleObjectContainer<IInstanceLabels>[]) resEnemies.toArray(new DoubleObjectContainer<?>[resEnemies.size()]);            //Sort neighnors according to the nearest order
            Arrays.sort(resTmp);
            for (DoubleObjectContainer<IInstanceLabels> container : resTmp) {
                int anymieID = (int) container.getSecond().getValueAsLong(Const.INDEX_CONTAINER);
                double distance = container.getFirst();
                enemies.get(associate).add(new DoubleIntContainer(distance, anymieID));
            }
        }
        for (DoubleIntContainer neighbor : neighbors.get(nodeId)) {
            associates.get(neighbor.getSecond()).remove(nodeId);
        }
    }
}
