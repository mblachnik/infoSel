/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.meta;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperatorChain;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.IntDoubleContainer;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.SetRelation;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.mixed.MixedEuclideanDistance;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Marcin
 */
public abstract class AbstractISMetaOperator extends AbstractPRulesOperatorChain {

    public static final String PARAMETER_ITERATIOINS = "Number of iterations";
    public static final String PARAMETER_THRESHOLD = "Acceptance threshold";
    public static final String PARAMETER_ADD_WEIGHTS = "Add weight attribute";    

    /**
     *
     */
    protected final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("ExampleSet");
    /**
     *
     */
    //protected final InputPort prototypeExampleSetOutput = getSubprocess(0).getInnerSinks().createPort("Output Example Set");
    protected final InputPort prototypeExampleSetOutput = getSubprocess(0).getInnerSinks().createPort("Output Example Set");
    /**
     *
     */
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("Model");
    private int numberOfInstancesBeaforeSelection;
    private int numberOfInstancesAfterSelection;
    private int compression;
    protected DistanceMeasureHelper measureHelper;

    public AbstractISMetaOperator(OperatorDescription description) {
        super(description, "Selection");
        measureHelper = new DistanceMeasureHelper(this);
        init();
    }

    private void init() {
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, exampleInnerSourcePort, SetRelation.EQUAL));
        //getTransformer().addRule(new PassThroughRule(exampleSetInputPort,exampleInnerSourcePort,true));    
        getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
        //prototypeExampleSetOutput.addPrecondition(new SimplePrecondition(predictionModelInputInnerSourcePort, new MetaData(PredictionModel.class)));
        prototypeExampleSetOutput.addPrecondition(new ExampleSetPrecondition(prototypeExampleSetOutput));
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        exampleSetInputPort.addPrecondition(new ExampleSetPrecondition(exampleSetInputPort, Attributes.ID_NAME, Ontology.ATTRIBUTE_VALUE));
        addValue(new ValueDouble("Instances_beafore_selection", "Number Of Examples in the training set") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesBeaforeSelection;
            }
        });
        addValue(new ValueDouble("Instances_after_selection", "Number Of Examples after selection") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesAfterSelection;
            }
        });
        addValue(new ValueDouble("Compression", "Compressin = #Instances_after_selection/#Instances_beafore_selection") {

            @Override
            public double getDoubleValue() {
                return compression;
            }
        });
        //getTransformer().addPassThroughRule(exampleSetInputPort, );
        //getTransformer().addPassThroughRule(exampleSetInnerSourcePort,exampleSetInnerResultsPort);
        //modelInnerSourcePort.addPrecondition(new LearnerPrecondition(this, exampleSetInnerSourcePort));
    }

    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        Attribute idAttribute = trainingSet.getAttributes().getId();
        if (idAttribute == null) {
            this.logError("This operator requires input ID attribute in input example set");
            return null;
        }
        double threshold = getParameterAsDouble(PARAMETER_THRESHOLD);
        int iterations = getParameterAsInt(PARAMETER_ITERATIOINS);
        HashMap<Double, Double> idCounter = new HashMap<Double, Double>(trainingSet.size());
        double step = 1.0 / iterations;
        int[] mapping;
        //Performing bootstrap validation
        for (int i = 0; i < iterations; i++) {
            ExampleSet trainingSubSet = prepareExampleSet(trainingSet);
            exampleInnerSourcePort.deliver(trainingSubSet);
            getSubprocess(0).execute();
            ExampleSet resultSet = prototypeExampleSetOutput.getDataOrNull(ExampleSet.class);
            if (resultSet != null) {
                for (Example e : resultSet) {
                    double id = e.getId();
                    Double value = idCounter.get(id);
                    if (value == null) {
                        value = 0.0;
                    }
                    value += step;
                    idCounter.put(id, value);
                }
            }
        }

        boolean addWeights = getParameterAsBoolean(PARAMETER_ADD_WEIGHTS);
        ExampleSet output = null;
        if (addWeights) {
            //ExampleSet tmpOutput = output.getParentExampleSet();
            //DataIndex fullIndex = output.getFullIndex();
            output = (ExampleSet) trainingSet.clone();
            Attribute weights = AttributeFactory.createAttribute(Attributes.WEIGHT_NAME, Ontology.NUMERICAL);
            Attributes attributes = output.getAttributes();
            output.getExampleTable().addAttribute(weights);
            attributes.setWeight(weights);

            ExampleSet sortedTrainingSet = new SortedExampleSet(output, attributes.getId(), SortedExampleSet.INCREASING);
            sortedTrainingSet.getAttributes().setWeight(weights);

            for (Example example : output) {
                double id = example.getId();
                Double valueO = idCounter.get(id);
                double value = valueO == null ? 0 : valueO.doubleValue();
                example.setWeight(value);

            }
        } else {
            DataIndex index = new DataIndex(trainingSet.size());
            int i = 0;
            //Selecting appropriate vectors
            for (Example e : trainingSet) {
                double id = e.getId();
                Double valueO = idCounter.get(id);
                double value = valueO == null ? Double.NaN : valueO.doubleValue();
                if ((Double.isNaN(value)) || (value < threshold)) { //Unselect vectors not included in idCounter or those which don't fulfill threshold requirements
                    index.set(i, false);
                }
                i++;
            }
            output = new SelectedExampleSet(trainingSet, index);
        }
        if (modelOutputPort.isConnected()) {
            DistanceMeasure distance = new MixedEuclideanDistance();
            distance.init(output);
            if (output.getAttributes().getLabel().isNominal()) {
                ISPRGeometricDataCollection<Number> samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, output, distance);                
                MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<Number>(output, samples, 1, VotingType.MAJORITY, PredictionType.Classification);
                modelOutputPort.deliver(model);
            } else if (output.getAttributes().getLabel().isNumerical()) {
                ISPRGeometricDataCollection<IntDoubleContainer> samples = KNNTools.initializeGeneralizedKNearestNeighbour(output, distance);
                //GeometricDataCollection<Integer> samples = KNNTools.initializeKNearestNeighbour(output, distance);                
                MyKNNClassificationModel<IntDoubleContainer> model = new MyKNNClassificationModel<IntDoubleContainer>(output, samples, 1, VotingType.MAJORITY, PredictionType.Regression);
                modelOutputPort.deliver(model);
            }

        }
        //IS selection statistics
        numberOfInstancesBeaforeSelection = trainingSet.size();
        numberOfInstancesAfterSelection = output.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        return output;
    }

    abstract ExampleSet prepareExampleSet(ExampleSet trainingSet) throws OperatorException;

    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData emd) throws UndefinedParameterError {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return new MDInteger(-1);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:
            case NUMERICAL_ATTRIBUTES:
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:
            case NUMERICAL_LABEL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeBoolean(PARAMETER_ADD_WEIGHTS, "Add weight attribute", false);
        type.setExpert(true);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_ITERATIOINS, "Number of iterations", 1, 10000, 10);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeDouble(PARAMETER_THRESHOLD, "Acceptance threshold", 0.0001, 1, 0.8);
        type.setExpert(false);
        types.add(type);
        types.addAll(RandomGenerator.getRandomGeneratorParameters(this));
        return types;
    }
}
