/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.generalized;

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
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.PredictionModel;
import org.prules.operator.AbstractPrototypeBasedOperatorChain;
import org.prules.operator.learner.classifiers.IS_KNNClassificationModel;
import org.prules.operator.learner.classifiers.PredictionType;
import org.prules.operator.learner.classifiers.VotingType;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.*;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.RandomGenerator;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import org.prules.tools.math.container.IntDoubleContainer;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.dataset.IInstanceLabels;

/**
 *
 * @author Marcin
 */
public abstract class AbstractInstanceSelectorChain extends AbstractPrototypeBasedOperatorChain {

    public static final String PARAMETER_RANDOMIZE_EXAMPLES = "randomize_examples";
    public static final String PARAMETER_ADD_WEIGHTS = "add weight attribute";
    /**
     *
     */
    protected final OutputPort exampleInnerSourcePort = getSubprocess(0).getInnerSources().createPort("exampleSet");
    /**
     *
     */
    protected final InputPort predictionModelInputInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("model");
    /**
     *
     */
    protected final OutputPort predictionModelOutputInnerSourcePort = getSubprocess(1).getInnerSources().createPort("model");
    /**
     *
     */
    protected final OutputPort testExampleInnerSourcePort = getSubprocess(1).getInnerSources().createPort("testSet");
    /**
     *
     */
    protected final InputPort predictedExampleSetInnerSourcePort = getSubprocess(1).getInnerSinks().createPort("labeled example set");
    /**
     *
     */
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("model");
    //protected final InputPort PerformanceInnerSourcePort = getSubprocess(1).getInnerSinks().createPort("Performance");
    int sampleSize = -1;
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    private boolean randomize;
    protected DistanceMeasureHelper measureHelper;

    /**
     *
     * @param description
     */
    public AbstractInstanceSelectorChain(OperatorDescription description) {
        super(description, "Training", "Testing");
        measureHelper = new DistanceMeasureHelper(this);   
        randomize = true;
        init();
    }

    public AbstractInstanceSelectorChain(OperatorDescription description, boolean randomize) {
        super(description, "Training", "Testing");// 
        measureHelper = new DistanceMeasureHelper(this);
        this.randomize = randomize;
        init();
    }

