package org.prules.operator.learner.clustering;

import java.util.List;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import org.prules.operator.learner.clustering.models.AbstractBatchModel;
import org.prules.operator.learner.clustering.models.FCMModel;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which implements Fuzzy C-means clustering operator
 * @author Marcin
 */
public class FCMOperator extends AbstractPrototypeClusteringBatchOperator {
    /**
     * Identification string for operator parameter responsible for number of iterations
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     * Identification string for operator parameter responsible for fuzzynes parameter
     */
    public static final String PARAMETER_FUZZYNES = "Fuzzynes";
    /**
     *Identification string for operator parameter responsible for minimal gain during clustering
     */
    public static final String PARAMETER_MIN_GAIN = "MinGain";
    /**
     *Identification string for operator parameter responsible for number of clusters
     */
    public static final String PARAMETER_NUMBER_OF_CLUSTERS = "Clusters";

    int c; //Number of clusters
    double m; //Fuzzynes values
    DistanceMeasureHelper measureHelper;
    int numberOfIteration;
    double minGain; //Minimum improvement of optimization process    
    

    /**
     * Constructor of FCM Operator
     *
     * @param description
     */
    public FCMOperator(OperatorDescription description) {
        super(description);        
        c = 3; //Number of clusters
        m = 2; //Fuzzynes values
        numberOfIteration = 50;
        minGain = 0.00001;
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     * Main method responsible for executing given FCM algorithm. It takes as
     * input training set, and returns ExampleSet with cluster centers and
     * nearest neighbor model initialized with cluster centers. If model output
     * port is not connected method model is null
     *
     * @param trainingSet - training ExampleSet
     * @return - pair of elements: cluster centers, and kNN model initialized
     * with cluster centers.
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
        AbstractBatchModel batchModel = new FCMModel(distance, m, numberOfIteration, minGain, RandomGenerator.getRandomGenerator(this), c);        
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
     * This method defines all possible input data capabilities provided by FCM
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
        } catch (Exception e) {
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
            default:
                return false;
        }
    }

    /**
     * Configuration of FCM Operator
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
