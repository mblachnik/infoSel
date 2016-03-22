package com.rapidminer.ispr.operator.learner.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.classifiers.VotingType;
import com.rapidminer.ispr.operator.learner.selection.models.AbstractInstanceSelectorModel;
import com.rapidminer.ispr.operator.learner.selection.models.CNNInstanceSelectionGeneralModel;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import com.rapidminer.ispr.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import com.rapidminer.ispr.operator.learner.tools.DataIndex;
import com.rapidminer.ispr.operator.learner.tools.KNNTools;
import com.rapidminer.ispr.tools.math.container.GeometricCollectionTypes;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.operator.ports.metadata.MDInteger;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.ispr.tools.math.container.ISPRGeometricDataCollection;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.ParameterConditionedPrecondition;
import com.rapidminer.operator.ports.metadata.SimpleMetaDataError;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class which should be used as a base for any instance selection
 * algorithm. It runs {@link #configureInstanceSelectionModel} method to execute instance
 * selection algorithm. The configuration parameters are set by overriding
 * configuration methods such as
 * {@link #isDistanceBased()},{@link #isSampleRandomize() }, {@link #useDecisionFunction()}.
 *
 * @author Marcin
 */
public abstract class AbstractInstanceSelectorOperator extends AbstractPRulesOperator {

    public static final String PARAMETER_RANDOMIZE_EXAMPLES = "randomize_examples";
    public static final String PARAMETER_ADD_WEIGHTS = "add weight attribute";
    public static final String PARAMETER_INVERSE_SELECTION = "inverse selection";
    int sampleSize = -1;
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    protected DistanceMeasureHelper measureHelper;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");    
    // private boolean isDistanceBasedMethod;

    /**
     * The default RapidMiner constructor
     *
     * @param description
     */
    public AbstractInstanceSelectorOperator(OperatorDescription description) {
        super(description);
        init();
    }

    /**
     * Method called from constructor to initialize this operator. It sets and
     * configures metadata etc.
     */
    private void init() {        
        //isDistanceBasedMethod = true;
        measureHelper = new DistanceMeasureHelper(this);
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        exampleSetInputPort.addPrecondition(
                new ParameterConditionedPrecondition(exampleSetInputPort, new ExampleSetPrecondition(exampleSetInputPort, Ontology.ATTRIBUTE_VALUE, Attributes.ID_NAME), this,
                        PARAMETER_ADD_WEIGHTS, "true"));
        final AbstractInstanceSelectorOperator currentOperator = this;
        exampleSetInputPort.addPrecondition(
                new SimplePrecondition(exampleSetInputPort, null, false) {

            @Override
            public void makeAdditionalChecks(MetaData received) {
                if (received instanceof ExampleSetMetaData) {
                    ExampleSetMetaData emd = (ExampleSetMetaData) received;
                    switch (emd.hasSpecial(Attributes.LABEL_NAME)) {
                        case NO:
                        case UNKNOWN:
                            return;
                        case YES:
                            if (useDecisionFunction()) {
                                IISDecisionFunction loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(currentOperator);
                                AttributeMetaData label = emd.getLabelMetaData();
                                if (label.isNominal() && !loss.supportedLabelTypes(OperatorCapability.POLYNOMINAL_LABEL)) {
                                    exampleSetInputPort.addError(new SimpleMetaDataError(ProcessSetupError.Severity.ERROR, exampleSetInputPort, "parameters.cannot_handle", OperatorCapability.POLYNOMINAL_LABEL, ISDecisionFunctionHelper.PARAMETER_DECISION_FUNCTION, loss.name()));
                                }
                                if (label.isNumerical() && !loss.supportedLabelTypes(OperatorCapability.NUMERICAL_LABEL)) {
                                    exampleSetInputPort.addError(new SimpleMetaDataError(ProcessSetupError.Severity.ERROR, exampleSetInputPort, "parameters.cannot_handle", OperatorCapability.NUMERICAL_LABEL, ISDecisionFunctionHelper.PARAMETER_DECISION_FUNCTION, loss.name()));

                                }
                            }
                    }
                }
            }
        }
        );

        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
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
    }

    /**
     * Implements basic functionality of this operator. It is implementation of
     * the {@link com.rapidminer.ispr.operator.learner.AbstractPRulesOperator}
     * abstract method. The input dataset is delivered as {@link com.rapidminer.example.set.SelectedExampleSet}
     * class, so it has special methods to extract DataIndex from it, what makes 
     * easier implementation of instance selection   
     * @param trainingSet dataset on which we want to perform instance selection
     * @return - results of instance selection
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        /*
         * Attribute weightsAttribute = trainingSet.getAttributes().getWeight(); if ( weightsAttribute == null ){ weightsAttribute
         * = AttributeFactory.createAttribute (PRulesUtil.INSTANCES_WEIGHTS_NAME,Ontology.NUMERICAL);
         * trainingSet.getExampleTable().addAttribute(weightsAttribute); trainingSet.getAttributes().setWeight(weightsAttribute);
         * for (Example example : trainingSet){ example.setWeight(1); } }
         */
        if (isSampleRandomize()) {
            boolean shufleExamples = getParameterAsBoolean(PARAMETER_RANDOMIZE_EXAMPLES);
            if (shufleExamples) { //We can shuffle examples ony if we don't use initial geometricCollection. Order of examples in both in GemoetricCollection and ExampleSet must be equal            
                ArrayList<Integer> indicesCollection = new ArrayList<Integer>(trainingSet.size());
                for (int i = 0; i < trainingSet.size(); i++) {
                    indicesCollection.add(i);
                }

                Collections.shuffle(indicesCollection, RandomGenerator.getRandomGenerator(this));

                int[] indices = new int[trainingSet.size()];
                for (int i = 0; i < trainingSet.size(); i++) {
                    indices[i] = indicesCollection.get(i);
                }

                trainingSet = new SortedExampleSet(trainingSet, indices);
            }
        }
        SelectedExampleSet instanceSelectionInput;
        SelectedExampleSet output;
        if (trainingSet instanceof SelectedExampleSet) {
            output = (SelectedExampleSet) trainingSet;
            instanceSelectionInput = (SelectedExampleSet) trainingSet.clone();

        } else {
            output = new SelectedExampleSet(trainingSet);
            instanceSelectionInput = (SelectedExampleSet) output.clone();
        }
        DataIndex initialIndex = instanceSelectionInput.getIndex();
        numberOfInstancesBeaforeSelection = trainingSet.size();        
        AbstractInstanceSelectorModel m = configureInstanceSelectionModel(instanceSelectionInput);
        DataIndex index = m.selectInstances(instanceSelectionInput); 
        if (index==null){
            throw new UserError(this, 0);
        }
        sampleSize = index.getLength();        
        boolean inverseSelection = getParameterAsBoolean(PARAMETER_INVERSE_SELECTION);
        if (inverseSelection) {            
            index.negate();
        }
        output.setIndex(index);
        numberOfInstancesAfterSelection = output.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        if (modelOutputPort.isConnected()) {
            ISPRGeometricDataCollection<Number> samples = m.getModel();
            if (samples == null){                                            
                DistanceMeasure distance = measureHelper.getInitializedMeasure(output);
                samples = KNNTools.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, output, distance);
            }
            PredictionType modelType = trainingSet.getAttributes().getLabel().isNominal() ? PredictionType.Classification : PredictionType.Regression;
            MyKNNClassificationModel<Number> model = new MyKNNClassificationModel<>(output, samples, 1, VotingType.MAJORITY, modelType);
            modelOutputPort.deliver(model);
        }
        boolean addWeights = getParameterAsBoolean(PARAMETER_ADD_WEIGHTS);
        if (addWeights) {
            //ExampleSet tmpOutput = output.getParentExampleSet();
            //DataIndex fullIndex = output.getFullIndex();
            ExampleSet tmpTraining = (ExampleSet) trainingSet.clone();
            Attribute weights = AttributeFactory.createAttribute(Attributes.WEIGHT_NAME, Ontology.NUMERICAL);
            Attributes attributes = tmpTraining.getAttributes();
            tmpTraining.getExampleTable().addAttribute(weights);
            attributes.setWeight(weights);

            Attribute idAttribute = attributes.getId();
            if (idAttribute == null) {
                this.logError("If add weights attribute is matched ID attribute in the input exampleSet is required");
                throw new UserError(this, 129);
                //return null;
            }
            ExampleSet sortedTrainingSet = new SortedExampleSet(tmpTraining, idAttribute, SortedExampleSet.INCREASING);
            sortedTrainingSet.getAttributes().setWeight(weights);
            ExampleSet sortedPrototypesSet = new SortedExampleSet(output, idAttribute, SortedExampleSet.INCREASING);
            Iterator<Example> trainingIterator = sortedTrainingSet.iterator();
            Iterator<Example> prototypeIterator = sortedPrototypesSet.iterator();
            while (prototypeIterator.hasNext()) {
                Example prototypeExample = prototypeIterator.next();
                while (trainingIterator.hasNext()) {
                    Example trainingExample = trainingIterator.next();
                    if (prototypeExample.getId() == trainingExample.getId()) {
                        trainingExample.setWeight(1);
                        break;
                    }
                }
            }
            return tmpTraining;
        }
        return output;
    }

    /**
     * This method may be override if an algorithm doesn't want to allow sample
     * randomization. This may be used for ENN algorithm because the order of
     * samples doesn't influence the result. This cannot be solved using class
     * field because in the constructor DistanceMeasureHelper executes the
     * geParametersType method
     *
     * @return
     */
    boolean isSampleRandomize() {
        return true;
    }

    /**
     * Method allows to define if particular instance selection method uses any
     * distance measure. Default value is true. If not then override this method
     * and return false
     *
     * @return
     */
    public boolean isDistanceBased() {
        return true;
    }

    /**
     * Method returns information of particular instance selection algorithm
     * utilize decision function or not. If true (default) then user has access
     * to the decision function configuration, if not, decision function
     * parameters are not avaliable for configuration. If you want to change the
     * default value override this method
     *
     * @return
     */
    public boolean useDecisionFunction() {
        return true;
    }

    /**
     * Used to configure RapidMiner metadata processing
     *
     * @param exampleSetMD
     * @return
     * @throws UndefinedParameterError
     */
    @Override
    protected MDInteger getSampledSize(ExampleSetMetaData exampleSetMD) throws UndefinedParameterError {
        if (sampleSize == -1) {
            return new MDInteger();
        } else {
            return new MDInteger(sampleSize);
        }
    }

    /**
     * Whenever ones would like to implement self instance selection algorithm
     * should override this method. It takes as input exampleSet on which we
     * would like to perform instance selection and returns an instance of
     * {@link AbstractInstanceSelectorModel} class which implements 
     * instance selection algorithm which is farther executed by {@link #processExampleswhich} 
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    public abstract AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet trainingSet) throws OperatorException;    

    /**
     * Setting and configuring operator parameters
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        if (this.isSampleRandomize()) {
            ParameterType type = new ParameterTypeBoolean(PARAMETER_RANDOMIZE_EXAMPLES, "Randomize examples", true);
            type.setExpert(true);
            types.add(type);
            types.addAll(RandomGenerator.getRandomGeneratorParameters(this));
        }

        ParameterType type = new ParameterTypeBoolean(PARAMETER_INVERSE_SELECTION, "Inverse the instance selection", false);
        type.setExpert(true);
        types.add(type);

        type = new ParameterTypeBoolean(PARAMETER_ADD_WEIGHTS, "Add weight attribute", false);
        type.setExpert(true);
        types.add(type);

        if (this.useDecisionFunction()) {
            types.addAll(ISDecisionFunctionHelper.getParameters(this));
        }
        if (this.isDistanceBased()) {
            types.addAll(DistanceMeasures.getParameterTypes(this));
        }
        return types;
    }

}