    private void init() {
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, exampleInnerSourcePort, SetRelation.EQUAL));
        //getTransformer().addRule(new PassThroughRule(exampleSetInputPort,exampleInnerSourcePort,true));    
        getTransformer().addRule(new SubprocessTransformRule(getSubprocess(0)));
        getTransformer().addRule(new ExampleSetPassThroughRule(exampleSetInputPort, testExampleInnerSourcePort, SetRelation.SUBSET));
        //getTransformer().addRule(new PassThroughRule(predictionModelInputInnerSourcePort, testExampleInnerSourcePort, true));
        getTransformer().addRule(new PassThroughRule(predictionModelInputInnerSourcePort, predictionModelOutputInnerSourcePort, false));
        predictionModelInputInnerSourcePort.addPrecondition(new SimplePrecondition(predictionModelInputInnerSourcePort, new MetaData(PredictionModel.class)));
        predictedExampleSetInnerSourcePort.addPrecondition(new ExampleSetPrecondition(predictedExampleSetInnerSourcePort));
        getTransformer().addRule(new SubprocessTransformRule(getSubprocess(1)));

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

    /**
     *
     * @param trainingSet
     * @return
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

        boolean shufleExamples = getParameterAsBoolean(PARAMETER_RANDOMIZE_EXAMPLES);
        if (randomize && shufleExamples) {
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
        SelectedExampleSet instanceSelectionInput;
        if (trainingSet instanceof SelectedExampleSet) {
            instanceSelectionInput = (SelectedExampleSet) trainingSet;
        } else {
            instanceSelectionInput = new SelectedExampleSet(trainingSet);
        }
        numberOfInstancesBeaforeSelection = trainingSet.size();
        ExampleSet output = selectInstances(instanceSelectionInput);
        numberOfInstancesAfterSelection = output.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        if (modelOutputPort.isConnected()) {
            DistanceMeasure distance = measureHelper.getInitializedMeasure(output);
            //if (output.getAttributes().getLabel().isNominal()) {
                ISPRGeometricDataCollection<IInstanceLabels> samples = KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH,output, distance);                
                IS_KNNClassificationModel<IInstanceLabels> model = new IS_KNNClassificationModel<>(output, samples, 1, VotingType.MAJORITY, PredictionType.Classification);
                modelOutputPort.deliver(model);
            //} else if (output.getAttributes().getLabel().isNumerical()) {
            //    ISPRGeometricDataCollection<IntDoubleContainer> samples = KNNTools.initializeGeneralizedKNearestNeighbour(output, distance);
            //    //GeometricDataCollection<Integer> samples = KNNTools.initializeKNearestNeighbour(output, distance);                
            //    IS_KNNClassificationModel<IntDoubleContainer> model = new IS_KNNClassificationModel<IntDoubleContainer>(output, samples, 1, VotingType.MAJORITY, PredictionType.Regression);
            //    modelOutputPort.deliver(model);
            //}

        }
        boolean addWeights = getParameterAsBoolean(PARAMETER_ADD_WEIGHTS);
        if (addWeights) {
            //ExampleSet tmpOutput = output.getParentExampleSet();
            //DataIndex fullIndex = output.getFullIndex();
            ExampleSet tmpTraining = (ExampleSet)trainingSet.clone();            
            Attribute weights = AttributeFactory.createAttribute(Attributes.WEIGHT_NAME,Ontology.NUMERICAL);
            Attributes attributes = tmpTraining.getAttributes();
            tmpTraining.getExampleTable().addAttribute(weights);
            attributes.setWeight(weights);
            
            ExampleSet sortedTrainingSet = new SortedExampleSet(tmpTraining,attributes.getId(),SortedExampleSet.INCREASING);
            sortedTrainingSet.getAttributes().setWeight(weights);
            ExampleSet sortedPrototypesSet = new SortedExampleSet(output,attributes.getId(),SortedExampleSet.INCREASING);
            Iterator<Example> trainingIterator = sortedTrainingSet.iterator();
            Iterator<Example> prototypeIterator = sortedPrototypesSet.iterator();
            while(prototypeIterator.hasNext()){
                Example prototypeExample = prototypeIterator.next();
                while(trainingIterator.hasNext()){
                    Example trainingExample = trainingIterator.next();
                    if (prototypeExample.getId() == trainingExample.getId()){
                        trainingExample.setWeight(1.0);
                        break;
                    }
                }
            }
            return tmpTraining;            
        }
        return output;
    }

   /**
     * Returns number of prototypes displayed in the MataData related with prototypeOutput
     *
     * @return
    */
    @Override
    protected MDInteger getNumberOfPrototypesMetaData() {
        return new MDInteger();        
    }

    /**
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    public abstract ExampleSet selectInstances(SelectedExampleSet trainingSet) throws OperatorException;

    /**
     *
     * @param trainingSet
     * @param testSet
     * @return
     * @throws OperatorException
     */
    public ExampleSet executeInerModel(ExampleSet trainingSet, ExampleSet testSet) throws OperatorException {

        exampleInnerSourcePort.deliver(trainingSet);
        getSubprocess(0).execute();
        inApplyLoop();
        PredictionModel trainedModel = predictionModelInputInnerSourcePort.getData(PredictionModel.class);
        predictionModelOutputInnerSourcePort.deliver(trainedModel);
        testExampleInnerSourcePort.deliver(testSet);
        getSubprocess(1).execute();
        ExampleSet predictedExampleSet = predictedExampleSetInnerSourcePort.getDataOrNull(ExampleSet.class);

        if (predictedExampleSet == null) {
            throw new UserError(this, "Need a result set");
        }
        return predictedExampleSet;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        if (randomize) {
            ParameterType type = new ParameterTypeBoolean(PARAMETER_RANDOMIZE_EXAMPLES, "Randomize examples", true);
            type.setExpert(false);
            types.add(type);
        }
        
        ParameterType type = new ParameterTypeBoolean(PARAMETER_ADD_WEIGHTS, "Add weight attribute", false);
        type.setExpert(true);
        types.add(type);
        
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
