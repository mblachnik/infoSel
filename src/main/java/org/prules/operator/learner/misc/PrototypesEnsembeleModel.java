/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.Setter;
import lombok.Getter;
import org.prules.operator.learner.misc.NearestPrototypesOperator.PiredTriple;

/**
 *
 * @author Marcin
 */
@Getter @Setter
public class PrototypesEnsembeleModel extends ResultObjectAdapter{

     
    double[][] prototypes;    
    double[] labels;    
    List<String> attributes;    
    Map<Long, PiredTriple >    selectedPairs;
    DistanceMeasure measure;

    public PrototypesEnsembeleModel(double[][] prototypes, double[] labels, List<String> attributes, DistanceMeasure measure, Map<Long, PiredTriple> selectedPairs) {
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
            PiredTriple pair = entry.getValue();
                sb.append("Pair:").append(pair.pired)
                        .append(" Proto 1:").append(pair.protoId1)
                        .append(" Proto 2:").append(pair.protoId2).append("\n");
        });
        sb.append("=====================================\n");
        sb.append("=========== Prototypes ==============\n");
        sb.append("=====================================\n");
        int i = 0;
        attributes.stream().forEach(str -> sb.append(str).append(" | "));
        sb.append("Label \n");
        IntStream.range(0, prototypes.length).forEachOrdered( idx -> {
            double[] row = prototypes[idx];        
            sb.append("id").append(idx).append(" | ");
            Arrays.stream(row).forEach( element -> {
                sb.append(element).append(" | ");
            });
            sb.append(labels[idx]);
            sb.append("\n");
        });
        return sb.toString(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
