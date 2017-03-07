/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.decisionfunctions;

import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.ExampleSet;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.operator.learner.weighting.Ontology;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import java.util.ArrayList;
import java.util.List;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Instance;

/**
 * ISLocalThresholdLinearDecisionFunction is an implementation of
 * IISThresholdDecisionFunction. It represents decision function which
 * calculates the difference between real (R) and predicted (P) value of given
 * instance (R-P) then checks if the error is greater then the standard
 * deviation of k nearest output values multiply be the threshold. If so returns
 * 1
 *
 * @author Marcin
 */
public class ISNNEThresholdLinearDecisionFunction extends AbstractISDecisionFunction implements IISThresholdDecisionFunction {

    private double threshold = 0;
    private boolean blockInit = false;

    public ISNNEThresholdLinearDecisionFunction() {

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
        if (!blockInit) {
        }
        AttributeRole ar = exampleSet.getAttributes().findRoleByName(Ontology.ATTRIBUTE_NOISE);
        if (ar == null) {
            throw new NullPointerException("Missing " + Ontology.ATTRIBUTE_NOISE + " attribute.");
        }

    }

    @Override
    public void init(ISPRGeometricDataCollection<IInstanceLabels> samples) {
        //if (!blockInit)
        //    this.samples = samples;
    }

    @Override
    public double getValue(Instance instance) {
        double real = instance.getLabels().getLabel();
        double predicted = instance.getPrediction().getLabel();
        double var = instance.getLabels().getValueAsDouble(Ontology.ATTRIBUTE_NOISE);
        double err = Math.abs(real - predicted);
        double value = err * err / var > threshold ? 1 : 0;
        return value;
    }

    @Override
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String name() {
        return "NNE Threshold Linear Loss";
    }

    @Override
    public String description() {
        return "Y=(R-P)^2 > NNE(i) * Thres";
    }

    @Override
    public boolean supportedLabelTypes(OperatorCapability capabilities) {
        switch (capabilities) {
            case NUMERICAL_LABEL:
                return true;
        }
        return false;
    }

    @Override
    public List<List<String>> makeAdditionalChecks(ExampleSetMetaData emd) {
        List<List<String>> errors = super.makeAdditionalChecks(emd);
        AttributeMetaData amd = emd.getAttributeByRole(Ontology.ATTRIBUTE_NOISE);
        if (amd == null) {
            List<String> error = new ArrayList<String>();
            error.add(ProcessSetupError.Severity.ERROR.name());
            error.add("exampleset.missing_role");
            error.add(Ontology.ATTRIBUTE_NOISE + ", which is required by " + this.name() + " decision function.");
            errors.add(error);
        } else if (!amd.isNumerical()) {
            List<String> error = new ArrayList<String>();
            error.add(ProcessSetupError.Severity.ERROR.name());
            error.add("parameters.cannot_handle");
            error.add(OperatorCapability.POLYNOMINAL_LABEL.getDescription());
            error.add(ISDecisionFunctionHelper.PARAMETER_DECISION_FUNCTION);
            error.add(this.name());
            errors.add(error);
        }
        return errors;
    }

}
