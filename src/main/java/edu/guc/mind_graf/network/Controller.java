package edu.guc.mind_graf.network;

import java.util.Hashtable;
import java.util.Map;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.set.ContextSet;

public class Controller {
    private static Context currContext;
    private static ContextSet contextSet;
    private static Hashtable<Integer, String> attitudeNames = new Hashtable<Integer, String>();
    private static Network network;
    private static boolean uvbrEnabled;
    
    public static void setUp(Hashtable<Integer, String> attitudeNames, boolean uvbrEnabled){
        network = new Network();
        Controller.attitudeNames = attitudeNames;
        Controller.uvbrEnabled = uvbrEnabled;
        contextSet = new ContextSet();
        
    }
    public static void setCurrContext(String currContext) {
        Context c = contextSet.get(currContext);
        if(c == null){
            throw new RuntimeException("no such context exists");
        }
        Controller.currContext = c;
    }
    
    public String getPropositionAttitudeName(Integer prop, Context c) {
        // get the key value from the attitudeNames hashtable
        System.out.println(c.getPropositionAttitude(prop));
        return attitudeNames.get(c.getPropositionAttitude(prop));
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

//    public static void setContextSet(ContextSet contextSet) {
//        Controller.contextSet = contextSet;
//    }
    
    public static void createNewContext(String name){
        if(contextSet.contains(name)){
            throw new RuntimeException("context Already Exists");
        }
        Context c = new Context(name, Controller.attitudeNames);
        contextSet.add(name,c);
    }

    public static Hashtable<Integer, String> getAttitudeNames() {
        return attitudeNames;
    }
    

}
