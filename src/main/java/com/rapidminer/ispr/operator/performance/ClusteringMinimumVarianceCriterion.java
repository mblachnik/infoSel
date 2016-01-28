/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.performance;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.DoubleObjectContainer;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.performance.AbstractExampleSetEvaluator;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ClusteringMinimumVarianceCriterion extends AbstractExampleSetEvaluator {

    public static final String PARAMETER_OPTIMIZATION_DIRECTION = "optimization_direction";

    private InputPort protoSetInput = getInputPorts().createPort("prototype set", ExampleSet.class);
    private double interClusterDistance = Double.NaN;
    protected DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    public ClusteringMinimumVarianceCriterion(OperatorDescription description) {
        super(description);
        addValue(new ValueDouble("intra_cluster_distance", "Average size between between cluster prototype and members of that cluster (k-means criterion)") {
            @Override
            public double getDoubleValue() {
                return interClusterDistance;
            }
        });
    }

    @Override
    public PerformanceVector evaluate(ExampleSet exampleSet) throws OperatorException {
        PerformanceVector performanceCriteria = new PerformanceVector();
        ExampleSet prototypes = protoSetInput.getDataOrNull(ExampleSet.class);
        if (prototypes!=null)   count(prototypes,exampleSet);        
        EstimatedPerformance interClusterDistancePerformance = new EstimatedPerformance("intar_cluster_distance", interClusterDistance, 1, true);
        performanceCriteria.addCriterion(interClusterDistancePerformance);
        return performanceCriteria;
    }
    
    private void count(ExampleSet prototypes, ExampleSet exampleSet) throws OperatorException{
        DistanceMeasure distance = measureHelper.getInitializedMeasure(prototypes);
        ISPRGeometricDataCollection<Number> knn = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,prototypes, distance);
        //MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<Number>(prototypes, knn, 1, VotingType.MAJORITY, false);
        Attributes attributes = prototypes.getAttributes();
        int n = attributes.size();
        double[] values = new double[n];
        interClusterDistance = 0;
        for (Example example : exampleSet){
            int i = 0;
            for(Attribute attribute : attributes){
                values[i] = example.getValue(attribute);
                i++;
            }
            Collection<DoubleObjectContainer<Number>> collDist = knn.getNearestValueDistances(1, values);
            double dist = collDist.iterator().next().getFirst();
            interClusterDistance += dist*dist;
        }        
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
