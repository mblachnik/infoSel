/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperatorChain;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.optimization.clustering.AbstractBatchModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.GenerateModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract structure supporting implementation of prototype based clustering
 * methods
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationChain extends AbstractPRulesOperatorChain {

    private static final long serialVersionUID = 21;
    /**
     *
     */
    protected final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("Example Set");
    /**
     *
     */
    protected final InputPort initialPrototypesInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("Codebooks");
    /**
     *
     */
    //protected final OutputPort initialPrototyoesOutputPort = getOutputPorts().createPassThroughPort("Initial Prototpes");    

    protected final OutputPort modelOutputPort = getOutputPorts().createPort("Model");

    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    protected final PredictionType predictionType;
    protected HashMap<Integer, String> clusterNamesMap;

    /**
     * Constructor
     *
     * @param description
     * @param predictionType
     */
    public AbstractPrototypeOptimizationChain(OperatorDescription description, PredictionType predictionType) {
        super(description, "Initialization");//
        this.predictionType = predictionType;
        getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
            @Override
            public boolean supportsCapability(OperatorCapability capability) {
                switch (capability) {
                    case BINOMINAL_ATTRIBUTES:
                    case POLYNOMINAL_ATTRIBUTES:
                    case NUMERICAL_ATTRIBUTES:
                    case POLYNOMINAL_LABEL:
                    case BINOMINAL_LABEL:
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
        }, exampleSetInputPort));

        initialPrototypesInnerSourcePort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
            @Override
            public boolean supportsCapability(OperatorCapability capability) {
                switch (capability) {
                    case BINOMINAL_ATTRIBUTES:
                    case POLYNOMINAL_ATTRIBUTES:
                    case NUMERICAL_ATTRIBUTES:
                    case POLYNOMINAL_LABEL:
                    case BINOMINAL_LABEL:
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
        }, initialPrototypesInnerSourcePort));

        initialPrototypesInnerSourcePort.addPrecondition(new ExampleSetPrecondition(exampleSetInputPort));
        initialPrototypesInnerSourcePort.addPrecondition(new DistanceMeasurePrecondition(initialPrototypesInnerSourcePort, this));

        getTransformer().addPassThroughRule(initialPrototypesInnerSourcePort, prototypesOutputPort);
        //getTransformer().addPassThroughRule(initialPrototypesInnerSourcePort, initialPrototyoesOutputPort);                
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleInnerSourcePort);

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
    }

    /**
     * Main method responsible for processing input example set
     *
     * @param trainingSet training set
     * @return - exampleSet of cluster centers
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        numberOfInstancesBeaforeSelection = trainingSet.size();
        exampleInnerSourcePort.deliver(trainingSet);
        getSubprocess(0).execute();
        inApplyLoop();
        ExampleSet codebooksInitialization = initialPrototypesInnerSourcePort.getDataOrNull(ExampleSet.class);
        if (codebooksInitialization == null) {
            throw new UserError(this, "Need initial prototypes");
        }
        //initialPrototyoesOutputPort.deliver(codebooksInitialization);             
        ExampleSet codebooks = PRulesUtil.duplicateExampleSet(codebooksInitialization);
        prepareClusterNamesMap(codebooks.size());
        MyKNNClassificationModel<Number> model = optimize(trainingSet, codebooks);
        modelOutputPort.deliver(model);
        prepareTrainingExampleSet(trainingSet,model);
        prepareTrainingExampleSet(codebooks,model);
        numberOfInstancesAfterSelection = codebooks.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        return codebooks;
    }

    /**
     * Configuration parameters
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        if (predictionType == PredictionType.Clustering) {
            ParameterType type;
            type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE,
                    "If enabled, a cluster id is generated as new special attribute directly in this operator, otherwise this operator does not add an id attribute. In the latter case you have to use the Apply Model operator to generate the cluster attribute.",
                    true, false);
            types.add(type);

            type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL,
                    "If true, the cluster id is stored in an attribute with the special role 'label' instead of 'cluster'.",
                    false);
            type.setExpert(false);
            types.add(type);
        }
        return types;
    }

    /**
     * Abstract method which needs to be implemented by any prototype based
     * optimization algorithm
     *
     * @param trainingSet
     * @param codebooks
     * @return
     * @throws OperatorException
     */
    public abstract MyKNNClassificationModel<Number> optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException;

    /**
     * Adds new attributes to the input trainingSet which would store clustering
     * results. These new atrributes can be returned with cluster or label role
     *
     * @param trainingSet
     * @param model
     */
    protected void prepareTrainingExampleSet(ExampleSet trainingSet, MyKNNClassificationModel<Number> model) throws OperatorException {
        //Preparing attributes for trainingSet   
        boolean b = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);        
        //LOG.log(Level.FINEST, "VQ:{0}, Updating, Iteratrion:{1}", new Object[]{this.toString(), currentIteration});
        if (b) {
            int c = clusterNamesMap.size();
            Attribute traininSetLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
            NominalMapping labelsNames = new PolynominalMapping(clusterNamesMap);
            traininSetLabels.setMapping(labelsNames);
            traininSetLabels.setDefault(Double.NaN);
            trainingSet.getExampleTable().addAttribute(traininSetLabels);
            Attributes attributes = trainingSet.getAttributes();
            if (getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL)) {
                attributes.setLabel(traininSetLabels);                
            } else {
                attributes.setCluster(traininSetLabels);                
            }            
            model.performPrediction(trainingSet, traininSetLabels);
        }
    }

    /**
     * Creates a HashMap<Integer, String> which maps cluster number into cluster
     * name in a form of {
     *
     * @see Attributes.CLUSTER_NAME} _\#cluster_number
     * @param c - number of clusters
     */
    protected void prepareClusterNamesMap(int c) {
        this.clusterNamesMap = new HashMap<>(c); //Map of nominal values of Cluster attribute        
        for (int i = 0; i < c; i++) {
            String clusterName = Attributes.CLUSTER_NAME + "_" + i;
            clusterNamesMap.put(i, clusterName);
        }
    }
}
