package com.rapidminer.example.set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import org.prules.operator.learner.tools.DataIndex;
import java.util.Iterator;

/** Class is an iterator which iterates over examples from EditedExampleSet and SelectedExampleSet
 * 
 * @author Marcin
 */
public class EditedExampleReader extends AbstractExampleReader {
		private final Iterator<Example> state; 
		private ISPRExample instance = null; 
		private int instanceIndex = 0; 
		private boolean hasNext = false;
		private DataIndex index;	
		private ExampleSet parent;		
		
                /**
                 * Constructor of the EditedExampleReader, which takes as an input EditedExampleSet
                 * @param parent - parent ExampleSet
                 */
                protected EditedExampleReader(EditedExampleSet parent){
			state = parent.parent.iterator();
			index = new DataIndex(parent.index);
			this.parent = parent;
			nextElement();	
		}
		
		@Override
		public boolean hasNext() {
			return hasNext;
		}

		@Override
		public ISPRExample next() {
			ISPRExample oldInstance = instance;
			nextElement();
			return oldInstance;
		}
		
		private void nextElement(){	
			DataRow tmpInstance;
			instance = null;
			hasNext = false;			
			while ( state.hasNext() ){ 
				tmpInstance = state.next().getDataRow();					
				if (index.get(instanceIndex)){
					instance = new ISPRExample(tmpInstance, parent, instanceIndex);
					hasNext = true;
					instanceIndex++;
					return;
				} else {
					instanceIndex++;
				}										
			}				
		}	
}
