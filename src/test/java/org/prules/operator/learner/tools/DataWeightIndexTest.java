/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
public class DataWeightIndexTest {

    IDataWeightIndex dataIndex;
    boolean[] referenceIndex;
    double[] d;

    public DataWeightIndexTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        boolean[] b = {false, true, true, false, true, true, false, false, true};
        double[] d = {0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1, 0.8, 0.9};
        this.referenceIndex = b;
        this.d = d;
        dataIndex = new DataWeightIndex(b,d);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of add method, of class IDataIndex.
     */
    @Test
    public void testIterator() {
        List<Integer> ints = new ArrayList<>();
        int i = 0;
        for (boolean bo : referenceIndex) {
            if (bo) {
                ints.add(i);
            }
            i++;
        }
        Iterator<Integer> itDi = dataIndex.iterator();
        Iterator<Integer> itInts = ints.iterator();
        while (itDi.hasNext() && itInts.hasNext()) {
            assertEquals(itDi.next(), itInts.next());
        }
        assertEquals(itInts.hasNext(), itDi.hasNext());
    }

    /**
     * Test of add method, of class IDataIndex.
     */
    @Test
    public void testIteratorDown() {
        List<Integer> ints = new ArrayList<>();
        int i = 0;
        for (boolean bo : referenceIndex) {
            if (bo) {
                ints.add(i);
            }
            i++;
        }
        ListIterator<Integer> itDi = dataIndex.iterator(dataIndex.getLength());
        ListIterator<Integer> itInts = ints.listIterator(ints.size());
        while (itDi.hasPrevious() && itInts.hasPrevious()) {
            assertEquals(itDi.previous(), itInts.previous());
        }
        assertEquals(itInts.hasPrevious(), itDi.hasPrevious());
    }

    /**
     * Test of clone method, of class IDataIndex.
     */
    @Test
    public void testClone() {
        //TODO needs to be finished
//        System.out.println("clone");
//        IDataIndex instance = new IDataIndexImpl();
//        Object expResult = null;
//        Object result = instance.clone();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class IDataIndex.
     */
    @Test
    public void testEquals() {
        //TODO needs to be finished
//        System.out.println("equals");
//        Object obj = null;
//        IDataIndex instance = new IDataIndexImpl();
//        boolean expResult = false;
//        boolean result = instance.equals(obj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of get method, of class IDataIndex.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        for (int i = 0; i < referenceIndex.length; i++) {
            assertEquals(dataIndex.get(i), referenceIndex[i]);
            assertEquals(dataIndex.getWeight(i), d[i],0.0001);
            // TODO review the generated test code and remove the default call to fail.        
        }
    }

    /**
     * Test of getAsInt method, of class IDataIndex.
     */
    @Test
    public void testGetAsInt() {
        List<Integer> ints = new ArrayList<>();
        int i = 0;
        for (boolean bo : referenceIndex) {
            if (bo) {
                ints.add(i);
            }
            i++;
        }
        int[] diInts = dataIndex.getAsInt();
        Iterator<Integer> itInts = ints.iterator();
        for (int j : diInts) {
            int jj = itInts.next();
            assertEquals(j, jj);
        }
        assertEquals(itInts.hasNext(), false);
    }

    /**
     * Test of getFullLength method, of class IDataIndex.
     */
    @Test
    public void testGetFullLength() {        
        assertEquals(dataIndex.size(), referenceIndex.length);        
    }

    /**
     * Test of getIndex method, of class IDataIndex.
     */
    @Test
    public void testGetIndex() {       
        //TODO needs to be finished
    }

    /**
     * Test of getIndexHandle method, of class IDataIndex.
     */
    @Test
    public void testGetIndexHandle() {        
        //TODO needs to be finished
    }

    /**
     * Test of getLength method, of class IDataIndex.
     */
    @Test
    public void testGetLength() {
        int sum = 0;
        for(boolean bo : referenceIndex){
            sum += bo ? 1 : 0;
        }        
        assertEquals(sum, dataIndex.getLength());        
    }

    /**
     * Test of getOryginalIndex method, of class IDataIndex.
     */
    @Test
    public void testGetOryginalIndex() {     
        //TODO needs to be finished
    }

    /**
     * Test of hashCode method, of class IDataIndex.
     */
    @Test
    public void testHashCode() {
        boolean[] bb = referenceIndex.clone();
        bb[0] = !bb[0];
        IDataIndex id2 = new DataWeightIndex(bb,d);
        assertNotEquals(bb.hashCode(), dataIndex.hashCode());
    }    

    /**
     * Test of remove method, of class IDataIndex.
     */
    @Test
    public void testRemove() {
        int ii = 3;
        dataIndex.remove(ii);
        int i=0;
        for(int k=0; k<referenceIndex.length; k++){
            boolean bb = referenceIndex[k];
            double dd = d[k];
            if (i != ii){
                assertEquals(bb, dataIndex.get(i));    
                assertEquals(dd, dataIndex.getWeight(i),0.001);    
                i++;
            }
        }        
    }


   
    /**
     * Test of setIndex method, of class IDataIndex.
     */
    @Test
    public void testSetIndex() {
        boolean[] bb = {false, true, true, false, true};
        int j=0;
        for(int i=0; i<referenceIndex.length; i++){
            if(referenceIndex[i]){
                referenceIndex[i] = bb[j];
                j++;
            }
        }
        IDataIndex di2 = new DataIndex(bb);
        dataIndex.setIndex(di2);         
        for (int i = 0; i < referenceIndex.length; i++) {
            assertEquals(dataIndex.get(i), referenceIndex[i]);
            assertEquals(dataIndex.getWeight(i), d[i],0.001);
            // TODO review the generated test code and remove the default call to fail.        
        }
    }        

    /**
     * Test of setIndex method, of class IDataIndex.
     */
    @Test
    public void testSetIndex2() {
        boolean[] bb = {false, true, true, false, true};
        double[] dd = {1, 2, 3, 4, 5};
        int j=0;
        for(int i=0; i<referenceIndex.length; i++){
            if(referenceIndex[i]){
                referenceIndex[i] = bb[j];
                d[i] = dd[j];
                j++;
            }
        }
        IDataIndex di2 = new DataWeightIndex(bb,dd);
        dataIndex.setIndex(di2);         
        for (int i = 0; i < referenceIndex.length; i++) {
            assertEquals(dataIndex.get(i), referenceIndex[i]);
            assertEquals(dataIndex.getWeight(i), d[i],0.001);
            // TODO review the generated test code and remove the default call to fail.        
        }
    }
}
