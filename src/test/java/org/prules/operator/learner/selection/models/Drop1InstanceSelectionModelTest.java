/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.TestUtils;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;

/**
 *
 * @author Marcin
 */
public class Drop1InstanceSelectionModelTest {
    
    public Drop1InstanceSelectionModelTest() {
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
     * Test of selectInstances method, of class Drop1InstanceSelectionModel.
     */
    @Test
    public void testSelectInstances() {
        System.out.println("selectInstances");        
        ISPRClassGeometricDataCollection<IInstanceLabels> samples = TestUtils.createSampleDataTwoClasses();
        Drop1InstanceSelectionModel instance = new Drop1InstanceSelectionModel(samples.getMeasure(), 1);
        IDataIndex expResult = new DataIndex(new boolean[]{true, true, true, false, false, false, true});
        IDataIndex result = instance.selectInstances(samples);
        assertEquals(expResult, result);                
    }
    
}
