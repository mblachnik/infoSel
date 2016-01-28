/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */ 

package com.rapidminer.ispr.operator.learner.feature.selection;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.*;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.math.similarity.DistanceMeasures;
import infosel.JavaInfosel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Marcin
 */
public class InfoselRangAssigner extends AbstractPRulesFeatureSelection{
     
    /**
     * 
     */
    public static final String SELECTION_METHOD = "Algorithm";
    /**
     * 
     */
    public static final String DISCRETIZATION_METHOD = "Discretization";
    /**
     * 
     */
    public static final String INFOSEL_PATH = "Infosel path";
    private String featureSelectionMethod = null;
    private String discretizationAlgorithm = null;
    private String infoselPath = System.getProperty("user.dir") + "\\lib\\infosel++.exe";
    private String stringRepresentationOfFeatureWeights = null;
    private int selectedFeaturesCount = 0;

    /**
     * 
     * @param description
     */
    public InfoselRangAssigner(OperatorDescription description) {
        super(description);
        addValue(new ValueString("FeatureWeights", "Obtained feature weights") {

            @Override
            public String getStringValue() {
                return InfoselRangAssigner.this.stringRepresentationOfFeatureWeights;
            }
        });

        addValue(new ValueDouble("FeaturesCount", "Number of selected features") {

            @Override
            public double getDoubleValue() {
                return InfoselRangAssigner.this.selectedFeaturesCount;
            }
        });
    }

    /**
     * 
     * @param exampleSet
     * @return
     * @throws OperatorException
     */
    @Override
    public AttributeWeights doSelection(ExampleSet exampleSet) throws OperatorException{
        featureSelectionMethod  = getParameterAsString(SELECTION_METHOD);
        discretizationAlgorithm = getParameterAsString(DISCRETIZATION_METHOD);
        infoselPath             = getParameterAsString(INFOSEL_PATH);
        
        selectedFeaturesCount = 0;

        AttributeWeights attributeWeights = null;
        try {
            attributeWeights = new AttributeWeights(exampleSet);
            Attributes attributes = exampleSet.getAttributes();

            File input = File.createTempFile("InfoSel_", ".dat");
            BufferedWriter out = new BufferedWriter(new FileWriter(input));
            StringBuilder line = new StringBuilder();
            boolean doComa = false;
            for (Example example : exampleSet) {
                for (Attribute attribute : attributes) {
                    if (doComa) {
                        line.append(",");
                    }
                    line.append(example.getValue(attribute));
                    doComa = true;
                }
                line.append(",");
                line.append(example.getLabel());
                doComa = false;
                line.append("\r\n");
            }
            out.write(line.toString());
            out.close();

            JavaInfosel infosel = new JavaInfosel();
            infosel.setInfoselPath(infoselPath);
            infosel.setVerbose(true); //Wynik diałania programu będzie wyświetlany na ekranie, u pana to można ustawic na false
            infosel.setInfoselPath(infoselPath); //Określamy położenie pliku Infosel++.exe
            infosel.setResultsPath(System.getProperty("java.io.tmpdir")); //Określamy katalog w którym  będą przechowywane wyniki, jeśli tego nie zrobimy to będzie to katalog w którym jest plik JavaInfosel.jar
            infosel.setDebug(false); //Wyłączamy debugowanie - tzn pliki skryptu i plik z danymi będą usuniete po obliczeniach
            infosel.setStoreInput(false); //Ozacza że plik z danymi zostanie usunięty po obliczeniach,
            infosel.setStoreResults(false); //Ta opcja powoduje że plik z wynikami ma nie byc kasowany
            //String[] algo = {"mi_mi(0)", "suc_suc(0)"}; //Określamy listę algorytmów do wykonania
            infosel.setPartition(discretizationAlgorithm); //Ustalenie algorytmu dyskretyzacji na  equiwidth i 24 biny
            infosel.setExec(featureSelectionMethod); //Ustawiamy algorytmy które będziemy chcieli wykonać

            int[][] featureIndexMatrix;

            infosel.run(input);
            featureIndexMatrix = infosel.getIndex();
            if (featureIndexMatrix == null) throw new OperatorException("Infosel error - Check Infosel console informations");
            int attributesSize = attributes.size();
            /*
             Map for feature number to feature weight relation. Warning: JavaInfosel returns just a set of
            selected features by sorting its results ordered from the most to the least relevance (or vice veras).
             *Rapid miner AttributeWeights class require assigning value to given attribute name, to do so we newd
             a map that maps feature index (feature number) to given value index
             */
    
            HashMap<Integer,Integer> featureIndexNumberMap = new HashMap<Integer,Integer>(attributesSize);
            //Initialization with zeros, because, Infosel may return just a feature subset
            for (int i = 0; i < attributesSize; i++) {
                featureIndexNumberMap.put(i+1, 0);
            }
            //Assigning weights to given feature by numbering each feature
            int[] fi = featureIndexMatrix[0];
            for (int i=0; i<fi.length; i++){
                featureIndexNumberMap.put(fi[i], attributesSize - i);
            }
            //Assigning real feature weights
            int j = 1; //Uwaga w Infosel cechy numerowane są od 1 w górę
            StringBuilder strAttrNames = new StringBuilder();
            StringBuilder strWeights = new StringBuilder();
            for (Attribute attribute : attributes) {
                String attrName = attribute.getName();
                double weight = featureIndexNumberMap.get(j);
                selectedFeaturesCount += (weight != 0)&(!Double.isNaN(weight))&(!Double.isInfinite(weight)) ? 1 : 0;
                attributeWeights.setWeight(attrName,weight);
                strAttrNames.append(attrName);
                strAttrNames.append(";");
                strWeights.append(weight);
                strWeights.append(";");
                j++;
            }
            stringRepresentationOfFeatureWeights = strAttrNames.toString() + " \n " + strWeights.toString();
            input.delete();
        } catch (IOException e) {
            throw new OperatorException("Unable to process or create input file for Infosel library");
        }

        return attributeWeights;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();

        ParameterType featureSelectionAlgorithmParameter = new ParameterTypeString(SELECTION_METHOD, "Feature selection method","mi_mi(0)");
        featureSelectionAlgorithmParameter.setExpert(false);
        types.add(featureSelectionAlgorithmParameter);

        ParameterType discretizationAlgorithmParameter = new ParameterTypeString(DISCRETIZATION_METHOD, "Discretization algorithm name","equiwidth(24)");
        discretizationAlgorithmParameter.setExpert(false);
        types.add(discretizationAlgorithmParameter);

        ParameterType infoselPathParameter = //new ParameterTypeString(INFOSEL_PATH, "Path to the infosel library",infoselPath);
                new ParameterTypeFile(INFOSEL_PATH, "Path to the infosel library","exe",infoselPath);
        infoselPathParameter.setExpert(false);
        types.add(infoselPathParameter);
        
        return types;
    }
    
        @Override
    public boolean supportsCapability(OperatorCapability capability) {
                
        switch (capability) {
            case BINOMINAL_ATTRIBUTES:
            case POLYNOMINAL_ATTRIBUTES:        
            case NUMERICAL_ATTRIBUTES:                
            case POLYNOMINAL_LABEL:
            case BINOMINAL_LABEL:            
                return true;
            default:
                return false;
        }
    }

}
