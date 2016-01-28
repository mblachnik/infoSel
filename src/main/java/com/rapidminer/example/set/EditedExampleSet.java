package com.rapidminer.example.set;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;

/**
 * A wrapper for the ExampleSet which allows for instance selection. The instance selection is based 
 * on boolean array DataIndex which stores information on each individual Example.
 * It is very similar to the SelectedExampleSet. Main difference between these two is that EditedExampleSet 
 * allows to edit all examples because here we have direct access to the DataIndex. 
 * @author Marcin
 */
public class EditedExampleSet extends AbstractExampleSet {

    private static final long serialVersionUID = 1L;
    /**
     * The source ExampleSet
     */
    protected ExampleSet parent;
    /**
     * DataIndex stores information on each example if this example is on or off.      
     */
    protected final DataIndex index;

    /**
     * Creates  EditedExampleSet by wrapping another ExampleSet
     * 
     * @param parentSet a parent ExampleSet
     */
    public EditedExampleSet(ExampleSet parentSet) {
        this(parentSet, new DataIndex(parentSet.size()));
        index.setAllTrue();
    }

    /**
     * Creates  EditedExampleSet by wrapping another ExampleSet
     * 
     * @param parentSet - a parent Example Set
     * @param index - an index of examples which are on/off
     */
    public EditedExampleSet(ExampleSet parentSet, DataIndex index) {
        parent = (ExampleSet) parentSet.clone();
        if (parentSet.size() != index.getFullLength()) {
            throw new RuntimeException("Incorect size of index variable");
        }
        this.index = index;
    }

    /**
     * Creates  a copy of another EditedExampleSet. This constructor makes a 
     * clone of parent ExampleSet and hardcopy of DataIndex
     * @param parentSet
     */
    public EditedExampleSet(EditedExampleSet parentSet) {
        parent = (ExampleSet) parentSet.parent.clone();
        index = new DataIndex(parentSet.index);
    }

    /**
     * Method returns a reference to the DataIndex
     * @return
     */
    public DataIndex getIndex() {
        return index;
    }

    @Override
    public Attributes getAttributes() {
        return parent.getAttributes();
    }

    @Override
    public Example getExample(int index) {
        int sum = -1;
        int size = this.index.getFullLength();
        for (int i = 0; i < size; i++) {
            sum += this.index.get(i) ? 1 : 0;
            if (sum == index) {
                return parent.getExample(i);
            }
        }
        return null;
    }

    @Override
    public ExampleTable getExampleTable() {
        return parent.getExampleTable();
    }

    @Override
    public int size() {
        return index.getLength();
    } 

    @Override
    public EditedExampleReader iterator() {
        return new EditedExampleReader(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EditedExampleSet other = (EditedExampleSet) obj;
        if (index == null) {
            if (other.index != null) {
                return false;
            }
        } else if (!index.equals(other.index)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }

    @Override
    public ExampleSet clone() {
        return new EditedExampleSet(this);
    }
}