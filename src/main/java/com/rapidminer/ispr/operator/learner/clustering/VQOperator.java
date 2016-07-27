/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.clustering;

import java.util.List;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.clustering.models.AbstractVQModel;
import com.rapidminer.ispr.operator.learner.clustering.models.VQModel;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

/**
 * This class provides vector quantization operator. It uses
 * {@link com.rapidminer.ispr.operator.learner.clustering.models.VQModel} class
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
    public static final String PARAMETER_UPDATE_RATE = "Alpha";
    private DistanceMeasureHelper measureHelper;
    private int numberOfIteration;
    private double updateRate;

    /**
     * Default operator constructor
     *
     * @param description
     *
     */
    public VQOperator(OperatorDescription description) {
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
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(codebooks);
        this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);                
        AbstractVQModel vqModel = new VQModel(codebooks, numberOfIteration, distance, updateRate);
        vqModel.run(trainingSet);        
        boolean addAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);
        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);        
        ISPRGeometricDataCollection<Number> knnModel;
        if (addAsLabel){
            knnModel = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, distance);
        } else {
            knnModel = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getCluster() ,distance);
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
