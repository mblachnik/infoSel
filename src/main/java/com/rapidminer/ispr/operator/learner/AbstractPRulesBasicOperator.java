package com.rapidminer.ispr.operator.learner;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;

/**
 * A base class for all PRules operators. 
 * @author Marcin
 */
public abstract class AbstractPRulesBasicOperator extends Operator implements CapabilityProvider {

    /**
     * Input port which delivers training ExampleSet
     */
    protected final InputPort exampleSetInputPort = getInputPorts().createPort("ExampleSet");    
    /**
     * Output port which returns an initial ExampleSet
     */
    protected final OutputPort exampleSetOutputPort = getOutputPorts().createPassThroughPort("ExampleSet");    

    /**
     * Creates AbstractPRulesBasicOperator class
     * @param description
     */
    public AbstractPRulesBasicOperator(OperatorDescription description) {
        super(description);//
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(this, exampleSetInputPort));
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);
    }

    @Override
    public void doWork() throws OperatorException {
        ExampleSet trainingSet = exampleSetInputPort.getData(ExampleSet.class);
                
        executeOperator(trainingSet);
        
        exampleSetOutputPort.deliver(trainingSet);        
    }
    
    /**
     * A method which implements the learning process.
     * @param trainingSet - training set
     * @throws OperatorException - in case of an exception during learning
     */
    public abstract void executeOperator(ExampleSet trainingSet) throws OperatorException;

    /**
     * Give access to the input port
     * @return
     */
    public InputPort getExampleSetInputPort() {
        return exampleSetInputPort;
    }

    /**
     * Give access to the output port
     * @return
     */
    public OutputPort getExampleSetOutputPort() {
        return exampleSetOutputPort;
    }
}
