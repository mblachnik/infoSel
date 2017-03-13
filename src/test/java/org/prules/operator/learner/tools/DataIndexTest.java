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
public class DataIndexTest {

    IDataIndex di;
    boolean[] b;

    public DataIndexTest() {
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
        this.b = b;
        di = new DataIndex(b);
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
        for (boolean bo : b) {
            if (bo) {
                ints.add(i);
            }
            i++;
        }
        Iterator<Integer> itDi = di.iterator();
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
        for (boolean bo : b) {
            if (bo) {
                ints.add(i);
            }
            i++;
        }
        ListIterator<Integer> itDi = di.iterator(di.getLength());
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
        for (int i = 0; i < b.length; i++) {
            assertEquals(di.get(i), b[i]);
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
        for (boolean bo : b) {
            if (bo) {
                ints.add(i);
            }
            i++;
        }
        int[] diInts = di.getAsInt();
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
        assertEquals(di.size(), b.length);        
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
        for(boolean bo : b){
            sum += bo ? 1 : 0;
        }        
        assertEquals(sum, di.getLength());        
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
        boolean[] bb = b.clone();
        bb[0] = !bb[0];
        IDataIndex id2 = new DataIndex(bb);
        assertNotEquals(bb.hashCode(), di.hashCode());
    }    

    /**
     * Test of remove method, of class IDataIndex.
     */
    @Test
    public void testRemove() {
        int ii = 3;
        di.remove(ii);
        int i=0;
        for(boolean bb : b){
            if (i != ii){
                assertEquals(bb, di.get(i));    
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
        for(int i=0; i<b.length; i++){
            if(b[i]){
                b[i] = bb[j];
                j++;
            }
        }
        IDataIndex di2 = new DataIndex(bb);
        di.setIndex(di2);         
        for (int i = 0; i < b.length; i++) {
            assertEquals(di.get(i), b[i]);
            // TODO review the generated test code and remove the default call to fail.        
        }
    }        

}
