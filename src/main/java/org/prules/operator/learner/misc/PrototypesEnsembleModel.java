/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import lombok.Getter;
import lombok.Setter;
import org.prules.operator.learner.misc.NearestPrototypesOperator.PairedTuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Marcin
 */
@Getter
@Setter
public class PrototypesEnsembleModel extends ResultObjectAdapter {


    double[][] prototypes;
    double[] labels;
    List<String> attributes;
    Map<Long, PairedTuple> selectedPairs;
    private DistanceMeasure measure;

    PrototypesEnsembleModel(double[][] prototypes, double[] labels, List<String> attributes, DistanceMeasure measure, Map<Long, PairedTuple> selectedPairs) {
        this.prototypes = prototypes;
        this.labels = labels;
        this.attributes = attributes;
        this.selectedPairs = selectedPairs;
        this.measure = measure;
    }


    @Override
    public String getName() {
        return super.getName(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toResultString() {
        StringBuilder sb = new StringBuilder();
        selectedPairs.entrySet().stream().forEachOrdered(entry -> {
            PairedTuple pair = entry.getValue();
            sb.append("Pair:").append(pair.paired)
                    .append(" Proto 1:").append(pair.protoId1)
                    .append(" Proto 2:").append(pair.protoId2).append("\n");
        });
        sb.append("=====================================\n");
        sb.append("=========== Prototypes ==============\n");
        sb.append("=====================================\n");
        int i = 0;
        attributes.stream().forEach(str -> sb.append(str).append(" | "));
        sb.append("Label \n");
        IntStream.range(0, prototypes.length).forEachOrdered(idx -> {
            double[] row = prototypes[idx];
            sb.append("id").append(idx).append(" | ");
            Arrays.stream(row).forEach(element -> {
                sb.append(element).append(" | ");
            });
            sb.append(labels[idx]);
            sb.append("\n");
        });
        return sb.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    Map<Long, PairedTuple> getSelectedPairs() {
        return selectedPairs;
    }

    public double[][] getPrototypes() {
        return prototypes;
    }

    public double[] getLabels() {
        return labels;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public DistanceMeasure getMeasure() {
        return measure;
    }
}
