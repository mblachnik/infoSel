/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.keel;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.Operator;
import keel.Algorithms.Instance_Selection.CCIS.CCIS;
import keel.Algorithms.Instance_Selection.HMNEI.HMNEI;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;

/**
 * @author Marcin
 */
public class KeelISModel extends AbstractInstanceSelectorModel {
    //
//    String configurationString;
//    Operator parent;
//
    KeelISAlgorithms model;

    public KeelISModel(KeelISAlgorithms model, String configurationString, Operator parent) {
//        this.configurationString = configurationString;
//        this.parent = parent;
        this.model = model;
    }
//  

    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        Attributes attrs = exampleSet.getAttributes();
        int m = attrs.size();
        int n = exampleSet.size();
        double[][] trainingSet = new double[exampleSet.size()][attrs.size()];
        int[] labels = new int[exampleSet.size()];
        //========================> Create dataset for Keel
        int i = 0;
        for (Example ex : exampleSet) {
            int j = 0;
            for (Attribute a : attrs) {
                trainingSet[i][j] = ex.getValue(a);
                int l = (int) ex.getLabel();
                labels[i] = l;
                j++;
            }
            i++;
        }
        //========================> Create final DataFilter to get an index out of KeelResults
        KeelDataFilter filter = new KeelDataFilter(trainingSet, labels);
        //========================> RUN KEEL IS model
        switch (model) {
            case CCIS: {
                CCIS model = new CCIS(trainingSet, labels);
                model.ejecutar();
                labels = model.getLabels();
                trainingSet = model.getSamples();
            }
            break;
            case HMNEI: {
                HMNEI model = new HMNEI(trainingSet, labels);
                model.ejecutar();
                labels = model.getLabels();
                trainingSet = model.getSamples();
            }
            break;
        }

        int numberOfMisses = 0;
        //=========================> Convert results to index
        IDataIndex index = filter.filterSamples(trainingSet, labels);
        if (filter.getNumberOfMisses() > 0)
            throw new RuntimeException("Numer of misses in DataFIlter should be 0 but it isnt. Number of misses= " + filter.getNumberOfMisses());
        return index;
    }
}
