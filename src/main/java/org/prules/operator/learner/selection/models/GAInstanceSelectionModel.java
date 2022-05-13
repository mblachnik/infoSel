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

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import io.jenetics.BitGene;
import static io.jenetics.engine.EvolutionResult.*;
import static io.jenetics.engine.Limits.bySteadyFitness;

/**
 * Implementation of the Genetic Algorithms based instance selection.
 * It is based on Jenetics framework
 */
public class GAInstanceSelectionModel extends AbstractInstanceSelectorModel {
    //Parameters for RapidMiner GUI
    final int numberOfGenerations;
    final int k;
    final double performanceRatio;
    final int populationSize;
    final int tournamentSelectorSize;
    final double singlePointCrossoverProbability;
    final double mutationProbability;
    final int limitBySteadyFitness;
    final double offspringFraction;
    final int numOfCrossoverPoints;

    //Internal parameters
    final PerformanceEvaluator evaluator;
    final Map<String,List<Double>> costFunctionPerformance;
    ISPRGeometricDataCollectionWithIndex<IInstanceLabels>  model;
    final DistanceMeasure distance;

    public GAInstanceSelectionModel(DistanceMeasure distance, int numberOfGenerations, int k, double performanceRatio, PerformanceEvaluator evaluator,
                                    int populationSize, int tournamentSelectorSize, double singlePointCrossoverProbability, double mutationProbability,
                                    int limitBySteadyFitness, double offspringFraction, int numOfCrossoverPoints) {
        this.populationSize=populationSize;
        this.tournamentSelectorSize=tournamentSelectorSize;
        this.singlePointCrossoverProbability=singlePointCrossoverProbability;
        this.mutationProbability=mutationProbability;
        this.limitBySteadyFitness=limitBySteadyFitness;
        this.offspringFraction=offspringFraction;
        this.numOfCrossoverPoints=numOfCrossoverPoints;

        this.numberOfGenerations = numberOfGenerations;
        this.distance = distance;
        this.k = k;
        this.performanceRatio =  performanceRatio;
        this.evaluator = evaluator;
        this.costFunctionPerformance = new HashMap<String,List<Double>>(){{
            put("Performance", Collections.synchronizedList(new ArrayList(numberOfGenerations)));
            put("Accuracy", Collections.synchronizedList(new ArrayList(numberOfGenerations)));
            put("Compression", Collections.synchronizedList(new ArrayList(numberOfGenerations)));
        }};
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
                .executor(Runnable::run)
                .populationSize(populationSize)
                .offspringFraction(offspringFraction)
                .survivorsSelector(new TournamentSelector<>(tournamentSelectorSize))
                .offspringSelector(new RouletteWheelSelector<>())
                .alterers(
                        new Mutator<>(mutationProbability),
                        new MultiPointCrossover<>(singlePointCrossoverProbability, numOfCrossoverPoints)
                ).build();

        final EvolutionStatistics<Double,?> statistics=EvolutionStatistics.ofNumber();



        final  Phenotype<BitGene,Double> best=engine.stream()
               // .limit(bySteadyFitness(limitBySteadyFitness))
//                .peek(r-> System.out.println("########CURRENT GEN: "
//                        +r.generation()+
//                        ": "+ r.totalGenerations()+
//                        ": "+r.bestPhenotype()+
//                        " ALTERED: "+r.alterCount()+
//                        " INVALID: "+r.invalidCount()+
//                        " GENOTYPE: "+r.genotypes()))
//                .peek(r-> {
//                    try {
//                        FileWriter fileWriter = new FileWriter("D:/INTELLIJ ULTIMATE/ZAPISY INTELLIJ ULTIMATE/jeneticsGenotype.txt",true);
//                        String output=r.genotypes()+"#####\n";
//                        String toWrite=output.replaceAll("\\[|\\]|\\|","");
//                        toWrite=toWrite.replaceAll(",","\n");
//                        int countOnes=0;
//                        for(int c=0;c<toWrite.length();c++){
//                            if(toWrite.charAt(c)=='1')countOnes++;
//                        }
//                        fileWriter.write(toWrite+"NumOfOnes: "+countOnes+"\n");
//                        fileWriter.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                })
                .limit(numberOfGenerations)
                .peek(statistics)
                .collect(toBestPhenotype());


//        try {
//            FileWriter fileWriter = new FileWriter("D:/INTELLIJ ULTIMATE/ZAPISY INTELLIJ ULTIMATE/jeneticsGenotype.txt",true);
//            fileWriter.write("NUMBER OF GENERATIONS: "+ numberOfGenerations+"#########\n\n\n");
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

       // System.out.println("STATISTICS: "+statistics);
       // System.out.println(statistics);

        //stop
        //Przykładowy podgląd jak sobie wyświetlić wyniki na potrzeby np. debugowania
        //Logger log = Logger.getLogger(this.getClass().getName());
        //log.info("ACC: " + acc);


  //      System.out.println("##############BEST: "+best.generation()+best);

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

   // int debugCounter=1;

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
        double compression = 1.0-((double)BasicMath.sum(chromosome)) / chromosome.length;
        double performance = accuracy *  performanceRatio + (1-performanceRatio) * compression;

        costFunctionPerformance.get("Performance").add(performance);
        costFunctionPerformance.get("Accuracy").add(accuracy);
        costFunctionPerformance.get("Compression").add(compression);
      //  System.out.println("COSTFUNCTION:"+debugCounter++ +": "+performance);

        return performance;
    }

    /**
     * Returns values of the performance of the GA model after each iteration including accuraacy and compression.
     * If the GA algorithm was not executed it returns null
     * @return
     */
    public Map<String,List<Double>> getCostFunctionPerformance() {
        return costFunctionPerformance;
    }

    public int getnumberOfGenerations() {
        return numberOfGenerations;
    }

    public int getK() {
        return k;
    }

    public double getPerformanceRatio() {
        return performanceRatio;
    }

    public PerformanceEvaluator getEvaluator() {
        return evaluator;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getTournamentSelectorSize() {
        return tournamentSelectorSize;
    }

    public double getSinglePointCrossoverProbability() {
        return singlePointCrossoverProbability;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    public int getLimitBySteadyFitness() {
        return limitBySteadyFitness;
    }

    public double getOffspringFraction() {
        return offspringFraction;
    }

    public int getNumOfCrossoverPoints() {
        return numOfCrossoverPoints;
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

   // int debugFitnessCounter=1;
    @Override
    public Function<ISeq<Integer>, Double> fitness() {
     //   System.out.println("FITNESS: "+ debugFitnessCounter++);
        return items->{
            boolean[] chromosome=new boolean[dataSize];
            for(Integer i: items){
                chromosome[i]= true;
            }
            return costFunction.apply(chromosome);
        };
    }

    @Override
    public Codec<ISeq<Integer>, BitGene> codec() {
        return _codec;
    }
}
