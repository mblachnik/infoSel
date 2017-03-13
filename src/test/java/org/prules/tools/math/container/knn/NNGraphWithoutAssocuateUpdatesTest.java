/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.prules.dataset.IInstanceLabels;
import org.prules.tools.math.container.DoubleIntContainer;

/**
 *
 * @author Marcin
 */
public class NNGraphWithoutAssocuateUpdatesTest {
    
    public NNGraphWithoutAssocuateUpdatesTest() {
    }

   public static INNGraph init(int k) {
        INNGraph nn;
        ISPRClassGeometricDataCollection<IInstanceLabels> samples = org.prules.operator.learner.tools.TestUtils.createSampleDataTwoClasses();
        nn = new NNGraphWithoutAssocuateUpdates(samples, k);
        return nn;
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class NNGraph.
     */
    @Test
    public void testInitialize() {
        INNGraph nn = init(3);
        int neigh;
        nn.initialize();
        Collection<DoubleIntContainer> sSet;
        
        {
            int[] res = {1, 3, 2, 6};
            sSet = nn.getNeighbors(0);
            int i = 0;
            for (DoubleIntContainer c : sSet) {
                neigh = c.getSecond();
                assertEquals(res[i],neigh);
                i++;
            }
        }
        {
            int[] res = {3, 1, 5, 0};
            sSet = nn.getNeighbors(2);
            int i = 0;
            for (DoubleIntContainer c : sSet) {
                neigh = c.getSecond();
                assertEquals(res[i],neigh);
                i++;
            }
        }                
        {
            int[] res = {6, 1, 3};
            sSet = nn.getAssociates(0);
            int i = 0;
            for (DoubleIntContainer c : sSet) {
                neigh = c.getSecond();
                assertEquals(res[i],neigh);
                i++;
            }
        }                                
    }

    /**
     * Test of remove method, of class NNGraph.
     */
    @Test
    public void testRemove() {
        INNGraph nn = init(3);
        nn.remove(3);
        int neigh;
        Collection<DoubleIntContainer> list;
        {             
            int[] res = {1, 2, 6, 5};
            list = nn.getNeighbors(0);
            int i = 0;
            for (DoubleIntContainer c : list) {
                neigh = c.getSecond();
                assertEquals(neigh, res[i]);
                i++;
            }
        }
        {             
            int[] res = {4, 2, 0, 1};
            list = nn.getNeighbors(5);
            int i = 0;
            for (DoubleIntContainer c : list) {
                neigh = c.getSecond();
                assertEquals(neigh, res[i]);
                i++;
            }
        }

        {             
            int[] res = {5};
            list = nn.getAssociates(4);
            int i = 0;
            for (DoubleIntContainer c : list) {
                neigh = c.getSecond();
                assertEquals(neigh, res[i]);
                i++;
            }
        }        
    }
    
}
