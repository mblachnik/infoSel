package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

public interface DecisionBorderSplitter {
    ExampleSet split(ExampleSet exampleSet) throws OperatorException;
}
