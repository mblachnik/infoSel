/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.neuralnet;

//import history.OldAbstractPRulesOperator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.AbstractModel;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.operator.ports.metadata.*;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.conditions.PortConnectedCondition;
import com.rapidminer.tools.RandomGenerator;
import org.prules.operator.AbstractPrototypeBasedOperatorChain;
import org.prules.operator.learner.classifiers.IS_KNNClassificationModel;
import org.prules.operator.learner.classifiers.PredictionType;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.RandomInstanceSelectionModel;
import org.prules.operator.learner.tools.PRulesUtil;

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
    private final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("example Set");
    /**
     *
     */
    final InputPort initialPrototypesSourcePort = getSubprocess(0).getInnerSinks().createPort("codeBooks");
    /**
     *
     */
    //protected final OutputPort initialPrototypesOutputPort = getOutputPorts().createPassThroughPort("Initial Prototypes");

    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");

    private final PredictionType predictionType;

    /**
     * Constructor
     *
     * @param description
     * @param predictionType
     */
    AbstractPrototypeOptimizationChain(OperatorDescription description, PredictionType predictionType) {
        super(description, "Initialize_codeBooks");//
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
        initialPrototypesSourcePort.addPrecondition(new IsConnectedPrecondition(initialPrototypesSourcePort, new CapabilityPrecondition(capability -> {
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
        ExampleSet codeBooksInitialization = initialPrototypesSourcePort.getDataOrNull(ExampleSet.class);
        ExampleSet codeBooks;
        if (codeBooksInitialization == null) {
            int numberOfNeurons = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
            if (numberOfNeurons == 0) {
                numberOfNeurons = trainingSet.size() / 10;
            }
            RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
            AbstractInstanceSelectorModel isModel = new RandomInstanceSelectionModel(numberOfNeurons, true, randomGenerator);
            codeBooks = PRulesUtil.duplicateExampleSet(isModel.run(trainingSet));
        } else {
            codeBooks = PRulesUtil.duplicateExampleSet(codeBooksInitialization);
        }
        AbstractModel model = optimize(trainingSet, codeBooks);
        modelOutputPort.deliver(model);
        return codeBooks;
    }

    /**
     * Abstract method which needs to be implemented by any prototype based
     * optimization algorithm
     *
     * @param trainingSet
     * @param codeBooks
     * @return
     * @throws OperatorException
     */
    public abstract AbstractModel optimize(ExampleSet trainingSet, ExampleSet codeBooks) throws OperatorException;

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
    public OutputPort getModelOutputPort() {
        return modelOutputPort;
    }
}
