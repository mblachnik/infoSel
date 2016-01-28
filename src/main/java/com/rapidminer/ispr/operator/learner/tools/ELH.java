/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;

/**
 *
 * @author Marcin
 */
public class ELH {

    private double[] lf;

    /**
     *
     */
    public ELH(int n) {
        setupLogFact(n + 1);
    }

    /**
     *
     * Returns the 'cost' (using Cameron-Jones' Encoding Length Heuristic) of a model that retains 'm' instances out of 'n'
     * (=numTrain), with 'x' misclassified instances and 'c' (=numClasses) possible output classes. This cost is the sum of four
     * values: 1. Cost of encoding which 'm' of the 'n' instances are kept. 2. Cost of encoding which of the 'c' classes each 'm'
     * instance is. 3. Cost of encoding which 'x' of the n-m remaining instances are misclassified by the model described in 1 and
     * 2. 4. Cost of encoding which of the c-1 remaining classes (since the one the model chose was wrong) each of the 'x'
     * instances belongs to. Inputs were changed from int's to double's so that fractional values could be used, indicating
     * differences in confidence as well as differences in actual counts of misclassifications.
     *
     * @param n number of training instances
     * @param m number of instances in training set that are not pruned.
     * @param x number of instances in training set not classified correctly.
     * @param c
     * @return
     */
    public double cost(int n, int m, int x, int c) {
        if (m > n) {
            m = n;
        }
        if (n + 1 > lf.length) {
            setupLogFact(n + 1);
        }
        int nm = m - n;
        if (x > nm) {
            nm = x;
        }
        double value;
        value = ownCost(m, n) + // 1. which 'm' of 'n' retained. 
                m * BasicMath.log2(c) + // 2. which 'c' all 'm' instances are.
                ownCost(x, nm) + // 3. which 'x' of 'n-m' misclassified.
                x * BasicMath.log2(c - 1);    // 4. what class all 'x' instances are.

        return value;
    }

    /**
     * Sets up global array logfact[0..n-1], and sets the global 'nlf' to be the number of elements in the array. This code
     * provided by Mike Cameron-Jones.
     */
    private void setupLogFact(int n) {
        lf = new double[n];
        lf[0] = 0;
        for (int i = 1; i < n; i++) {
            lf[i] = BasicMath.log2(i + 1) + lf[i - 1];
        }
    }

    private double ownCost(int m, int n) {
        // Coding cost of m out of n, using Mike Cameron-Jones' code. 
        double s = 1.518535; // log_2(2.865) from Rissanen's book 
        double increment = log2SumB(m, n);
        while (increment > 0.0) {
            s += increment;
            increment = BasicMath.log2(increment);
        }
        return s;
    }

    /**
     * Log base 2 of sum of binomial coefficients for coding cost formula: log_2( Sum from 0 to m of C(m,n)) where C(m,n) is "n
     * taken m at a time." = log_2( Sum from 0 to m of n!/(m!(n-m)!) ) where 'n' is number of instances in training set
     * (numTrain), and 'm' is number of instances in model (non-pruned). Note that m must be <= n. Provided by Mike Cameron-Jones.
     */
    private double log2SumB(double m, double n) {

        double x = 0.5;
        if (m > n) {
            //error
        }
        if (m == 0) {
            return 0;
        }
        if (m == n) {
            return n;
        }
        double a = n - m;
        double b = m + 1;
        double bt = lf[(int)(a + b)] - lf[(int)(a)] - lf[(int)(b)] - 1.0;
        if (x < (a + 1) / (a + b + 2)) {
            return bt + BasicMath.log2(betafc(a, b, x) / a);
        } else {
            bt = bt - n;
            bt = Math.exp(bt * 0.6931472);
            x = (1.0 - bt * betafc(b, a, 1 - x) / b);
            if (x > 0.0 && x < 1.0) {
                return n + BasicMath.log2(x);
            } else {
                return n;
            }
        }
    }

    /**
     * Continued fraction part for incomplete beta function. Taken from "Numerical Recipes in C", as provided by Mike
     * Cameron-Jones.
     */
    private double betafc(double a, double b, double x) {
        double bm = 1;
        double az = 1;
        double am = 1;

        double qab = a + b;
        double qap = a + 1;
        double qam = a - 1;
        double bz = 1 - qab * x / qap;
        double eps = 3.0e-7;
        double imax = 100;
        for (int i = 1; i <= imax; i++) {
            double em = i;
            double tem = em + em;
            double d = em * (b - em) * x / ((qam + tem) * (a + tem));
            double ap = az + d * am;
            double bp = bz + d * bm;
            d = -(a + em) * (qab + em) * x / ((qap + tem) * (a + tem));
            double app = ap + d * az;
            double bpp = bp + d * bz;
            double aold = az;
            am = ap / bpp;
            bm = bp / bpp;
            az = app / bpp;
            bz = 1.0;

            if (Math.abs(az - aold) < eps * Math.abs(az)) {
                return az;
            }
        }
        return az;    
    }
}
