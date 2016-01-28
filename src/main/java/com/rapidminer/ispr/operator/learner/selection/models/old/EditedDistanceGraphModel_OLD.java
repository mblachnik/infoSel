/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.old;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.ispr.operator.learner.selection.models.EditedDistanceGraphCriteria;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 *
 * @author Marcin
 */
public class EditedDistanceGraphModel_OLD {
    
    private DistanceMeasure distance;
    private EditedDistanceGraphCriteria criteria;

    /**
     * 
     * @param distance
     */
    public EditedDistanceGraphModel_OLD(DistanceMeasure distance, EditedDistanceGraphCriteria criteria) {
        this.distance = distance;
        this.criteria = criteria;
    }

    /**
     * 
     * @param inputExampleSet
     * @return
     */
    
    public ExampleSet run(ExampleSet inputExampleSet) {
        SelectedExampleSet exampleSet;
        if (inputExampleSet instanceof SelectedExampleSet) {
            exampleSet = (SelectedExampleSet) inputExampleSet;
        } else {
            exampleSet = new SelectedExampleSet(inputExampleSet);
        }
        int size = exampleSet.size();
        EditedExampleSet exampleSetB = new EditedExampleSet(exampleSet);
        EditedExampleSet exampleSetC = new EditedExampleSet(exampleSet);             
        DataIndex indexA = new DataIndex(size);
        indexA.setAllFalse();
        DataIndex indexB = exampleSetB.getIndex();
        DataIndex indexC = exampleSetC.getIndex();
        int counterA = 0;
        int counterB;
        for (Example exampleA : exampleSet) {
            double labelA = exampleA.getLabel();
            ISPRExample exampleAA = (ISPRExample) exampleA;
            int exampleAId = exampleAA.getIndex();
            indexB.set(exampleAId, false);
            indexC.set(exampleAId, false);
            counterB = counterA+1;
            for (Example exampleB : exampleSetB) {                
                if (exampleB.getLabel() != labelA) {
                    ISPRExample exampleBB = (ISPRExample) exampleB;
                    indexC.set(exampleBB.getIndex(), false);
                    boolean chk = true;
                    double dAB = distance.calculateDistance(exampleA, exampleB);  
                    for (Example exampleC : exampleSetC) {                        
                        double dAC = distance.calculateDistance(exampleA, exampleC);  
                        double dBC = distance.calculateDistance(exampleB, exampleC);  
                        if (criteria.evaluate(dAB, dAC, dBC)) {                            
                            chk = false;
                            break;
                        }
                    }
                    if (chk) {                        
                        indexA.set(counterA,true);
                        indexA.set(counterB,true);                        
                    }
                    indexC.set(exampleBB.getIndex(), true);
                }
                counterB++;
            }
            indexC.set(exampleAId, true);
            counterA++;
        }
        exampleSet.setIndex(indexA);
        return exampleSet;
    }    
}

