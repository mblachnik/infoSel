package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.prules.concurent.PRulesExecutorFactory;
import org.prules.concurent.PRulesExecutorService;
import org.prules.concurent.PRulessConcurentTools;
import org.prules.operator.learner.PRulesModel;
import org.prules.operator.learner.tools.genetic.RandomGenerator;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * A basic class for any LVQ implementation.
 * To implement new algorithm it is enough to implement the update(...) and isNextIteration(..) methods.
 * If you need you can also use the beforeTraining(...) and afterTraining(...) methods to perform some initialization steps
 * and postprocessing. If the isParallelizable() is set to true note that you have to assure that the update(...) function
 * is thread safe becouse it can be called concurrently by multiple threads. The remaining methods are executed from a single
 * thread so they are safe.
 * Note that when random==null the calculations are much faster but the results can be worse.
 * @author Marcin
 */
public abstract class AbstractLVQModel implements PRulesModel<ExampleSet> {

    public static final String LEARNING_RATE_KEY = "LearningRate";
    public static final String LAMBDA_RATE_KEY = "LambdaRate";
    
    private final ExampleSet prototypes; //Collection of prototypes
//    private Example example; //Current example from the example set
//    private double[] exampleValues; //double values retrived from the example set
//    private double exampleLabel; //Current label
    //private Attributes prototypeAttributes; //List of attributes
    private List<Attribute> attributesOrderedList; //List of attributes in order common to prototypes and training Set.
    private final int attributesSize; //number of attributes
    private final int numberOfPrototypes; //Number of prototypes
    private double[][] prototypeValues;
    private double[] prototypeLabels;
    Map<String, Object> storedValues;

    private int minBatchSize = 1000;
    RandomGenerator random = null;

    /**
     *
     * @param prototypes
     */
    public AbstractLVQModel(ExampleSet prototypes) {
        Attributes prototypeAttributes = prototypes.getAttributes();
        this.prototypes = prototypes;
        attributesSize = prototypeAttributes.size();
        numberOfPrototypes = prototypes.size();
        storedValues = new HashMap<>();
    }

    /**
     *
     * @param trainingSet
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet trainingSet) {
        //======================= INITIALIZATION
        Attributes tmpTrainingAttributes = trainingSet.getAttributes();
        Attributes prototypeAttributes = prototypes.getAttributes();
        attributesOrderedList = new ArrayList<Attribute>(tmpTrainingAttributes.size());
        //Synchronize order of attributes for both prototypes and trainingSet in case the order of attributes is different.
        //Here we always use the order of prototypes
        //TODO: add error handling in case attributes do not match
        for (Attribute a : prototypeAttributes) {
            attributesOrderedList.add(tmpTrainingAttributes.get(a.getName()));
        }
        //Caching codebooks for faster optimization
        prototypeValues = new double[numberOfPrototypes][attributesSize];
        prototypeLabels = new double[numberOfPrototypes];
        int i = 0, j = 0;
        for (Example p : prototypes) {
            j = 0;
            for (Attribute a : prototypeAttributes) {
                prototypeValues[i][j] = p.getValue(a);
                j++;
            }
            prototypeLabels[i] = p.getLabel();
            i++;
        }
        double[] exampleValues = new double[prototypeAttributes.size()];
        double exampleLabel;

        //Prepare for parallelizm
        PRulesExecutorService executorService = null;
        double[][][] prototypesForParallelizm = null;
        int poolSize = 1;
        List<ExampleSet> subsets = null;
        if (isParallelizable()) {
            executorService = PRulesExecutorFactory.getInstance();
            poolSize = executorService.getParallelizmLevel();
            int requiredPoolSize = trainingSet.size()/minBatchSize;
            poolSize = requiredPoolSize > poolSize ? poolSize : requiredPoolSize;
            int batchSize = trainingSet.size() > minBatchSize ? trainingSet.size()/poolSize : trainingSet.size();
            batchSize = batchSize * poolSize < trainingSet.size() ? batchSize : batchSize+1;
            prototypesForParallelizm = new double[poolSize][getNumberOfPrototypes()][getAttributesSize()];
            if (random==null)
                subsets = PRulessConcurentTools.partitionDatasets(trainingSet,minBatchSize,random);
        }
        //================= MAIN PROCEDURE
        beforeTraining(trainingSet);
        do {
            if (!isParallelizable() || poolSize <= 1) { //Single thread implementation
                for (Example trainingExample : trainingSet) {
                    //example = trainingExample;
                    j = 0;
                    for (Attribute attribute : attributesOrderedList) {
                        exampleValues[j] = trainingExample.getValue(attribute);
                        j++;
                    }
                    exampleLabel = trainingExample.getLabel();
                    update(prototypeValues, prototypeLabels, exampleValues, exampleLabel, trainingExample);
                }
            } else { //Parallel implementation
                propagatePrototypes(prototypesForParallelizm); //Create prototypesForParallelizm
                if (random!=null)
                    subsets = PRulessConcurentTools.partitionDatasets(trainingSet,minBatchSize,random);
                //Create tasks
                List<Callable<Void>> tasks = new ArrayList<>(poolSize);
                int poolId = 0;
                for(ExampleSet localSet : subsets){
                    double[][] localPrototypes = prototypesForParallelizm[poolId];
                    tasks.add(()->{
                        double[] localExampleValues = new double[getAttributesSize()];
                        for(Example e : localSet){
                            int jj = 0;
                            for (Attribute attribute : attributesOrderedList) {
                                localExampleValues[jj] = e.getValue(attribute);
                                jj++;
                            }
                            double localExampleLabel = e.getLabel();
                            update(localPrototypes, prototypeLabels, localExampleValues, localExampleLabel, e);
                        }
                        return null;
                    });
                    poolId++;
                }
                //Execute tasks
                try {
                    executorService.invokeAll(tasks);
                    //context.invokeAll(tasks);
                    //context.call(tasks);
                } catch (InterruptedException e) {
                //} catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //Aggregate obtained prototypes
                aggregatePrototypes(prototypesForParallelizm);
            }
        } while (isNextIteration(trainingSet));
        
        afterTraining(trainingSet);

        //=============== FINALIZATION
        i = 0;
        for (Example p : prototypes) {
            j = 0;
            for (Attribute a : prototypeAttributes) {
                p.setValue(a, prototypeValues[i][j]);
                j++;
            }
            i++;
        }
        return prototypes;
    }

    //Get prototypes and populate its values so that each task has its local copy
    private void propagatePrototypes(double[][][] prototypesForParallelizm){
        //TODO to trzeba zaimplementować aby całość działała
        int numOfCopies = prototypesForParallelizm.length;
        int numOfPrototypes = prototypesForParallelizm[0].length;
        for(int j=0; j<numOfPrototypes; j++) {
            for (int i = 0; i < numOfCopies; i++) {
                System.arraycopy(prototypeValues[j],0,prototypesForParallelizm[i][j],0,this.attributesSize);
            }
        }
    }

    //Get prototypes obtained in each task and update the true prototypes as an average
    private void aggregatePrototypes(double[][][] prototypesForParallelizm){
        int poolSize = prototypesForParallelizm.length;
        //Fill current prototypes with 0
        for(int j=0; j<prototypeValues.length; j++) {
            Arrays.fill(prototypeValues[j], 0);
        }
        //Aggregate prototypes
        for(int i=0; i<poolSize; i++){ //Iterate over each susbet of prototypes
            double[][] currentParPrototypes = prototypesForParallelizm[i];
            for(int j=0; j<prototypeValues.length; j++){ //Iterate over each prototype
                double[] parPrototype = currentParPrototypes[j];
                double[] currentPrototype = prototypeValues[j];
                for(int k=0; k<currentPrototype.length; k++){ //Iterate over each attribute
                    currentPrototype[k] += parPrototype[k];
                }
            }
        }
        //Dividing by the denominator of the average
        for(int j=0; j<prototypeValues.length; j++){ //
            double[] currentPrototype = prototypeValues[j];
            for(int k=0; k<currentPrototype.length; k++){
                currentPrototype[k] /=  poolSize;
            }
        }
    }
    /**
     * Returns total number of iterations (maximum number of iterations)
     *
     * @return
     */
    abstract public int getMaxIterations();

