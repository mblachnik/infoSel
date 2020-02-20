package org.prules.operator.learner.classifiers.data_splitter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.operator.learner.CapabilityProvider;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.CapabilityPrecondition;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

import java.util.*;

import java.util.Map.Entry;

import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

/**
 * The class implements NearestPrototypeBatchOperator. It takes the prototypes
 * and for each training vector it identifies two closest prototypes from opposite classes.
 * This allows to mark all training vectors with appropriate pair of prototypes.
 * This allows to split the training data along the decision boundary. As an output it adds
 * three new attributes to the training data: batch which indicates data subsets
 * which belong to a single pair, and two additional attributes ID_Proto_1 and ID_Proto_2
 * which indicates respectively index of closest prototype from same class label and opposite
 * class label respectively. This operator also returns PrototypeEnsembleModel which
 * contains information on prototypes and its labels and additional information require to finally build ensemble etc.
 * @author Marcin
 */
public class NearestPrototypeEnsembleOperator extends OperatorChain implements CapabilityProvider {

    public static final String PARAMETER_MIN_COUNT_FACTOR = "Min. counts factor";
    public static final String PARAMETER_MINIMUM_SUPPORT = "Min. support";

    /**
     * Input data - training set
     */
    private final InputPort exampleSetInputPort = this.getInputPorts().createPort("example set");
    /**
     * Input data - prototypes
     */
    private final InputPort prototypesInputPort = this.getInputPorts().createPort("prototypes");
    /**
     * THe final prediction model ensemble
     */
    private final OutputPort finalModelOutputPort = getOutputPorts().createPort("prediction model");
    /**
     * example set with three additional attributes as described in class description
     */
    private final OutputPort exampleSetOutputPort = this.getOutputPorts().createPort("example set");
    /**
     * training data delivered to the subprocess
     */
    private final OutputPort exampleInnerSourcePort   = getSubprocess(0).getInnerSources().createPort("example Set");
    /**
     * PredictionModel obtained after training the prediction model
     */
    private final InputPort predictionModelInnerSourcePort = getSubprocess(0).getInnerSinks().createPort("model", PredictionModel.class);


    /**
     * Distance measure helper for creating appropriate distance measure
     */
    private DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);

    /**
     * <p>
     * Creates a  NearestPrototypeBatch operator. S
     * </p>
     * <p>
     * NOTE: the preferred way for operator creation is using one of the factory
     * methods of {@link OperatorService}.
     * </p>
     *
     * @param description
     */
    public NearestPrototypeEnsembleOperator(OperatorDescription description) {
        super(description,"Train prediction model");
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        exampleSetInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
            try {
                measureType = measureHelper.getSelectedMeasureType();
            } catch (UndefinedParameterError ignored) {
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
                case MISSING_VALUES:
                case BINOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, exampleSetInputPort));
        prototypesInputPort.addPrecondition(new DistanceMeasurePrecondition(prototypesInputPort, this));
        prototypesInputPort.addPrecondition(new CapabilityPrecondition(capability -> {
            int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
            try {
                measureType = measureHelper.getSelectedMeasureType();
            } catch (UndefinedParameterError ignored) {
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
                case MISSING_VALUES:
                case BINOMINAL_LABEL:
                    return true;
                default:
                    return false;
            }
        }, prototypesInputPort));
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleSetOutputPort);
        getTransformer().addGenerationRule(finalModelOutputPort, PrototypesEnsembelePredictionModel.class);
        getTransformer().addPassThroughRule(exampleSetInputPort, exampleInnerSourcePort);
    }

    /**
     * This method performs prediction model training for every value of batch attribute. The algorithm starts by
     * identifing examples with given batch value, and than in a loop it trains prediction model defined within the
     * inner process. Each prediction model is collected and returned to the output as PrototypesEnsemblePredictionModel
     * @param prototypePiredModelAndData
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {
        double minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        int minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        //boolean detectPureSubsets = getParameterAsBoolean(PARAMETER_DETECT_PURE_SUBSETS);
        ExampleSet exampleSet = this.exampleSetInputPort.getDataOrNull(ExampleSet.class);
        ExampleSet prototypeSet = this.prototypesInputPort.getDataOrNull(ExampleSet.class);
        NearestPrototypesSplitter inputModel = new NearestPrototypesSplitter(prototypeSet,measureHelper.getInitializedMeasure(exampleSet),minFactor,minSupport);
        exampleSet = inputModel.split(exampleSet);
        this.exampleSetOutputPort.deliver(exampleSet);
        Map<Long,PredictionModel> modelsMap;
        Attribute attr = exampleSet.getAttributes().findRoleBySpecialName(Attributes.BATCH_NAME).getAttribute();
        //Map which contons list of elements which belong to given batch
        Map<Long, IDataIndex> pairsMap = new HashMap<>();
        //Get all possible pairs and samples which belong to given pair
        IDataIndex idx;
        int exampleIndex = 0;
        for(Example e : exampleSet){
            double pairId = e.getValue(attr);
            if (pairsMap.containsKey((long)pairId)){
                idx = pairsMap.get((long)pairId);
            }else {
                idx = new DataIndex(exampleSet.size());
                idx.setAllFalse();
                pairsMap.put((long)pairId, idx);
            }
            idx.set(exampleIndex,true);
            exampleIndex++;
        }
        modelsMap = new HashMap<>(pairsMap.size());
        for (Entry<Long, PiredTriple> entry : inputModel.getSelectedPairs().entrySet()){
            Long pair = entry.getKey();
            if (pairsMap.containsKey(pair)){
                idx = pairsMap.get(pair);
            } else {
                continue;
            }
            //Select samples from given batch
            SelectedExampleSet selectedExampleSet = new SelectedExampleSet(exampleSet);
            selectedExampleSet.setIndex(idx);
            //And deliver these samples to train a model
            exampleInnerSourcePort.deliver(selectedExampleSet);
            //Execute inner process (train the model)
            getSubprocess(0).execute();
            inApplyLoop();

            PredictionModel  model = predictionModelInnerSourcePort.getData(PredictionModel.class);
            modelsMap.put(pair, model);
        }

        PredictionModel finalModel = new PrototypesEnsembelePredictionModel(inputModel, modelsMap, exampleSet, ExampleSetUtilities.SetsCompareOption.ALLOW_SUPERSET, ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS);
        finalModelOutputPort.deliver(finalModel);
    }

    @Override
    public boolean supportsCapability(OperatorCapability capability) {
        int measureType = DistanceMeasures.MIXED_MEASURES_TYPE;
        try {
            measureType = measureHelper.getSelectedMeasureType();
        } catch (UndefinedParameterError e) {
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
            case BINOMINAL_LABEL:
            case MISSING_VALUES:
                return true;
            default:
                return false;
        }
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_COUNT_FACTOR, "Factor indicating minimum number of instances in a single batch. It is multiplayed by the max counts.", 0, 1, 0.1);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMUM_SUPPORT, "Minimum number of samples in a single batch. It it has lower number of samples it will be removed and the samples will be redistributed into another batches", 0, Integer.MAX_VALUE, 20);
        types.add(type);
        //type = new ParameterTypeBoolean(PARAMETER_DETECT_PURE_SUBSETS,"Detect pure subsets and keep them (for examples falling into this pair label will be determined without training a model) ",false);
        //types.add(type);
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
