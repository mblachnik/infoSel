/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.optimization;

//import history.OldAbstractPRulesOperator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.*;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.clustering.clusterer.RMAbstractClusterer;
import com.rapidminer.ispr.operator.learner.AbstractPRulesOperator;
import com.rapidminer.ispr.operator.learner.classifiers.MyKNNClassificationModel;
import com.rapidminer.ispr.operator.learner.classifiers.PredictionType;
import com.rapidminer.ispr.operator.learner.optimization.clustering.AbstractBatchModel;
import com.rapidminer.ispr.tools.math.container.DoubleDoubleContainer;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.DistanceMeasurePrecondition;
import com.rapidminer.operator.ports.metadata.GeneratePredictionModelTransformationRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;
import com.rapidminer.ispr.tools.math.container.PairContainer;

import java.util.*;

/**
 *
 * @author Marcin
 */
public abstract class AbstractPrototypeOptimizationOperator extends AbstractPRulesOperator {

    private static final long serialVersionUID = 21;
    private double numberOfInstancesBeaforeSelection = -1;
    private double numberOfInstancesAfterSelection = -1;
    private double compression = -1;
    protected final PredictionType predictionType;
    protected final OutputPort modelOutputPort = getOutputPorts().createPort("Model");
    protected HashMap<Integer, String> clusterNamesMap;

