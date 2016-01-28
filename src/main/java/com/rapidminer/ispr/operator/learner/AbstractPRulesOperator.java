package com.rapidminer.ispr.operator.learner;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.PassThroughRule;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This class is used as a base for any instance selection and optimization operator.
 * It delivers the prototype output port and defines the basic metadate requirements.
 * @author Marcin
 */
public abstract class AbstractPRulesOperator extends AbstractPRulesBasicOperator {

    /**
     * Output port which is used to return selected prototypes
     */
    protected final OutputPort prototypesOutputPort = getOutputPorts().createPort("Prototypes");

    /**
     * Constructor of the AbstractPRulesOperator class.
     * @param description
     */
    public AbstractPRulesOperator(OperatorDescription description) {
        super(description);//
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        getTransformer().addRule(new PassThroughRule(exampleSetInputPort, prototypesOutputPort, true) {

            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData) {
                    try {
                        return AbstractPRulesOperator.this.modifyMetaData((ExampleSetMetaData) metaData);
                    } catch (UndefinedParameterError ex) {
                        return metaData;
                    }
                } else {
                    return metaData;
                }
            }
        });        
    }

    /**
     * It overloads the executeOperator, and executes the  processExamples method.
     * @param trainingSet - training set
     * @throws OperatorException
     */
    @Override
    public void executeOperator(ExampleSet trainingSet) throws OperatorException {
                
        ExampleSet outputSet = processExamples(trainingSet);                        
        prototypesOutputPort.deliver(outputSet);
    }
    
    /**
     * Method which implements the prototype selection process.
     * @param trainingSet - training set
     * @return
     * @throws OperatorException
     */
    public abstract ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException;

    /**
     * subclasses must implement this method for exact size meta data.
     * @param emd 
     * @return 
     * @throws UndefinedParameterError 
     */
    protected abstract MDInteger getSampledSize(ExampleSetMetaData emd)
            throws UndefinedParameterError;

    /**
     * Used to define the metadata properties
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
}
