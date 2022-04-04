package org.prules.operator.learner.selection;

import com.rapidminer.example.*;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessSetupError;
import com.rapidminer.operator.ProcessSetupError.Severity;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.error.AttributeNotFoundError;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.*;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.AbstractPrototypeBasedOperator;
import org.prules.operator.learner.classifiers.IS_KNNClassificationModel;
import org.prules.operator.learner.classifiers.PredictionType;
import org.prules.operator.learner.classifiers.VotingType;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.selection.models.decisionfunctions.ISDecisionFunctionHelper;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.learner.tools.PRulesUtil;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class which should be used as a base for any instance selection
 * algorithm. It runs {@link #configureInstanceSelectionModel} method to execute
 * instance selection algorithm. The configuration parameters are set by
 * overriding configuration methods such as
 * {@link #isDistanceBased()},{@link #isSampleRandomize() }, {@link #useDecisionFunction()}.
 *
 * @author Marcin
 */
public abstract class AbstractInstanceSelectorOperator extends AbstractPrototypeBasedOperator {

    public static final String PARAMETER_RANDOMIZE_EXAMPLES = "randomize_examples";
    public static final String PARAMETER_ADD_WEIGHTS = "add weight attribute";
    public static final String PARAMETER_INVERSE_SELECTION = "inverse selection";
    public static final String PARAMETER_ONE_CLASS = "Keep one sample if mono class";
    int sampleSize = -1;
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
        if (isDistanceBased()){
            measureHelper = new DistanceMeasureHelper(this);
        }
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_KNNClassificationModel.class));
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
                                IISDecisionFunction loss;
                                try {
                                    loss = ISDecisionFunctionHelper.getConfiguredISDecisionFunction(currentOperator, null);
                                    List<List<String>> errors = loss.makeAdditionalChecks(emd);
                                    for (List<String> error : errors){  
                                        String errorType = error.get(0);                                        
                                        String errorI18nKey = error.get(1);
                                        error.remove(0);
                                        error.remove(0);
                                        Object[] params = error.toArray();
                                        Severity severity = ProcessSetupError.Severity.valueOf(errorType);                                        
                                        exampleSetInputPort.addError(new SimpleMetaDataError(severity , exampleSetInputPort, errorI18nKey, params));
                                    }
                                } catch (OperatorException ex) {
                                    currentOperator.getLog().logWarning("Exception in loss functin configuration.");
                                }
                            }
                    }
                }
            }
        }
        );

        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, IS_KNNClassificationModel.class));
    }

    /**
     * Implements basic functionality of this operator. It is implementation of
     * the {@link org.prules.operator.AbstractPrototypeBasedOperator}
     * abstract method. The input dataset is delivered as
     * {@link com.rapidminer.example.set.SelectedExampleSet} class, so it has
     * special methods to extract DataIndex from it, what makes easier
     * implementation of instance selection
     *
     * @param trainingSet dataset on which we want to perform instance selection
     * @return - results of instance selection
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        if (trainingSet.size()==0) return trainingSet;
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

        /** Check if label is required. If so, then check if the dataset containes more than one class. If only one class
         * is present then check the isMidPointIfOneClass(). If false then return entire set, but if true then select only
         * the most representative sample. (sample closest to the average)
         *
         */
        if (isLabelRequired()){
            if (trainingSet.getAttributes().getLabel()==null){
                throw new AttributeNotFoundError(this,"","label");
            }
            if (PRulesUtil.isSingleLabel(trainingSet)){
                LogService.getRoot().info("All sampels have the same class label");
                if (isMidPointIfOneClass()){
                    LogService.getRoot().info("Return only the most representative sample");
                    return findReferencePoint(trainingSet);
                } else {
                    LogService.getRoot().info("Return the first sample (the sample depedns on randomization parameter)");
                    return trainingSet;
                }
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
        IDataIndex initialIndex = instanceSelectionInput.getIndex();
        AbstractInstanceSelectorModel m = configureInstanceSelectionModel(instanceSelectionInput);
        IDataIndex index = m.selectInstances(instanceSelectionInput);
        postProcessingAfterIS(m);
        if (index == null) {
            throw new UserError(this, 0);
        }
        sampleSize = index.getLength();
        boolean inverseSelection = getParameterAsBoolean(PARAMETER_INVERSE_SELECTION);
        if (inverseSelection) {
            index.negate();
        }
        output.setIndex(index);
        if (modelOutputPort.isConnected()) {
            ISPRGeometricDataCollection<IInstanceLabels> samples = m.getModel();
            if (samples == null) {
                DistanceMeasure distance = measureHelper.getInitializedMeasure(output);
                samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, output, distance);
            }
            PredictionType modelType = trainingSet.getAttributes().getLabel().isNominal() ? PredictionType.Classification : PredictionType.Regression;
            IS_KNNClassificationModel<IInstanceLabels> model = new IS_KNNClassificationModel<>(output, samples, 1, VotingType.MAJORITY, modelType);
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
     * This method is executed after performing instance selection in order to extract additional information
     * @param m
     */
    public void postProcessingAfterIS(AbstractInstanceSelectorModel m) {
    }

    /**
     * Procedure to find the most representative examples that is the closes to the average example. THe algorithms determines
     * average or mode for each column, which is a reference point and then search for example which is closest to that reference point
     * Only the cosest example is marked to be keepted in the final set. If given instance selection method is not based on
     * the distance measure then the first samle is selected.
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    private ExampleSet findReferencePoint(ExampleSet exampleSet) throws OperatorException {
        SelectedExampleSet outSet = new SelectedExampleSet(exampleSet);
        IDataIndex index = outSet.getIndex();
        index.setAllFalse();
        if (isDistanceBased()){
            DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);
            Attributes attrs = exampleSet.getAttributes();
            double[] stats = new double[attrs.size()];
            int i=0;
            for(Attribute a : attrs){
                if (a.isNumerical()) {
                    stats[i] = exampleSet.getStatistics(a, Statistics.AVERAGE);
                } else {
                    stats[i] = exampleSet.getStatistics(a, Statistics.MODE);
                }
                i++;
            };
            double minDist = Double.POSITIVE_INFINITY;
            int minI = -1;
            i=0;
            for(Example ex : exampleSet){
                double dist = measure.calculateDistance(ex,stats);
                if (dist < minDist){
                    minDist = dist;
                    minI = i;
                }
                i++;
            }
            index.set(i,true);
        } else {
            index.set(0,true);
        }
        outSet.setIndex(index);
        return outSet;
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
    public boolean isSampleRandomize() {
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
     * Method allows to set if label attribute is required by the instance selection operator.
     * By default label attribute is required, if ovverriden and method returns false then
     * no checks are made according to label existance.
     * @return 
     */
    public boolean isLabelRequired(){
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
     * Returns number of prototypes displayed in the MataData related with
     * prototypeOutput
     *
     * @return
     * @throws com.rapidminer.parameter.UndefinedParameterError
     */
    @Override
    public MDInteger getNumberOfPrototypesMetaData() throws UndefinedParameterError {
        return new MDInteger();
    }        

    public boolean isMidPointIfOneClass(){
        return true;
    }
    /**
     * Whenever ones would like to implement self instance selection algorithm
     * should override this method. It takes as input exampleSet on which we
     * would like to perform instance selection and returns an instance of
     * {@link AbstractInstanceSelectorModel} class which implements instance
     * selection algorithm which is farther executed by
     * {@link #processExamples}
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    public abstract AbstractInstanceSelectorModel configureInstanceSelectionModel(SelectedExampleSet trainingSet) throws OperatorException;

    /**
     * Return reference to distance measure helper, which is used to configure
     * distance function of given operator
     *
     * @return
     */
    public DistanceMeasureHelper getDistanceMeasureHelper() {
        if (isDistanceBased()) {
            return measureHelper;
        }
        return null;
    }
    
    

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

        if (isMidPointIfOneClass()){
            type = new ParameterTypeBoolean(PARAMETER_ONE_CLASS, "", true);
            type.setExpert(true);
            types.add(type);
        }
        return types;
    }

}
