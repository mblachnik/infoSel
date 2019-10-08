/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.ensemble;

import com.rapidminer.example.*;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import org.prules.dataset.IInstanceLabels;
import org.prules.dataset.InstanceFactory;
import org.prules.dataset.Vector;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.tools.math.container.knn.GeometricCollectionTypes;
import org.prules.tools.math.container.knn.ISPRClassGeometricDataCollection;
import org.prules.tools.math.container.knn.KNNFactory;
import org.prules.tools.math.container.knn.KNNTools;

import java.util.*;
import java.util.logging.Level;

/**
 * @author Marcin
 */
public class ISEnsembleAdaBoostOperator extends AbstractISEnsembleOperator {

    private double[] instanceWeights; //weights of instances
    private double[] modelWeights; //weights of model in each iteration
    private int sampleSize; //Number of samples in training set
    private ExampleSet iterationExampleSet; //Temporal store for data set used in given iteration
    private ExampleSet initialExampleSet;
    private Attribute rowIdAttribute;
    private DistanceMeasure measure;
    private int k = 3;
    private List<Attribute> listOfAttributes; //It is used to synchronize attributes in case user will change the order of attributes in the resultant exampleSet
    private int largeErrorCounter; //Counts how many times AdaBoost with Error > 0.5 was executed
    private RandomGenerator random;

    public ISEnsembleAdaBoostOperator(OperatorDescription description) {
        super(description);
    }

    @Override
    public void initializeProcessExamples(ExampleSet exampleSet) throws OperatorException {
        super.initializeProcessExamples(exampleSet);
        random = RandomGenerator.getRandomGenerator(this);
        initialExampleSet = (ExampleSet) exampleSet.clone();
        sampleSize = initialExampleSet.size();
        instanceWeights = new double[sampleSize];
        modelWeights = new double[iterations];
        double weight = 1.0 / sampleSize;
        DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this);
        measure = measureHelper.getInitializedMeasure(initialExampleSet);
        rowIdAttribute = AttributeFactory.createAttribute("RowIdAttribute", Ontology.INTEGER);
        initialExampleSet.getExampleTable().addAttribute(rowIdAttribute);
        AttributeRole rowIdAttributeRole = new AttributeRole(rowIdAttribute);
        rowIdAttributeRole.setSpecial("tmp_rowId");
        Attributes attributes = initialExampleSet.getAttributes();
        attributes.add(rowIdAttributeRole);
        int i = 0;
        listOfAttributes = new ArrayList<>(attributes.size());
        for (Example e : initialExampleSet) {
            e.setValue(rowIdAttribute, i);
            instanceWeights[i] = weight;
            i++;
        }
        for (Attribute a : attributes) {
            listOfAttributes.add(a);
        }
    }

    /**
     * Prepares example set before each iteration
     *
     * @param trainingSet
     * @return
     */
    @Override
    protected ExampleSet preProcessExampleSet(ExampleSet trainingSet) {
        //Sample according to the distribution of 'instanceWeights'
        Set<Integer> set = new HashSet<>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            int j = random.randomIndex(instanceWeights);
            set.add(j);
        }
        //Create index array out of list
        int[] idx = new int[set.size()];
        int i = 0;
        for (int s : set) {
            idx[i] = s;
            i++;
        }
        iterationExampleSet = new MappedExampleSet(trainingSet, idx);
        return iterationExampleSet;
    }

    /**
     * Method can be overridden to process results of instance selection. It is
     * called in a loop every time an internal process finishes processing of
     * the data By default it returns input exampleSet but for example in
     * AdaBoost algorithms it can be used to check which samples were returned
     *
     * @param resultSet
     * @return
     */
    @Override
    protected ExampleSet postProcessExampleSet(ExampleSet resultSet) {
        double error = 0;
        ISPRClassGeometricDataCollection<IInstanceLabels> model;
        model = (ISPRClassGeometricDataCollection<IInstanceLabels>) KNNFactory.initializeKNearestNeighbourFactory(GeometricCollectionTypes.LINEAR_SEARCH, resultSet, measure);
        Vector values;
        IDataIndex index = new DataIndex(resultSet.size());
        index.setAllTrue();
        boolean[] predictions = new boolean[sampleSize]; //by default all values are false. Parameter is true for all correctly classified samples
        for (Example e : initialExampleSet) {
            int id = (int) e.getValue(rowIdAttribute);
            values = InstanceFactory.createVector(e, listOfAttributes);
            Collection<IInstanceLabels> results = model.getNearestValues(k, values, index);
            double labelPredicted = KNNTools.getMostFrequentValue(results);
            double labelReal = e.getLabel();
            if (labelPredicted != labelReal) {
                error += instanceWeights[id];
            } else {
                predictions[id] = true;
            }
        }
        int iteration = getIteration();
        double alpha;
        if (error > 0.5) {
            largeErrorCounter++; //Counts how many times this piece of code (error > 0.5) was executed
            this.getLogger().log(Level.FINEST, "Error < 0.5, iteration: {0}", iteration);
            alpha = 0;
            for (int i = 0; i < sampleSize; i++) {
                instanceWeights[i] = 1.0 / sampleSize;
            }
        } else if (error == 0) {
            this.getLogger().log(Level.FINEST, "Error < 0.5, iteration: {0}", iteration);
            alpha = 1;
            for (int i = 0; i < sampleSize; i++) {
                instanceWeights[i] = 1.0 / sampleSize;
            }
        } else {
            alpha = 0.5 * Math.log10((1.0 - error) / error);
            int i = 0;
            for (boolean b : predictions) {
                if (b) {
                    instanceWeights[i] = instanceWeights[i] / (2 * (1 - error));
                } else {
                    instanceWeights[i] = instanceWeights[i] / (2 * error);
                }
                i++;
            }
        }
        modelWeights[iteration] = alpha;
        //Normalize weights
        double sum = 0;
        for (int i = 0; i < sampleSize; i++) {
            sum += instanceWeights[i];
        }
        for (int i = 0; i < sampleSize; i++) {
            instanceWeights[i] = instanceWeights[i] / sum;
        }
        return resultSet;
    }

    /**
     * @return
     */
    @Override
    public double getIterationWeight(int iteration) {
        return modelWeights[iteration];
    }

    /**
     *
     */
    @Override
    public void finalizeProcessExamples() {
        super.finalizeProcessExamples();
        initialExampleSet.getAttributes().remove(rowIdAttribute);
        initialExampleSet.getExampleTable().removeAttribute(rowIdAttribute);
        iterationExampleSet = null;
        rowIdAttribute = null;
        initialExampleSet = null;
        listOfAttributes = null;
    }

    @Override
    public List<ParameterType> getParameterTypes() {

        List<ParameterType> types = super.getParameterTypes();
        types.addAll(DistanceMeasures.getParameterTypes(this));
        return types;
    }
}
