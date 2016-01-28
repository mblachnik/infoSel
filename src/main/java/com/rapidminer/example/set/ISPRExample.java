package com.rapidminer.example.set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;

/**
 * Class represent a single Example from ExampleSet returned by EditedExampleSet and SelectedExampleSet. 
 * This Example hold information about vector index from the DataIndex
 * @author Marcin
 */
public class ISPRExample extends Example {
	private static final long serialVersionUID = -7270851147017131695L;
	
	private int instanceIndex;
	
        /** Constructor which 
         * 
         * @param data - DataRow from the ExampleSet
         * @param parentExampleSet - link to the ExampleSet
         * @param idx - index of the DataIndex
         */
        public ISPRExample(DataRow data, ExampleSet parentExampleSet, int idx) {
		super(data, parentExampleSet);
		instanceIndex = idx;
	}

        /** Returns index value of the DataIndex from EditedExampleSet and SelectedExampleSet         
         * @return
         */
        public int getIndex(){
		return instanceIndex;
	}

}
