/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.classifiers.neuralnet;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.ispr.operator.AbstractPrototypeBasedOperatorChain;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.RandomInstanceSelectionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.IsConnectedPrecondition;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.conditions.PortConnectedCondition;
import com.rapidminer.tools.RandomGenerator;
import java.util.Collection;
import java.util.List;

/**
 * An abstract structure supporting implementation of prototype based clustering
 * methods
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationChain extends AbstractPrototypeBasedOperatorChain {

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
    /**
     *
     */
    //protected final OutputPort initialPrototyoesOutputPort = getOutputPorts().createPassThroughPort("Initial Prototpes");    

    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");

    protected final PredictionType predictionType;

    /**
     * Constructor
     *
     * @param description
     * @param predictionType
     */
    public AbstractPrototypeOptimizationChain(OperatorDescription description, PredictionType predictionType) {
        super(description, "Initialize_codebooks");//
        this.predictionType = predictionType;
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
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
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new CapabilityPrecondition(new CapabilityProvider() {
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
        }, initialPrototypesSourcePort)));
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new ExampleSetPrecondition(exampleSetInputPort)));
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new DistanceMeasurePrecondition(initialPrototypesSourcePort, this)));
        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_KNNClassificationModel.class));        
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
        ExampleSet codebooksInitialization = initialPrototypesSourcePort.getDataOrNull(ExampleSet.class);
        ExampleSet codebooks;
        if (codebooksInitialization == null) {
            int numberOfNeurons = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
            if (numberOfNeurons == 0) {
                numberOfNeurons = trainingSet.size() / 10;
            }
            RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
            AbstractInstanceSelectorModel isModel = new RandomInstanceSelectionModel(numberOfNeurons, true, randomGenerator);
            codebooks = PRulesUtil.duplicateExampleSet(isModel.run(trainingSet));
        } else {
            codebooks = PRulesUtil.duplicateExampleSet(codebooksInitialization);
        }
        AbstractModel model = optimize(trainingSet, codebooks);
        modelOutputPort.deliver(model);
        return codebooks;
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
    public abstract AbstractModel optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException;

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        //ParameterType lvqTypeParameter =  new 
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
    
    /**
     * Returns the handle to the Model output port
     *
     * @return output port
     */
    public OutputPort getModelOutputPort(){
        return modelOutputPort;
    }
}
