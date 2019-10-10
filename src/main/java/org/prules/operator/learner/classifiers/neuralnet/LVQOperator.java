/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.neuralnet;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.*;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.classifiers.IS_KNNClassificationModel;
import org.prules.operator.learner.classifiers.PredictionType;
import org.prules.operator.learner.classifiers.VotingType;
import org.prules.operator.learner.classifiers.neuralnet.models.*;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LVQ Operator which provides a set of LVQ neuralNetwork GLVQ algorithm - based
 * on paper: Atsushi Sato, Keiji Yamada Generalized Learning Vector
 * Quantization, 1996
 *
 * @author Marcin
 */
public class LVQOperator extends //AbstractPrototypeClassificationOnlineOperator {
        AbstractPrototypeOptimizationChain {

    /**
     * Number of iterations
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";

    /**
     * Initial value of update rate
     */
    private static final String PARAMETER_UPDATE_RATE = "Alpha";
    /**
     * Type of LVQ algorithm
     */
    private static final String PARAMETER_LVQ_TYPE = "LVQ_type";
    /**
     * Parameter for LVQ 2 and 3
     */
    private static final String PARAMETER_WINDOW = "window";
    /**
     * Parameter for LVQ3
     */
    private static final String PARAMETER_EPSILON = "epsilon";

    /**
     * Parameter for WTM based methods such as SNG WTM_LVQ
     */
    private static final String PARAMETER_LVQ_NEIGHBOURHOOD = "Neighbourhood";

    /**
     * Neighborhood parameter for WTM based algorithms
     */
    private static final String PARAMETER_LAMBDA = "Lambda";

    /**
     * Debug mode - store debug values in the log file
     */
    private static final String PARAMETER_DEBUG = "Debug mode";

    public static final String PARAMETER_CALC_DIFF = "Calc dF(u)/du";

    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double updateRate;
    private LVQTypes lvqType;
    private double window;
    private double lambda;
    private double epsilon;
    private LVQNeighborhoodTypes lvqNeighborhoodType;

    /**
     * Constructor of LVQ operator
     *
     * @param description
     */
    public LVQOperator(OperatorDescription description) {
        super(description, PredictionType.Classification);
        numberOfIteration = 50;
        updateRate = 0.02;
        lambda = 0.5;
        lvqType = LVQTypes.LVQ1;
        window = 0.2;
        epsilon = 0.2;
        measureHelper = new DistanceMeasureHelper(this);
        lvqNeighborhoodType = LVQNeighborhoodTypes.GAUSSIAN;
    }

    /**
     * Main method responsible for executing given LVQ algorithm. It takes as
     * input training set and initial codebooks. The codebooks are directly
     * modified, so it cannot be directly delivered examples from output of
     * another operator. This must be new ExampleSet
     *
     * @param trainingSet - training ExampleSet
     * @param codeBooks   - initial CodeBooks position
     * @return - kNN model initialized with codeBooks. If model output port of
     * the operator is not connected it returns null
     * @throws OperatorException
     */
    @Override
    public IS_KNNClassificationModel<IInstanceLabels> optimize(ExampleSet trainingSet, ExampleSet codeBooks) throws OperatorException {
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        distance.init(codeBooks.getAttributes(), trainingSet.getAttributes());
        AbstractLVQModel lvqModel = null;
        int idLvqType = getParameterAsInt(PARAMETER_LVQ_TYPE);
        lvqType = LVQTypes.values()[idLvqType];
        int idLvqNeighborhood = 0;
        switch (lvqType) {
            case LVQ1:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lvqModel = new LVQ1Model(codeBooks, numberOfIteration, distance, updateRate);
                break;
            case SLVQ:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lvqModel = new SLVQ1Model(codeBooks, numberOfIteration, distance, updateRate);
                break;
            case LVQ2:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lvqModel = new LVQ2Model(codeBooks, numberOfIteration, distance, updateRate);
                break;
            case LVQ21:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                window = getParameterAsDouble(PARAMETER_WINDOW);
                lvqModel = new LVQ21Model(codeBooks, numberOfIteration, distance, updateRate, window);
                break;
            case LVQ3:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                window = getParameterAsDouble(PARAMETER_WINDOW);
                epsilon = getParameterAsDouble(PARAMETER_EPSILON);
                lvqModel = new LVQ3Model(codeBooks, numberOfIteration, distance, updateRate, window, epsilon);
                break;
            case WLVQ:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                if (trainingSet.getAttributes().getWeight() == null) {
                    throw new UserError(this, "WLVQ algorithm requires instances weight attribute.");
                }
                lvqModel = new WLVQModel(codeBooks, numberOfIteration, distance, updateRate);
                break;
            case OLVQ:
                updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lvqModel = new OLVQModel(codeBooks, numberOfIteration, distance, updateRate);
                break;
            case GLVQ:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lvqModel = new GLVQModel(codeBooks, numberOfIteration, distance, updateRate);
                break;
            case WTM_LVQ:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lambda = getParameterAsDouble(PARAMETER_LAMBDA);
                idLvqNeighborhood = getParameterAsInt(PARAMETER_LVQ_NEIGHBOURHOOD);
                lvqNeighborhoodType = LVQNeighborhoodTypes.values()[idLvqNeighborhood];
                lvqModel = new WTMLVQModel(codeBooks, numberOfIteration, distance, this.updateRate, lambda, lvqNeighborhoodType);
                break;
            case SNG:
                this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
                lambda = getParameterAsDouble(PARAMETER_LAMBDA);
                lvqModel = new SNGModel(codeBooks, numberOfIteration, distance, this.updateRate, lambda);
                break;
            //case LVQ21SGD
            default:
                throw new UserError(this, "Unknown LVQ type");

        }

        lvqModel.run(trainingSet);

        if (getParameterAsBoolean(PARAMETER_DEBUG)) {
            String commaSeparatedValues;
            //Log cost function
            commaSeparatedValues = lvqModel.getCostFunctionValues().stream().map(Object::toString).collect(Collectors.joining(";"));
            this.log("CostFunction: " + commaSeparatedValues, LogService.MINIMUM);
            List<Double> rates;
            //Log learning rates
            rates = (List<Double>) lvqModel.getStoredValue(AbstractLVQModel.LEARNING_RATE_KEY);
            if (rates != null) {
                commaSeparatedValues = rates.stream().map(Object::toString).collect(Collectors.joining(";"));
                this.log("LearningRate: " + commaSeparatedValues, LogService.MINIMUM);
            }
            //Log lambda rates
            rates = (List<Double>) lvqModel.getStoredValue(AbstractLVQModel.LAMBDA_RATE_KEY);
            if (rates != null) {
                commaSeparatedValues = rates.stream().map(Object::toString).collect(Collectors.joining(";"));
                this.log("LambdaRate: " + commaSeparatedValues, LogService.MINIMUM);
            }
        }
        if (this.modelOutputPort.isConnected()) {
            ISPRGeometricDataCollection<IInstanceLabels> knn = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codeBooks, distance);
            return new IS_KNNClassificationModel<>(codeBooks, knn, 1, VotingType.MAJORITY, PredictionType.Classification);
        }
        return null;
    }

    /**
     * Returns metadata defining number of prototypes
     *
     * @return
     * @throws UndefinedParameterError
     */
    @Override
    protected MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
        if (this.initialPrototypesSourcePort.isConnected()) {
            ExampleSetMetaData prototypesMetaData = (ExampleSetMetaData) this.initialPrototypesSourcePort.getMetaData();
            if (prototypesMetaData != null) {
                return prototypesMetaData.getNumberOfExamples();
            }
        }
        int num = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
        return new MDInteger(num);
    }

    /**
     * This method defines all possible input data properties required by LVQ
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
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
                return true;
            default:
                return false;
        }
    }

    /**
     * Configuration of LVQ Operator
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        //ParameterType lvqTypeParameter =  new 
        ParameterType type;

        type = new ParameterTypeCategory(PARAMETER_LVQ_TYPE, "Defines on type of lvq algorithm.", LVQTypes.typeNames(), 0);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.typeNames(), false,
                LVQTypes.LVQ1.ordinal(), LVQTypes.LVQ2.ordinal(), LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal(),
                LVQTypes.OLVQ.ordinal(), LVQTypes.WLVQ.ordinal(), LVQTypes.SLVQ.ordinal(), LVQTypes.GLVQ.ordinal(),
                LVQTypes.WTM_LVQ.ordinal(), LVQTypes.SNG.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, this.updateRate);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.typeNames(), false,
                LVQTypes.LVQ1.ordinal(), LVQTypes.LVQ2.ordinal(), LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal(),
                LVQTypes.WLVQ.ordinal(), LVQTypes.SLVQ.ordinal(), LVQTypes.GLVQ.ordinal(), LVQTypes.OLVQ.ordinal(),
                LVQTypes.WTM_LVQ.ordinal(), LVQTypes.SNG.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_WINDOW, "Defines the relative window width", 0, 1, window);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.typeNames(), false,
                LVQTypes.LVQ21.ordinal(), LVQTypes.LVQ3.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_EPSILON, "Defines the epsilon of LVQ3", 0, 1, epsilon);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.typeNames(), false,
                LVQTypes.LVQ3.ordinal()));
        types.add(type);

        type = new ParameterTypeCategory(PARAMETER_LVQ_NEIGHBOURHOOD, "Type of Neighborhood function.", LVQNeighborhoodTypes.typeNames(), 0);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.typeNames(), false,
                LVQTypes.WTM_LVQ.ordinal()));
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_LAMBDA, "Initial neighborhood rate", 0, Double.MAX_VALUE, this.lambda);
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LVQ_TYPE, LVQTypes.typeNames(), false,
                LVQTypes.WTM_LVQ.ordinal(), LVQTypes.SNG.ordinal()));
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));

        type = new ParameterTypeBoolean(PARAMETER_DEBUG, "Debug mode. Recorded values are stored in a RapidMiner log", false);
        types.add(type);
        return types;
    }
}
