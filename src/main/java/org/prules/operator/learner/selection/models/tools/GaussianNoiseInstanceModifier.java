/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.models.tools;

import com.rapidminer.tools.RandomGenerator;
import org.prules.dataset.Vector;

/**
 * This class allows to modify instance by adding Gaussian noise to it.
 * @author Marcin
 */
public class GaussianNoiseInstanceModifier implements InstanceModifier {

    /**
     * Field describes the standard deviation of the noise
     */
    double noiseLevel;
    /**
     * RandomGenerator used for generating Gaussian noise
     */
    RandomGenerator randomGenerator;

    transient double[] tmpInstance;

    public GaussianNoiseInstanceModifier(double noiseLevel, RandomGenerator randomGenerator) {
        this.noiseLevel = noiseLevel;
        this.randomGenerator = randomGenerator;
    }

    @Override
    public Vector modify(Vector values) {  
        values = (Vector)values.clone();
        if (values.isSparse()){
            int[] idx = values.getNonEmptyIndex();
            for(int i : idx){
                double val = values.getValue(i);
                val += randomGenerator.nextGaussian() * noiseLevel;
                values.setValue(i, val);
            }
        } else {
            for(int i=0; i<values.size(); i++){
                double val = values.getValue(i);
                val += randomGenerator.nextGaussian() * noiseLevel;
                values.setValue(i, val);
            }
        }        
        return values;
    }
}
