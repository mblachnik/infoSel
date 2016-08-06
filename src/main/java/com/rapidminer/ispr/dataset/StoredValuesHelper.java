/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rapidminer.ispr.dataset;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marcin
 */
public class StoredValuesHelper {
    public static final String LABEL = "label";
    public static final String CLUSTER = "cluster";
    public static final String WEIGHT = "weight";
    public static final String ID = "id";
    public static final String DISTANCE = "distance";
    public static final String INDEX = "index";
    
    public static IStoredValues createStoredValue(Example ex, Map<Attribute,String> attributes){
        SimpleValuesStore values = new SimpleValuesStore();
        for(Map.Entry<Attribute,String> a : attributes.entrySet()){    
            values.setValue(a.getValue(),ex.getValue(a.getKey()));
        }
        return values;
    }
    
    public static IStoredValues createStoredValue(Example ex){        
        Map<Attribute,String> map = new HashMap<>();
        map.put(ex.getAttributes().getLabel(),LABEL);
        map.put(ex.getAttributes().getCluster(),CLUSTER);
        map.put(ex.getAttributes().getId(),ID);        
        map.put(ex.getAttributes().getWeight(),WEIGHT);        
        return createStoredValue(ex, map);
    }
    
}
