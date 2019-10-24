package org.prules.operator.learner.prototype.model;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import org.prules.operator.learner.prototype.PrototypesEnsembleModel;

public abstract class AbstractBatchLoopModel {
    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Training data set
     */
    private ExampleSet examples;
    /**
     * Input model
     */
    private PrototypesEnsembleModel model;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * Constructor for AbstractNearestProtoModel
     *
     * @param examples - training examples
     * @param model    - model to be used to
     */
    public AbstractBatchLoopModel(ExampleSet examples, PrototypesEnsembleModel model) {
        //Copy data
        this.examples = (ExampleSet) examples.clone();
        this.model = model;
    }
    //</editor-fold>

    //<editor-fold desc="Compute stage" defaultState="collapsed" >

    /**
     * Compute model
     */
    public abstract void process() throws OperatorException;
    //</editor-fold>

    //<editor-fold desc="Delivery stage" defaultState="collapsed" >

    /**
     * Method run at the end, delivers created model from operator
     *
     * @return PredictionModel - created model
     */
    public abstract PredictionModel retrieveModel();
    //</editor-fold>

    //<editor-fold desc="Public getters" defaultState="collapsed" >
    public ExampleSet getExamples() {
        return examples;
    }

    public PrototypesEnsembleModel getModel() {
        return model;
    }
    //</editor-fold>
}
