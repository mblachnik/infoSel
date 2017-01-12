/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2010 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.ispr.operator.learner.classifiers;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.ispr.tools.math.container.knn.KNNTools;
import com.rapidminer.ispr.operator.learner.tools.TaskType;
import com.rapidminer.ispr.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.ispr.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import com.rapidminer.ispr.dataset.IValuesStoreLabels;
import com.rapidminer.ispr.tools.math.container.knn.KNNFactory;

/**
 * A k nearest neighbor implementation.
 *
 * @author Sebastian Land
 *
 */
public class IS_KNNLearner extends AbstractLearner {

    /**
     * The parameter name for &quot;The used number of nearest neighbors.&quot;
     */
    public static final String PARAMETER_K = "k";
    /**
     * The parameter name for &quot;Indicates if the votes should be weighted.&quot;
     */
    public static final String PARAMETER_WEIGHTING_TYPE = "Weighted vote type";
    public static final String PARAMETER_NN_SEARCH_TYPE = "NN search algorithm";
    public static final String[] WEIGHTING_TYPES = new String[VotingType.values().length];
    public static final String[] NN_SEARCH_TYPES = GeometricCollectionTypes.getFriendlyNames(TaskType.ANY);
    
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    /**
     *
     * @param description
     */
    public IS_KNNLearner(OperatorDescription description) {
        super(description);
        VotingType[] weightingTypes = VotingType.values();
        int i = 0;
        for(VotingType weightType : weightingTypes){
            WEIGHTING_TYPES[i] = weightType.toString();
            i++;
        }
        this.
        getExampleSetInputPort().addPrecondition(new DistanceMeasurePrecondition(getExampleSetInputPort(), this));
    }

    @Override
    public Model learn(ExampleSet exampleSet) throws OperatorException {
        DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
        int weightingNum = getParameterAsInt(PARAMETER_WEIGHTING_TYPE);        
        int nnSearchNum = getParameterAsInt(PARAMETER_NN_SEARCH_TYPE);        
        VotingType weightingType = VotingType.valueOf(WEIGHTING_TYPES[weightingNum]);        

        GeometricCollectionTypes nnSearchType = GeometricCollectionTypes.valueOfFriendlyName(NN_SEARCH_TYPES[nnSearchNum]);
        
        if (exampleSet.getAttributes().getLabel().isNominal()) {
            ISPRGeometricDataCollection<IValuesStoreLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(nnSearchType,exampleSet, measure);            
            return new IS_KNNClassificationModel<>(exampleSet, samples, getParameterAsInt(PARAMETER_K), weightingType, PredictionType.Classification);
        }
        if (exampleSet.getAttributes().getLabel().isNumerical()) {
            ISPRGeometricDataCollection<IValuesStoreLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(nnSearchType, exampleSet, measure);
            return new IS_KNNClassificationModel<>(exampleSet, samples, getParameterAsInt(PARAMETER_K), weightingType, PredictionType.Regression);
        }
        throw new OperatorException("Unknown output type. Class label should be numeric or symbolic");
    }

    @Override
    public Class<? extends PredictionModel> getModelClass() {
        return super.getModelClass();
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception e) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case NUMERICAL_LABEL:
            case WEIGHTED_EXAMPLES:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeInt(PARAMETER_K, "The number of nearest neighbors.", 1, Integer.MAX_VALUE, 1);
        type.setExpert(false);
        types.add(type);
        
        type = new ParameterTypeCategory(PARAMETER_WEIGHTING_TYPE, "Instance weighting type.", WEIGHTING_TYPES, 0);
        type.setExpert(false);
        types.add(type);    
        
        type = new ParameterTypeCategory(PARAMETER_NN_SEARCH_TYPE, "NN Search algorithm", NN_SEARCH_TYPES, 0);
        type.setExpert(false);
        types.add(type);    

        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
