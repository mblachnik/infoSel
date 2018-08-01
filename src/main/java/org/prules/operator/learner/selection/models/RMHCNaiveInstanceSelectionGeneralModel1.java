/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.EditedExampleSet;
import com.rapidminer.example.set.ISPRExample;
import com.rapidminer.example.set.SelectedExampleSet;
import org.prules.dataset.Const;
import org.prules.operator.learner.selection.models.decisionfunctions.IISDecisionFunction;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.tools.math.container.knn.KNNTools;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollection;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.prules.dataset.InstanceFactory;
import org.prules.tools.math.container.knn.KNNFactory;
import java.util.Set;
import org.prules.dataset.IInstanceLabels;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.dataset.Instance;
import org.prules.dataset.Vector;
import org.prules.dataset.IInstancePrediction;
import org.prules.dataset.InstanceLabels;
import static org.prules.tools.math.container.knn.KNNTools.t;

/**
 * Naive implementation of RMHC algorithm. Here instead of binary coding of
 * instances we use a table of int, and each instance is represented as its id.
 * When performing a change in the example set instead of bit mutation a single
 * new prototype is determined. This requires more memory, but is much simpler
 * and intuitive to understand. Mutating a bit infact is equivalent to selecting
 * new instance. In binary coding the problem is that not all bits are equally
 * likely to represent certain value (instance) for the value of 128 the oldest
 * bit is set to 1 only once over 128 cases so it makes a problem for mutation
 * because it is high chance 127/128 that the selected instance will be out of
 * range. In natural integer coding this problem does not appear
 *
 * @author Marcin
 */
public class RMHCNaiveInstanceSelectionGeneralModel1 extends AbstractInstanceSelectorModel {

    private final DistanceMeasure measure;
    private final int numberOfPrototypes;
    private final RandomGenerator randomGenerator;
    private final int iterations;
    private final IISDecisionFunction loss;

    /**
     *Constructor of RMHC algorithm implementation. Here instead of binary coding of
     * instances we use a table of int, and each instance is represented as its
     * id. When performing a change in the example set instead of bit mutation a
     * single new prototype is determined. This requires more memory, but is
     * much simpler and intuitive to understand. Mutating a bit infact is
     * equivalent to selecting new instance. In binary coding the problem is
     * that not all bits are equally likely to represent certain value
     * (instance) for the value of 128 the oldest bit is set to 1 only once over
     * 128 cases so it makes a problem for mutation because it is high chance
     * 127/128 that the selected instance will be out of range. In natural
     * integer coding this problem does not appear
     *
     * @param measure
     * @param populationSize
     * @param iterations
     * @param randomGenerator
     * @param loss
     */
    public RMHCNaiveInstanceSelectionGeneralModel1(DistanceMeasure measure, int populationSize, int iterations, RandomGenerator randomGenerator, IISDecisionFunction loss) {
        this.measure = measure;
        this.randomGenerator = randomGenerator;
        this.numberOfPrototypes = populationSize;
        this.iterations = iterations;
        this.loss = loss;
    }

    /**
     * Performs instance selection
     *
     * @param exampleSet - example set for which instance selection will be
     * performed
     * @return - index of selected examples
     */
    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        //loss.init(exampleSet);
        //TODO make use of the loss function, because it is not used now
        //TODO Update the code, because now it repeates the iterations if selected instance is out of range (based on bit enciding) and this may not be good. Moreover it may happen that certain instance is selected more then once, so the final number of samples mey not be equalt to the number of prototypes which should be selected 
        //initialization                
        int[] selectedInstances = new int[numberOfPrototypes];
        IDataIndex workingIndex = exampleSet.getIndex();
        int size = exampleSet.size();                       
        workingIndex.setAllFalse();
        //Initialize
        int positionToChange    = -1; //a value in range 0-selectedInstances.length-1. It is index of element of selectedInstances array which we are going to change
        int oldSelectedInstance = -1; //index of example in traiing set - old value
        int newSelectedInstance = -1; //index of example in traiing set - new value
        