    /**
     *
     * @param description
     * @param predictionType
     */
    public AbstractPrototypeOptimizationOperator(OperatorDescription description, PredictionType predictionType) {
        super(description);
        this.predictionType = predictionType;
        exampleSetInputPort.addPrecondition(new DistanceMeasurePrecondition(exampleSetInputPort, this));
        //getTransformer().addRule(new GenerateModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));
        getTransformer().addRule(new GeneratePredictionModelTransformationRule(exampleSetInputPort, modelOutputPort, MyKNNClassificationModel.class));

        addValue(new ValueDouble("Instances_beafore_selection", "Number Of Examples in the training set") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesBeaforeSelection;
            }
        });
        addValue(new ValueDouble("Instances_after_selection", "Number Of Examples after selection") {

            @Override
            public double getDoubleValue() {
                return numberOfInstancesAfterSelection;
            }
        });
        addValue(new ValueDouble("Compression", "Compressin = #Instances_after_selection/#Instances_beafore_selection") {

            @Override
            public double getDoubleValue() {
                return compression;
            }
        });
    }

    /**
     * method implemented after AbstractPRulesOperator operator - responsible
     * for main data operations It runs the model and performs clustering, also
     * responsible for delivering results to the output.
     *
     * @param trainingSet
     * @return
     * @throws OperatorException
     */
    @Override
    public ExampleSet processExamples(ExampleSet trainingSet) throws OperatorException {
        numberOfInstancesBeaforeSelection = trainingSet.size();
        PairContainer<ExampleSet, MyKNNClassificationModel<Number>> result = optimize(trainingSet);
        ExampleSet codebooks = result.getFirst();
        MyKNNClassificationModel<Number> model = result.getSecond();
        if (model != null) {
            //prototypesOutputPort.deliver(codebooks);
            modelOutputPort.deliver(model);
        }

        numberOfInstancesAfterSelection = codebooks.size();
        compression = numberOfInstancesAfterSelection / numberOfInstancesBeaforeSelection;
        return codebooks;
    }

    /**
     * Configuration options
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        if (predictionType == PredictionType.Clustering) {
            ParameterType type;
            type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE, 
                    "If enabled, a cluster id is generated as new special attribute directly in this operator, otherwise this operator does not add an id attribute. In the latter case you have to use the Apply Model operator to generate the cluster attribute.", 
                    true, false);
            types.add(type);

            type = new ParameterTypeBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL,
                    "If true, the cluster id is stored in an attribute with the special role 'label' instead of 'cluster'.",
                    false);
            type.setExpert(false);
            types.add(type);
        }
        return types;
    }

    /**
     * Abstract method required to be implemented by the successor of
     * AbstractPrototypeOptimizationOperator. It should implement the main
     * prototype optimization algorithm
     *
     * @param trainingSet - dataset to be clustered
     * @return - pair of elements. The first one is Dataset and the second one
     * is clustering model - namely kNN
     * @throws OperatorException
     */
    public abstract PairContainer<ExampleSet, MyKNNClassificationModel<Number>> optimize(ExampleSet trainingSet) throws OperatorException;

    /**
     * This method creates ExampleSet from a collection of {
     *
     * @see Prototype} objects. It is used to convert cluster centers into
     * ExampleSet As prototypes are represented as double[] they have to be
     * mapped into appropriate attribute names, so a list of attributes is
     * required.
     * @param codebooks - collection of {
     * @see Prototype}
     * @param attributes -
     * @return
     */
    protected ExampleSet prepareCodebooksExampleSet(Collection<Prototype> codebooks, List<Attribute> attributes) {
        Attribute codebookLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
        NominalMapping codebookLabelsNames = new PolynominalMapping(new HashMap<Integer, String>(clusterNamesMap));
        codebookLabels.setMapping(codebookLabelsNames);
        attributes.add(codebookLabels);
        ExampleTable codebooksTable = new MemoryExampleTable(attributes, new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), codebooks.size());
        ExampleSet codebooksSet = new SimpleExampleSet(codebooksTable, attributes);
        codebooksSet.getAttributes().setLabel(codebookLabels);
        Iterator<Prototype> codebookIterator = codebooks.iterator();
        Iterator<Example> codebookExampleIterator = codebooksSet.iterator();
        //Rewrite codebooks to codebooks ExampleSet
        Attributes codebookAttributes = codebooksSet.getAttributes();
        int codebookIndex = 0;
        while (codebookIterator.hasNext()) {
            Prototype codebook = codebookIterator.next();
            Example codebookExample = codebookExampleIterator.next();
            int i = 0;
            for (Attribute a : codebookAttributes) {
                codebookExample.setValue(a, codebook.getValues()[i]);
                i++;
            }
            codebookExample.setLabel(codebookIndex);
            codebookIndex++;
        }
        return codebooksSet;
    }

    /**
     * Creates a HashMap<Integer, String> which maps cluster number into cluster
     * name in a form of {
     *
     * @see Attributes.CLUSTER_NAME} _\#cluster_number
     * @param c - number of clusters
     */
    protected void prepareClusterNamesMap(int c) {
        this.clusterNamesMap = new HashMap<Integer, String>(c); //Map of nominal values of Cluster attribute        
        for (int i = 0; i < c; i++) {
            String clusterName = Attributes.CLUSTER_NAME + "_" + i;
            clusterNamesMap.put(i, clusterName);
        }
    }

    /**
     * Adds new attributes to the input trainingSet which would store clustering
     * results. These new atrributes can be returned with cluster or label role
     * As well as level of degree for fuzzy clustering (fuzzy partition matrix)
     *
     * @param trainingSet
     * @param batchModel
     */
    protected void prepareTrainingExampleSet(ExampleSet trainingSet, AbstractBatchModel batchModel) {
        //Preparing attributes for trainingSet   
        boolean b = getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_CLUSTER_ATTRIBUTE);
        if (b) {
            int c = clusterNamesMap.size();
            ArrayList<Attribute> partitionMatrixAttributes = new ArrayList<Attribute>(c); //partition matrix + cluster attribute
            for (int i = 0; i < c; i++) {
                Attribute attribute = AttributeFactory.createAttribute(clusterNamesMap.get(i), Ontology.NUMERICAL);
                partitionMatrixAttributes.add(attribute);
            }
            Attribute traininSetLabels = AttributeFactory.createAttribute(Attributes.CLUSTER_NAME, Ontology.NOMINAL);
            NominalMapping labelsNames = new PolynominalMapping(new HashMap<Integer, String>(clusterNamesMap));
            traininSetLabels.setMapping(labelsNames);
            traininSetLabels.setDefault(Double.NaN);
            trainingSet.getExampleTable().addAttributes(partitionMatrixAttributes);
            trainingSet.getExampleTable().addAttribute(traininSetLabels);
            //TODO Uwaga tutaj dodajemy etykiety klastrów jako Label, a powinno być w zależności od ustawień przełącznika                
            int i = 0;
            Attributes attributes = trainingSet.getAttributes();
            for (Attribute attribute : partitionMatrixAttributes) {
                attributes.addRegular(attribute);
                attributes.setSpecialAttribute(attribute, clusterNamesMap.get(i));
                i++;
            }
            Iterator<double[]> partitionMatrixIterator = batchModel.getPartitionMatrix().iterator();
            Iterator<Example> exampleIterator = trainingSet.iterator();
            while (exampleIterator.hasNext() && partitionMatrixIterator.hasNext()) {
                Example example = exampleIterator.next();
                double[] partitionMatrix = partitionMatrixIterator.next();
                i = 0;
                for (Attribute attribute : partitionMatrixAttributes) {
                    example.setValue(attribute, partitionMatrix[i]);
                    i++;
                }
            }
            if (getParameterAsBoolean(RMAbstractClusterer.PARAMETER_ADD_AS_LABEL)) {
                attributes.setLabel(traininSetLabels);
            } else {
                attributes.setCluster(traininSetLabels);
            }

            batchModel.apply(trainingSet, traininSetLabels);
        }
    }
}
