package com.rapidminer.ispr.operator.learner;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This class is used as a base for any instance selection and optimization
 * operator which are based on any submodels. For example it is used in LVQ to
 * represent the prototype initialization. It delivers the prototype output port
 * and defines the basic metadate requirements.
 *
 * @author Marcin
 */
public abstract class AbstractPRulesOperatorChain extends OperatorChain implements CapabilityProvider {

    /**
     * Input port which delivers training ExampleSet
     */
    protected final InputPort exampleSetInputPort = getInputPorts().createPort("ExampleSet");
    /**
     * Output port which delivers ExampleSet with constructed prototypes
     */
    protected final OutputPort prototypesOutputPort = getOutputPorts().createPort("Prototypes");
    /**
     * Output port which delivers the training ExampleSet
     */
    protected final OutputPort exampleSetOutputPort = getOutputPorts().createPassThroughPort("ExampleSet");
    //getOutputPorts().createPort("labelled ExampleSet");		

    /**
     * Constructor, called by the RapidMiner core
     *
     * @param description
     * @param subprocessNames
     */
    public AbstractPRulesOperatorChain(OperatorDescription description, String... subprocessNames) {
        super(description, subprocessNames);//
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(this, exampleSetInputPort));
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);
        //getTransformer().addPassThroughRule(exampleSetInputPort,originalExampleSetOutputPort);
        getTransformer().addRule(new PassThroughRule(exampleSetInputPort, prototypesOutputPort, false) {

            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData) {
                    try {
                        return AbstractPRulesOperatorChain.this.modifyMetaData((ExampleSetMetaData) metaData);
                    } catch (UndefinedParameterError ex) {
                        return metaData;
                    }
                } else {
                    return metaData;
                }
            }
        });
    }

    @Override
    public void doWork() throws OperatorException {
        ExampleSet trainingSet = exampleSetInputPort.getData(ExampleSet.class);

        ExampleSet outputSet = processExamples(trainingSet);

        exampleSetOutputPort.deliver(trainingSet);
        prototypesOutputPort.deliver(outputSet);
    }

    /**
     * Method which implements the instance selection method
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    public abstract ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException;

    /**
     * subclasses must implement this method for exact size meta data.
     *
     * @param emd
     * @return
     * @throws UndefinedParameterError
     */
    protected abstract MDInteger getSampledSize(ExampleSetMetaData emd)
            throws UndefinedParameterError;

    /**
     * Method to represent preconditions of ExampleSet
     *
     * @param metaData
     * @return
     * @throws UndefinedParameterError
     */
    protected MetaData modifyMetaData(ExampleSetMetaData metaData)
            throws UndefinedParameterError {
        try {
            metaData.setNumberOfExamples(getSampledSize(metaData));
        } catch (UndefinedParameterError e) {
            metaData.setNumberOfExamples(new MDInteger(-1));
        }
        return metaData;
    }

    /**
     * Returns the handle to the ExampleSet input port
     *
     * @return - input port
     */
    public InputPort getExampleSetInputPort() {
        return exampleSetInputPort;
    }

    /**
     * Returns the handle to the ExampleSet output port
     *
     * @return output port
     */
    public OutputPort getExampleSetOutputPort() {
        return exampleSetOutputPort;
    }

}
