/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.operator.learner.tools;

import com.rapidminer.MacroHandler;
import com.rapidminer.datatable.DataTable;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.AttributeAddingExampleSetPassThroughRule;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author Marcin
 */
public class DummyOperator extends Operator {

    public static final String PARAMETER_STRING = "List of attributes separated by ';' ";
    private static final String nameExtation = " doff";
    protected final InputPort exampleSetInputPort = getInputPorts().createPort("ExampleSet");
    protected final OutputPort exampleSetOutputPort = getOutputPorts().createPort("ExampleSet");

    public DummyOperator(OperatorDescription description) {
        super(description);
        //ExampleSetMetaData md = 
        //getTransformer().addRule(new GenerateNewMDRule(exampleSetOutputPort,));
        getTransformer().addRule(new AttributeAddingExampleSetPassThroughRule(exampleSetInputPort, exampleSetOutputPort, null));
    }
@Override
    public void doWork() throws OperatorException {
    
        MacroHandler m = getProcess().getMacroHandler();
        m.addMacro(nameExtation, nameExtation);
                
        Operator operator = this;
        ExampleSet exampleSet = operator.getInputPorts().getPortByIndex(0).getData(ExampleSet.class);
        InputPort p = operator.getInputPorts().getPortByIndex(0);
        OutputPort o = operator.getOutputPorts().getPortByIndex(0);
        String strAttributes = getParameterAsString(DummyOperator.PARAMETER_STRING);
        String[] attributesDiff = strAttributes.split(";");
        String[] strTab = 
        {"PomiarTlen","Data Double","PomiarTb1","PomiarTb2","PomiarTb3","PomiarTb4","PomiarTb5","PomiarTb6","PomiarTb1Czas","PomiarTb2Czas","PomiarTb3Czas","PomiarTb4Czas","PomiarTb5Czas","PomiarTb6Czas"};
        
        HashMap<String,Double> h = new HashMap<String,Double>();
        for (String s : strTab)
        {
            h.put(s, 0.0);
        }
        for(Entry<String,Double> en : h.entrySet()){
            String attributeName = en.getKey();
            double attributeValue = en.getValue();            
        }
        
        ArrayList<Attribute> al = new ArrayList<Attribute>();
        for (String s : attributesDiff) {
            Attribute a = AttributeFactory.createAttribute(s + nameExtation, Ontology.REAL);
            a.setDefault(Double.NaN);
            al.add(a);
        }
        DataTable t;

        exampleSet.getExampleTable().addAttributes(al);
        for (Attribute a : al) {
            exampleSet.getAttributes().addRegular(a);            
        }

        Attributes as = exampleSet.getAttributes();
        Attribute nrWytopu = as.get("NrWytopu");

        double tmp = -1,
                oldValNrWytopu = -1;
        double[] oldVal = new double[attributesDiff.length];
        for (Example e : exampleSet) {
            double valNrWytopu = e.getValue(nrWytopu);
            if (oldValNrWytopu == valNrWytopu) {
                oldValNrWytopu = valNrWytopu;
                int i = 0;
                for (String s : attributesDiff) {
                    Attribute a = as.get(s);
                    Attribute diffA = as.get(s + nameExtation);
                    double val = e.getValue(a);
                    e.setValue(diffA, val - oldVal[i]);
                    oldVal[i] = val;
                    i++;
                }
            } else {
                oldValNrWytopu = valNrWytopu;
                int i = 0;
                for (String s : attributesDiff) {
                    Attribute a = as.get(s);
                    Attribute diffA = as.get(s + nameExtation);
                    oldVal[i] = e.getValue(a);                    
                    e.setValue(diffA,Double.NaN);
                    i++;
                }
            }
            
        }
        Date d = new Date();
        Iterator<Example> ex =  exampleSet.iterator();        
        operator.getOutputPorts().getPortByIndex(0).deliver(exampleSet);
        
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeString(PARAMETER_STRING, "List of attributes for differentiation separated by ';' ");
        types.add(type);
        return types;
    }
}

/*
 * 
 */