/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marcin
 */
public class SimpleLinearRegressionModelTest {
    
    public SimpleLinearRegressionModelTest() {
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
     * Test of train method, of class SimpleLinearRegressionModel.
     * Test if null input accepted
     */
    @Test(expected=AssertionError.class)
    public void testTrainEx1() {
        System.out.println("train - Test input null 1");
        double[] x = {1, 2};
        double[] y = null;
        SimpleLinearRegressionModel instance = new SimpleLinearRegressionModel();
        instance.train(x, y);
    }
    
    /**
     * Test of train method, of class SimpleLinearRegressionModel.
     * Test if null input accepted (second input)
     */
    @Test(expected=AssertionError.class)
    public void testTrainEx2() {
        System.out.println("train - Test input null 2");
        double[] x = null;
        double[] y = {1, 2};
        SimpleLinearRegressionModel instance = new SimpleLinearRegressionModel();
        instance.train(x, y);
    }
    
    /**
     * Test of train method, of class SimpleLinearRegressionModel.
     * Test if sample sizes are equal
     */
    @Test(expected=AssertionError.class)
    public void testTrainEx3() {
        System.out.println("train");
        double[] x = {1, 2, 4};
        double[] y = {1, 2};
        SimpleLinearRegressionModel instance = new SimpleLinearRegressionModel();                
        instance.train(y, x);
    }
    
    /**
     * Test of train method, of class SimpleLinearRegressionModel.
     * Test calculations
     */
    @Test
    public void testTrainCalculations() {
        System.out.println("train - test claculation");
        SimpleLinearRegressionModel instance = new SimpleLinearRegressionModel();                
        double[] x = {2, 1, 4};
        double[] y = {1, 2, 3};
        instance.train(x, y);
        double a = instance.getA();
        double b = instance.getB();        
        assertEquals(3.0/7.0, a, 0.0000001);
        assertEquals(1, b, 0.0000001);                
    }

    /**
     * Test of apply method, of class SimpleLinearRegressionModel.
     */
    @Test
    public void testApply() {
        System.out.println("apply");
        SimpleLinearRegressionModel instance = new SimpleLinearRegressionModel();                
        double[] x = {2, 1, 4};
        double[] y = {1, 2, 3};
        instance.train(x, y);
        double out = instance.apply(0);
        assertEquals(1.0, out, 0);
        out = instance.apply(7);
        assertEquals(4.0, out, 0.0000001);                
    }

    /**
     * Test of getMSE method, of class SimpleLinearRegressionModel.
     */
    @Test
    public void testGetMSE() {
        System.out.println("getMSE");
        SimpleLinearRegressionModel instance = new SimpleLinearRegressionModel();                
        double[] x = {2, 1, 4};
        double[] y = {1, 2, 3};
        instance.train(x, y);
        double out = instance.getMSE(x,y);
        double trueMSE = (4.0/7.0 * 4.0/7.0 + 6.0/7.0 * 6.0/7.0 + 2.0/7.0 *2.0/7.0)/3;
        assertEquals(trueMSE, out, 0.000000001);
        
    }

}