    /**
     * Returns current iteration
     *
     * @return
     */
    abstract public int getIteration();

    /**
     * Returns the value of cost function
     *
     * @return
     */
    abstract public double getCostFunctionValue();

    /**
     * Returns list of cost function values
     *
     * @return
     */
    abstract public List<Double> getCostFunctionValues();

//    /**
//     *
//     * @return
//     */
//    protected Example getCurrentExample() {
//        return example;
//    }
//
//    /**
//     *
//     * @return
//     */
//    protected double[] getCurrentExampleValues() {
//        return exampleValues;
//    }


    protected List<Attribute> getAttributes() {
        return attributesOrderedList;
    }

    /**
     *
     * @return
     */
    protected int getAttributesSize() {
        return attributesSize;
    }

    /**
     *
     * @return
     */
    protected int getNumberOfPrototypes() {
        return numberOfPrototypes;
    }

    /**
     *
     * @param i
     * @return
     */
    protected double[] getPrototypeAsDouble(int i) {
        return prototypeValues[i];
    }

    /**
     *
     * @param i
     * @return
     */
    protected double getPrototypeLabel(int i) {
        return prototypeLabels[i];
    }

    /**
     *
     * @return
     */
    abstract boolean isNextIteration(ExampleSet trainingSet);
    
    /**
     * Method executed before the training starts.
     * @param trainingSet 
     */
    public void beforeTraining(ExampleSet trainingSet){        
    }
    
    /**
     * Method executed then the main loop of the LVQ algorithm is finished.
     * @param trainingSet 
     */
    public void afterTraining(ExampleSet trainingSet){        
    }

    /**
     *
     */
    abstract void update(double[][] prototypeValues, double[] prototypeLabels, double[] exampleValues, double exampleLabel, Example example);

    public final Object getStoredValue(String key) {        
        return storedValues.get(key);
    }
    
    public final void addStoredValue(String key, Object value){        
        storedValues.put(key,value);
    }

    /**
     *  Returns value indicating if a given implementation can be parallelized
     *  This method should be implemented by the inheriting classes and can be used to indicate if the parallelized
     *  version of the main algorithm can be used. If the value is true then the parallelized implementation can be used,
     *  if false then the serial version will be forced.
     *  When implementing new algorithm note that if you want to achieve parallelizm the update function is execued from
     *  multpiple threads so if you want to update some parameters they should be synchronized!!!!
     *  For that reason the paralelized iimplementatin work on copies of prototypes which are synchronized after each iteration oover training samples
     * @return
     */
    public boolean isParallelizable(){ return false; };

    /**
     * Returns RandomGenerator used by the LVQ algorithm
     * @return
     */
    public RandomGenerator getRandom() {
        return random;
    }

    /**
     * Sets RandomGenerator used by the LVQ algorithm. The RandomGenerator is used for randomizing order of samples which
     * are used for training but only for Parralell implementaion in order to prevent the same subsets of samples.
     * @param random
     */
    public void setRandom(RandomGenerator random) {
        this.random = random;
    }

    public int getMinBatchSize() {
        return minBatchSize;
    }

    public void setMinBatchSize(int minBatchSize) {
        this.minBatchSize = minBatchSize;
    }
}
