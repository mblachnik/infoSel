/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.clustering;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.ispr.operator.AbstractPrototypeBasedOperatorChain;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.clustering.models.AbstractVQModel;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.RandomInstanceSelectionModel;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.clustering.ClusterModel;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.IsConnectedPrecondition;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.PortConnectedCondition;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is an abstract class for all prototype based clustering algorithms which
 * use online update method. In fact it requires that the clustering algorithm
 * will be implemented using the
 * {@link  com.rapidminer.ispr.operator.learner.clustering.models.AbstractVQModel}
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeClusteringOnlineOperator extends AbstractPrototypeBasedOperatorChain {

    private static final long serialVersionUID = 21;
    public static final String PARAMETER_NUMBER_OF_NEURONS = "Number of neurons";
    /**
     *
     */
    protected final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("example Set");
    /**
     *
     */
    protected final InputPort initialPrototypesSourcePort = getSubprocess(0).getInnerSinks().createPort("codebooks");  
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");
    double costFunctionValue = Double.NaN;
    protected Map<Integer, String> clusterNames;

    /**
     * Constructor of prototype based clustering methods
     *
     * @param description
     */
    public AbstractPrototypeClusteringOnlineOperator(OperatorDescription description) {        
        super(description, "Initialize_codebooks");//        
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(new CapabilityProvider() {
            @Override
            public boolean supportsCapability(OperatorCapability capability) {
                switch (capability) {
                    case BINOMINAL_ATTRIBUTES:
                    case POLYNOMINAL_ATTRIBUTES:
                    case NUMERICAL_ATTRIBUTES:
                    //case POLYNOMINAL_LABEL:
                    //case BINOMINAL_LABEL:
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
        }, exampleSetInputPort));        
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new CapabilityPrecondition(new CapabilityProvider() {
            @Override
            public boolean supportsCapability(OperatorCapability capability) {
                switch (capability) {
                    case BINOMINAL_ATTRIBUTES:
                    case POLYNOMINAL_ATTRIBUTES:
                    case NUMERICAL_ATTRIBUTES:
                    //case POLYNOMINAL_LABEL:
                    //case BINOMINAL_LABEL:
                    case MISSING_VALUES:
                        return true;
                    default:
                        return false;
                }
            }
        }, initialPrototypesSourcePort)));
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new ExampleSetPrecondition(exampleSetInputPort)));
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new DistanceMeasurePrecondition(initialPrototypesSourcePort, this)));
        getTransformer().addRule(new GenerateNewMDRule(modelOutputPort, new MetaData(ClusterModel.class)));                                                
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, exampleInnerSourcePort, SetRelation.EQUAL));        
        getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
        //initialPrototypesSourcePort. 
        addPrototypeTransformationRule();
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
        exampleInnerSourcePort.deliver(trainingSet);
        this.getSubprocess(0).execute();
        // checking and creating ids if necessary        
        ExampleSet codebooksInitialization = initialPrototypesSourcePort.getDataOrNull(ExampleSet.class);
        ExampleSet codebooks;
        if (codebooksInitialization == null) {
            int numberOfNeurons = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
            if (numberOfNeurons == 0) {
                numberOfNeurons = trainingSet.size() / 10;
            }
            RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
            AbstractInstanceSelectorModel isModel = new RandomInstanceSelectionModel(numberOfNeurons, false, randomGenerator);
            codebooks = PRulesUtil.duplicateExampleSet(isModel.run(trainingSet));
        } else {            
            codebooks = PRulesUtil.duplicateExampleSet(codebooksInitialization);            
        }
        //Here we generate cluster attribute and label codebooks
        clusterNames = IS_ClusterModelTools.prepareClusterNamesMap(codebooks.size());
        Attribute codebookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
        NominalMapping codebookLabelsNames = new PolynominalMapping(clusterNames);
        codebookLabels.setMapping(codebookLabelsNames);        
        codebooks.getExampleTable().addAttribute(codebookLabels);  
        if (getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL))
            codebooks.getAttributes().setLabel(codebookLabels);
        else {
            codebooks.getAttributes().setCluster(codebookLabels);
        }
        Iterator<Integer> clusterLabelIterator = clusterNames.keySet().iterator();        
        for (Example codebook : codebooks){
            int value = clusterLabelIterator.next();            
            codebook.setValue(codebookLabels,value);
        }            
        IS_PrototypeClusterModel model = optimize(trainingSet, codebooks);
        modelOutputPort.deliver(model);
        Tools.checkAndCreateIds(trainingSet);
        model.apply(trainingSet, true);
        //model.apply(codebooks, false);
        return codebooks;                                        
    }

    /**
     * Abstract method which needs to be implemented by any prototype based
     * optimization operator which are based on {@link AbstractVQModel}
     * clustering algorithms
     *
     * @param trainingSet
     * @param codebooks
     * @return
     * @throws OperatorException
     */
    public abstract IS_PrototypeClusterModel optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException;

    /**
     * Configuration options
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type;
        type = new ParameterTypeInt(PARAMETER_NUMBER_OF_NEURONS, "Number of neurons", 0, Integer.MAX_VALUE, 10);
        type.registerDependencyCondition(new PortConnectedCondition(this, new PortProvider() {
            @Override
            public Port getPort() {
                return initialPrototypesSourcePort;
            }
        }, true, false));
        type.setExpert(false);
        types.add(type);
        
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
        Collection<ParameterType> parameters = RandomGenerator.getRandomGeneratorParameters(this);
        for (ParameterType param : parameters) {
            param.registerDependencyCondition(new PortConnectedCondition(this, new PortProvider() {
                @Override
                public Port getPort() {
                    return initialPrototypesSourcePort;
                }
            }, true, false));
        }
        types.addAll(parameters);
        return types;
    }
}
