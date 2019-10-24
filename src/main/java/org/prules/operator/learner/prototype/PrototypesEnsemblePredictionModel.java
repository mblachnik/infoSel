/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.prototype;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

/**
 * A class implementing Ensembles based on nearest prototypes local competence models.
 * For given testing vector it searches for a pair of nearest prototypes and applies corresponding prediction model
 *
 * @author Marcin, Paweł
 */
public class PrototypesEnsemblePredictionModel extends PredictionModel {
    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * PrototypesEnsembleModel which contains information's such as:
     * -> prototypes
     * -> position
     * -> labels
     * -> map decoding pair into the prototypes
     */
    private PrototypesEnsembleModel model;
    /**
     * It maps given pair into prediction model
     */
    private Map<Long, PredictionModel> predictionModelsMap;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >
    PrototypesEnsemblePredictionModel(PrototypesEnsembleModel model, Map<Long, PredictionModel> predictionModelsMap, ExampleSet trainingExampleSet, ExampleSetUtilities.SetsCompareOption sizeCompareOperator, ExampleSetUtilities.TypesCompareOption typeCompareOperator) {
        super(trainingExampleSet, sizeCompareOperator, typeCompareOperator);
        this.model = model;
        this.predictionModelsMap = predictionModelsMap;
    }
    //</editor-fold>

    //<editor-fold desc="Model Methods" defaultState="collapsed" >

    /**
     * Method to create model of trained experts
     *
     * @param exampleSet     set of training data
     * @param predictedLabel label for which perform inner model prediction
     * @return ExampleSet
     * @throws OperatorException
     */
    @Override
    public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabel) throws OperatorException {
        double[] exampleValues = new double[model.getAttributes().size()];
        Attributes attributes = exampleSet.getAttributes();
        double[] distances = new double[model.getPrototypes().length];
        Map<Long, IDataIndex> subsetMap = new HashMap<>();
        int n = exampleSet.size();
        int exampleIndex = 0;
        for (Example example : exampleSet) {
            int i = 0;
            for (String attribute : model.getAttributes()) {
                exampleValues[i++] = example.getValue(attributes.get(attribute));
            }
            i = 0;
            for (double[] prototype : model.getPrototypes()) {
                distances[i++] = model.getMeasure().calculateDistance(exampleValues, prototype);
            }
            double minSum = Double.MAX_VALUE;
            Long bestPair = (long) -1;
            for (Entry<Long, PrototypeTuple> entry : model.getSelectedPairs().entrySet()) {
                try {
                    PrototypeTuple pair = entry.getValue();
                    double sum = distances[pair.getPrototypeId1()]
                            + distances[pair.getPrototypeId2()];
                    if (sum < minSum) {
                        minSum = sum;
                        bestPair = entry.getKey();
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
//                    skip this pair
                }
            }
            IDataIndex index;
            if ((index = subsetMap.get(bestPair)) == null) {
                index = new DataIndex(n);
                index.setAllFalse();
                subsetMap.put(bestPair, index);
            }
            index.set(exampleIndex, true);
            exampleIndex++;
        }
        for (Entry<Long, IDataIndex> entry : subsetMap.entrySet()) {
            SelectedExampleSet subset = new SelectedExampleSet(exampleSet, entry.getValue());
            PredictionModel predictionModel = predictionModelsMap.get(entry.getKey());
            if (predictionModel != null) {
                predictionModel.performPrediction(subset, predictedLabel);
            }
        }
        return exampleSet;
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
        model.getSelectedPairs().entrySet().stream().forEachOrdered(entry -> {
            PrototypeTuple tuple = entry.getValue();
            sb.append(tuple.toString()).append("\n");
        });
        sb.append("=====================================\n");
        sb.append("=========== Prototypes ==============\n");
        sb.append("=====================================\n");
        int i = 0;
        model.getAttributes().stream().forEach(str -> sb.append(str).append(" | "));
        sb.append("Label \n");
        IntStream.range(0, model.getPrototypes().length).forEachOrdered(idx -> {
            double[] row = model.getPrototypes()[idx];
            sb.append("id").append(idx).append(" | ");
            Arrays.stream(row).forEach(element -> {
                sb.append(element).append(" | ");
            });
            sb.append(model.getLabels()[idx]);
            sb.append("\n");
        });
        return sb.toString(); //To change body of generated methods, choose Tools | Templates.
    }
    //</editor-fold>
}
