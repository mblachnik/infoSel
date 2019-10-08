/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.preprocessing;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.AbstractModel;
import org.prules.operator.learner.preprocessing.model.VDMNominal2NumericalModel;

/**
 * @author Marcin
 */

/**
 * This is a preprocessing model which applies VDM Nominal to numerical attribute transformation.
 *
 * @author Marcin
 */

//TODO add metadata processing
public class VDMNominal2NumericalRMModel extends AbstractModel {
    private VDMNominal2NumericalModel model;


    VDMNominal2NumericalRMModel(ExampleSet exampleSet, VDMNominal2NumericalModel model) {
        super(exampleSet);
        this.model = model;
    }

    @Override
    public ExampleSet apply(ExampleSet testSet) {
        return model.run((ExampleSet) testSet.clone());
    }
}
