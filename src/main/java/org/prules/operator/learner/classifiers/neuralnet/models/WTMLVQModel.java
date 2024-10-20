package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import java.util.ArrayList;
import java.util.List;


public class WTMLVQModel extends AbstractLVQModel {

    private DistanceMeasure measure; // zastosowana metryka służąca do obliczania odległości
    private int currentIteration, iterations; // liczba iteracji i numer bieżącej iteracji
    private double alpha, lambda; // aktualna wartosc współczynnika uczenia i promienia sąsiedztwa
    private double initialAlpha,initialLambdaRate; // początkowa wartosć współczynnika uczenia i promienia sąsiedztwa
    private LVQNeighborhoodTypes neighborhoodType; // typ sąsiedztwa : prostokątne lub gaussowskie

//konstruktor inicjujący parametry wymagane przez algorytm
    public WTMLVQModel(ExampleSet prototypes, int maxIterations, DistanceMeasure measure, double alpha, double lambda, LVQNeighborhoodTypes neighbourhoodType) throws OperatorException {
        super(prototypes);
        this.iterations = maxIterations;
        this.currentIteration = 0;        
        this.alpha = alpha;        
        this.initialAlpha = alpha;
        this.measure = measure;
        this.lambda = lambda;
        this.initialLambdaRate = lambda;
        this.neighborhoodType = neighbourhoodType;
        this.measure.init(prototypes);
    }

// metoda uaktualniająca wagi wynikajace z procesu uczenia
    public void update(double[][] prototypeValues, double[] prototypeLabels, double[] exampleValues, double exampleLabel, Example example) {
        double dist, minDist = Double.MAX_VALUE;
        int selectedPrototype = 0;
        int i = 0;
        
        //obliczenie który wektor leży najblizej zadanego
        for (double[] prototype : prototypeValues) {
            dist = measure.calculateDistance(prototype, exampleValues);
            if (dist < minDist) {
                minDist = dist;
                selectedPrototype = i;
            }
            i++;
        }

        int j = 0;
        double g = 0;

        //obliczenie odległości między wektorem zwycięzcą a pozostałymi oraz adaptacja wag zgodnie z regułą WTM
        for (double[] prototype : prototypeValues) {

            dist = measure.calculateDistance(prototype, prototypeValues[selectedPrototype]);
            switch (this.neighborhoodType) {
                case EUCLIDIAN:
                    if (dist <= lambda) {
                        g = 1;
                    } else {
                        g = 0;
                    }
                    break;

                case GAUSSIAN:
                    g = Math.exp(-1 * ((dist * dist) / (2 * lambda * lambda)));
                    break;

            }
        //przyciąganie jeśli etykiety klas są ze sobą zgodne lub odpychanie gdy etykiety klas są niezgodne
            
            if (prototypeLabels[j] == exampleLabel || (Double.isNaN(prototypeLabels[j]))) {
                for (i = 0; i < getAttributesSize(); i++) {
                    double value = prototypeValues[j][i];
                    value += alpha * g * (exampleValues[i] - value);
                    prototypeValues[j][i] = value;
                }
            } else {
                for (i = 0; i < getAttributesSize(); i++) {
                    double value = prototypeValues[j][i];
                    value -= alpha * g * (exampleValues[i] - value);
                    prototypeValues[j][i] = value;
                }
            }
            j++;
        }

    }

    // metoda uaktualniająca współczynnik uczenia oraz promienia sąsiedztwa, które maleją w czasie
    @Override
    public boolean isNextIteration(ExampleSet trainingSet) {
        currentIteration++;
        alpha = LVQTools.learingRateUpdateRule(alpha, currentIteration, iterations, initialAlpha);        
        lambda = LVQTools.lambdaRateUpdateRule(lambda, currentIteration, iterations, initialLambdaRate);

        return currentIteration < iterations;
    }

    /**
     * Returns total number of iterations (maximum number of iterations)
     *
     * @return
     */
    @Override
    public int getMaxIterations() {
        return iterations;
    }

    /**
     * Returns current iteration
     *
     * @return
     */
    @Override
    public int getIteration() {
        return currentIteration;
    }

    /**
     * Returns the value of cost function
     *
     * @return
     */
    @Override
    public double getCostFunctionValue() {
        return Double.NaN;
    }

    /**
     * Returns list of cost function values
     *
     * @return
     */
    @Override
    public List<Double> getCostFunctionValues() {
        return new ArrayList<>(0);
    }

}
