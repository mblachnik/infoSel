package org.prules.operator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.*;
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * This class is used as a base for any instance selection and optimization
 * operator. It delivers the prototype output port and defines the basic
 * metadate requirements.
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeBasedOperator extends AbstractPRulesBasicOperator {

    /**
     * Output port which is used to return selected prototypes
     */
    protected final OutputPort prototypesOutputPort = getOutputPorts().createPort("prototypes");
    /**
     * Output port which returns an initial ExampleSet
     */
    private double numberOfInstancesBeforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;

    /**
     * Constructor of the AbstractPRulesOperator class.
     *
     * @param description
     */
    public AbstractPrototypeBasedOperator(OperatorDescription description) {
        super(description);//
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        getTransformer().addRule(new PassThroughRule(exampleSetInputPort, prototypesOutputPort, true) {

            @Override
            public MetaData modifyMetaData(MetaData metaData) {
                if (metaData instanceof ExampleSetMetaData) {
                    try {
                        ExampleSetMetaData exampleSetMetaData = (ExampleSetMetaData) metaData;
                        return AbstractPrototypeBasedOperator.this.modifyPrototypeOutputMetaData(exampleSetMetaData);
                    } catch (UndefinedParameterError ex) {
                        return metaData;
                    }
                } else {
                    return metaData;
                }
            }
        });
        addValue(new ValueDouble("Instances_before_selection", "Number Of Examples in the training set") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesBeforeSelection;
            }
        });
        addValue(new ValueDouble("Instances_after_selection", "Number Of Examples after selection") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesAfterSelection;
            }
        });
        addValue(new ValueDouble("Compression", "Compressing = #Instances_after_selection/#Instances_beafore_selection") {

            @Override
            public double getDoubleValue() {
                return compression;
            }
        });
    }

    /**
     * It overloads the executeOperator, and executes the processExamples
     * method.
     *
     * @param trainingSet - training set
     * @throws OperatorException
     */
    @Override
    public void executeOperator(ExampleSet trainingSet) throws OperatorException {
        numberOfInstancesBeforeSelection = trainingSet.size();
        ExampleSet outputSet = processExamples(trainingSet);
        prototypesOutputPort.deliver(outputSet);
        exampleSetOutputPort.deliver(trainingSet);
        numberOfInstancesAfterSelection = outputSet.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeforeSelection;
    }

    /**
     * Method which implements the prototype selection process.
     *
     * @param trainingSet - training set
     * @return
     * @throws OperatorException
     */
    public abstract ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException;

    /**
     * It returns number of proptotypes in the ExampleSetMetaData returned by the prototypeOutput
     *
     * @return
     * @throws UndefinedParameterError
     */
    public abstract MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError;

    /**
     * Used to define the metadata properties of prototypeOutput
     *
     * @param prototypeOutputMetaData
     * @return
     * @throws UndefinedParameterError
     */
    protected ExampleSetMetaData modifyPrototypeOutputMetaData(ExampleSetMetaData prototypeOutputMetaData)
            throws UndefinedParameterError {
        try {
            prototypeOutputMetaData.setNumberOfExamples(getNumberOfPrototypesMetaData());
        } catch (UndefinedParameterError e) {
            prototypeOutputMetaData.setNumberOfExamples(new MDInteger());
        }
        return prototypeOutputMetaData;
    }
}
