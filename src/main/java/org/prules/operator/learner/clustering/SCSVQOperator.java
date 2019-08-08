/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import org.prules.operator.learner.clustering.models.AbstractVQModel;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.*;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import static org.prules.operator.learner.clustering.AbstractPrototypeClusteringOnlineOperator.PARAMETER_NUMBER_OF_NEURONS;
import org.prules.operator.learner.clustering.models.SCSVQModelBuilder;
import org.prules.operator.learner.clustering.models.SRVQModelBuilder;
import org.prules.tools.math.container.knn.KNNFactory;

/**
 * This class provides vector quantization operator. It uses
 * {@link org.prules.operator.learner.clustering.models.SRVQModel} class
 * which implements the algorithm.
 *
 * @author Marcin Blachnik
 */
public class SCSVQOperator extends AbstractPrototypeClusteringOnlineOperator {

    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     *
     */
    public static final String PARAMETER_UPDATE_RATE = "Alpha";
    public static final String PARAMETER_VQ_TYPE = "VQ_type";
    public static final String PARAMETER_SR_TEMPERATURE = "Temperature";
    public static final String PARAMETER_SR_TEMP_RATE = "Temperature_rate";    
    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double updateRate;
    private double temperature;
    private double temperatureRate;
    /**
     * Default operator constructor
     *
     * @param description
     */
    public SCSVQOperator(OperatorDescription description) {
        super(description);
        numberOfIteration = 50;
        updateRate = 0.02;
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     * Main method in which an instance of
     * {@link com.rapidminer.ispr.operator.learner.clustering.models.VQModel} VQ
     * model is executed
     *
     * @param trainingSet
     * @param codebooks
     * @return
     * @throws OperatorException
     */
    @Override
    public IS_PrototypeClusterModel optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException {
        AbstractVQModel vqModel;
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(codebooks);
        this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
        this.temperature = getParameterAsDouble(PARAMETER_SR_TEMPERATURE);
        this.temperatureRate = getParameterAsDouble(PARAMETER_SR_TEMP_RATE);
        vqModel = SCSVQModelBuilder.builder()
                        .withPrototypes(codebooks)
                        .withIterations(numberOfIteration)
                        .withMeasure(distance)
                        .withAlpha(updateRate)
                        .withTemperature(temperature)
                        .withTemperatureRate(temperatureRate)
                        .build();
        vqModel.run(trainingSet);

        boolean addAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);
        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
        ISPRGeometricDataCollection<IInstanceLabels> knnModel;
        if (addAsLabel) {
            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getLabel(), Const.LABEL, distance);
        } else {
            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getCluster(), Const.LABEL, distance);
        }
        IS_PrototypeClusterModel model = new IS_PrototypeClusterModel(trainingSet, knnModel, codebooks.size(), clusterNames, addAsLabel, addCluster);
        return model;
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
     * Checks requirements of input dataset
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
     * Operator parameters configuration
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        //ParameterType vqTypeParameter =  new
        ParameterType type;

        type = new ParameterTypeDouble(PARAMETER_SR_TEMPERATURE, "Initial temperature", 0, Double.MAX_VALUE, 50.0);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_SR_TEMP_RATE, "Temperature update rate", 0, Double.MAX_VALUE, 0.89);
        type.setExpert(false);
        types.add(type); 

        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, 0.3);
        type.setExpert(false);
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
