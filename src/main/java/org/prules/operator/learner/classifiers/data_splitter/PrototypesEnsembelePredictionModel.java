/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
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
 * @author Marcin
 */
public class PrototypesEnsembelePredictionModel extends PredictionModel {

    /**
     * {@link NearestPrototypesSplitterV1} which contains informations such as: prototypes
     * position, labels, a map which allows to decode pair into the prototypes
     */
    NearestPrototypesSplitter model;
    /**
     * It maps given pair into prediction model
     */
    Map<Long, PredictionModel> predictionModelsMap;

    public PrototypesEnsembelePredictionModel(NearestPrototypesSplitter model, Map<Long, PredictionModel> predictionModelsMap, ExampleSet trainingExampleSet, ExampleSetUtilities.SetsCompareOption sizeCompareOperator, ExampleSetUtilities.TypesCompareOption typeCompareOperator) {
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
        DistanceMeasure distance = model.getDistance();
        for (Example example : exampleSet) {
            //Adjust attributes so that they are in same order
            int i = 0;
            for (String attribute : model.getAttributes()) {
                exampleValues[i++] = example.getValue(attributes.get(attribute));
            }
            //Calculate distance to all prototypes
            i = 0;
            for (double[] prototype : model.getPrototypes()) {
                distances[i++] = distance.calculateDistance(exampleValues, prototype);
            }
            //Iterate over regions and get the closest one, the one which is closest to the pair of prototypes, here we use sum of distances
            double minSum = Double.MAX_VALUE;
            long bestPair = -1;
            for (Entry<Long, PiredTriple> entry : model.getSelectedPairs().entrySet()) {
                PiredTriple pair = entry.getValue();
                double sum = distances[pair.protoId1]
                        + distances[pair.protoId2];
                if (sum < minSum) {
                    minSum = sum;
                    bestPair = entry.getKey();
                }
            }
            //Set appropriate ID of the sample, that is determine a region, and as each region has its DataIndex which will be used at the end to deliver samples of the same region
            IDataIndex index;
            if ((index = subsetMap.get(bestPair)) == null) { //subsetMap holds a DataIndex structure, one for each region, if that sample does not exist initialize it
                index = new DataIndex(n);
                index.setAllFalse();
                subsetMap.put(bestPair, index);
            }
            index.set(exampleIndex, true); //Set sample id as present for given region
            exampleIndex++;
        }
        for (Entry<Long, IDataIndex> entry : subsetMap.entrySet()) { // Iterate over region and get DataIndex structure and create specific SelectedExampleSets which will be used for cclassification
            SelectedExampleSet subset = new SelectedExampleSet(exampleSet, entry.getValue());
            PredictionModel predictionModel = predictionModelsMap.get(entry.getKey());
            predictionModel.performPrediction(subset, predictedLabel);
        }
        return exampleSet;
    }

    @Override
    public String toResultString() {
        StringBuilder sb = new StringBuilder();
        model.getSelectedPairs().entrySet().stream().forEachOrdered(entry -> {
            PiredTriple pair = entry.getValue();
            sb.append("Pair:").append(pair.pired)
                    .append(" Proto 1:").append(pair.protoId1)
                    .append(" Proto 2:").append(pair.protoId2).append("\n");
        });
//        long[] protoIds = new long[2];
//        model.selectedPairs.stream().forEachOrdered(pair -> {
//            BasicMath.depair(pair,protoIds);
//            sb.append("Pair:").append(pair)
//                    .append(" Proto 1:").append(protoIds[0])
//                    .append(" Proto 2:").append(protoIds[1]).append("\n");
//        });
        sb.append("=====================================\n");
        sb.append("=========== Prototypes ==============\n");
        sb.append("=====================================\n");
        model.getAttributes().stream().forEach(str -> sb.append(str).append(" | "));
        sb.append("Label \n");
        double[][] prototypes = model.getPrototypes();
        IntStream.range(0, prototypes.length).forEachOrdered(idx -> {
            double[] row = prototypes[idx];
            sb.append("id").append(idx).append(" | ");
            Arrays.stream(row).forEach(element ->
                sb.append(element).append(" | ")
            );
            sb.append(model.getPrototypeLabels()[idx]);
            sb.append("\n");
        });
        return sb.toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
