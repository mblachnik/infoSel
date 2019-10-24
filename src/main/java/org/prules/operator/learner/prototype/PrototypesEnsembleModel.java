/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.prototype;

import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * @author Marcin, Paweł
 */
@Getter
@Setter
public class PrototypesEnsembleModel extends ResultObjectAdapter {
    //<editor-fold desc="Private fields" defaultState="collapsed" >
    private double[][] prototypes;
    private double[] labels;
    private List<String> attributes;
    private Map<Long, PrototypeTuple> selectedPairs;
    private DistanceMeasure measure;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >
    PrototypesEnsembleModel(double[][] prototypes, double[] labels, List<String> attributes, DistanceMeasure measure, Map<Long, PrototypeTuple> selectedPairs) {
        this.prototypes = prototypes;
        this.labels = labels;
        this.attributes = attributes;
        this.selectedPairs = selectedPairs;
        this.measure = measure;
    }
    //</editor-fold>

    //<editor-fold desc="Model Methods" defaultState="collapsed" >

    /**
     * Getter for object name
     *
     * @return String of object name
     */
    @Override
    public String getName() {
        return super.getName(); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Method to create text with data to create model
     *
     * @return String text showing data of model
     */
    @Override
    public String toResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("===========  PairTuples =============\n");
        sb.append("=====================================\n");
        selectedPairs.entrySet().stream().forEachOrdered(entry -> {
            PrototypeTuple tuple = entry.getValue();
            sb.append(tuple.toString()).append("\n");
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
    //</editor-fold>

    //<editor-fold desc="Getters" defaultState="collapsed" >

    /**
     * Getter for selected pairs
     *
     * @return selectedPairs
     */
    Map<Long, PrototypeTuple> getSelectedPairs() {
        return selectedPairs;
    }

    /**
     * Getter for array mapping prototypes by array
     *
     * @return prototypes
     */
    public double[][] getPrototypes() {
        return prototypes;
    }

    /**
     * Method to return labels ids
     *
     * @return labels
     */
    public double[] getLabels() {
        return labels;
    }

    /**
     * Getter for list of attributes names
     *
     * @return attributes
     */
    public List<String> getAttributes() {
        return attributes;
    }

    /**
     * Getter for measure
     *
     * @return measure
     */
    public DistanceMeasure getMeasure() {
        return measure;
    }
    //</editor-fold>
}
