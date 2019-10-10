/**
 * RapidMiner
 * <p>
 * Copyright (C) 2001-2015 by RapidMiner and the contributors
 * <p>
 * Complete list of developers available at our web site:
 * <p>
 * https://rapidminer.com
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.prules.operator.learner.feature.construction;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasureHelper;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import mdsj.ClassicalScaling;
import mdsj.MDSJ;

import java.util.LinkedList;
import java.util.List;

/**
 * This is the MDS operator. Operator MDS @author Marek Pietryga.
 * Algorithms @author Silesian University of Technology, Department of Industrial Informatics.
 */

public class MdsOperator extends Operator {

    private final InputPort exampleSetInput = getInputPorts().createPort("example set"); //Data set input port
    private final OutputPort exampleSetOutput = getOutputPorts().createPort("example set"); //Data set Output port
    private final DistanceMeasureHelper measureHelper = new DistanceMeasureHelper(this); //Distance Measure

    private static final String PARAMETER_SCALING_ALGORITHM = "kind of scaling"; //Parameter string of the operator: type of MDS algorithm
    private static final String PARAMETER_SCALING_DIMENSIONS = "dim"; //Parameter string of the operator: number of put put dimensions
    private static final String PARAMETER_ORIGINALS = "keep originals"; //Parameter string of the operator: boolean to keep original features

    private static final String ATTRIBUTES_PREFIX = "MDSattr"; //Prefix of new attributes which are added to the example set

    //Defaule Operator constructor
    public MdsOperator(OperatorDescription description) {
        super(description);
    }

    /**
     * Calculate distance matrix for given example set
     *
     * @param exampleSet input ExampleSet
     * @return square distance matrix
     * @throws OperatorException
     */
    private double[][] generateDistancesMatrix(ExampleSet exampleSet) throws OperatorException {

        Attributes attributes = exampleSet.getAttributes();
        int aSize = attributes.size();
        int eSize = exampleSet.size();

        DistanceMeasure measure = measureHelper.getInitializedMeasure(exampleSet);

        double[] values = new double[aSize]; // tablica pomocnicza
        double[][] distancesMatrix = new double[eSize][eSize];

        // obliczanie macierzy odleglosci
        int ie = 0;
        for (Example e : exampleSet) {

            int ia = 0;
            for (Attribute a : attributes) {
                values[ia] = e.getValue(a);
                ia++;
            }

            ie++;
            /*
             * inkrementacja przed petla w celu pominiecia obliczania odleglosci
             * pomiedzy tymi samymi obiektami
             */
            for (int j = ie; j < eSize; j++) {
                double d = measure.calculateDistance(exampleSet.getExample(j), values);
                distancesMatrix[j][ie - 1] = d;
                distancesMatrix[ie - 1][j] = d;
            }
        }

        return distancesMatrix;
    }

    /**
     * Call MDSJ and perform MDS scaling
     *
     * @param distancesMatrix
     * @return rectangular coordinates matrix, where number of rows is equal
     * to the number of examples in the input exampleSet, and number of columns
     * is equal to the number of attributes - output dimension
     * @throws OperatorException
     */
    private double[][] calculateCoordinatesMatrix(double[][] distancesMatrix) throws OperatorException {

        // wybrany algorytm mds
        String selectedAlgorithmName = getParameter(PARAMETER_SCALING_ALGORITHM);
        MdsAlgorithm mds = MdsAlgorithm.fromString(selectedAlgorithmName);
        // oczekiwany wymiar
        int dim = getParameterAsInt(PARAMETER_SCALING_DIMENSIONS);

        int eSize = distancesMatrix.length;
        // ograniczenie przekroczenia liczby wymiarow wzgledem obiektow
        if (dim > eSize)
            dim = eSize;

        double[][] coordinatesMatrix = new double[dim][eSize];

        switch (mds) {
            case CLASSICAL_SCALING:
                coordinatesMatrix = MDSJ.classicalScaling(distancesMatrix, dim);
                break;
            case FULL_MDS:
                ClassicalScaling.fullmds(distancesMatrix, coordinatesMatrix);
                break;
            case PIVOT_MDS:
                ClassicalScaling.pivotmds(distancesMatrix, coordinatesMatrix);
                break;
            case LANDMARK_MDS:
                ClassicalScaling.lmds(distancesMatrix, coordinatesMatrix);
                break;
            default:
                throw new IllegalStateException("Unknown value:" + mds);
        }

        return coordinatesMatrix;
    }

