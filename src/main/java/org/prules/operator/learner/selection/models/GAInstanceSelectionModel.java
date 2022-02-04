package org.prules.operator.learner.selection.models;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;
import org.prules.tools.math.container.knn.KNNFactory;

/**
 * Implementation of the Genetic Algorithms based instance selection.
 * It is based on Jenetix framework
 */
public class GAInstanceSelectionModel extends AbstractInstanceSelectorModel{
    int liczbaGeneracji;
    private ISPRGeometricDataCollectionWithIndex<IInstanceLabels>  model;
    DistanceMeasure distance;

    public GAInstanceSelectionModel(DistanceMeasure distance, int liczbaGeneracji) {
        this.liczbaGeneracji = liczbaGeneracji;
        this.distance = distance;
    }

    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        model = (ISPRGeometricDataCollectionWithIndex)KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, distance);
        IDataIndex dataIndex = exampleSet.getIndex();

        boolean[] idx = new boolean[dataIndex.size()];


        return dataIndex;
    }


//    private double costFunction(SelectedExampleSet exampleSet, boolean[] index){
//        //TODO: dokończyć - tutaj można wykorzystać dataIndex i wstrzykiwać go do model
//        IDataIndex indexSwitchOfClassifiedSample = exampleSet.getIndex();
//        indexSwitchOfClassifiedSample.setAllTrue();
//        Iterator<Vector> sampleIterator = model.samplesIterator();
//        Iterator<IInstanceLabels> labelIterator = model.storedValueIterator();
//        double dokladnosc = 0;
//        int instanceIndex = 0;
//        while(sampleIterator.hasNext() && labelIterator.hasNext()) {
//            Vector instance = sampleIterator.next();
//            IInstanceLabels label = labelIterator.next();
//            indexSwitchOfClassifiedSample.set(instanceIndex,false); //Turn off sample for which we do prediction
//            Collection<IInstanceLabels> nn = model.getNearestValues(1, instance, indexSwitchOfClassifiedSample);
//            indexSwitchOfClassifiedSample.set(instanceIndex,true); //Turn bck on sample for which we do prediction
//            double predLlab = nn.iterator().next().getLabel();
//            double trueLabel = label.getLabel();
//            if (predLlab == trueLabel){
//                dokladnosc++;
//            }
//        }
//        dokladnosc = dokladnosc/model.size();
//        return dokladnosc;
//    }
}
