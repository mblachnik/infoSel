/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.*;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.clustering.ISPRClusterModel;
import com.rapidminer.ispr.operator.learner.clustering.ISPRClusterModelTools;
import com.rapidminer.ispr.operator.learner.clustering.ISPRPrototypeBatchClusterModel;
import com.rapidminer.ispr.operator.learner.optimization.clustering.AbstractBatchModel;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

import java.util.*;

/**
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationOperator extends AbstractPRulesOperator {

    public static final String PARAMETER_ADD_PARTITION_MATRIX = "Add partition matrix";
    private static final long serialVersionUID = 21;
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    double costFunctionValue = Double.NaN;
    protected final PredictionType predictionType;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");
    protected Map<Integer, String> clusterName;

    /**
     *
     * @param description
     * @param predictionType
     */
    public AbstractPrototypeOptimizationOperator(OperatorDescription description, PredictionType predictionType) {
        super(description);
        this.predictionType = predictionType;
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        if (predictionType == PredictionType.Clustering) {
            getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, ISPRClusterModel.class));
        } else {
            getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        }

        addValue(new ValueDouble("Instances_beafore_selection", "Number Of Examples in the training set") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesBeaforeSelection;
            }
        });
        addValue(new ValueDouble("Instances_after_selection", "Number Of Examples after selection") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesAfterSelection;
            }
        });
        addValue(new ValueDouble("Compression", "Compressin = #Instances_after_selection/#Instances_beafore_selection") {

            @Override
            public double getDoubleValue() {
                return compression;
            }
        });
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
        numberOfInstancesBeaforeSelection = trainingSet.size();
        AbstractBatchModel trainModel = optimize(trainingSet);
        Collection<Prototype> prototypes = trainModel.getPrototypes();
        costFunctionValue = trainModel.getCostFunctionValue();
        clusterName = ISPRClusterModelTools.prepareClusterNamesMap(prototypes.size());
        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);        
        boolean addClusterAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);        
        boolean keepPartitionMatrix = getParameterAsBoolean(PARAMETER_ADD_PARTITION_MATRIX);
        ISPRPrototypeBatchClusterModel model = new ISPRPrototypeBatchClusterModel(trainModel, clusterName, trainingSet, prototypes.size(), addCluster, addClusterAsLabel, true, keepPartitionMatrix);
        modelOutputPort.deliver(model);        
        model.apply(trainingSet,true);        
        ExampleSet codebooks = ISPRClusterModelTools.prepareCodebooksExampleSet(prototypes, clusterName, trainingSet.getAttributes());
        numberOfInstancesAfterSelection = codebooks.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
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
        if (predictionType == PredictionType.Clustering) {
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
        }
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
