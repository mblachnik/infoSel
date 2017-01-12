/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.classifiers.neuralnet;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.operator.ports.metadata.IsConnectedPrecondition;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.AbstractPrototypeBasedOperator;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import static com.rapidminer.ispr.operator.learner.classifiers.neuralnet.LVQOperator.PARAMETER_NUMBER_OF_NEURONS;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.RandomInstanceSelectionModel;
import com.rapidminer.ispr.operator.learner.tools.PRulesUtil;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.CompatibilityLevel;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.Precondition;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.PortConnectedCondition;
import com.rapidminer.tools.RandomGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationOperator extends AbstractPrototypeBasedOperator {

    /**
     * Number of neurons (prototypes)
     */
    public static final String PARAMETER_NUMBER_OF_NEURONS = "Number of neurons";

    private static final long serialVersionUID = 21;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");
    protected final InputPort initialPrototypesSourcePort = getInputPorts().createPort("proto");

    double costFunctionValue = Double.NaN;
    PredictionType predictionType;

    /**
     *
     * @param description
     * @param predictionType
     */
    public AbstractPrototypeOptimizationOperator(OperatorDescription description, PredictionType predictionType) {
        super(description);
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

        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_KNNClassificationModel.class));
                     
        
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort,new CapabilityPrecondition(new CapabilityProvider() {
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
                }, initialPrototypesSourcePort) ));
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new ExampleSetPrecondition(exampleSetInputPort)));
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort,new DistanceMeasurePrecondition(initialPrototypesSourcePort, this)));        
        
        addValue(new ValueDouble("CostFunctionValue", "Cost Function Value") {
            @Override
            public double getDoubleValue() {
                return costFunctionValue;
            }
        });

        //initialPrototypesSourcePort.
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, prototypesOutputPort, SetRelation.EQUAL) {
            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                MetaData md = super.modifyMetaData(metaData);
                if (md instanceof ExampleSetMetaData) {
                    if (!initialPrototypesSourcePort.isConnected()) {
                        MDInteger num = new MDInteger();
                        try {
                            num = new MDInteger(getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS));
                        } catch (UndefinedParameterError ex) {
                        }
                        ((ExampleSetMetaData) md).setNumberOfExamples(num);
                    }
                }
                return md;
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
     * Abstract method required to be implemented by the successor of
     * AbstractPrototypeOptimizationOperator. It should implement the main
     * prototype optimization algorithm
     *
     * @param trainingSet - dataset to be clustered
     * @return - pair of elements. The first one is Dataset and the second one
     * is clustering model - namely kNN
     * @throws OperatorException
     */
    public abstract PredictionModel optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException;

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
        types.add(type);
        return types;
    }
}
