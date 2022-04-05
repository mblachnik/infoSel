package org.prules.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.*;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.GAInstanceSelectionModel;
import org.prules.operator.performance.evaluator.Accuracy;
import org.prules.operator.performance.evaluator.PerformanceEvaluator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Genetic Algorithms-based instance selection operator based on Jenetics library
 */
public class GAInstanceSelectionOperator extends AbstractInstanceSelectorOperator{
    public static final String PARAMETER_K = "K";
    public static final String PARAMETER_PERF_RATIO = "Performance ratio";
    public static final String PARAMETER_NUM_OF_GENERATIONS = "Number of generations";
    public static final String PARAMETER_POPULATION_SIZE = "Population size";
    public static final String PARAMETER_TOURNAMENT_SELECTOR_SIZE = "Tournament selector size";
    public static final String PARAMETER_MUTATION_PROB = "Mutation probability";
    public static final String PARAMETER_CROSSOVER_PROB = "Crossover probability";
    public static final String PARAMETER_LIMIT_BY_STEADY_FITNESS = "Limit by steady fitness";

    protected final OutputPort performanceOutputPort = getOutputPorts().createPort("perf");
    /**
     * Default constructor for Genetic Algorithms-based instance selection
     *
     * @param description description of the operator
     */
    public GAInstanceSelectionOperator(OperatorDescription description) {
        super(description);
        //Here we add metadata to the additional output ExampleSet which will hold performance values.
        getTransformer().addRule(new GenerateNewExampleSetMDRule(performanceOutputPort){
            /**
             * Modifies the standard meta data before it is passed to the output. Can be used if the
             * transformation depends on parameters etc. The default implementation just returns the
             * original. Subclasses may safely modify the meta data, since a copy is used for this method.
             */
            public MetaData modifyMetaData(ExampleSetMetaData meta) {
                meta.addAttribute(new AttributeMetaData("Performance",Ontology.REAL));
                meta.addAttribute(new AttributeMetaData("Performance",Ontology.REAL));
                meta.addAttribute(new AttributeMetaData("Performance",Ontology.REAL));
                return meta;
            }
        });
    }


    @Override
    public AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet trainingSet) throws OperatorException {
        int numberOfGenerations=this.getParameterAsInt(PARAMETER_NUM_OF_GENERATIONS);
        int k = this.getParameterAsInt(PARAMETER_K);
        double performanceRatio = this.getParameterAsDouble(PARAMETER_PERF_RATIO);
        int populationSize=this.getParameterAsInt(PARAMETER_POPULATION_SIZE);
        int tournamentSelectorSize=this.getParameterAsInt(PARAMETER_TOURNAMENT_SELECTOR_SIZE);
        double singlePointCrossoverProbability=this.getParameterAsDouble(PARAMETER_CROSSOVER_PROB);
        double mutationProbability=this.getParameterAsDouble(PARAMETER_MUTATION_PROB);
        int limitBySteadyFitness=this.getParameterAsInt(PARAMETER_LIMIT_BY_STEADY_FITNESS);

        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        PerformanceEvaluator evaluator = new Accuracy();
        GAInstanceSelectionModel model = new GAInstanceSelectionModel(distance,numberOfGenerations,k,performanceRatio,evaluator,populationSize,tournamentSelectorSize,
                singlePointCrossoverProbability,mutationProbability,limitBySteadyFitness);
        return model;
    }


    public void postProcessingAfterIS(AbstractInstanceSelectorModel m) {
        //https://docs.rapidminer.com/9.2/developers/creating-your-own-extension/publishing-your-extension/
        //Tam jest też PDF z instrukcją jak i co się robi w RapidMinerze
        //https://docs.rapidminer.com/9.2/developers/changes-in-7.3/

        GAInstanceSelectionModel model = (GAInstanceSelectionModel)m;
        ExampleSetBuilder esb = ExampleSets.from(AttributeFactory.createAttribute("a1", Ontology.REAL),
                AttributeFactory.createAttribute("a2", Ontology.REAL),
                AttributeFactory.createAttribute("a3", Ontology.REAL));

        Map<String,List<Double>> performances = model.getCostFunctionPerformance();

        esb.withExpectedSize(model.getnumberOfGenerations());
        Set<String> keys = performances.keySet();

        int n = performances.get(keys.iterator().next()).size();
        for (int i=0; i<n; i++) {
            double[] row = new double[]{Double.NaN, Double.NaN, Double.NaN};
            int j=0;
            for(String key : keys){
                row[j] = performances.get(key).get(i);
                j++;
            }
            esb.addRow(row);
        }

        ExampleSet performancesSet = esb.build();

        Iterator<Attribute> as = performancesSet.getAttributes().iterator();
        for(String key : keys){
            as.next().setName(key);
        }
        performanceOutputPort.deliver(performancesSet);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception e) {
            e.printStackTrace();
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
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    /**
     * It allows configuring model whether it uses cost function or not.
     *
     * @return always false
     */
    @Override
    public boolean useDecisionFunction() {
        return false;
    }

    /**
     * This method may be override if an algorithm doesn't want to allow sample
     * randomization. This may be used for ENN algorithm because the order of
     * samples doesn't influence the result. This cannot be solved using class
     * field because in the constructor DistanceMeasureHelper executes the
     * geParametersType method
     *
     * @return - always false
     */
    public boolean isSampleRandomize() {
        return false;
    }


    /**
     * Operator configuration parameters
     *
     * @return set of additional parrameters added to the operator
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType typeK = new ParameterTypeInt(PARAMETER_K, "The value for k in kNN", 1, Integer.MAX_VALUE, 1);
        typeK.setExpert(false);
        types.add(typeK);

        ParameterType typeA2CRatio = new ParameterTypeDouble(PARAMETER_PERF_RATIO, "The performance ratio between accuracy and compression", 0.5,  1,  0.9);
        typeA2CRatio.setExpert(false);
        types.add(typeA2CRatio);

        ParameterType typeNumOfGens = new ParameterTypeInt(PARAMETER_NUM_OF_GENERATIONS, "The value for number of generations in GA", 1, Integer.MAX_VALUE, 100);
        typeK.setExpert(false);
        types.add(typeNumOfGens);

        ParameterType typePopulationSize = new ParameterTypeInt(PARAMETER_POPULATION_SIZE, "The value for the population size in GA", 1, Integer.MAX_VALUE, 50);
        typeK.setExpert(false);
        types.add(typePopulationSize);

        ParameterType typeTournSelSize = new ParameterTypeInt(PARAMETER_TOURNAMENT_SELECTOR_SIZE, "The value for the size of tournament selector of survivors in GA", 1, Integer.MAX_VALUE, 5);
        typeK.setExpert(true);
        types.add(typeTournSelSize);

        ParameterType typeCrossoverProb = new ParameterTypeDouble(PARAMETER_CROSSOVER_PROB, "The value for the probability of crossover operation in GA", 0.0, 1.0, 0.115);
        typeK.setExpert(true);
        types.add(typeCrossoverProb);

        ParameterType typeMutProb = new ParameterTypeDouble(PARAMETER_MUTATION_PROB, "The value for the probability of mutation operation in GA", 0.0, 1.0, 0.2);
        typeK.setExpert(true);
        types.add(typeMutProb);

        ParameterType typeLmtBySteadyFitness = new ParameterTypeInt(PARAMETER_LIMIT_BY_STEADY_FITNESS, "The value for the number of generations that will be generated when the fitness function no longer changes its value", 1, Integer.MAX_VALUE, 15);
        typeK.setExpert(true);
        types.add(typeLmtBySteadyFitness);


        return types;
    }
}
