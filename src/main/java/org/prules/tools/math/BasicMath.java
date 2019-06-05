/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.tools.math;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import org.prules.dataset.IInstanceLabels;


/**
 *
 * @author Marcin
 */
public class BasicMath {

/*
    public static void printLabeledData(LabeledDataInterface d) {
        int anX = d.getXAttributesNumber();
        int anY = d.getYAttributesNumber();
        int dn = d.getVectorsNumber();
        for (int i = 0; i < dn; i++) {
            System.out.print("X = ");
            for (int j = 0; j < anX; j++) {
                System.out.print(d.getXElement(i, j) + " ");
            }
            System.out.print("Y = ");
            for (int j = 0; j < anY; j++) {
                System.out.print(d.getYElement(i, j) + " ");
            }
            System.out.print("\n");
        }
    }
*/
    /**
     * Sort elements in X and order elements in Y in the same order as in X
     * @param X
     * @param Y
     */
    public static void sort(double[] X, double[] Y) {
        double tmpX, tmpY;
        int size = X.length;
        for (int j = 0; j < X.length; j++) {
            for (int i = 1; i < size; i++) {
                if (X[i] < X[i - 1]) {
                    tmpX = X[i - 1];
                    X[i - 1] = X[i];
                    X[i] = tmpX;
                    tmpY = Y[i - 1];
                    Y[i - 1] = Y[i];
                    Y[i] = tmpY;
                }
            }
            size--;
        }
    }

    /**
     * Sort elements in X and order elements in Y in the same order as in X
     * @param X - array which elements will be sorted
     * @param Y - array which elements will be reordered according to the order of X
     */
    public static void sort(double[] X, int[] Y) {
        double tmpX;
        int tmpY;
        int size = X.length;
        for (int j = 0; j < X.length; j++) {
            for (int i = 1; i < size; i++) {
                if (X[i] < X[i - 1]) {
                    tmpX = X[i - 1];
                    X[i - 1] = X[i];
                    X[i] = tmpX;
                    tmpY = Y[i - 1];
                    Y[i - 1] = Y[i];
                    Y[i] = tmpY;
                }
            }
            size--;
        }
    }
        
    /**
     * Calculate arytchmetic mean value of elements in X between start and end index
     * @param X 
     * @param start - starting element
     * @param end - last accepted element
     * @return
     */
    public static double mean(double[] X, int start, int end){
        double mean = 0;
        for (int i=start;i<=end;i++)
            mean += X[i];
        mean /= end-start+1;
        return mean;
    }
    
    /**
     * Mean value of all elements in X
     * @param X
     * @return
     */
    public static double mean(double[] X){
        return mean(X,0,X.length-1);
    }

    /**
     * Calculate arythmetic mean value of elements in X between start and end index
     * @param X
     * @param start - first element in X
     * @param end - last element in X
     * @return
     */
    public static double mean(Collection<Number> X, int start, int end){
        double mean = 0;
        Iterator<Number> iter = X.iterator();        
        int i = 0;
        while (iter.hasNext()){
            if (i < start) continue;
            if (i > end) break;            
            Number x = iter.next();
            mean += x.doubleValue();
            i++;
        }            
        mean /= end-start+1;
        return mean;
    }
    
    /**
     * Calculate arythmetic mean value of elements in X between start and end index
     * @param X
     * @param start - first element in X
     * @param end - last element in X
     * @param name - name of stored value
     * @return
     */
    public static double mean(Collection<IInstanceLabels> X, int start, int end, String name){
        double mean = 0;
        Iterator<IInstanceLabels> iter = X.iterator();        
        int i = 0;
        while (iter.hasNext()){
            if (i < start) continue;
            if (i > end) break;                        
            mean += iter.next().getValueAsDouble(name);
            i++;
        }            
        mean /= end-start+1;
        return mean;
    }
    
    /**
     * 
     * Mean value of all elements in X
     * @param X
     * @return
     */
    public static double mean(Collection<Number> X){
        return mean(X,0,X.size()-1);
    }
    
