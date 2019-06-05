/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.prules.operator.learner.misc;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.rapidminer.example.set.SelectedExampleSet;
import com.rapidminer.operator.ValueDouble;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeAttributes;
import com.rapidminer.parameter.ParameterTypeBoolean;
import java.util.Map.Entry;
import org.prules.operator.learner.tools.DataIndex;
import org.prules.operator.learner.tools.IDataIndex;

/**
 *
 * @author Marcin
 */
public class SubtractExampleSets extends Operator {

    public static final String PARAMETER_INVERSE = "Inverse selection";
    public static final String PARAMETER_INCLUDE_LABEL = "Include label";
    public static final String PARAMETER_ATTRIBUTES = "Attributes";

    protected double numberOfMisses = 0;

    InputPort in1 = getInputPorts().createPort("exampleSet 1", ExampleSet.class);
    InputPort in2 = getInputPorts().createPort("exampleSet 2", ExampleSet.class);
    OutputPort out = getOutputPorts().createPort("exampleSet");
    OutputPort ori1 = getOutputPorts().createPort("original 1");
    OutputPort ori2 = getOutputPorts().createPort("original 2");

    public SubtractExampleSets(OperatorDescription description) {
        super(description);
        getTransformer().addPassThroughRule(in1, ori1);
        getTransformer().addPassThroughRule(in2, ori2);
        in1.addPrecondition(new ExampleSetPrecondition(in1));
        in2.addPrecondition(new ExampleSetPrecondition(in2));
        getTransformer().addPassThroughRule(in1, out);
        addValue(new ValueDouble("Missing elements", "The number Of misses - how meny times element from exampleSet2 did not appeared in exampleSet1") {

            @Override
            public double getDoubleValue() {
                return numberOfMisses;
            }
        });
    }

    @Override
    public void doWork() throws OperatorException {        
        super.doWork(); //To change body of generated methods, choose Tools | Templates.
        numberOfMisses = 0;
        ExampleSet exampleSet1 = in1.getDataOrNull(ExampleSet.class);
        ExampleSet exampleSet2 = in2.getDataOrNull(ExampleSet.class);        
        Map<String, ArrayList<Integer>> map = new HashMap<>(exampleSet1.size()); //Map used to identify identical rows - here we have List<Integer> becouse two rows may be identical, each with different id        
        //Create map from first file
        Attributes attributes1 = exampleSet1.getAttributes();
        Attributes attributes2 = exampleSet2.getAttributes();
        //Create common set of attributes
        List<Attribute> attributes1List = new ArrayList(attributes1.size());
        List<Attribute> attributes2List = new ArrayList(attributes2.size());
        for (Attribute a2 : attributes2) {
            Attribute a1 = attributes1.get(a2.getName());
            if (a1 != null) {
                attributes1List.add(a1);
                attributes2List.add(a2);
            }

        }
        int i = 0;
        boolean includeLabel = getParameterAsBoolean(PARAMETER_INCLUDE_LABEL);
        Attribute label1 = attributes1.getLabel();
        Attribute label2 = attributes2.getLabel();
        DataIndex dataIndex = new DataIndex(exampleSet1.size());
        dataIndex.setAllFalse();
        if (!(includeLabel && (label2 == null && label1 != null) || (label1 == null && label2 != null))) {        
            for (Example ex : exampleSet1) {
                StringBuilder sb = new StringBuilder();
                for (Attribute a : attributes1List) {
                    String val;
                    //if (a.isNominal()){
                    val = ex.getValueAsString(a);
                    //} else {
                    //    val = Double.toString(ex.getValue(a));

                    //}
                    sb.append(val);
                }
                if (includeLabel) {
                    sb.append("|").append(ex.getValueAsString(label1));
                }
                String s = sb.toString();
                if (map.containsKey(s)) { //If given row already exist
                    map.get(s).add(i);   //Add new id             
                } else {
                    ArrayList<Integer> li = new ArrayList<>(); //Create new list and add ID
                    li.add(i);
                    map.put(s, li);
                }
                i++;
            }
            //Now analyze second exampleSet                        
            for (Example ex : exampleSet2) {
                //Extract row as string
                StringBuilder sb = new StringBuilder();
                for (Attribute a : attributes2List) {
                    String val = ex.getValueAsString(a);
                    sb.append(val);
                }
                if (includeLabel) {
                    sb.append("|").append(ex.getValueAsString(label2));
                }
                String s = sb.toString();
                //Scheck if map contains the string
                ArrayList<Integer> l = map.get(s);
                if (l != null) {
                    if (!l.isEmpty()) {
                        l.remove(0); //Remove first occurance
                    }                //We do not remove from map 
                    //if (l.isEmpty()) { //If list is empty remove from map
                    //    map.remove(s);
                    //}
                } else { //The number of times the row didn't appear 
                    numberOfMisses++;
                }
            }

            for (Entry<String, ArrayList<Integer>> val : map.entrySet()) {
                for (int j : val.getValue()) {
                    dataIndex.set(j, true);
                }
            }
        }
        if (getParameterAsBoolean(PARAMETER_INVERSE)) {
            dataIndex.negate();
        }
        SelectedExampleSet output = new SelectedExampleSet(exampleSet1, dataIndex);
        out.deliver(output);
        ori1.deliver(exampleSet1);
        ori2.deliver(exampleSet2);
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type;
        //type = new ParameterTypeAttributes(PARAMETER_ATTRIBUTES,"Select attributes", in2);
        //types.add(type);
        type = new ParameterTypeBoolean(PARAMETER_INVERSE, "Inverse selection", false, false);
        types.add(type);
        type = new ParameterTypeBoolean(PARAMETER_INCLUDE_LABEL, "Include label attribute", true, false);
        types.add(type);
        return types;
    }

}
