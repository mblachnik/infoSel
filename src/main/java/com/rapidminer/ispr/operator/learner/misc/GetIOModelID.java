/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.misc;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;

/**
 *
 * @author Marcin
 */
public class GetIOModelID extends Operator {

    /**
     * Input port which delivers IOmodel
     */
    protected final InputPort inputPort = getInputPorts().createPort("In");
    /**
     * Output port which returns what it got as input
     */
    protected final OutputPort outputPort = getOutputPorts().createPassThroughPort("Out");
    private long id = -1;
    
    public GetIOModelID(OperatorDescription description) {
        super(description);
        
        addValue(new ValueDouble("InputModelID", "Unique identfifier of the input IOObject.", false) {
            @Override
            public double getDoubleValue() {
                return id;
            }
        });
        
        getTransformer().addPassThroughRule(inputPort, outputPort);
    }
    
    @Override
    public void doWork() throws OperatorException {
        IOObject in = inputPort.getAnyDataOrNull();
        //id = in.getObjectID();
        outputPort.deliver(in);
    }
}
