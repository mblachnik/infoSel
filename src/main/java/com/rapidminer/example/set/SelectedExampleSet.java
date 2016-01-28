package com.rapidminer.example.set;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;

/**
 * A wrapper for the ExampleSet which allows for instance selection. The instance selection is based 
 * on boolean array DataIndex which stores information on each individual Example.
 * It is very similar to the EditedExampleSet. Main difference between these two is that SelectedExampleSet 
 * allows to edit just the examples which are "on". Examples which are off are not reachable. 
 * @author Marcin
 */
public class SelectedExampleSet extends AbstractExampleSet {
    
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
         * Creates  SelectedExampleSet by wrapping another ExampleSet
         * @param parentSet a parent ExampleSet
         */
        public SelectedExampleSet(ExampleSet parentSet){
		this(parentSet, new DataIndex(parentSet.size()));
		index.setAllTrue();				
	}
	
       /**
     * Creates  SelectedExampleSet by wrapping another ExampleSet
     * 
     * @param parentSet - a parent Example Set
     * @param index - an index of examples which are on/off
     */
        public SelectedExampleSet(ExampleSet parentSet, DataIndex index){
		parent = (ExampleSet)parentSet.clone();
		if (parentSet.size() != index.getFullLength())
			throw new RuntimeException("Incorect size of index variable"); 
		this.index= index;				
	}
	
	
    /**
     * Creates  a copy of another SelectedExampleSet. This constructor makes a 
     * clone of parent ExampleSet and hardcopy of DataIndex
     * @param parentSet
     */
        public SelectedExampleSet(SelectedExampleSet parentSet){
		parent = (ExampleSet)parentSet.parent.clone();
		index = new DataIndex(parentSet.index);		
	}
	
    /**
     * Method returns an index of all examples which are "on"
     * @return DataIndex with the size of examples which are "on"
     */
        public DataIndex getIndex(){
		return index.getIndex();
	}
        
/**
 * Method returns parent exampleSet, this may be sometimes useful for example to create weights from the instance selection operators
 * Be careful and use this method very carefully 
 * @return original example set which is a parent example set
 */        
//TODO check the accesor, maybe weaker would be better       
        public ExampleSet getParentExampleSet(){
            return parent;
        }
  /**
 * Method returns handle to original dataIndex - 
 * Be careful and use this method very carefully 
 * @return handle to the original DataIndex 
 */     
//TODO check the accesor, maybe weaker would be better       
        public DataIndex getFullIndex(){
            return index;
        }
	
        /**
         * Setter for DataIndex, it allows to set indexes of examples which are "on". 
         * @param index - DataIndex with size equal to the number of examples which are "on" i the parent DataIndex
         */
        public void setIndex(DataIndex index){
		this.index.setIndex(index);
	}
	
	@Override
	public Attributes getAttributes() {
		return parent.getAttributes();
	}

	@Override
	public Example getExample(int index) {
		int sum = -1;
		int size = this.index.getFullLength();
		for (int i = 0; i < size; i++ ){
			sum += this.index.get(i) ? 1 : 0;
			if (sum==index)
				return parent.getExample(i); 
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
	public SelectedExampleReader iterator() {
		return new SelectedExampleReader(this);
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
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectedExampleSet other = (SelectedExampleSet) obj;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}		
	
	@Override
	public ExampleSet clone(){
		return new SelectedExampleSet(this);
	}

}
