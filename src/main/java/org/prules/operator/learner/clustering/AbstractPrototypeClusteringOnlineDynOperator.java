/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import org.prules.operator.learner.clustering.models.AbstractVQModel;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.clustering.ClusterModel;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.conditions.PortConnectedCondition;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.container.Pair;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.AbstractPrototypeBasedOperator;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

/**
 * This is an abstract class for prototype based clustering algorithms which use
 * online update (stochastic gradient) method which dynamicaly determine the
 * number of clusters. In fact it requires that the clustering algorithm will be
 * implemented using the
 * {@link  org.prules.operator.learner.clustering.models.AbstractVQModel}
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeClusteringOnlineDynOperator extends AbstractPrototypeBasedOperator {

    private static final long serialVersionUID = 21;
    public static final String PARAMETER_NUMBER_OF_NEURONS = "Number of neurons";
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");
    double costFunctionValue = Double.NaN;
    protected Map<Integer, String> clusterNames;
    DistanceMeasureHelper measureHelper;     

    /**
     * Constructor of prototype based clustering methods
     *
     * @param description
     */
    public AbstractPrototypeClusteringOnlineDynOperator(OperatorDescription description) {
        super(description);//        
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
        getTransformer().addRule(new GenerateNewMDRule(modelOutputPort, new MetaData(ClusterModel.class)));        
        /**
         * This method should be executed at the end of all other transformation
         * rules. It generates PrototypeOutput metadata. This metadata usually
         * depends on the subprocess metadata. In this case when this method
         * will be executed to early it wouldn't be able to generate metadata
         * values for prototypeOutput
         */

        getTransformer().addRule(new PassThroughRule(exampleSetInputPort, prototypesOutputPort, true) {
            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData) {
                    ExampleSetMetaData esData = (ExampleSetMetaData) metaData;
                    
                    esData.setNumberOfExamples(-1);
                    return esData;
                } else {
                    return metaData;
                }
            }
        });
        measureHelper = new DistanceMeasureHelper(this);
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
        ExampleSet codebooks  = optimize(trainingSet);                        
        clusterNames = IS_ClusterModelTools.prepareClusterNamesMap(codebooks.size());
        Attribute codebookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
        NominalMapping codebookLabelsNames = new PolynominalMapping(clusterNames);
        codebookLabels.setMapping(codebookLabelsNames);
        codebooks.getExampleTable().addAttribute(codebookLabels);
        if (getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL)) {
            codebooks.getAttributes().setLabel(codebookLabels);
        } else {
            codebooks.getAttributes().setCluster(codebookLabels);
        }
        Iterator<Integer> clusterLabelIterator = clusterNames.keySet().iterator();
        for (Example codebook : codebooks) {
            int value = clusterLabelIterator.next();
            codebook.setValue(codebookLabels, value);
        }       
      
        boolean addAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);
        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
        ISPRGeometricDataCollection<IInstanceLabels> knnModel;
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        if (addAsLabel) {
            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getLabel(), Const.LABEL, distance);
        } else {
            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getCluster(), Const.LABEL, distance);
        }
        IS_PrototypeClusterModel model = new IS_PrototypeClusterModel(trainingSet, knnModel, codebooks.size(), clusterNames, addAsLabel, addCluster);
                
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
     * @return
     * @throws OperatorException
     */
    public abstract ExampleSet optimize(ExampleSet trainingSet) throws OperatorException;

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
        types.addAll(parameters);
        
        types.addAll(DistanceMeasures.getParameterTypes(this));         
        return types;
    }
}
