package org.prules.operator.learner.clustering;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.clustering.models.AbstractBatchModel;
import org.prules.operator.learner.clustering.models.CFCMModel;

import java.util.List;

/**
 * Class which implements Conditional Fuzzy C-means clustering operator
 *
 * @author Marcin
 */
public class CFCMOperator extends AbstractPrototypeClusteringBatchOperator {

    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     *
     */
    private static final String PARAMETER_FUZZYNES = "Fuzzynes";
    /**
     *
     */
    private static final String PARAMETER_MIN_GAIN = "MinGain";
    /**
     *
     */
    public static final String PARAMETER_NUMBER_OF_CLUSTERS = "Clusters";

    private int c; //Number of clusters
    private double m; //Fuzzynes values
    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double minGain; //Minimum improvement of optimization process

    /**
     * Constructor of FCM operator
     *
     * @param description
     */
    public CFCMOperator(OperatorDescription description) {
        super(description);
        c = 3; //Number of clusters
        m = 2; //Fuzzynes values
        numberOfIteration = 50;
        minGain = 0.0001;
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     * Main method responsible for executing given CFCM algorithm. It takes as
     * input training set, and returns ExampleSet with cluster centers and
     * nearest neighbor model initialized with cluster centers. If model output
     * port is not connected method model is null
     *
     * @param trainingSet - training ExampleSet
     * @return - pair of elements: cluster centers returned as ExampleSet and
     * kNN model initialized with cluster centers.
     * @throws OperatorException
     */
    @Override
    public AbstractBatchModel optimize(ExampleSet trainingSet) throws OperatorException {
        //Creating attributes related with partition Matrix
        this.c = getParameterAsInt(PARAMETER_NUMBER_OF_CLUSTERS);
        if (c > trainingSet.size()) {
            throw new UserError(this, "Number of clusters greater then number of samples");
        }
        m = getParameterAsDouble(PARAMETER_FUZZYNES);
        minGain = getParameterAsDouble(PARAMETER_MIN_GAIN);
        numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        AbstractBatchModel batchModel = new CFCMModel(distance, m, numberOfIteration, minGain, RandomGenerator.getRandomGenerator(this), c);
        batchModel.train(trainingSet);
        return batchModel;
    }

    /**
     * Returns number of prototypes
     *
     * @return
     * @throws com.rapidminer.parameter.UndefinedParameterError
     */
    @Override
    public MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
        c = this.getParameterAsInt(PARAMETER_NUMBER_OF_CLUSTERS);
        return new MDInteger(c);
    }

    /**
     * This method defines all possible input data properties required by CFCM
     * operator
     *
     * @param capability
     * @return
     */
    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception ignored) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            case WEIGHTED_EXAMPLES:
            case NO_LABEL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Configuration of CFCM Operator
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type;

        type = new ParameterTypeInt(PARAMETER_NUMBER_OF_CLUSTERS, "Number of clusters", 1, Integer.MAX_VALUE, c);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, numberOfIteration);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_FUZZYNES, "Fuzzynes", 2, Double.MAX_VALUE, m);
        type.setExpert(true);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_MIN_GAIN, "Minimum gain during optimization", 0, Double.MAX_VALUE, minGain);
        type.setExpert(true);
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));

        types.addAll(RandomGenerator.getRandomGeneratorParameters(this));

        return types;
    }
}
