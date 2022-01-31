package org.prules.operator.learner.classifiers.neuralnet.models;

import com.rapidminer.example.Attribute;

import java.util.*;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.BinominalMapping;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.numerical.EuclideanDistance;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class ParallelLVQ1ModelTest extends TestCase {

    public void testTestRun() throws OperatorException {
        boolean doRead = true;
        String resDir = "d:\\desktop";
        ExampleSet prototypes = null;
        ExampleSet data = null;
        if (doRead){
            prototypes = read2DClassificationData(resDir + "\\prototypes.csv");
            data = read2DClassificationData(resDir + "\\data.csv");

            System.out.println("Prototypes:");
            System.out.println(prototypes);
            System.out.println("Data:");
            System.out.println(data);
        } else {
            prototypes = genrate2Gauuss(2);
            writeCSV(resDir + "\\prototypes.csv", prototypes);
            data = genrate2Gauuss(10000);
            writeCSV(resDir+"\\data.csv",data);

            System.out.println("Prototypes:");
            System.out.println(prototypes);
            System.out.println("Data:");
            System.out.println(data);
        }

        AbstractLVQModel model;
        model = new ParallelLVQ1Model(prototypes,100,new EuclideanDistance(), 0.02);
        //model = new LVQ1Model(prototypes,100,new EuclideanDistance(), 0.02);
        System.out.println("Prototypes before");
        prototypes.forEach(e -> System.out.println(e));
        ExampleSet res = model.run(data);
        System.out.println("Prototypes after");
        res.forEach(e -> System.out.println(e));
        writeCSV(resDir+"\\res.csv",res);
    }


    public static ExampleSet read2DClassificationData(String file){
        double[][] dd = readCSV(file);
        int n = dd.length;
        NominalMapping labelMapping = new BinominalMapping();
        int nl = labelMapping.mapString("N");
        int pl = labelMapping.mapString("P");
        ExampleSetBuilder builder = ExampleSets.from(AttributeFactory.createAttribute("X1",Ontology.REAL),
                AttributeFactory.createAttribute("X2",Ontology.REAL),
                AttributeFactory.createAttribute("Y", Ontology.BINOMINAL)
        );
        builder.withExpectedSize(n);
        for(int i=0; i<n; i++){
            double[] row = dd[i];
            builder.addRow(row);
        }
        ExampleSet es = builder.build();
        es.getAttributes().setLabel(es.getAttributes().get("Y"));
        es.getAttributes().getLabel().setMapping(labelMapping);
        return es;
    }

    public static ExampleSet genrate2Gauuss(int n){
        NominalMapping labelMapping = new BinominalMapping();
        int nl = labelMapping.mapString("N");
        int pl = labelMapping.mapString("P");
        ExampleSetBuilder builder = ExampleSets.from(AttributeFactory.createAttribute("X1",Ontology.REAL),
                AttributeFactory.createAttribute("X2",Ontology.REAL),
                AttributeFactory.createAttribute("Y", Ontology.BINOMINAL)
                );
        builder.withExpectedSize(n);
        double[][] dd = generate2GaussMatrix(n);
        int[] idx = randomizeIndex(n);
        for(int i=0; i<n; i++){
            double[] row = dd[idx[i]];
            builder.addRow(row);
        }
        ExampleSet es = builder.build();
        es.getAttributes().setLabel(es.getAttributes().get("Y"));
        es.getAttributes().getLabel().setMapping(labelMapping);
        return es;
    }

    public static double[][] generate2GaussMatrix(int n){
        Random r = new Random();
        double[][] dd = new double[n][];
        int pl = 1;
        int nl = 0;
        int n1 = n/2;
        for (int i=0; i<n1; i++) {
            double[] row = new double[3];
            row[0] = r.nextGaussian()-1.5;
            row[1] = r.nextGaussian()-1.5;
            row[2] = pl;
            dd[i] = row;
        }
        for (int i=n1; i<n; i++) {
            double[] row = new double[3];
            row[0] = r.nextGaussian()+1.5;;
            row[1] = r.nextGaussian()+1.5;;
            row[2] = nl;
            dd[i] = row;
        }
        return dd;
    }
    public static void writeCSV(String fileName, ExampleSet es){
        try (PrintStream ps = new PrintStream(fileName)){
            es.forEach(e -> {
                StringBuilder sb = new StringBuilder();
                Iterator<Attribute> ia = e.getAttributes().allAttributes();
                while(ia.hasNext()){
                    Attribute a = ia.next();
                    sb.append(e.getValue(a)).append(";");
                }
                ps.println(sb.toString());
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int[] randomizeIndex(int n){
        int[] idx = new int[n];
        for(int i=0; i<n; i++){
            idx[i]=i;
        }
        Random r = new Random();
        for (int i=0; i<n; i++){
            int j = r.nextInt(n);
            int t = idx[i];
            idx[i] = idx[j];
            idx[j] = t;
        }
        return idx;
    }

    public static double[][] readCSV(String f){
        double[][] out = null;
        try(Scanner sc = new Scanner(new File(f))){
            List<List<Double>> ll = new LinkedList<>();
            while(sc.hasNext()){
                String line = sc.nextLine();
                String[] tl = line.split(";");
                List<Double> row = new LinkedList<>();
                for(String l : tl){
                    double d = Double.parseDouble(l);
                    row.add(d);
                }
                ll.add(row);
            }
            out = new double[ll.size()][ll.get(0).size()];
            int i=0;
            for(List<Double> row : ll){
                int j=0;
                for(double d : row){
                    out[i][j] = d;
                    j++;
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }
}