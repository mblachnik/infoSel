/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.selection.AbstractInstanceSelectorOperator;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class of extra static methods which supports usage of instance selection decision functions
 * @author Marcin
 */
public class ISDecisionFunctionHelper {

    static final IISDecisionFunction[] decisionFunctionList = {new ISClassDecisionFunction(), new ISThresholdLinearDecisionFunction(), new ISThresholdRelativeLinearDecisionFunction(), new ISNNEThresholdLinearDecisionFunction(), new ISLocalThresholdLinearDecisionFunction(), new ISLocalThresholdRelativeLinearDecisionFunction()};
    static final String[] decisionFunctionNames = new String[decisionFunctionList.length];
    public static final String PARAMETER_THRESHOLD = "Threshold";
    public static final String PARAMETER_NOISE_ESTIMATION = "Noise estimation";
    public static final String PARAMETER_DECISION_FUNCTION = "Decision function"; 

    static {
        for (int i = 0; i < decisionFunctionList.length; i++) {
            decisionFunctionNames[i] = decisionFunctionList[i].name();
        }
    }

    /**
     * Returns array of known instance selection decision functions
     * @return 
     */
    public static IISDecisionFunction[] getISDecisionFunctions() {
        return decisionFunctionList;
    }

    /**
     * returns array of instance selection decision function names
     * @return 
     */
    public static String[] getNames() {
        return decisionFunctionNames;
    }

    /**
     * Returns a list of configuration parameters for the instance selection decision functions
     * @param operator
     * @return 
     */
    public static List<ParameterType> getParameters(Operator operator) {
        List<ParameterType> types = new ArrayList<ParameterType>();
        ParameterType type;
        type = new ParameterTypeCategory(PARAMETER_DECISION_FUNCTION, "Loss function", ISDecisionFunctionHelper.getNames(), 0);        
        types.add(type);
        
        type = new ParameterTypeDouble(PARAMETER_THRESHOLD, "The loss function threshold acceptance value", 0, Double.MAX_VALUE, 0.1);
        type.setExpert(true);
        String[] s = ISDecisionFunctionHelper.getNames();
        int[] dfTab = new int[s.length];        
        int i = 0, j = 0;
        for (IISDecisionFunction df : getISDecisionFunctions()){
            if (df instanceof IISThresholdDecisionFunction){
                dfTab[i] = j;
                i++;
            }
            j++;
        }        
        type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_DECISION_FUNCTION, s, false, Arrays.copyOf(dfTab, i)));
        types.add(type);
        
        type = new ParameterTypeInt(PARAMETER_NOISE_ESTIMATION, "Number of nearest neighbors for noise estimation", 0, Integer.MAX_VALUE, 3);
        type.setExpert(true);                
        i = 0; 
        j = 0;
        for (IISDecisionFunction df : getISDecisionFunctions()){
            if (df instanceof IISLocalDecisionFunction){
                dfTab[i] = j;
                i++;
            }
            j++;
        }     
        type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_DECISION_FUNCTION, s, false, Arrays.copyOf(dfTab, i)));
        type.setExpert(true);
        types.add(type);
        return types;
    }

    /**
     * Returns configured instance selection decision function. Configuration of 
     * the decision function is obtained form the operator
     * @param operator
     * @param exampleSet
     * @return 
     */
    public static IISDecisionFunction getConfiguredISDecisionFunction(AbstractInstanceSelectorOperator operator, ExampleSet exampleSet) throws OperatorException {
        int lossId;
        IISDecisionFunction loss = null;
        try {
            lossId = operator.getParameterAsInt(PARAMETER_DECISION_FUNCTION);
            loss = ISDecisionFunctionHelper.getISDecisionFunctions()[lossId];
            loss = loss.getClass().newInstance();                        
            if (loss instanceof IISThresholdDecisionFunction ){            
                double threshold = operator.getParameterAsDouble(PARAMETER_THRESHOLD);                
                ((IISThresholdDecisionFunction)loss).setThreshold(threshold);
            }  
            if (loss instanceof IISLocalDecisionFunction ){            
                int k = operator.getParameterAsInt(PARAMETER_NOISE_ESTIMATION);                
                ((IISLocalDecisionFunction)loss).setK(k);
            }  
            if (!loss.isBlockInit() && exampleSet != null){
                DistanceMeasure distance = operator.getDistanceMeasureHelper().getInitializedMeasure(exampleSet);
                loss.init(exampleSet, distance);
            }
        } catch (UndefinedParameterError e) {
            if (operator != null){
                operator.getLog().logError("Error in loss function: UndefinedParameterError");
            }
            throw new RuntimeException("Error in loss function: UndefinedParameterError");
        } catch (InstantiationException e) {
            if (operator != null){
                operator.getLog().logError("Error in loss function: InstantiationException");
            }
            throw new RuntimeException("Error in loss function: InstantiationException");
        } catch (IllegalAccessException e) {
            if (operator != null){
                operator.getLog().logError("Error in loss function: IllegalAccessException");
            }
            throw new RuntimeException("Error in loss function: IllegalAccessException");
        }
        return loss;
    }
}
