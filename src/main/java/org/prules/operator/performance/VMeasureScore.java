package org.prules.operator.performance;

import JavaMI.MutualInformation;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


/**
 * Created by Łukasz Migdałek on 2016-09-26.
 */
public class VMeasureScore {

    public static void main(String[] args) {
        System.out.println(new VMeasureScore().measure(new int[]{0, 1, 1, 1, 2, 5, 5, 9, 0}, new int[]{0, 1, 1, 1, 2, 9, 9, 5, 0}));
    }

    public double measure(int[] first, int[] prediction) {
        double entropy_c = calcEntropy(first);
        double entropy_k = calcEntropy(prediction);
        double mi = MutualInformation.calculateMutualInformation(toDoubleArray(first), toDoubleArray(prediction));

        double homogeneity = mi / entropy_c;
        double completeness = mi / entropy_k;

        if (homogeneity + completeness == 0) {
            return 0;
        } else {
            return ((2 * homogeneity * completeness) / (homogeneity + completeness));
        }
    }

    private double calcEntropy(int[] array) {
        double entropy = 0.0;
        List<Integer> list = Arrays.asList(ArrayUtils.toObject(array));
        HashSet<Integer> uniqueElements = new HashSet<>(list);
        for (Integer unique : uniqueElements) {
            int freq = Collections.frequency(list, unique);
            double prob = (double) freq / array.length;
            entropy -= prob * (Math.log(prob) / Math.log(2));
        }
        return entropy;
    }

    /**
     * JavaMI need array of doubles.
     *
     * @param array
     * @return
     */
    private double[] toDoubleArray(int[] array) {
        double[] doubleArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            doubleArray[i] = array[i];
        }
        return doubleArray;
    }
}
