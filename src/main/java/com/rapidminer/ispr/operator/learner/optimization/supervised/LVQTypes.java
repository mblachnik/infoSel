package com.rapidminer.ispr.operator.learner.optimization.supervised;

import java.util.Map;

/**
 * 
 * @author Marcin
 */
public enum LVQTypes {

    LVQ1, LVQ2, LVQ21, LVQ3, OLVQ, WLVQ, SLVQ, 
    //MyLVQ2, MyLVQ21, MyLVQ3, 
     WTM_LVQ, SNG, GLVQ;
            
    public static final String PARAMETER_LVQ_TYPE = "LVQ Type";
    
    private static final String[] typeNames;
    
    static {
        LVQTypes[] fields = LVQTypes.values();
        typeNames = new String[fields.length];
        int i = 0;
        for (LVQTypes value : fields) {
            typeNames[i] = value.name();
            i++;
        }
    }
    
    public static String[] typeNames() {        
        return typeNames;
    }
}
