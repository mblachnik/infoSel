/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.selection.keel;

import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.Operator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.prules.operator.learner.selection.models.AbstractInstanceSelectorModel;
import org.prules.operator.learner.tools.IDataIndex;
import org.prules.rmtools.io.KeelExampleSetWriter;
import keel.Algorithms.Instance_Selection.CCIS.CCIS;

/**
 *
 * @author Marcin
 */
public class KeelISModel extends AbstractInstanceSelectorModel {

    String configurationString;
    Operator parent;

    public KeelISModel(String configurationString, Operator parent) {
        this.configurationString = configurationString;
        this.parent = parent;
    }

    @Override
    public IDataIndex selectInstances(SelectedExampleSet exampleSet) {
        File trainingFile = null, resultsFile = null;
        try {
            trainingFile = File.createTempFile("RapidMiner-Keel-", "-train.dat");
            resultsFile = File.createTempFile("RapidMiner-Keel-", "-results.dat");            
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(trainingFile),Charset.defaultCharset()))) {
                KeelExampleSetWriter.writeKeel(exampleSet, out);
                out.flush();
                configurationString = trainingFile.getCanonicalPath() + " " + trainingFile.getCanonicalPath() + " " + configurationString;
                CCIS alg = new CCIS(configurationString);
                alg.ejecutar();
                
                List<String> trainingFileText;
                List<String> resultsFileText;
                trainingFileText = Files.readAllLines(Paths.get(trainingFile.getCanonicalPath()), Charset.defaultCharset());
                resultsFileText = Files.readAllLines(Paths.get(resultsFile.getCanonicalPath()), Charset.defaultCharset());                                       
                Iterator<String> trainingIterator = trainingFileText.iterator();
                Iterator<String> resultsIterator = resultsFileText.iterator();
                int i = 0;
                while(trainingIterator.hasNext()){
                    String tmp = trainingIterator.next();
                    trainingIterator.remove();
                    if (tmp.contains("@data")){                        
                        break;
                    }                         
                }                                   
                while(resultsIterator.hasNext()){
                    String tmp = resultsIterator.next();
                    resultsIterator.remove();
                    if (tmp.contains("@data")){
                        break;
                    }                    
                }                
                while(resultsIterator.hasNext() && !resultsFileText.isEmpty()){
                    String row = resultsIterator.next();
                    i = resultsFileText.indexOf(row);
                    if (i>-1){
                        
                    }
                }
                
                
                //TODO Tutaj treba dopisać resztę w tym uruchomienie danego modelu IS.
                //Tej części nie można zrobić jako uruchomienie procesu ze względu na ograniczenia uprawnień więc trzeba to wbudować
                //jako klasę javy i od razu uruchomić dany algorytm. 
                //Potem trzeba wczytać wyniki i na koniec poszukać które rekordy zostały usunięte.
                //To ostatnie można zrobić na dwa sposoby; 1-na podstawie stringów skleić stringi opisujące poszczególne wektory,
                //tak aby każdy wektor był jednym stringiem, a na koniec usunąć te stringi które się powtarzają.
                
                

//                if (configurationString==null) configurationString = "";
//                List<String> paramsList = new ArrayList<>(Arrays.asList(configurationString.split(";")));
//                paramsList.add(0, trainingFile.getAbsolutePath());
//                String[] params = paramsList.toArray(new String[0]);
//                final Process p = Runtime.getRuntime().exec("java -jar ", params);
//
//                new Thread(() -> {
//                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    String line = null;
//                    try {
//                        while ((line = input.readLine()) != null) {
//                            System.out.println(line);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }).start();
//
//                p.waitFor();               

                IDataIndex index = exampleSet.getIndex();
                index.setAllFalse();
                // for (int i : vIndex) {
                //    index.set(i, true);
                //}
                return index;
            } catch (IOException ex) {
                return null;
            } catch (Exception ex) {
                return null;
            }
        } catch (IOException ex) {
            System.err.println("=========================================");
            System.err.println("Not able to create training file for Keel");
            System.err.println("=========================================");
            return null;
        } finally {
            if (trainingFile != null) {
                trainingFile.delete();
            }
            if (resultsFile != null) {
                resultsFile.delete();
            }
        }
    }

}
