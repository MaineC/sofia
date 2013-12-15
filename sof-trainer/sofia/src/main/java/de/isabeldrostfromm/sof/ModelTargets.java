package de.isabeldrostfromm.sof;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelTargets {
    
    /** Possible prediction results */
    public static final HashMap<String, Integer> STATES = new HashMap<String, Integer>();
    public static final List<String> INDECES = new ArrayList<String>();
    public static final String[] STATEVALUES = {"open", "not a real question", "not constructive", "off topic", "too localized"};
    static {
        for (int i = 0; i < STATEVALUES.length; i++) {
            STATES.put(STATEVALUES[i], new Integer(i));
            INDECES.add(STATEVALUES[i]);
        }
    }
}