///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.prules.operator.learner.clustering;
//
//import java.util.Iterator;
//import java.util.List;
//
//import com.rapidminer.example.Attribute;
//import com.rapidminer.example.Attributes;
//import com.rapidminer.example.Example;
//import com.rapidminer.example.ExampleSet;
//import com.rapidminer.example.table.AttributeFactory;
//import com.rapidminer.example.table.NominalMapping;
//import com.rapidminer.example.table.PolynominalMapping;
//import org.prules.operator.learner.clustering.models.AbstractVQModel;
//import org.prules.operator.learner.clustering.models.VQModel;
//import org.prules.operator.learner.clustering.models.VQTypes;
//import org.prules.operator.learner.clustering.models.GNG_VQ_Model;
//import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
//import org.prules.operator.learner.selection.models.RandomInstanceSelectionModel;
//import org.prules.tools.math.container.knn.KNNTools;
//import org.prules.operator.learner.tools.PRulesUtil;
//import org.prules.tools.math.container.knn.GeometricCollectionTypes;
//import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
//import com.rapidminer.operator.OperatorCapability;
//import com.rapidminer.operator.OperatorDescription;
//import com.rapidminer.operator.OperatorException;
//import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
//import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
//import com.rapidminer.operator.ports.metadata.MDInteger;
//import com.rapidminer.parameter.*;
//import com.rapidminer.parameter.conditions.EqualTypeCondition;
//import com.rapidminer.tools.Ontology;
//import com.rapidminer.tools.RandomGenerator;
//import com.rapidminer.tools.math.similarity.DistanceMeasure;
//import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
//import com.rapidminer.tools.math.similarity.DistanceMeasures;
//import org.prules.dataset.Const;
//import org.prules.dataset.IInstanceLabels;
//import org.prules.operator.learner.clustering.models.SCSVQModelBuilder;
//import org.prules.operator.learner.clustering.models.SRVQModelBuilder;
//import org.prules.tools.math.container.knn.KNNFactory;
//
///**
// * This class provides vector quantization operator. It uses
// * {@link com.rapidminer.ispr.operator.learner.clustering.models.VQModel} class
// * which implements the algorithm.
// *
// * @author Marcin
// */
//public class VQOperator extends AbstractPrototypeClusteringOnlineOperator {
//
//    /**
//     *
//     */
//    public static final String PARAMETER_ITERATION_NUMBER = "Iterations";
//    /**
//     *
//     */
//    public static final String PARAMETER_UPDATE_RATE = "Alpha";
//    public static final String PARAMETER_VQ_TYPE = "VQ_type";
//    public static final String PARAMETER_SR_TEMPERATURE = "Temperature";
//    public static final String PARAMETER_SR_TEMP_RATE = "Temperature_rate";
//    public static final String PARAMETER_BETA = "Beta";
//    public static final String PARAMETER_EB = "eb";
//    public static final String PARAMETER_EN = "en";
//    public static final String PARAMETER_LAMBDA = "Lambda";
//    public static final String PARAMETER_AGE = "Age";
//    private VQTypes vqType;
//    private DistanceMeasureHelper measureHelper;
//    private int numberOfIteration, lambda;
//    private double updateRate;
//    private double temperature;
//    private double temperatureRate;
//    private double beta, eb, en;
//    private int age;
//
//    /**
//     * Default operator constructor
//     *
//     * @param description
//     */
//    public VQOperator(OperatorDescription description) {
//        super(description);
//        numberOfIteration = 50;
//        updateRate = 0.02;
//        measureHelper = new DistanceMeasureHelper(this);
//    }
//
//    /**
//     * Main method in which an instance of
//     * {@link com.rapidminer.ispr.operator.learner.clustering.models.VQModel} VQ
//     * model is executed
//     *
//     * @param trainingSet
//     * @param codebooks
//     * @return
//     * @throws OperatorException
//     */
//    @Override
//    public IS_PrototypeClusterModel optimize(ExampleSet trainingSet, ExampleSet codebooks) throws OperatorException {
//        AbstractVQModel vqModel;
//        this.numberOfIteration = getParameterAsInt(PARAMETER_ITERATION_NUMBER);
//        DistanceMeasure distance = measureHelper.getInitializedMeasure(codebooks);
//        this.updateRate = getParameterAsDouble(PARAMETER_UPDATE_RATE);
//        this.temperature = getParameterAsDouble(PARAMETER_SR_TEMPERATURE);
//        this.temperatureRate = getParameterAsDouble(PARAMETER_SR_TEMP_RATE);
//        this.lambda = getParameterAsInt(PARAMETER_LAMBDA);
//        this.age = getParameterAsInt(PARAMETER_AGE);
//        this.beta = getParameterAsDouble(PARAMETER_BETA);
//        this.eb = getParameterAsDouble(PARAMETER_EB);
//        this.en = getParameterAsDouble(PARAMETER_EN);
//        int idVqType = getParameterAsInt(PARAMETER_VQ_TYPE);
//        vqType = VQTypes.values()[idVqType];
//
//        switch (vqType) {
//            case SCS:
//                vqModel = SCSVQModelBuilder.builder()
//                        .withPrototypes(codebooks)
//                        .withIterations(numberOfIteration)
//                        .withMeasure(distance)
//                        .withAlpha(updateRate)
//                        .withTemperature(temperature)
//                        .withTemperatureRate(temperatureRate)
//                        .build();
//                break;
//            case SR:
//                vqModel = SRVQModelBuilder.builder()
//                        .withPrototypes(codebooks)
//                        .withIterations(numberOfIteration)
//                        .withMeasure(distance)
//                        .withAlpha(updateRate)
//                        .withTemperature(temperature)
//                        .withTemperatureRate(temperatureRate)
//                        .build();
//                break;
//            case GNG:
//                // Two random prototypes - it will ignore user selection
//                RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(this);
//                AbstractInstanceSelectorModel isModel = new RandomInstanceSelectionModel(2, true, randomGenerator);
//                codebooks = PRulesUtil.duplicateExampleSet(isModel.run(trainingSet));
//
//                // Target number of neurons
//                int numberOfNeurons = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
//
//                vqModel = new GNG_VQ_Model(codebooks, numberOfIteration, distance, numberOfNeurons, updateRate, lambda, beta, eb, en, age);
//                break;
//            case WTA:
//            default:
//                vqModel = new VQModel(codebooks, numberOfIteration, distance, updateRate);
//                break;
//        }
//
//        /**
//         * If we are using GNG algorithm then it is necessary to start from 2
//         * neurons because our neural network is growing. Also we need to create
//         * new cluster names.
//         */
//        if (VQTypes.GNG.equals(vqType)) {
////            codebooks = vqModel.run(trainingSet);
////            clusterNames = IS_ClusterModelTools.prepareClusterNamesMap(codebooks.size());
////            Attribute codebookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
////            NominalMapping codebookLabelsNames = new PolynominalMapping(clusterNames);
////            codebookLabels.setMapping(codebookLabelsNames);
////            codebooks.getExampleTable().addAttribute(codebookLabels);
////            if (getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL)) {
////                codebooks.getAttributes().setLabel(codebookLabels);
////            } else {
////                codebooks.getAttributes().setCluster(codebookLabels);
////            }
////            Iterator<Integer> clusterLabelIterator = clusterNames.keySet().iterator();
////            for (Example codebook : codebooks) {
////                int value = clusterLabelIterator.next();
////                codebook.setValue(codebookLabels, value);
////            }
////            this.codebooks = codebooks;
//        } else {
//            vqModel.run(trainingSet);
//        }
//
//        boolean addAsLabel = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL);
//        boolean addCluster = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
//        ISPRGeometricDataCollection<IInstanceLabels> knnModel;
//        if (addAsLabel) {
//            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getLabel(), Const.LABEL, distance);
//        } else {
//            knnModel = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, codebooks, codebooks.getAttributes().getCluster(), Const.LABEL, distance);
//        }
//        IS_PrototypeClusterModel model = new IS_PrototypeClusterModel(trainingSet, knnModel, codebooks.size(), clusterNames, addAsLabel, addCluster);
//        return model;
//    }
//
//    /**
//     * Returns metadata defining number of prototypes
//     *
//     * @return
//     * @throws UndefinedParameterError
//     */
//    @Override
//    protected MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
//        if (this.initialPrototypesSourcePort.isConnected()) {
//            ExampleSetMetaData prototypesMetaData = (ExampleSetMetaData) this.initialPrototypesSourcePort.getMetaData();
//            if (prototypesMetaData != null) {
//                return prototypesMetaData.getNumberOfExamples();
//            }
//        }
//        int num = getParameterAsInt(PARAMETER_NUMBER_OF_NEURONS);
//        return new MDInteger(num);
//    }
//
//    /**
//     * Checks requirements of input dataset
//     *
//     * @param capability
//     * @return
//     */
//    @Override
//    public boolean supportsCapability(OperatorCapability capability) {
//        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
//        try {
//            measureType = measureHelper.getSelectedMeasureType();
//        } catch (Exception e) {
//        }
//        switch (capability) {
//            case BINOMINAL_ATTRIBUTES:
//                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
//                        || (measureType == DistanceMeasures.NOMINAL_MEASURES_TYPE);
//            case NUMERICAL_ATTRIBUTES:
//                return (measureType == DistanceMeasures.MIXED_MEASURES_TYPE)
//                        || (measureType == DistanceMeasures.DIVERGENCES_TYPE)
//                        || (measureType == DistanceMeasures.NUMERICAL_MEASURES_TYPE);
//            default:
//                return false;
//        }
//    }
//
//    /**
//     * Operator parameters configuration
//     *
//     * @return
//     */
//    @Override
//    public List<ParameterType> getParameterTypes() {
//        List<ParameterType> types = super.getParameterTypes();
//
//        //ParameterType vqTypeParameter =  new
//        ParameterType type;
//
//        type = new ParameterTypeCategory(PARAMETER_VQ_TYPE, "Defines on type of vq algorithm.", VQTypes.typeNames(), 0);
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeDouble(PARAMETER_SR_TEMPERATURE, "Initial temperature", 0, Double.MAX_VALUE, 50.0);
//        type.setExpert(false);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.SR.ordinal(), VQTypes.SCS.ordinal()));
//        types.add(type);
//
//        type = new ParameterTypeDouble(PARAMETER_SR_TEMP_RATE, "Temperature update rate", 0, Double.MAX_VALUE, 0.89);
//        type.setExpert(false);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.SR.ordinal(), VQTypes.SCS.ordinal()));
//        types.add(type);
//
//        type = new ParameterTypeInt(PARAMETER_LAMBDA, "Lambda", 1, Integer.MAX_VALUE, 500);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.GNG.ordinal()));
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeInt(PARAMETER_AGE, "Age", 1, Integer.MAX_VALUE, 100);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.GNG.ordinal()));
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeDouble(PARAMETER_BETA, "Beta", 0, Double.MAX_VALUE, 0.0005);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.GNG.ordinal()));
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeDouble(PARAMETER_EB, "eb", 0, Double.MAX_VALUE, 0.02);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.GNG.ordinal()));
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeDouble(PARAMETER_EN, "en", 0, Double.MAX_VALUE, 0.006);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.GNG.ordinal()));
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeInt(PARAMETER_ITERATION_NUMBER, "Number of iteration loop", 1, Integer.MAX_VALUE, this.numberOfIteration);
//        type.setExpert(false);
//        types.add(type);
//
//        type = new ParameterTypeDouble(PARAMETER_UPDATE_RATE, "Value of update rate", 0, Double.MAX_VALUE, 0.3);
//        type.setExpert(false);
//        type.registerDependencyCondition(new EqualTypeCondition(this, PARAMETER_VQ_TYPE, VQTypes.typeNames(), false,
//                VQTypes.SR.ordinal(), VQTypes.WTA.ordinal(), VQTypes.SCS.ordinal(), VQTypes.GNG.ordinal()));
//        types.add(type);
//
//        types.addAll(DistanceMeasures.getParameterTypes(this));
//        return types;
//    }
//}