    /**
     * 
     * Mean value of all elements in X
     * @param X
     * @param name - name of stored value
     * @return
     */
    public static double mean(Collection<IInstanceLabels> X, String name){
        return mean(X,0,X.size()-1, name);
    }

    /**     
     * square error
     * @param X
     * @param mean
     * @return
     */
    public static double simpleVariance(double[] X, double mean){
        double var = 0;
        for (int i=0; i<X.length; i++)
            var += (X[i] - mean)*(X[i] - mean);
        return var/X.length;
    }

     /**
     * Square error of elements in collection
     * @param X
     * @param mean
     * @return
     */
    public static double simpleVariance(Collection<Number> X, double mean){
        double var = 0;
        for (Number x : X)
            var += (x.doubleValue() - mean)*(x.doubleValue() - mean);
        return var/X.size();
    }
    
    /**
     * Square error of elements in collection
     * @param X
     * @param mean
     * @param name - name of stored value
     * @return
     */
    public static double simpleVariance(Collection<IInstanceLabels> X, double mean, String name){
        double var = 0;
        for (IInstanceLabels x : X)
            var += (x.getValueAsDouble(name) - mean)*(x.getValueAsDouble(name) - mean);
        return var/X.size();
    }

    /**
     * Standard deviation of elements in X, calculated with pre calculated mean 
     * value, and an index of first and las element
     * @param X
     * @param mean
     * @param start
     * @param end
     * @return
     */
    public static double std(double[] X, double mean, int start, int end){
        double var = 0;        
        for (int i=start; i<=end; i++)
            var += (X[i] - mean)*(X[i] - mean);
        //if (start != end)
        var /= end-start+1;
        var = Math.sqrt(var);
        return var;
    }

    /**
     * Standard deviation of elements in X, calculated within the elements between  first and last element
     * @param X
     * @param start
     * @param end
     * @return
     */
    public static double std(double[] X, int start, int end){
        double m = mean(X,start,end);
        return BasicMath.std(X, m, start, end);
    }

    /**
     * Standard deviation of elements in X
     * @param X
     * @return
     */
    public static double std(double[] X){
        return BasicMath.std(X,0,X.length-1);
    }
    
    /**
     * Calculate standard deviation of elements in X
     * @param X - input collection
     * @param mean - mean value
     * @param start - index of first element in collection taken into account
     * @param end - index of last element in collection taken into account
     * @return value of standard deviation
     */
    public static double std(Collection<Number> X, double mean, int start, int end){
        double var = 0;                
        Iterator<Number> iter = X.iterator();        
        int i = 0;
        while (iter.hasNext()){
            if (i < start) continue;
            if (i > end) break;            
            Number x = iter.next();
            var += (x.doubleValue() - mean)*(x.doubleValue() - mean);
            i++;
        }        
        //if (start != end)
        var /= end-start+1;
        var = Math.sqrt(var);
        return var;
    }
    
    /**
     * Calculate standard deviation of elements in X
     * @param X - input collection
     * @param mean - mean value
     * @param start - index of first element in collection taken into account
     * @param end - index of last element in collection taken into account
     * @param name - name of stored value
     * @return value of standard deviation
     */
    public static double std(Collection<IInstanceLabels> X, double mean, int start, int end, String name){
        double var = 0;                
        Iterator<IInstanceLabels> iter = X.iterator();        
        int i = 0;
        while (iter.hasNext()){
            if (i < start) continue;
            if (i > end) break;            
            double x = iter.next().getValueAsDouble(name);
            var += (x - mean)*(x - mean);
            i++;
        }        
        //if (start != end)
        var /= end-start+1;
        var = Math.sqrt(var);
        return var;
    }