    /**
     * Convert coordinate matrix into exampleSet (adding new attributes to the input exampleSet)
     *
     * @param exampleSetOut     - output set
     * @param coordinatesMatrix
     * @return
     */
    private ExampleSet convertCoordinatesMatrixToExampleSet(ExampleSet exampleSetOut, double[][] coordinatesMatrix) {

        // zachowanie originalnych danych
        boolean keepOriginal = getParameterAsBoolean(PARAMETER_ORIGINALS);
        if (!keepOriginal)
            exampleSetOut.getAttributes().clearRegular();

        List<Attribute> attributesList = new LinkedList<>();

        // przygotowanie attrybutow wyjsciowego ExampleSeta
        for (int i = 0; i < coordinatesMatrix.length; i++) {
            Attribute attr = AttributeFactory.createAttribute(ATTRIBUTES_PREFIX + i, Ontology.NUMERICAL);
            attributesList.add(attr);
            exampleSetOut.getExampleTable().addAttribute(attr);
            exampleSetOut.getAttributes().addRegular(attr);
        }

        // wypelnienie attrybutow ExampleSeta danymi macierzy wspolrzednych
        int ie = 0;
        for (Example e : exampleSetOut) {
            int ia = 0;
            for (Attribute a : attributesList) {
                e.setValue(a, coordinatesMatrix[ia][ie]);
                ia++;
            }
            ie++;
        }

        return exampleSetOut;
    }

    /**
     * Perform MDS scaling in 3 steps:
     * 1) calculating full distance matrix,
     * 2) calling MDSJ library
     * 3) converting output of MDSJ into ExamplSet
     *
     * @throws OperatorException
     */
    @Override
    public void doWork() throws OperatorException {

        ExampleSet exampleSet = exampleSetInput.getData(ExampleSet.class);

        // generating distance matrix
        double[][] distancesMatrix = generateDistancesMatrix(exampleSet);

        // finding coordinates of examples in new space
        double[][] coordinatesMatrix = calculateCoordinatesMatrix(distancesMatrix);

        // generating output exampleSet
        ExampleSet exampleSetOut = (ExampleSet) exampleSet.clone();

        // konwersja macierzy wspolrzednych na postac ExampleSeta
        exampleSetOut = convertCoordinatesMatrixToExampleSet(exampleSetOut, coordinatesMatrix);

        exampleSetOutput.deliver(exampleSetOut);
    }

    /**
     * Operator parameters configuration -
     * 3 parameters avalizable - MDS algorithm (ENUM),
     * Number of output dimensions (int),
     * Keep original - (boolean) if we want to keep original feature set and just add newly created features
     *
     * @return
     */
    @Override
    public List<ParameterType> getParameterTypes() {

        List<ParameterType> types = super.getParameterTypes();
        types.addAll(DistanceMeasures.getParameterTypes(this));

        // pozyskanie tablicy nazw algorytmow String[] z MdsAlgorithm[]
        int i = 0;
        String[] algs = new String[MdsAlgorithm.values().length];
        for (MdsAlgorithm a : MdsAlgorithm.values()) {
            algs[i++] = a.toString();
        }

        // combobox - wybor rodzaju algorytmu
        String algorithmsDescription = "Classical Scaling - Performs classical multidimensional scaling on a given sparse dissimilarity matrix.\n"
                + "Full MDS - Computes a classical multidimensional scaling of a square matrix of distances.\n"
                + "Pivot MDS and Landmark MDS - Computes an approximation of classical multidimensional scaling for a given matrix of dissimilarities.";
        ParameterType parameterTypeAlgorithm = new ParameterTypeCategory(PARAMETER_SCALING_ALGORITHM,
                algorithmsDescription, algs, 0);
        types.add(parameterTypeAlgorithm);

        // label - okreslenie wspolrzednych macierzy wynikowej
        String dimensionsDescription = "Number of output dimensions";
        ParameterType parameterTypeDimensions = new ParameterTypeInt(PARAMETER_SCALING_DIMENSIONS,
                dimensionsDescription, 1, Integer.MAX_VALUE, 2);
        types.add(parameterTypeDimensions);

        // checkbox - zachowanie danych oryginalnych
        String originalsDescription = "Keep the original data attributes";
        ParameterType parameterTypeOriginals = new ParameterTypeBoolean(PARAMETER_ORIGINALS, originalsDescription,
                false);
        types.add(parameterTypeOriginals);

        return types;
    }
}