        List<Integer> rndInts = new ArrayList<>(size);        
        for(int i=0; i<size; i++){
            rndInts.add(i);
        }        
        Collections.shuffle(rndInts,randomGenerator);        
        for (int i = 0; i < numberOfPrototypes; i++) {            
            newSelectedInstance = rndInts.get(i);            
            selectedInstances[i] = newSelectedInstance;
            workingIndex.set(newSelectedInstance,true);            
        }                        
        //Create faster dataset
        List<Vector> samples = new ArrayList<>(size);
        List<IInstanceLabels> labels = new ArrayList<>(size);
        for(Example e : exampleSet){
            samples.add(InstanceFactory.createVector(e));
            labels.add(InstanceFactory.createInstaceLabels(e));
        }
        IInstancePrediction prediction = InstanceFactory.createPrediction(Double.NaN, null);                               
        loss.init(exampleSet,measure);
        double errorRateBest = Double.MAX_VALUE;        
        Instance inst = InstanceFactory.createEmptyInstance();
        for (int i = 0; i < iterations; i++) {
            double errorRate = 0;
            int q=0;            
            //Calculate performance            
            Iterator<Vector> sampleIterator = samples.iterator();
            Iterator<IInstanceLabels> labelIterator = labels.iterator();
            while (sampleIterator.hasNext() && labelIterator.hasNext()) {                   
                Vector currentSample = sampleIterator.next();
                IInstanceLabels curentLabel = labelIterator.next();
                double predictedLabel = KNNTools.predictOneNearestNeighbor(currentSample,samples,labels,measure, workingIndex);                
                prediction.setLabel(predictedLabel);                
                inst.setPrediction(prediction);
                inst.setLabels(curentLabel);
                inst.setVector(currentSample);
                double lossVal = loss.getValue(inst);
                q ++;
                errorRate += lossVal;
            }                 
            //Asses current set
            if (errorRate < errorRateBest) {
                errorRateBest = errorRate;                                
            } else { //We have to restore old current Vector and Label in place of candidate                
                workingIndex.set(oldSelectedInstance, true );
                workingIndex.set(newSelectedInstance, false);                
                selectedInstances[positionToChange] = oldSelectedInstance;
            }
            //Choose which prototype to change            
            positionToChange = randomGenerator.nextInt(numberOfPrototypes);
            //Get new prototype id and assure that the value is different from the previous one
            oldSelectedInstance = selectedInstances[positionToChange];                         
            rndInts.clear();
            for(int k=0; k<size; k++){
                if (!workingIndex.get(k)){
                    rndInts.add(k);
                }
            }
            Collections.shuffle(rndInts,randomGenerator);                    
            newSelectedInstance = rndInts.get(0);                        
            workingIndex.set(oldSelectedInstance, false);
            workingIndex.set(newSelectedInstance, true);                
            selectedInstances[positionToChange] = newSelectedInstance;                                                                                            
            //System.out.println("--- NextIter ------");
        }     
        //System.out.println("--" + t[0] + "  "+ t[1] + "  "+ t[2] + "  "+ t[3] + "  "+ t[4] + "  ");
        //Arrays.fill(t,0);
        return workingIndex;
    }

    private void disp(ISPRGeometricDataCollection<Number> kNN, int[] si, SelectedExampleSet es) {
        for (int i = 0; i < si.length; i++) {
            double[] values = kNN.getSample(i).getValues();
            int in = si[i];
            System.out.println("Indeks = " + in);
            System.out.print(" [");
            
            for (int j = 0; j < values.length; j++) {
                System.out.print(values[j] + " ; ");
            }
            System.out.print("] ");
            Iterator it = es.iterator();
            ISPRExample ex;
            do {

                ex = (ISPRExample) it.next();
            } while (ex.getIndex() != in);
            KNNTools.extractExampleValues(ex, values);
            double id = ex.getId();
            String ids = es.getAttributes().getId().getMapping().mapIndex((int) id);
            in = ex.getIndex();
            System.out.println("Indeks = " + in);
            System.out.print("  " + ids + " [");
            for (int j = 0; j < values.length; j++) {
                System.out.print(values[j] + " ; ");
            }
            System.out.print("] ");
            System.out.println("");
        }
        System.out.println("------------------------------------------------");
    }

    
}
