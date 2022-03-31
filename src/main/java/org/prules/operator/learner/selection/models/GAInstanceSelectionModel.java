package org.prules.operator.learner.selection.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.operator.performance.evaluator.PerformanceEvaluator;
import org.prules.tools.math.BasicMath;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRGeometricDataCollectionWithIndex;
import org.prules.tools.math.container.knn.KNNFactory;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.ISeq;
import java.util.function.Function;
import io.jenetics.BitGene;
import static io.jenetics.engine.EvolutionResult.*;
import static io.jenetics.engine.Limits.bySteadyFitness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Implementation of the Genetic Algorithms based instance selection.
 * It is based on Jenetics framework
 */
public class GAInstanceSelectionModel extends AbstractInstanceSelectorModel {
    final int liczbaGeneracji;
    ISPRGeometricDataCollectionWithIndex<IInstanceLabels>  model;
    final DistanceMeasure distance;
    final int k;
    final double performanceRatio;
    final PerformanceEvaluator evaluator;


    public GAInstanceSelectionModel(DistanceMeasure distance, int liczbaGeneracji, int k, double performanceRatio, PerformanceEvaluator evaluator) {
        this.liczbaGeneracji = liczbaGeneracji;
        this.distance = distance;
        this.k = k;
        this.performanceRatio =  performanceRatio;
        this.evaluator = evaluator;

    }

    @Override
    public IDataIndex selectInstances(final SelectedExampleSet exampleSet) {
        model = (ISPRGeometricDataCollectionWithIndex)KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, exampleSet, distance);
        boolean[] idx = new boolean[model.size()];
        Arrays.fill(idx,true);
        double acc = costFunction(exampleSet,  idx);
//        Przykładowy podgląd jak sobie wyświetlić wyniki na potrzeby np. debugowania
//        Logger log = Logger.getLogger(this.getClass().getName());
//        log.info("ACC: " + acc);
//
//        return new DataIndex(idx);
//        boolean[] bestChromosome = new boolean[model.size()];
//        Arrays.fill(bestChromosome,true);
//        double acc = costFunction(exampleSet,  idx);
        //start
        Integer[] items = new Integer[exampleSet.size()];
        int i = 0;
        for (Example example : exampleSet){
            items[i] = i;
            i++;
        }
        ISeq<Integer> setForGA = ISeq.of(items);
        final ISProblem instanceSelectionProblem= new ISProblem(setForGA,
                chromosome -> costFunction(exampleSet, chromosome)
        );

        final Engine<BitGene,Double> engine= Engine.builder(instanceSelectionProblem)
                .populationSize(50)
                .survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelector<>())
                .alterers(
                        new Mutator<>(0.115),
                        new SinglePointCrossover<>(0.20)
                ).build();

//        final EvolutionStatistics<Double,?> statistics=EvolutionStatistics.ofNumber();

        final  Phenotype<BitGene,Double> best=engine.stream()
                .limit(bySteadyFitness(15))
                .limit(100)
//                .peek(statistics)
                .collect(toBestPhenotype());


        //stop
        //Przykładowy podgląd jak sobie wyświetlić wyniki na potrzeby np. debugowania
        //Logger log = Logger.getLogger(this.getClass().getName());
        //log.info("ACC: " + acc);


        Object[] tab=best.genotype().chromosome().stream().toArray();
        boolean[] bestChromosome = new boolean[model.size()];

        int l=0;
        for(Object o:tab){
            BitGene b=(BitGene)o;
            bestChromosome[l]=b.booleanValue();
            l++;
        }
        return new DataIndex(bestChromosome);
    }


    private double costFunction(SelectedExampleSet exampleSet,  boolean[] chromosome){
        IDataIndex index = new DataIndex(chromosome);
        double[] predictions = new double[model.size()];
        double[] trueLabels  = new double[model.size()];
        Iterator<Vector> sampleIterator = model.samplesIterator();
        Iterator<IInstanceLabels> labelIterator = model.storedValueIterator();
        int instanceIndex = 0;
        Collection<IInstanceLabels> nn = null;
        while(sampleIterator.hasNext() && labelIterator.hasNext()) {
            Vector instance = sampleIterator.next();
            IInstanceLabels label = labelIterator.next();
//            if (chromosome[instanceIndex]) {
//                index.set(instanceIndex, false); //Turn off sample for which we do prediction
//                nn = model.getNearestValues(k, instance, index);
//                index.set(instanceIndex, true); //Turn bck on sample for which we do prediction
//            } else {
//                nn = model.getNearestValues(k, instance, index);
//            }

            nn = model.getNearestValues(k, instance, index);
            predictions[instanceIndex] = nn.iterator().next().getLabel();
            trueLabels[instanceIndex] = label.getLabel();
            instanceIndex++;
        }
        double accuracy = evaluator.getPerformance(trueLabels,predictions);
        double compression = 1 - BasicMath.sum(chromosome) / chromosome.length;
        double performance = accuracy *  performanceRatio + (1-performanceRatio) * compression;
        return performance;
    }


}
class ISProblem implements Problem<ISeq<Integer>, BitGene,Double>{

    private final Codec<ISeq<Integer>,BitGene> _codec;
    public ISeq<Integer> items;
    public int dataSize;
    Function<boolean[], Double> costFunction;

    public ISProblem(final ISeq<Integer> items, Function<boolean[], Double> costFunction) {
        _codec = Codecs.ofSubSet(items);
        this.items = items;
        dataSize = items.size();
        this.costFunction = costFunction;
    }

    @Override
    public Function<ISeq<Integer>, Double> fitness() {
        return items->{
            boolean[] chromosome=new boolean[dataSize];
            for(Integer i: items){
                chromosome[i]=true;
            }
            return costFunction.apply(chromosome);
        };
    }

    @Override
    public Codec<ISeq<Integer>, BitGene> codec() {
        return _codec;
    }
}
