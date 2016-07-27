/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.clustering;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.ispr.operator.learner.clustering.IS_ClusterModel;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.ispr.operator.learner.clustering.IS_ClusterModelTools;
import com.rapidminer.ispr.operator.learner.clustering.IS_PrototypeBatchClusterModel;
import com.rapidminer.ispr.dataset.SimpleInstance;
import com.rapidminer.ispr.operator.learner.clustering.models.AbstractBatchModel;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

import java.util.*;

/**
 * This is an abstract class for all prototype based clustering operators which use batch based update method.
 * In fact it requires that the clustering algorithm will be implemented based on the {@link  com.rapidminer.ispr.operator.learner.clustering.models.AbstractBatchModel} 
 * @author Marcin
 */
public abstract class AbstractPrototypeClusteringBatchOperator extends AbstractPRulesOperator {

    public static final String PARAMETER_ADD_PARTITION_MATRIX = "Add partition matrix";
    private static final long serialVersionUID = 21;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");
    double costFunctionValue = Double.NaN;
    protected Map<Integer, String> clusterNames;

    /**
     * Constructor of prototype based clustering operator. 
     * 
     *
     * @param description     
     */
    public AbstractPrototypeClusteringBatchOperator(OperatorDescription description) {
        super(description);
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_ClusterModel.class));
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));        
       
        addValue(new ValueDouble("CostFunctionValue", "Cost Function Value") {
            @Override
            public double getDoubleValue() {
                return costFunctionValue;
            }
        });
    }

    /**
     * method implemented after AbstractPRulesOperator operator - responsible
     * for main data operations It runs the model and performs clustering, also
     * responsible for delivering results to the output.
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {        
        AbstractBatchModel trainModel = optimize(trainingSet);
        Collection<SimpleInstance> prototypes = trainModel.getPrototypes();
        costFunctionValue = trainModel.getCostFunctionValue();
        clusterNames = IS_ClusterModelTools.prepareClusterNamesMap(prototypes.size());
        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
        boolean addClusterAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);
        boolean keepPartitionMatrix = getParameterAsBoolean(PARAMETER_ADD_PARTITION_MATRIX);
        IS_PrototypeBatchClusterModel model = new IS_PrototypeBatchClusterModel(trainModel, clusterNames, trainingSet, prototypes.size(), addCluster, addClusterAsLabel, true, keepPartitionMatrix);
        modelOutputPort.deliver(model);
        Tools.checkAndCreateIds(trainingSet);
        model.apply(trainingSet, true);
        ExampleSet codebooks = IS_ClusterModelTools.prepareCodebooksExampleSet(prototypes, clusterNames, trainingSet.getAttributes());
        return codebooks;
    }

    /**
     * Configuration options
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type;
        type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE,
                "If true, the cluster id is stored in an attribute with the special role 'label' instead of 'cluster'.",
                true);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL,
                "If true, the cluster id is stored in an attribute with the special role 'label' instead of 'cluster'.",
                false);
        type.setExpert(false);
        types.add(type);        
     
        type = new ParameterTypeBoolean(PARAMETER_ADD_PARTITION_MATRIX,
                "If true, results also contain membership matrix which shows how strong given example belong to particular cluster.",
                false);
        type.setExpert(true);
        types.add(type);

        return types;
    }

    /**
     * Abstract method required to be implemented by the successor of
     * AbstractPrototypeOptimizationOperator. It should implement the main
     * prototype optimization algorithm
     *
     * @param trainingSet - dataset to be clustered
     * @return - pair of elements. The first one is Dataset and the second one
     * is clustering model - namely kNN
     * @throws OperatorException
     */
    public abstract AbstractBatchModel optimize(ExampleSet trainingSet) throws OperatorException;

}
