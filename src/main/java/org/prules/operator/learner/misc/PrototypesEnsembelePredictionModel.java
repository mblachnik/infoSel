/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import org.prules.operator.learner.misc.NearestPrototypesOperator.PiredTriple;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

/**
 * A class implementing Ensembles based on nearest prototypes local competence models.
 * For given testing vector it searches for a pair of nearest prototypes and applies corresponding prediction model
 * @author Marcin
 */
public class PrototypesEnsembelePredictionModel extends PredictionModel {

    /**
     * PrototypesEnsembeleModel which contains informations such as: prototypes 
     * position, labels, a map which allows to decode pair into the prototypes
     */
    PrototypesEnsembeleModel model;
    /**
     * It maps given pair into prediction model
     */
    Map<Long, PredictionModel> predictionModelsMap;

    public PrototypesEnsembelePredictionModel(PrototypesEnsembeleModel model, Map<Long, PredictionModel> predictionModelsMap, ExampleSet trainingExampleSet, ExampleSetUtilities.SetsCompareOption sizeCompareOperator, ExampleSetUtilities.TypesCompareOption typeCompareOperator) {
        super(trainingExampleSet, sizeCompareOperator, typeCompareOperator);
        this.model = model;
        this.predictionModelsMap = predictionModelsMap;
    }

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
            Long bestPair = new Long(-1);
            for (Entry<Long, PiredTriple> entry : model.getSelectedPairs().entrySet()) {
                PiredTriple pair = entry.getValue();
                double sum = distances[pair.protoId1]
                        + distances[pair.protoId2];
                if (sum < minSum) {
                    minSum = sum;
                    bestPair = entry.getKey();
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
            SelectedExampleSet subset = new SelectedExampleSet(exampleSet,entry.getValue());
            PredictionModel predictionModel = predictionModelsMap.get(entry.getKey());
            predictionModel.performPrediction(subset, predictedLabel);
        }
        return exampleSet;
    }

    @Override
    public String toResultString() {
        StringBuilder sb = new StringBuilder();
        model.selectedPairs.entrySet().stream().forEachOrdered(entry -> { 
            PiredTriple pair = entry.getValue();
                sb.append("Pair:").append(pair.pired)
                        .append(" Proto 1:").append(pair.protoId1)
                        .append(" Proto 2:").append(pair.protoId2).append("\n");
        });
        sb.append("=====================================\n");
        sb.append("=========== Prototypes ==============\n");
        sb.append("=====================================\n");
        int i = 0;
        model.attributes.stream().forEach(str -> sb.append(str).append(" | "));
        sb.append("Label \n");
        IntStream.range(0, model.prototypes.length).forEachOrdered( idx -> {
            double[] row = model.prototypes[idx];        
            sb.append("id").append(idx).append(" | ");
            Arrays.stream(row).forEach( element -> {
                sb.append(element).append(" | ");
            });
            sb.append(model.labels[idx]);
            sb.append("\n");
        });
        return sb.toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
