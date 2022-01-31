package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SelectedExampleReader;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.AttributeSetPrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.IsConnectedPrecondition;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;
import org.prules.operator.learner.classifiers.data_splitter.NearestPrototypesSplitter;
import org.prules.operator.learner.classifiers.data_splitter.NearestPrototypesSplitterV2;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.similarity.numerical.SquareEuclidianDistance;

import java.util.*;

public class ISEnsembleProtoPairOperator extends AbstractISEnsembleOperator {
    public static final String PARAMETER_MIN_COUNT_FACTOR = "Min. counts factor";
    public static final String PARAMETER_MINIMUM_SUPPORT = "Min. support";

    InputPort prototypesInputPort = getInputPorts().createPort("prototypes", ExampleSet.class);
    //Map which contons list of elements which belong to given batch
    Map<Double, IDataIndex> batchIndexMap = null;
    Iterator<Map.Entry<Double, IDataIndex>> iterator;

    public ISEnsembleProtoPairOperator(OperatorDescription description) {
        super(description);
        prototypesInputPort.addPrecondition(new IsConnectedPrecondition(prototypesInputPort, new ExampleSetPrecondition(getExampleSetInputPort())));
    }

    @Override
    protected void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {
        double minFactor = getParameterAsDouble(PARAMETER_MIN_COUNT_FACTOR);
        int minSupport = getParameterAsInt(PARAMETER_MINIMUM_SUPPORT);
        //boolean detectPureSubsets = getParameterAsBoolean(PARAMETER_DETECT_PURE_SUBSETS);
        ExampleSet prototypeSet = this.prototypesInputPort.getDataOrNull(ExampleSet.class);
        SquareEuclidianDistance measure = new SquareEuclidianDistance();//measureHelper.getInitializedMeasure(exampleSet)
        measure.init(exampleSet);
        //measureHelper.getInitializedMeasure(exampleSet)
        NearestPrototypesSplitter inputModel = new NearestPrototypesSplitterV2(prototypeSet,measure,minFactor,minSupport);
        exampleSet = inputModel.split(exampleSet);
        Attribute attr = exampleSet.getAttributes().findRoleBySpecialName(Attributes.BATCH_NAME).getAttribute();
        //Get all possible pairs and samples which belong to given pair
        batchIndexMap = new HashMap<>();
        int exampleIndex = 0;
        int sampleSize = exampleSet.size();
        for(Example e : exampleSet){
            double batchVal = e.getValue(attr);
            IDataIndex idx = batchIndexMap.computeIfAbsent(batchVal, val -> {
                IDataIndex index = new DataIndex(sampleSize);
                index.setAllFalse();
                return index;
            });
            idx.set(exampleIndex,true);
            exampleIndex++;
        }
        int currentIteration = 0;
        iterator = batchIndexMap.entrySet().iterator();
    }

    @Override
    protected ExampleSet preprocessingMainLoop(ExampleSet trainingSet) throws OperatorException {
        LogService.getRoot().fine("");
        SelectedExampleSet outputSet = new SelectedExampleSet(trainingSet,iterator.next().getValue());
        return outputSet;
    }

    @Override
    public boolean isNextIteration() {
        return iterator.hasNext();
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> oldTypes = super.getParameterTypes();
        Iterator<ParameterType> iterator = oldTypes.iterator();
        while(iterator.hasNext()){
            ParameterType type = iterator.next();
            if (type.getKey().equals(AbstractISEnsembleOperator.PARAMETER_ITERATIOINS)) {
                type.setHidden(true);
                break;
            }
        }
        List<ParameterType> types = new ArrayList<>();
        ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_COUNT_FACTOR, "Factor indicating minimum number of instances in a single batch. It is multiplayed by the max counts.", 0, 1, 0.1);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMUM_SUPPORT, "Minimum number of samples in a single batch. It it has lower number of samples it will be removed and the samples will be redistributed into another batches", 0, Integer.MAX_VALUE, 20);
        types.add(type);
        types.addAll(oldTypes);
        return types;
    }
}

