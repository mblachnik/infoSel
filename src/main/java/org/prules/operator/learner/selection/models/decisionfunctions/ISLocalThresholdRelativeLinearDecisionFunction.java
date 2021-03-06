/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Const;
import org.prules.tools.math.BasicMath;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Collection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;

/**
 * ISLocalThresholdRelativeLinearDecisionFunction is an implementation of IISThresholdDecisionFunction. It represents
 * decision function which calculates the difference between real (R) and predicted (P) value of given instance, and 
 * calculates relative error (R-P)/R then checks if the error is greater then the standard deviation of k nearest
 * output values multiply be the threshold. If so returns 1 
 * @author Marcin
 */
public class ISLocalThresholdRelativeLinearDecisionFunction extends AbstractISDecisionFunction  implements IISThresholdDecisionFunction, IISLocalDecisionFunction {
    
    private double threshold = 0;
    private int k = 3;
    private ISPRGeometricDataCollection<IInstanceLabels> samples;
    private boolean blockInit = false;    


    public ISLocalThresholdRelativeLinearDecisionFunction() {        
    }
    
    @Override
    public void setBlockInit(boolean block) {        
        blockInit = block;
    }

    @Override
    public boolean isBlockInit() {        
        return blockInit;
    }
    
    @Override
    public void init(ExampleSet exampleSet, DistanceMeasure distance) {
        if (!blockInit){
            samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, distance);
        }
    }

    @Override
    public void init(ISPRGeometricDataCollection<IInstanceLabels> samples){        
        if (!blockInit){
            this.samples = samples;
        }
    }
    
       
    @Override
    public double getValue(Instance instance){      
        Collection<IInstanceLabels> nn = samples.getNearestValues(k, instance.getVector());
        double real = instance.getLabels().getLabel();
        double predicted = instance.getPrediction().getLabel();
        double std = BasicMath.std(nn, Const.LABEL);
        return Math.abs(real - predicted) / Math.abs(real) > std * threshold ? 1 : 0;        
    }      
    
    @Override
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    @Override
    public String name() {
        return "Local Threshold Relative Linear Loss";
    }

    @Override
    public String description() {
        return "Local Y=(R-P)/R > noise(k)*Thresh";
    }
    
    @Override
    public boolean supportedLabelTypes(OperatorCapability capabilities){
        switch (capabilities){
            case NUMERICAL_LABEL:
                return true;
        }
        return false;
    }

    @Override
    public void setK(int k) {
        this.k = k;
    }

    @Override
    public int getK() {
        return k;
    }
    
    
}
