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
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;

/**
 *
 * @author Marcin
 */
public class CNNInstanceSelectionGeneralModelTest {
    
    public CNNInstanceSelectionGeneralModelTest() {
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
     * Test of selectInstances method, of class CNNInstanceSelectionGeneralModel.
     */
    @Test
    public void testSelectInstances() {
        System.out.println("selectInstances");
        SelectedExampleSet exampleSet = null;
        CNNInstanceSelectionGeneralModel instance = null;
        DataIndex expResult = null;
        DataIndex result = instance.selectInstances(exampleSet);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
