/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math;

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
        assert x.length > 0;
        int n = x.length;
        double meanX=0, meanY=0, meanX2=0, meanXY=0;                
        for(int i=0; i<n; i++){
            meanX  += x[i];
            meanY  += y[i];
            meanX2 += x[i]*x[i];
            meanXY += x[i]*y[i];
        }        
        meanX  = n==0 ? 0 : meanX  / n;
        meanY  = n==0 ? 0 : meanY  / n;
        meanXY = n==0 ? 0 : meanXY / n;
        meanX2 = n==0 ? 0 : meanX2 / n;
        double numerator = meanX * meanY - meanXY;
        double denominator = meanX*meanX - meanX2;        
        a = (numerator == 0) && (denominator == 0) ? 0 : numerator/denominator;
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
        mse = n == 0 ? 0 : mse / n;
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
