/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.tools;

import org.prules.tools.math.BasicMath;
import java.util.Arrays;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * This class is a binary index which allows to set true/false if given element is enebled or disabled for example in array or matrix or ExampleSet
 * @author Marcin
 */
public class DataIndex implements IDataIndex {

    boolean[] index;
    int length = -1;

    /**
     * Weight attribute from ExampleSet is converted into binary index. If weight is 0 the instance is marked as absent
     * @param examples
     * @return
     */
    public static DataIndex weightsToDataIndex(ExampleSet examples) {
        DataIndex index = new DataIndex(examples.size());
        if (examples.getAttributes().getWeight() == null) {
            index.setAllTrue();
            return index;
        }
        int i = 0;
        for (Example example : examples) {
            index.set(i, example.getWeight() != 0);
            i++;
        }
        return index;
    }

    /**
     * Constructor which initialize DataIndex with specific binary index
     * @param index
     */
    public DataIndex(boolean[] index) {
        this.index = index;
        length = BasicMath.sum(index);
    }

    /**
     * Constructor which set all instances as present. 
     * @param indexSize - number of elements in the structure
     */
    public DataIndex(int indexSize) {
        this.index = new boolean[indexSize];
        Arrays.fill(this.index, true);
        length = indexSize;
    }

//    /**
//     * Constructor which set all instances as present.
//     * @param indexSize - number of elements in the structure
//     */
//    public DataIndex(int indexSize, boolean defaultValue) {
//        this.index = new boolean[indexSize];
//        Arrays.fill(this.index, defaultValue);
//        length = indexSize;
//    }

    /**
     * Copy constructor
     * @param index
     */
    public DataIndex(DataIndex index) {
        this.index = index.index.clone();
        this.length = index.length;
    }
    
    /**
     * Copy constructor
     * @param index
     */
    public DataIndex(IDataIndex index) {
        this.index = new boolean[index.size()];
        this.length = index.getLength();
        for(int i : index){
            this.index[i] = true;
        }                 
    }

    /**
     * Set specific index into true/false
     * @param i
     * @param value
     */
    @Override
    public void set(int i, boolean value) {
        if ((index[i] == false) && value) {
            length++;
            index[i] = true;
        } else if ((index[i] == true) && !value) {
            length--;
            index[i] = false;
        }
    }

    /**
     * The same as set but if "i" is out range add new field in the binary index
     * @param i
     * @param value
     */
    @Override
    public void add(int i, boolean value) {
        if (i >= index.length) {
            boolean[] tIndex = new boolean[i + 1];
            System.arraycopy(index, 0, tIndex, 0, index.length);
            index = tIndex;
            index[i] = value;
            length = BasicMath.sum(index);
        } else {
            set(i, value);
        }
    }

    /**
     * add to the end of index new value
     * @param value
     */
    @Override
    public void add(boolean value) {
        add(index.length, value);
    }

    /**
     * mremove i'th value from binary index (it realocates memory)
     * @param i
     */
    @Override
    public void remove(int i) {
        boolean[] tIndex = new boolean[index.length - 1];
        if (index[i]) {
            length--;
        }
        System.arraycopy(index, 0, tIndex, 0, i);
        System.arraycopy(index, i + 1, tIndex, i, index.length - i - 1);
        index = tIndex;
    }

    /**
     * reads the index at position i
     * @param i
     * @return
     */
    @Override
    public boolean get(int i) {
        return index[i];
    }

    /**
     * Return handle to binary index. The booleans array is now shared
     * @return @deprecated
     */
    @Deprecated
    @Override
    public boolean[] getIndexHandle() {
        return index;
    }

    /**
     * sets external index. 
     * @param index
     * @deprecated
     */
    @Deprecated
    protected void setIndexHandle(boolean[] index) {
        this.index = index;
        length = BasicMath.sum(index);
    }

    /**
     * returns new DataIndex to all elements marked as active (selected)
     * @return
     */
    @Override
    public DataIndex getIndex() {
        boolean[] tmpindex;
        tmpindex = new boolean[length];
        Arrays.fill(tmpindex, true);
        return new DataIndex(tmpindex);
    }

    /**
     * Acquire new index to all selected elements, such that all these elements 
     * which were set to true in the original data would now have new index value.
     * Size of input dataindex must be equal to this.length()
     * @param index
     */
    public void setIndex(DataIndex index) {
        if (length == index.index.length) {
            int j = 0;
            for (int i = 0; i < this.index.length; i++) {
                if (this.index[i]) {
                    this.index[i] = index.index[j];
                    j++;
                }
            }
        } else {
            throw new RuntimeException("Indexes doesn't much");
        }
        length = index.length;
    }
    
