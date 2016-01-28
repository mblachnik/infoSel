/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.misc;

import com.rapidminer.FileProcessLocation;
import com.rapidminer.ProcessLocation;
import com.rapidminer.RepositoryProcessLocation;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.DummyPortPairExtender;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.PortPairExtender;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeRepositoryLocation;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcin
 */
public class ProcessExportOperator extends Operator {

    private static final String PARAMETER_LOCATION_TYPE = "Store process in:";    
    private static final int LOCATION_FILE = 0;
    private static final int LOCATION_REPOSITORY = 1;
    private static final String PARAMETER_FILE = "File";
    private static final String PARAMETER_REPOSITORY = "Repository";
    private static final String PARAMETER_LOCATION_TYPES[] = {PARAMETER_FILE, PARAMETER_REPOSITORY};
    
    private PortPairExtender dummyPorts = new DummyPortPairExtender("through", getInputPorts(), getOutputPorts());

    /**
     * Creates AbstractPRulesBasicOperator class
     *
     * @param description
     */
    public ProcessExportOperator(OperatorDescription description) {
        super(description);//        
        dummyPorts.start();
	getTransformer().addRule(dummyPorts.makePassThroughRule());
    }

    
    @Override
    public void doWork() throws OperatorException {       
        com.rapidminer.Process process = this.getProcess();
        ProcessLocation location;
        if (getParameterAsInt(PARAMETER_LOCATION_TYPE) == LOCATION_FILE) {
            File file = getParameterAsFile(PARAMETER_FILE);
            location = new FileProcessLocation(file);
        } else {
            RepositoryLocation repository = getParameterAsRepositoryLocation(PARAMETER_REPOSITORY);
            location = new RepositoryProcessLocation(repository);
        }
        try {
            location.store(process, null);
        } catch (IOException ex) {
            Logger.getLogger(ProcessExportOperator.class.getName()).log(Level.SEVERE, null, ex);
        }
        dummyPorts.passDataThrough();
    }
    
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        //ParameterType lvqTypeParameter =  new 
        ParameterType type = new ParameterTypeCategory(PARAMETER_LOCATION_TYPE, "Type of the archivum where to store the process.", PARAMETER_LOCATION_TYPES, 0);
        type.setExpert(false);
        types.add(type);
                
        type = new ParameterTypeFile(PARAMETER_FILE, PARAMETER_FILE, "rmp", true);                        
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LOCATION_TYPE, PARAMETER_LOCATION_TYPES, false,LOCATION_FILE));
        types.add(type);

        type = new ParameterTypeRepositoryLocation(PARAMETER_REPOSITORY, PARAMETER_REPOSITORY, true);                
        type.setExpert(false);
        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_LOCATION_TYPE, PARAMETER_LOCATION_TYPES, false,LOCATION_REPOSITORY));
        types.add(type);
        return types;
    }
}