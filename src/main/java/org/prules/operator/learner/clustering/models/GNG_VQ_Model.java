package org.prules.operator.learner.clustering.models;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.mixed.MixedEuclideanDistance;
import org.prules.operator.learner.clustering.gng.NeuronNode;
import org.prules.operator.learner.clustering.models.gng.comparators.DistanceComparator;
import org.prules.operator.learner.clustering.models.gng.comparators.LocalErrorComparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Łukasz Migdałek on 2016-07-29.
 */
public class GNG_VQ_Model extends AbstractVQModel {

    private final Random random = new Random();
    private final MixedEuclideanDistance euclideanDistance;
    private int iterations;
    private int internalLoopIteration;
    private int currentIteration;
    // FIXME: 2016-08-02 to constructor
    private int lambda;
    private int maxAge;
    private int maxNeurons;
    private double beta;
    private List<NeuronNode> neurons = new ArrayList<>();

    private double alpha;
    private DistanceMeasure measure;

    // Constant used for calculating new weights for winner (eb) and neighbours (en).
    private double eb;
    private double en;

    /**
     * Constructor which requires initialization of prototypes/codebooks
     *
     * @param prototypes
     */
    public GNG_VQ_Model(ExampleSet prototypes, int iterations, DistanceMeasure measure, int maxNeurons, double alpha, int lambda, double beta, double eb, double en, int maxAge) throws OperatorException {
        super(prototypes);
        this.internalLoopIteration = 0;
        this.iterations = iterations;
        this.currentIteration = 0;
        this.alpha = alpha;
        this.measure = measure;
        this.measure.init(prototypes);
        this.lambda = lambda;
        this.euclideanDistance = new MixedEuclideanDistance();
        this.euclideanDistance.init(prototypes);
        this.maxNeurons = maxNeurons;
        this.beta = beta;
        this.eb = eb;
        this.en = en;
        this.maxAge = maxAge;
    }

    /**
     * Execute the training process with training data as an input
     *
     * @param trainingSet - input training data
     * @return
     */
    @Override
    public ExampleSet run(ExampleSet trainingSet) {
        Attributes tmpTrainingAttributes = trainingSet.getAttributes();
        trainingAttributes = new ArrayList<Attribute>(tmpTrainingAttributes.size());
        //Caching codebooks for faster optimization
        prototypeValues = new double[numberOfPrototypes][attributesSize];
        int i = 0, j = 0;
        for (Example p : prototypes) {
            j = 0;
            for (Attribute a : prototypeAttributes) {
                prototypeValues[i][j] = p.getValue(a);
                j++;
            }
            i++;
        }
        //Reordering attributes for
        for (Attribute a : prototypeAttributes) {
            trainingAttributes.add(tmpTrainingAttributes.get(a.getName()));
        }
        exampleValues = new double[prototypeAttributes.size()];
        int exampleNumber;
        do {
            internalLoopIteration = 0;
            // Needed for GNG
            exampleNumber = 0;
            for (Example trainingExample : trainingSet) {
                exampleNumber++;
                this.example = trainingExample;
                j = 0;
                for (Attribute attribute : trainingAttributes) {
                    exampleValues[j] = trainingExample.getValue(attribute);
                    j++;
                }
                update();
            }
        } while (nextIteration());

        List<Attribute> attributesList = new ArrayList<>();
        for (Attribute attr : prototypeAttributes) {
            attributesList.add(attr);
        }

        ExampleTable codeBooksTable = new MemoryExampleTable(attributesList, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), neurons.size());
        ExampleSet codeBooksSet = new SimpleExampleSet(codeBooksTable, attributesList);
        Iterator<Example> codeBookExampleIterator = codeBooksSet.iterator();
        //Rewrite codeBooks to codeBooks ExampleSet
        Attributes codeBookAttributes = codeBooksSet.getAttributes();
        int codeBookIndex = 0;

