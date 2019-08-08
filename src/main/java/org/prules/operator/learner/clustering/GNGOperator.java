/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.clustering;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import org.prules.operator.learner.clustering.models.AbstractVQModel;
import org.prules.operator.learner.clustering.models.GNG_VQ_Model;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.RandomInstanceSelectionModel;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.*;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.container.Pair;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.dataset.Const;
import org.prules.dataset.IInstanceLabels;
import org.prules.tools.math.container.knn.KNNFactory;

/**
 * This class provides vector quantization operator. It uses
 * {@link com.rapidminer.ispr.operator.learner.clustering.models.VQModel} class
 * which implements the algorithm.
 *
 * @author Marcin
 */
public class GNGOperator extends AbstractPrototypeClusteringOnlineDynOperator {

    /**
     *
     */
    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
    /**
     *
     */
    public static final String PARAMETER_UPDATE_RATE = "Alpha";        
    public static final String PARAMETER_BETA = "Beta";
    public static final String PARAMETER_EB = "eb";
    public static final String PARAMETER_EN = "en";
    public static final String PARAMETER_LAMBDA = "Lambda";
    public static final String PARAMETER_AGE = "Age";    
   
    private int numberOfIteration, lambda;
    private double updateRate;
    private double beta, eb, en;
    private int age;
    private int numberOfPrototypes = -1;
    
    /**
     * Default operator constructor
     *
     * @param description
     */
    public GNGOperator(OperatorDescription description) {
        super(description);
        numberOfIteration = 50;
        updateRate = 0.02;        
    }

    /**
     * Main method in which an instance of
     * {@link com.rapidminer.ispr.operator.learner.clustering.models.VQModel} VQ
     * model is executed
     *
     * @param trainingSet
     * @param codebooks
     * @return
     * @throws OperatorException
     */
    @Override
    public ExampleSet optimize(ExampleSet trainingSet) throws OperatorException {
        AbstractVQModel vqModel;
        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
        DistanceMeasure distance = measureHelper.getInitializedMeasure(trainingSet);
        this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);        
        this.lambda = getParameterAsInt(PARAMETER_LAMBDA);
        this.age = getParameterAsInt(PARAMETER_AGE);
        this.beta = getParameterAsDouble(PARAMETER_BETA);
        this.eb = getParameterAsDouble(PARAMETER_EB);
        this.en = getParameterAsDouble(PARAMETER_EN);

        // Two random prototypes - it will ignore user selection
        RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
        AbstractInstanceSelectorModel isModel = new RandomInstanceSelectionModel(2, true, randomGenerator);
        ExampleSet codebooks = PRulesUtil.duplicateExampleSet(isModel.run(trainingSet));

        // Target number of neurons
        int numberOfNeurons = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);

        vqModel = new GNG_VQ_Model(codebooks, numberOfIteration, distance, numberOfNeurons, updateRate, lambda, beta, eb, en, age);

        /**
         * If we are using GNG algorithm then it is necessary to start from 2
         * neurons because our neural network is growing. Also we need to create
         * new cluster names.
         */
        codebooks = vqModel.run(trainingSet);
        numberOfPrototypes = codebooks.size();
        return codebooks;
    }

//    /**
//     * Returns metadata defining number of prototypes
//     *
//     * @return
//     * @throws UndefinedParameterError
//     */
//    @Override
//    public MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
//        if (this.initialPrototypesSourcePort.isConnected()) {
//            ExampleSetMetaData prototypesMetaData = (ExampleSetMetaData) this.initialPrototypesSourcePort.getMetaData();
//            if (prototypesMetaData != null) {
//                return prototypesMetaData.getNumberOfExamples();
//            }
//        }
//        int num = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
//        return new MDInteger(num);
//    }

    /**
     * Checks requirements of input dataset
     *
     * @param capability
     * @return
     */
    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (Exception e) {
        }
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
            case NUMERICAL_ATTRIBUTES:
                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
            default:
                return false;
        }
    }

    /**
     * Operator parameters configuration
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        //ParameterType vqTypeParameter =  new
        ParameterType type;

        type = new ParameterTypeInt(PARAMETER_LAMBDA, "Lambda", 1, Integer.MAX_VALUE, 500);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_AGE, "Age", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_BETA, "Beta", 0, Double.MAX_VALUE, 0.0005);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_EB, "eb", 0, Double.MAX_VALUE, 0.02);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_EN, "en", 0, Double.MAX_VALUE, 0.006);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
        type.setExpert(false);
        types.add(type);

        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, 0.3);
        type.setExpert(false);
        types.add(type);
        
        return types;
    }

    @Override
    public MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
        return new MDInteger(numberOfPrototypes);
    }

}
