package context;

import java.util.*;

import set.ContextSet;
import set.Set;
import network.Network;

public class ContextController {
    private static Context currContext;
    private static ContextSet contextSet;
    private static Set<String,Integer> attitudes = new Set<>();
    private static Network network;
    private static boolean uvbrEnabled;
    
    
    
    private static ArrayList<ArrayList<Integer>> consistentAttitudes;
    
    public static void setUp(Set<String,Integer> attitudeNames, ArrayList<ArrayList<Integer>> consistentAttitudes, boolean uvbrEnabled){
        network = new Network();
        ContextController.attitudes = attitudeNames;
        ContextController.uvbrEnabled = uvbrEnabled;
        ContextController.consistentAttitudes = consistentAttitudes;
        contextSet = new ContextSet();
        
    }
    public static void setCurrContext(String currContext) {
        Context c = contextSet.get(currContext);
        if(c == null){
            throw new RuntimeException("no such context exists");
        }
        ContextController.currContext = c;
    }
    
    public static int getAttitudeNumber(String attitudeName) {
        return attitudes.get(attitudeName);
    }

    public static Context getContext(String contextName) {
        Context c = contextSet.get(contextName);
        if(c == null){
            throw new RuntimeException("no such context exists");
        }
        return c;
    }

    public static String getCurrContextName() {
        return currContext.getName();
    }

    public static ContextSet getContextSet() {
        return contextSet;
    }
    
    public static ArrayList<ArrayList<Integer>> getConsistentAttitudes() {
        return consistentAttitudes;
    }

    public static Set<String,Integer> getAttitudes() {
        return attitudes;
    }

    

}
