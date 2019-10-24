package org.prules.operator.learner.prototype.model;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.ExampleSetUtilities;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.PredictionModel;
import org.prules.operator.learner.prototype.PrototypeTuple;
import org.prules.operator.learner.prototype.PrototypesEnsembleModel;
import org.prules.operator.learner.prototype.PrototypesEnsemblePredictionModel;
import org.prules.operator.learner.prototype.model.interfaces.BatchLoopInterface;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

import java.util.HashMap;
import java.util.Map;

public class BasicBatchLoopModel extends AbstractBatchLoopModel {
    //<editor-fold desc="Private fields" defaultState="collapsed" >
    /**
     * Map which cantons list of elements which belong to given batch
     */
    private Map<Long, PredictionModel> modelsMap;
    /**
     * Map of pair Id to Data Index
     */
    private Map<Long, IDataIndex> pairsMap;
    /**
     * Attributes of Batch
     */
    private Attribute attribute;
    /**
     * Callback used when training experts
     */
    private BatchLoopInterface callback;
    //</editor-fold>

    //<editor-fold desc="Constructor" defaultState="collapsed" >

    /**
     * Constructor for AbstractNearestProtoModel
     *
     * @param examples - training examples
     * @param model    - model to be used to
     * @param callback - callback used to train experts
     */
    public BasicBatchLoopModel(ExampleSet examples, PrototypesEnsembleModel model, BatchLoopInterface callback) {
        super(examples, model);
        this.callback = callback;
        //Initialize maps
        modelsMap = new HashMap<>();
        pairsMap = new HashMap<>();
    }
    //</editor-fold>

    //<editor-fold desc="Compute stage" defaultState="collapsed" >

    /**
     * Gets all possible pairs and samples which belong to given pair
     */
    private void mapPairs() {
        int exampleIndex = 0;
        IDataIndex idx;
        for (Example example : getExamples()) {
            double pairId = example.getValue(attribute);
            if (!pairsMap.containsKey((long) pairId)) {
                idx = new DataIndex(getExamples().size());
                idx.setAllFalse();
                pairsMap.put((long) pairId, idx);
            }
            pairsMap.get((long) pairId).set(exampleIndex, true);
            exampleIndex++;
        }
    }

    /**
     * Train experts in inner sub process
     */
    private void trainExperts() throws OperatorException {
        for (Map.Entry<Long, PrototypeTuple> entry : getModel().getSelectedPairs().entrySet()) {
            long pair = entry.getKey();
            if (pairsMap.containsKey(pair)) {
                IDataIndex idx = pairsMap.get(pair);

                //Select samples from given batch
                SelectedExampleSet selectedExampleSet = new SelectedExampleSet(getExamples());
                selectedExampleSet.setIndex(idx);
                PredictionModel model = callback.trainExpert(selectedExampleSet);
                modelsMap.put(pair, model);
            }
        }
    }

    @Override
    public void process() throws OperatorException {
        attribute = getExamples().getAttributes().findRoleBySpecialName(Attributes.BATCH_NAME).getAttribute();
        mapPairs();
        trainExperts();
    }

    @Override
    public PredictionModel retrieveModel() {
        return new PrototypesEnsemblePredictionModel(getModel(), modelsMap, getExamples(),
                ExampleSetUtilities.SetsCompareOption.ALLOW_SUPERSET, ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS);
    }
    //</editor-fold>
}