        while (codeBookExampleIterator.hasNext()) {
            Example codeBookExample = codeBookExampleIterator.next();
            i = 0;
            if (codeBookExample != null) {
                for (Attribute a : codeBookAttributes) {
                    codeBookExample.setValue(a, prototypeValues[codeBookIndex][i]);
                    i++;
                }
//            codeBookExample.setLabel(codeBookIndex);

            }
            codeBookIndex++;

        }
        return codeBooksSet;
    }

    @Override
    public void update() {
        // If collection empty then init.
        if (neurons.isEmpty()) {
            for (double[] values : prototypeValues) {
                NeuronNode neuron = new NeuronNode(values, maxAge);
                neurons.add(neuron);
            }
        }
        calcDistances();

        // Sort by distance
        neurons.sort(new DistanceComparator());

        if (neurons.size() >= 2) {
            NeuronNode winner = neurons.get(0);
            NeuronNode nearestToWinner = neurons.get(1);

            // Add connection and reset age between neurons.
            winner.addNeighbor(nearestToWinner);

            for (int i = 0; i < getAttributesSize(); i++) {
                double winnerValue = winner.getValues()[i];
                double x_minus_wi = exampleValues[i] - winnerValue;

                // Add distance between input and winner
                winner.setLocalError(winner.getLocalError() + Math.pow(euclideanDistance.calculateDistance(exampleValues, winner.getValues()), 2));

                winnerValue += eb * (x_minus_wi);
                winner.getValues()[i] = winnerValue;
            }
            updateWeightsOfNeighbour(winner);
            winner.incrementAges();

        }

        removeNotConnected();
        if (internalLoopIteration != 0 && internalLoopIteration % lambda == 0 && maxNeurons > neurons.size()) {
            createNewNeuron();
        }

        // Update local error for all neurons
        for (NeuronNode neuron : neurons) {
            neuron.setLocalError(neuron.getLocalError() - (beta * neuron.getLocalError()));
        }

        internalLoopIteration++;

    }

    private void createNewNeuron() {
        System.out.println("Creating neuron");
        LocalErrorComparator comparator = new LocalErrorComparator();

        // Find neuron with highest error (Eq)
        neurons.sort(comparator);
        NeuronNode neuronEq = neurons.get(0);

        // Find neighbour with highest error (Ef)
        neuronEq.getNeighbors().sort(comparator);
        NeuronNode neuronEf = neuronEq.getNeighbors().get(0);

        //Set weight for new neuron
        double[] newNeuronWeights = new double[getAttributesSize()];
        for (int i = 0; i < getAttributesSize(); i++) {
            double weight = (neuronEq.getValues()[i] / neuronEf.getValues()[i]) / 2;
            newNeuronWeights[i] = weight;
        }

        // Create new neuron
        NeuronNode neuron = new NeuronNode(newNeuronWeights, maxAge);

        // Remove connection between neuron q and f
        neuronEf.deleteNeighbor(neuronEq);

        // Connect new neuron (r) with neuron q and f
        neuron.addNeighbor(neuronEf);
        neuron.addNeighbor(neuronEq);

        // Decrease local error for q and f neurons
        neuronEq.setLocalError(neuronEq.getLocalError() - (alpha * neuronEq.getLocalError()));
        neuronEf.setLocalError(neuronEf.getLocalError() - (alpha * neuronEf.getLocalError()));

        // Set local error for new neuron
        neuron.setLocalError((neuronEq.getLocalError() + neuronEf.getLocalError()) / 2);

        neurons.add(neuron);
    }

    //Remove neurons without connections
    private void removeNotConnected() {
//		System.out.println("Removing neuron");
        List<NeuronNode> toRemove = new ArrayList<>();
        // Find not connected
        for (NeuronNode neuron : neurons) {
            if (neuron.getNeighbors().isEmpty()) {
                toRemove.add(neuron);
            }
        }

        neurons.remove(toRemove);
    }

    private void updateWeightsOfNeighbour(NeuronNode neuron) {
        for (NeuronNode neighbour : neuron.getNeighbors()) {
            for (int i = 0; i < getAttributesSize(); i++) {
                double neighbourValue = neighbour.getValues()[i];
                double x_minus_wi = exampleValues[i] - neighbourValue;

                neighbourValue += en * (x_minus_wi);
                neighbour.getValues()[i] = neighbourValue;
            }
        }
    }

    private void calcDistances() {
        double dist;
        for (NeuronNode neuron : neurons) {
            dist = measure.calculateDistance(neuron.getValues(), exampleValues);
            neuron.setDist(dist);
        }
    }

    @Override
    public boolean nextIteration() {
        currentIteration++;
        boolean nextIteration = currentIteration < iterations;
        // If operation finished then copy to prototypes
        if (!nextIteration) {
            prototypeValues = new double[neurons.size()][getAttributesSize()];
            int iteration = 0;
            for (NeuronNode neuron : neurons) {
                for (int i = 0; i < getAttributesSize(); i++) {
                    prototypeValues[iteration][i] = neuron.getValues()[i];
                }
                iteration++;
            }
        }
        return nextIteration;
    }
}