    /**
     * Calculate standard deviation of elements in X
     * @param X - input collection    
     * @param start - index of first element in collection taken into account
     * @param end - index of last element in collection taken into account
     * @return value of standard deviation
     */
    public static double std(Collection<Number> X, int start, int end){
        double m = mean(X,start,end);
        return BasicMath.std(X, m, start, end);
    }
 
    
    /**
     * Calculate standard deviation of elements in X
     * @param X - input collection    
     * @param start - index of first element in collection taken into account
     * @param end - index of last element in collection taken into account
     * @param name - name of stored property
     * @return value of standard deviation
     */
    public static double std(Collection<IInstanceLabels> X, int start, int end, String name){
        double m = mean(X,start,end, name);
        return BasicMath.std(X, m, start, end, name);
    }

    /**
     * Calculate standard deviation of elements in X
     * @param X - input collection     
     * @return value of standard deviation
     */
    public static double std(Collection<Number> X){
        return BasicMath.std(X,0,X.size()-1);
    }
    
    /**
     * Calculate standard deviation of elements in X for value type
     * @param X - input collection     
     * @param type  - name of stored value property    
     * @return value of standard deviation
     */
    public static double std(Collection<IInstanceLabels> X, String type){
        return BasicMath.std(X,0,X.size()-1, type);
    }

    /**
     * 
     * @param b
     * @return
     */
    public static boolean[] not(boolean[] b){
        for (int i = 0; i<b.length; i++)
            b[i] = !b[i];
        return b;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static int sum(boolean[] b){
        int sum = 0;
        for (int i = 0; i<b.length; i++)
            if (b[i]) sum++;
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static double sum(double[] b){
        double sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static double sum(float[] b){
        double sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static int sum(int[] b){
        int sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param b
     * @return
     */
    public static long sum(long[] b){
        long sum = 0;
        for (int i = 0; i<b.length; i++)
            sum += b[i];
        return sum;
    }

    /**
     * 
     * @param x1
     * @param x2
     * @return
     */
    public static double[] concatenate(double[] x1, double[] x2){
        double[] y = new double[x1.length + x2.length];
        System.arraycopy(x1, 0, y, 0, x1.length);
        System.arraycopy(x2, 0, y, x1.length, x2.length);
        return y;
    }
    
    public static double log2(double x){        
        return Math.log(x)/Math.log(2);
    }
    
    
    public static double sigmoid(double x){
        return 1.0/(1.0 + Math.exp(-x));
    }
/*
    public static double classificationAccuracy(IndexedData y1, IndexedData y2){
        if (y1.getRowsNumber() != y2.getRowsNumber()){
            System.err.println("Incorect data sizes y1 and y2");
            return 0;
        }
        double acc = 0;
        for (int i=0; i<y1.getRowsNumber(); i++)
            acc += (Math.round(y2.getElement(i, 0)) == y1.getElement(i, 0)) ? 1 : 0;
        acc /= y1.getRowsNumber();
        return acc;
    }
 */  
    
    public static long pair(long a, long b) {

        //Cantors pairing function only works for positive integers
        if (a > -1 || b > -1) {
            //Creating an array of the two inputs for comparison later
            long[] input = {a, b};

            //Using Cantors paring function to generate unique number
            long result = (a + b) * (a + b + 1)/2 + b;

            /*Calling depair function of the result which allows us to compare
             the results of the depair function with the two inputs of the pair
             function*/
            if (Arrays.equals(depair(result), input)) {
                return result; //Return the result
            } else {
                return -1; //Otherwise return rouge value
            }
        } else {
            return -1; //Otherwise return rouge value
        }
    }

    public static long[] depair(long z) {
        /*Depair function is the reverse of the pairing function. It takes a
         single input and returns the two corespoding values. This allows
         us to perform a check. As well as getting the orignal values*/

        //Cantors depairing function:
        long t = (int) (Math.floor((Math.sqrt(8 * z + 1) - 1) / 2));
        long x = t * (t + 3) / 2 - z;
        long y = z - t * (t + 1) / 2;
        return new long[]{x, y}; //Returning an array containing the two numbers
    }

    public static void main(String[] args){
        for (double i = -1; i < 1; i+=0.01) {
            System.out.println(sigmoid(i));
        }
    }
        
}