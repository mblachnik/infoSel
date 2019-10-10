/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;

/**
 * @author Marcin
 */
@Deprecated
public class GetIOModelID extends Operator {

    /**
     * Input port which delivers IOmodel
     */
    private final InputPort inputPort = getInputPorts().createPort("In");
    /**
     * Output port which returns what it got as input
     */
    private final OutputPort outputPort = getOutputPorts().createPassThroughPort("out");
    private long id = -1;

    public GetIOModelID(OperatorDescription description) {
        super(description);

        addValue(new ValueDouble("InputModelID", "Unique identifier of the input IOObject.", false) {
            @Override
            public double getDoubleValue() {
                return id;
            }
        });

        getTransformer().addPassThroughRule(inputPort, outputPort);
    }

    @Override
    public void doWork() {
        IOObject in = inputPort.getAnyDataOrNull();
        //id = in.getObjectID();
        //No longer supported
        outputPort.deliver(in);
    }
}
