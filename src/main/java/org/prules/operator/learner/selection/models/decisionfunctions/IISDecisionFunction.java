/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.ExampleSet;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;
import java.util.List;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Instance;

/**
 * IISAcceptanceFunction is an interface for an DecisionFunction which is used 
 * by instance selection methods as a decision function to accept or reject particular instance.
 * This allows to generalize instance selection methods to be able to deal also with regression problems
 * @author Marcin
 */
public interface IISDecisionFunction extends Cloneable{
    void init(ExampleSet exampleSet, DistanceMeasure distance);
    void init(ISPRGeometricDataCollection<IInstanceLabels> samples);
    /**
     * Returns decision if an instance should be accepted or not. If 0 then it will be removed
     * @param instance
     * @return 
     */
    double getValue(Instance instance);
    /**
     * Returns the name of this decision function
     * @return 
     */
    String name();
    /**
     * Long string describing this decision function
     * @return 
     */
    String description();
    /**
     * Set state of the blockInit boolean state. If true then init(...) metheod is ignored
     * @param block 
     */
    void setBlockInit(boolean block);
    /**
     * check state of blockInit. If true then the method init is not executer or ignored
     * @return 
     */
    boolean isBlockInit();
    /**
     * Supported capabilities
     * @param capabilities
     * @return 
     */
    boolean supportedLabelTypes(OperatorCapability capabilities);
    
    /**
     * Function used to define metadata requirements for a loss function. It derives from the fact that some of the decision function require nominal label, some other numerical label, some may need some additional checks.
     * By overriding this method you can define your own meta data requirements.
     * Each first level list represent single error or warning, than in the second level string the first element defines type error/warning than the second parameter
     * is the i18nKey of the error, the remaining ones are appropriate parameters.
     * @param received meta data of the input example set
     * @return  list of lists of strings - where in the first order list each element represent single error or warning  entry, than in the second list each string represent 1) type of error/warning, 2) i18nKey, 3..) parameters 
     */
    List<List<String>> makeAdditionalChecks(ExampleSetMetaData received);
}
