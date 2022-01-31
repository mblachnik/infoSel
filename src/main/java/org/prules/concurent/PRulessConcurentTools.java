package org.prules.concurent;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import org.prules.operator.learner.tools.genetic.RandomGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class PRulessConcurentTools {

    public static void parEach(ExampleSet exampleSet, Consumer<Example> action) {
        PRulesExecutorService service = PRulesExecutorFactory.getInstance();
        Spliterator<Example> spliterator = exampleSet.spliterator();
        long targetBatchSize = spliterator.estimateSize() / service.getParallelizmLevel();
        new ParEach(null, spliterator, action, targetBatchSize).invoke();
    }

    static class ParEach<T> extends CountedCompleter<Void> {
        final Spliterator<T> spliterator;
        final Consumer<T> action;
        final long targetBatchSize;

        ParEach(ParEach<T> parent, Spliterator<T> spliterator,
                Consumer<T> action, long targetBatchSize) {
            super(parent);
            this.spliterator = spliterator;
            this.action = action;
            this.targetBatchSize = targetBatchSize;
        }

        public void compute() {
            Spliterator<T> sub;
            while (spliterator.estimateSize() > targetBatchSize &&
                    (sub = spliterator.trySplit()) != null) {
                addToPendingCount(1);
                new ParEach<>(this, sub, action, targetBatchSize).fork();
            }
            spliterator.forEachRemaining(action);
            propagateCompletion();
        }
    }

    public static void parFor(ExampleSet trainingSet, int minBatchSize, RandomGenerator random, Consumer<Example> action) {
        List<ExampleSet> subsets = partitionDatasets(trainingSet, minBatchSize, random);
        PRulesExecutorService executorService = PRulesExecutorFactory.getInstance();
        int poolSize = executorService.getParallelizmLevel();
        int requiredPoolSize = trainingSet.size()/minBatchSize;
        poolSize = requiredPoolSize > poolSize ? poolSize : requiredPoolSize;
        List<Callable<Void>> tasks = new LinkedList<>();
        for(ExampleSet subset : subsets){
            tasks.add(()->{
                for(Example e : subset){
                    action.accept(e);
                }
                return null;
            });
        }
    }

    /**
     * This functin takes as input ExampleSet and devides it into subsets based on given parallelizm level. It also
     * determines the required number of parallel threads which are required. THe RandomGenerator is used to force that
     * each time the method is called the input data will be splietted differently. If RnadomGenerator is null then the
     * samples will be divided without randomization.
     * @param trainingSet
     * @param minBatchSize
     * @param random
     * @return
     */
    public static List<ExampleSet> partitionDatasets(ExampleSet trainingSet, int minBatchSize, RandomGenerator random){
        int trainingSetsize = trainingSet.size();
        PRulesExecutorService executorService = PRulesExecutorFactory.getInstance();
        int poolSize = executorService.getParallelizmLevel();
        int requiredPoolSize = trainingSetsize/minBatchSize;
        poolSize = requiredPoolSize > poolSize ? poolSize : requiredPoolSize;
        int batchSize = trainingSetsize > minBatchSize ? trainingSetsize/poolSize : trainingSetsize;
        batchSize = batchSize * poolSize < trainingSetsize ? batchSize : batchSize+1;
        int exampleId = 0;
        int poolId = 0;
        //Generate random samples of indexes so that each time different subsets will be
        int[] randomizeIdx = new int[trainingSetsize];
        for(int i=0;i<trainingSetsize;i++){
            randomizeIdx[i]=i;
        }
        //If random is null the order will be always the same, otherwise the order of samples will be different and depend in random generator
        if (random!=null) {
            for (int i = 0; i < trainingSetsize; i++) {
                int j = random.nextInteger(trainingSetsize);
                int tmp = randomizeIdx[i];
                randomizeIdx[i] = randomizeIdx[j];
                randomizeIdx[j] = tmp;
            }
        }
        //Generate subsets of ExampleSet
        List<ExampleSet> subsets = new ArrayList<>(poolSize);
        for(poolId = 0; poolId<poolSize-1; poolId++){
            int[] idx = new int[batchSize];
            for(int exampleWithinBatchId=0; exampleWithinBatchId<batchSize && exampleId<trainingSet.size(); exampleWithinBatchId++, exampleId++){
                idx[exampleWithinBatchId] = randomizeIdx[exampleId];
            }
            subsets.add(new MappedExampleSet(trainingSet,idx));
        }
        {
            int[] idx = new int[trainingSetsize - exampleId];
            for (int exampleWithinBatchId = 0; exampleWithinBatchId < batchSize && exampleId < trainingSet.size(); exampleWithinBatchId++, exampleId++) {
                idx[exampleWithinBatchId] = randomizeIdx[exampleId];
            }
            subsets.add(new MappedExampleSet(trainingSet, idx));
        }
        return subsets;
    }
}
