/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.tools.math;

/**
 * Simple linear regression for 2D problems where x is scalar, and y is scalar
 * It learns coefficient of a linear regression, and allows to apply it, and calculate the output.
 * @author Marcin
 */
public class SimpleLinearRegressionModel {
    double a;
    double b;
    
    
    public void train(double[] x, double y[]){
        assert x!=null && y!=null;
        assert x.length == y.length;
        int n = x.length;
        double meanX=0, meanY=0, meanX2=0, meanXY=0;                
        for(int i=0; i<n; i++){
            meanX  += x[i];
            meanY  += y[i];
            meanX2 += x[i]*x[i];
            meanXY += x[i]*y[i];
        }
        meanX  /= n;
        meanY  /= n;
        meanXY /= n;
        meanX2 /= n;
        a = (meanX * meanY - meanXY)/(meanX*meanX - meanX2);
        b = meanY - a*meanX;        
        
    }
                
    public double apply(double x){
        return a * x + b;
    }

    public double getMSE(double[] x, double[] y){
        assert x != null && y !=null;
        assert x.length == y.length;
        int n = x.length;
        double mse=0;
        double error = 0;
        for(int i=0; i<n; i++){
            error = y[i] - apply(x[i]);
            mse += error * error;
        }
        mse /= n;
        return mse;
    }
    
    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }
}
