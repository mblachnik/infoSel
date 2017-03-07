/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcin
 */
public abstract class AbstractISDecisionFunction implements IISDecisionFunction{
     /**
     * Function used to define metadata requirements for a loss function. It derives from the fact that some of the decision function require nominal label, some other numerical label, some may need some additional checks.
     * By overriding this method you can define your own meta data requirements.
     * Each first level list represent single error or warning, than in the second level string the first element defines type error/warning than the second parameter
     * is the i18nKey of the error, the remaining ones are appropriate parameters.
     * @param received meta data of the input example set
     * @return  list of lists of strings - where in the first order list each element represent single error or warning  entry, than in the second list each string represent 1) type of error/warning, 2) i18nKey, 3..) parameters 
     */
    @Override
    public List<List<String>> makeAdditionalChecks(ExampleSetMetaData emd){
        List<List<String>> errors = new ArrayList<>();
        AttributeMetaData label = emd.getLabelMetaData();
                                    if (label.isNominal() && !this.supportedLabelTypes(OperatorCapability.POLYNOMINAL_LABEL)) {
                                        List<String> error = new ArrayList<String>();
                                        error.add(ProcessSetupError.Severity.ERROR.name());
                                        error.add("parameters.cannot_handle");
                                        error.add(OperatorCapability.POLYNOMINAL_LABEL.getDescription());
                                        error.add(ISDecisionFunctionHelper.PARAMETER_DECISION_FUNCTION);
                                        error.add(this.name());                                        
                                        errors.add(error);
                                    }
                                    if (label.isNumerical() && !this.supportedLabelTypes(OperatorCapability.NUMERICAL_LABEL)) {
                                        List<String> error = new ArrayList<String>();
                                        error.add(ProcessSetupError.Severity.ERROR.name());
                                        error.add("parameters.cannot_handle");
                                        error.add(OperatorCapability.NUMERICAL_LABEL.getDescription());
                                        error.add(ISDecisionFunctionHelper.PARAMETER_DECISION_FUNCTION);
                                        error.add(this.name());                                        
                                        errors.add(error);                                        
                                    }
        return errors;
    }
}
