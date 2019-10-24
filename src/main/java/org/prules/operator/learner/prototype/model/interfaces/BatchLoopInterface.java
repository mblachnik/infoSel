package org.prules.operator.learner.prototype.model.interfaces;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;

public interface BatchLoopInterface {
    PredictionModel trainExpert(SelectedExampleSet selectedExamples) throws OperatorException;
}
