/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.clustering.models.AbstractVQModel;
import org.prules.operator.learner.clustering.models.VQModel;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.List;

/**
 * This class provides vector quantization operator. It uses
 * {@link org.prules.operator.learner.clustering.models.VQModel} class
 * which implements the algorithm.
 *
 * @author Marcin
 */
public class VQOperator extends AbstractPrototypeClusteringOnlineOperator {

    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     *
     */
    private static final String PARAMETER_UPDATE_RATE = "Alpha";
    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double updateRate;

    /**
     * Default operator constructor
     *
     * @param description
     */
    public VQOperator(OperatorDescription description) {
        super(description);
        numberOfIteration = 50;
        updateRate = 0.02;
        measureHelper = new DistanceMeasureHelper(this);
    }

    /**
     * Main method in which an instance of
     * {@link org.prules.operator.learner.clustering.models.VQModel} VQ
     * model is executed
     *
     * @param trainingSet
     * @param codeBooks
     * @return
     * @throws OperatorException
     */
    @Override
    public IS_PrototypeClusterModel optimize(ExampleSet trainingSet, ExampleSet codeBooks) throws OperatorException {
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(codeBooks);
        this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
        AbstractVQModel vqModel = new VQModel(codeBooks, numberOfIteration, distance, updateRate);
        vqModel.run(trainingSet);
        boolean addAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);
        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
        ISPRGeometricDataCollection<IInstanceLabels> knnModel;
        if (addAsLabel) {
            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codeBooks, codeBooks.getAttributes().getLabel(), Const.LABEL, distance);
        } else {
            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codeBooks, codeBooks.getAttributes().getCluster(), Const.LABEL, distance);
        }
        IS_PrototypeClusterModel model = new IS_PrototypeClusterModel(trainingSet, knnModel, codeBooks.size(), clusterNames, addAsLabel, addCluster);
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
     * Checks requirements of input data set
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

        //ParameterType lvqTypeParameter =  new 
        ParameterType type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, 0.3);
        type.setExpert(false);
        types.add(type);

        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