    /**
     * Acquire new index to all selected elements, such that all these elements 
     * which were set to true in the original data would now have new index value.
     * Size of input dataindex must be equal to this.length()
     * @param index
     */
    @Override
    public void setIndex(IDataIndex index) {
        if (length == index.size()) {  
            Iterator<Integer> ite = this.iterator();          
            int j=0;
            while(ite.hasNext()){
                int i = ite.next();
                this.index[i] = index.get(j++);
            }
        } else {
            throw new RuntimeException("Indexes doesn't much");
        }
        length = index.getLength();
    }

    /*
     * Create a clon by inserting "index" a colling {@link #DataIndex(boolean[])}
     */
    @Override
    public Object clone() {
        return new DataIndex(index);
    }

    /**
     * Total number of elements in the index (both selected and unselected)
     * @return
     */
    @Override
    public int size() {
        return index.length;
    }

    /**
     * Number of elements indexed true
     * @return
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * Returns index of the original base data structure
     * @param i
     * @return
     */
    @Override
    public int getOryginalIndex(int i) {
        int m = -1;
        int an = index.length;
        for (int k = 0; k < an; k++) {
            if (index[k]) {
                m++;
                if (m == i) {
                    return k;
                }
            }
        }
        return -1;
    }

    /**
     * Mark all elements as selected
     */
    @Override
    public void setAllTrue() {
        Arrays.fill(index, true);
        length = index.length;
    }

    /**
     * Mark all elements as unselected
     */
    @Override
    public void setAllFalse() {
        Arrays.fill(index, false);
        length = 0;
    }

    /**
     * Iterator over elements
     * @return 
     */
    @Override
    public ListIterator<Integer> iterator() {
        return new Itr();
    }

    /**
     * Iterator over elements which starts from index
     * @param index
     * @return 
     */
    @Override
    public ListIterator<Integer> iterator(int index) {
        return new Itr(index);
    }

    /**
     * Implementation of the iterator
     * @return 
     */
    private class Itr implements ListIterator<Integer> {

        int iteratorState;
        int[] indexes = new int[length];

        public Itr() {
            iteratorState = -1;
            createIndexes();
        }

        public Itr(int id) {
            iteratorState = id;
            createIndexes();
        }

        private void createIndexes() {
            int j = 0;
            for (int i = 0; i < index.length; i++) {
                if (index[i]) {
                    indexes[j] = i;
                    j++;
                    if (j>indexes.length) break;
                }
            }
        }

        /**
         * Returns true if next element appears in the data structure
         * @return 
         */
        @Override
        public boolean hasNext() {
            return iteratorState < length - 1;
        }
        
        /**
         * Returns index of the next element
         * @return 
         */
        @Override
        public Integer next() {
            if (hasNext()) {
                iteratorState++;
                return indexes[iteratorState];
            }
            throw new NoSuchElementException();
        }

        /**
         * Returns index of the prevoius element
         * @return 
         */
        @Override
        public Integer previous() {
            if (hasPrevious()) {
                iteratorState--;
                return indexes[iteratorState];
            }
            throw new NoSuchElementException();
        }

        /**
         * Returns true if previous element exist
         * @return 
         */
        @Override
        public boolean hasPrevious() {
            return iteratorState > 0;
        }

        /**
         * Returns index of the next element similar to {@link #next()}, but this
         * method don't use Integer class returning the primitive int
         * @return 
         */
        @Override
        public int nextIndex() {
            if (hasNext()) {
                return iteratorState + 1;
            }
            return length;
        }

        /**
         * Returns index of the previous element similar to {@link #previous()}, but this
         * method don't use Integer class returning the primitive int
         * @return 
         */
        @Override
        public int previousIndex() {
            if (hasPrevious()) {
                return iteratorState - 1;
            }
            return -1;
        }

        /**
         * Not implemented
         * @param e 
         */
        @Override
        public void set(Integer e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        /**
         * Not implemented
         * @param e 
         */
        @Override
        public void add(Integer e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        /**
         * Not implemented
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");

        }
    }

    /**
     * Makes an inverse of selected elements    
    */
    @Override
    public void negate(){
        for(int i=0; i<index.length; i++){
            index[i] = !index[i];
        }
        length = BasicMath.sum(index);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(index);
        result = prime * result + length;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DataIndex other = (DataIndex) obj;
        if (!Arrays.equals(index, other.index)) {
            return false;
        }
        return length == other.length;
    }
    
    /**
     * Returns and index of selected elements
     */    
    @Override
    public int[] getAsInt(){
        int[] tab = new int[length];
        int j = 0;
        for (int i=0; i<index.length; i++){
            if (index[i]){
                tab[j]=i;
                j++;
            }
        }
        return tab;
    }
    
}
