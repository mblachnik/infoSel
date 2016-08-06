/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.selection.ensemble;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.ispr.dataset.IStoredValues;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperatorChain;
import com.rapidminer.ispr.operator.learner.classifiers.IS_KNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.tools.math.container.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.ispr.tools.math.container.IntDoubleContainer;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.OutputPortExtender;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.operator.ports.metadata.SubprocessTransformRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import com.rapidminer.tools.math.similarity.mixed.MixedEuclideanDistance;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class ISEnsembleVoteOperator extends AbstractPRulesOperatorChain {

    public static final String PARAMETER_THRESHOLD = "Acceptance threshold";
    public static final String PARAMETER_ADD_WEIGHTS = "Add weight attribute";

    /**
     *
     */
    protected OutputPortExtender subprocessInputExtender = new OutputPortExtender("training set", getSubprocess(0).getInnerSources());
    protected InputPortExtender subprocessOutputExtender = new InputPortExtender("base model", getSubprocess(0).getInnerSinks(), new ExampleSetMetaData(), 2);
    /**
     *
     */
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");

    private int numberOfInstancesBeaforeSelection;
    private int numberOfInstancesAfterSelection;
    private int compression;
    protected DistanceMeasureHelper measureHelper;

    public ISEnsembleVoteOperator(OperatorDescription description) {
        super(description, "Selection");
        measureHelper = new DistanceMeasureHelper(this);
        init();
    }

    private void init() {
        subprocessInputExtender.start();
        subprocessOutputExtender.start();

        exampleSetInputPort.addPrecondition(new ExampleSetPrecondition(exampleSetInputPort, Attributes.ID_NAME, Ontology.ATTRIBUTE_VALUE));
        getTransformer().addRule(subprocessInputExtender.makePassThroughRule(exampleSetInputPort));
        //getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, subprocessInputExtender, SetRelation.EQUAL));
        //getTransformer().addRule(new PassThroughRule(exampleSetInputPort,exampleInnerSourcePort,true));    
        getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
        //prototypeExampleSetOutput.addPrecondition(new SimplePrecondition(predictionModelInputInnerSourcePort, new MetaData(PredictionModel.class)));
        //subprocessOutputExtender.addPrecondition(new ExampleSetPrecondition(prototypeExampleSetOutput));
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_KNNClassificationModel.class));
        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_KNNClassificationModel.class));

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
        HashMap<Double, Double> idCounter = new HashMap<Double, Double>(trainingSet.size());

        //Performing bootstrap validation
        subprocessInputExtender.deliverToAll(trainingSet, false);
        getSubprocess(0).execute();
        List<ExampleSet> resultsList = subprocessOutputExtender.getData(ExampleSet.class, false);
        if (resultsList != null) {
            int iterations = resultsList.size();
            double step = 1.0 / iterations;
            for (ExampleSet resultSet : resultsList) {
                for (Example example : resultSet) {
                    double id = example.getId();
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
        ExampleSet output;
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
            //if (output.getAttributes().getLabel().isNominal()) {
                ISPRGeometricDataCollection<IStoredValues> samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, output, distance);
                IS_KNNClassificationModel<IStoredValues> model = new IS_KNNClassificationModel<>(output, samples, 1, VotingType.MAJORITY, PredictionType.Classification);
                modelOutputPort.deliver(model);
            //} else if (output.getAttributes().getLabel().isNumerical()) {
            //    ISPRGeometricDataCollection<IntDoubleContainer> samples = KNNTools.initializeGeneralizedKNearestNeighbour(output, distance);
            //    //GeometricDataCollection<Integer> samples = KNNTools.initializeKNearestNeighbour(output, distance);
            //    IS_KNNClassificationModel<IntDoubleContainer> model = new IS_KNNClassificationModel<IntDoubleContainer>(output, samples, 1, VotingType.MAJORITY, PredictionType.Regression);
            //    modelOutputPort.deliver(model);
            //}
        }
        //IS selection statistics
        numberOfInstancesBeaforeSelection = trainingSet.size();
        numberOfInstancesAfterSelection = output.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        return output;
    }

    /**
     * Returns number of prototypes displayed in the MataData related with prototypeOutput
     *
     * @return     
     * @throws com.rapidminer.parameter.UndefinedParameterError     
     */    
    @Override
    public MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {        
        return new MDInteger();        
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
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
        type = new ParameterTypeDouble(PARAMETER_THRESHOLD, "Acceptance threshold", 0.0001, 1, 0.8);
        type.setExpert(false);
        types.add(type);
        return types;
    }
}
