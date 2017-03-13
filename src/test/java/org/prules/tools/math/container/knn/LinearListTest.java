/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math.container.knn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.PairContainer;
import org.prules.operator.learner.tools.TestUtils;
import org.prules.tools.math.container.DoubleObjectContainer;

/**
 *
 * @author Marcin
 */
public class LinearListTest {

    public LinearListTest() {
    }

//    @Test
//    public void testGetNearestValues_int_Vector() {
//        System.out.println("getNearestValues");
//        int k = 0;
//        Vector values = null;
//        LinearList instance = null;
//        Collection expResult = null;
//        Collection result = instance.getNearestValues(k, values);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValues_3args() {
//        System.out.println("getNearestValues");
//        int k = 0;
//        Vector values = null;
//        IDataIndex index = null;
//        LinearList instance = null;
//        Collection expResult = null;
//        Collection result = instance.getNearestValues(k, values, index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValueDistances_int_Vector() {
//        System.out.println("getNearestValueDistances");
//        int k = 0;
//        Vector values = null;
//        LinearList instance = null;
//        Collection<DoubleObjectContainer<T>> expResult = null;
//        Collection<DoubleObjectContainer<T>> result = instance.getNearestValueDistances(k, values);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValueDistances_3args_1() {
//        System.out.println("getNearestValueDistances");
//        int k = 0;
//        Vector values = null;
//        IDataIndex index = null;
//        LinearList instance = null;
//        Collection<DoubleObjectContainer<T>> expResult = null;
//        Collection<DoubleObjectContainer<T>> result = instance.getNearestValueDistances(k, values, index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValueDistances_double_Vector() {
//        System.out.println("getNearestValueDistances");
//        double withinDistance = 0.0;
//        Vector values = null;
//        LinearList instance = null;
//        Collection<DoubleObjectContainer<T>> expResult = null;
//        Collection<DoubleObjectContainer<T>> result = instance.getNearestValueDistances(withinDistance, values);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValueDistances_3args_2() {
//        System.out.println("getNearestValueDistances");
//        double withinDistance = 0.0;
//        Vector values = null;
//        IDataIndex index = null;
//        LinearList instance = null;
//        Collection<DoubleObjectContainer<T>> expResult = null;
//        Collection<DoubleObjectContainer<T>> result = instance.getNearestValueDistances(withinDistance, values, index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValueDistances_3args_3() {
//        System.out.println("getNearestValueDistances");
//        double withinDistance = 0.0;
//        int butAtLeastK = 0;
//        Vector values = null;
//        LinearList instance = null;
//        Collection<DoubleObjectContainer<T>> expResult = null;
//        Collection<DoubleObjectContainer<T>> result = instance.getNearestValueDistances(withinDistance, butAtLeastK, values);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetNearestValueDistances_4args() {
//        System.out.println("getNearestValueDistances");
//        double withinDistance = 0.0;
//        int butAtLeastK = 0;
//        Vector values = null;
//        IDataIndex index = null;
//        LinearList instance = null;
//        Collection<DoubleObjectContainer<T>> expResult = null;
//        Collection<DoubleObjectContainer<T>> result = instance.getNearestValueDistances(withinDistance, butAtLeastK, values, index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testSize() {
//        System.out.println("size");
//        LinearList instance = null;
//        int expResult = 0;
//        int result = instance.size();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetStoredValue() {
//        System.out.println("getStoredValue");
//        int index = 0;
//        LinearList instance = null;
//        Object expResult = null;
//        Object result = instance.getStoredValue(index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testGetSample() {
//        System.out.println("getSample");
//        int index = 0;
//        LinearList instance = null;
//        Vector expResult = null;
//        Vector result = instance.getSample(index);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testRemove() {
//        System.out.println("remove");
//        int n = 0;
//        LinearList instance = null;
//        instance.remove(n);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testStoredValueIterator() {
//        System.out.println("storedValueIterator");
//        LinearList instance = null;
//        Iterator expResult = null;
//        Iterator result = instance.storedValueIterator();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testSamplesIterator() {
//        System.out.println("samplesIterator");
//        LinearList instance = null;
//        Iterator<Vector> expResult = null;
//        Iterator<Vector> result = instance.samplesIterator();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testSetSample() {
//        System.out.println("setSample");
//        int index = 0;
//        Vector sample = null;
//        Object storedValue = null;
//        LinearList instance = null;
//        instance.setSample(index, sample, storedValue);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testNumberOfUniquesOfStoredValues() {
//        INNGraph nn;
//        nn = new LinearList<IInstanceLabels>(new EuclideanDistance(),3);
//    }
    @Test
    public void testGetNearestNeighbors() {
        System.out.println("getNearestNeighbors");
        int k = 3;
        Collection<IInstanceLabels> res;        
        ISPRClassGeometricDataCollection<IInstanceLabels> nnTmp = TestUtils.createSampleDataThreeClasses();
        LinearList<IInstanceLabels> nn = (LinearList<IInstanceLabels>) nnTmp;
        Vector v = InstanceFactory.createVector(new double[]{1.5, 1.4});
        IInstanceLabels l = InstanceFactory.createInstanceLabels(0);
        res = nn.getNearestValues(k, v);
        List<IInstanceLabels> list;
        list = new ArrayList(res);
        Collections.sort(list, new Cmp());

        long[] indexs;
        indexs = new long[]{0, 1, 2};
        int i = 0;
        for (IInstanceLabels ll : list) {
            long id = ll.getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);
            i++;
        }        
    }
    
    @Test
    public void testGetNearestNeighborsIndex() {
        System.out.println("getNearestNeighbors");
        int k = 3;
        Collection<IInstanceLabels> res;        
        ISPRClassGeometricDataCollection<IInstanceLabels> nnTmp = TestUtils.createSampleDataThreeClasses();
        LinearList<IInstanceLabels> nn = (LinearList<IInstanceLabels>) nnTmp;
        Vector v = InstanceFactory.createVector(new double[]{1.5, 1.4});
        IInstanceLabels l = InstanceFactory.createInstanceLabels(0);
        IDataIndex idx = new DataIndex(nn.size());
        idx.set(1,false);
        res = nn.getNearestValues(k, v, idx);
        List<IInstanceLabels> list;
        list = new ArrayList(res);
        Collections.sort(list, new Cmp());

        long[] indexs;
        indexs = new long[]{0, 2, 3};
        int i = 0;
        for (IInstanceLabels ll : list) {
            long id = ll.getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);
            i++;
        }        
    }
    
    @Test
    public void testGetNearestNeighborsDistances() {
        System.out.println("getNearestNeighborsDistances");
        int k = 3;
        Collection<DoubleObjectContainer<IInstanceLabels>> res;        
        ISPRClassGeometricDataCollection<IInstanceLabels> nnTmp = TestUtils.createSampleDataThreeClasses();
        LinearList<IInstanceLabels> nn = (LinearList<IInstanceLabels>) nnTmp;
        Vector v = InstanceFactory.createVector(new double[]{1.5, 1.4});
        IInstanceLabels l = InstanceFactory.createInstanceLabels(0);
        res = nn.getNearestValueDistances(k, v);
        List<DoubleObjectContainer<IInstanceLabels>> list;
        list = new ArrayList(res);
        Collections.sort(list);

        long[] indexs;
        double[] dists;
        indexs = new long[]{1, 2, 0};
        dists = new double[]{0.41, 0.45, 0.61};
        int i = 0;
        for (DoubleObjectContainer<IInstanceLabels> ll : list) {
            long id = ll.getSecond().getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);            
            double dist = ll.getFirst();
            assertEquals(dists[i], dist*dist ,0.001);
            i++;
        }        
    }
    
    @Test
    public void testGetNearestNeighborsAndAnymies() {
        System.out.println("getNearestNeighborsAndAnymies");
        int k = 3;
        PairContainer<Collection<IInstanceLabels>, Collection<IInstanceLabels>> res;        
        ISPRClassGeometricDataCollection<IInstanceLabels> nnTmp = TestUtils.createSampleDataThreeClasses();
        LinearList<IInstanceLabels> nn = (LinearList<IInstanceLabels>) nnTmp;
        Vector v = InstanceFactory.createVector(new double[]{1.5, 1.4});
        IInstanceLabels l = InstanceFactory.createInstanceLabels(0);
        res = nn.getNearestNeighborsAndAnymies(k, v, l);
        List<IInstanceLabels> list;
        list = new ArrayList(res.getFirst());
        Collections.sort(list, new Cmp());

        long[] indexs;
        indexs = new long[]{0, 2, 3};
        int i = 0;
        for (IInstanceLabels ll : list) {
            long id = ll.getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);
            i++;
        }

        list = new ArrayList(res.getSecond());
        Collections.sort(list, new Cmp());
        indexs = new long[]{1, 5, 6};
        i = 0;
        for (IInstanceLabels ll : list) {
            long id = ll.getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);
            i++;
        }
    }

    @Test
    public void testGetNearestNeighborsAndAnymiesDistances() {
        System.out.println("getNearestNeighborsAndAnymies");
        int k = 3;
        PairContainer<Collection<DoubleObjectContainer<IInstanceLabels>>, Collection<DoubleObjectContainer<IInstanceLabels>>> res;        
        ISPRClassGeometricDataCollection<IInstanceLabels> nnTmp = TestUtils.createSampleDataThreeClasses();
        LinearList<IInstanceLabels> nn = (LinearList<IInstanceLabels>) nnTmp;
        Vector v = InstanceFactory.createVector(new double[]{1.5, 1.4});
        IInstanceLabels l = InstanceFactory.createInstanceLabels(0);
        res = nn.getNearestNeighborsAndAnymiesDistances(k, v, l);
        List<DoubleObjectContainer<IInstanceLabels>> list;
        list = new ArrayList(res.getFirst());
        Collections.sort(list);

        long[] indexs;
        double[] dists;
        indexs = new long[]{2, 0, 3};
        dists = new double[]{0.45, 0.61, 0.85};
        int i = 0;
        for (DoubleObjectContainer<IInstanceLabels> ll : list) {            
            long id = ll.getSecond().getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);
            double dist = ll.getFirst();
            assertEquals(dists[i], dist*dist, 0.001);
            i++;
        }

        list = new ArrayList(res.getSecond());
        Collections.sort(list);
        
        indexs = new   long[]{1, 6, 5};
        dists  = new double[]{0.41, 1.81, 2.74};
        i = 0;
        for (DoubleObjectContainer<IInstanceLabels> ll : list) {            
            long id = ll.getSecond().getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(indexs[i], id);
            double dist = ll.getFirst();
            assertEquals(dists[i], dist*dist, 0.001);
            i++;
        }
    }
    
     @Test
    public void testRemove() {
        System.out.println("remove");                
        ISPRClassGeometricDataCollection<IInstanceLabels> nnTmp = TestUtils.createSampleDataThreeClasses();
        int size = nnTmp.size();
        nnTmp.remove(3);
        size--;
        for (int i=0;i<size;i++){
            int ic = (int)nnTmp.getStoredValue(i).getValueAsLong(Const.INDEX_CONTAINER);
            assertEquals(i, ic);
        }       
    }
      
}

class Cmp implements Comparator<IInstanceLabels> {
    @Override
    public int compare(IInstanceLabels o1, IInstanceLabels o2) {
        if (o1.getValueAsLong(Const.INDEX_CONTAINER) > o2.getValueAsLong(Const.INDEX_CONTAINER)){
            return 1;
        }
        if (o1.getValueAsLong(Const.INDEX_CONTAINER) < o2.getValueAsLong(Const.INDEX_CONTAINER)){
            return -1;
        }
        return 0;
    }
}
