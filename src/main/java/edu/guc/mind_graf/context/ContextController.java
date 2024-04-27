package edu.guc.mind_graf.context;

import java.util.ArrayList;

import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.ContextSet;
import edu.guc.mind_graf.set.Set;

public class ContextController {
    private static Context currContext;
    private static ContextSet contextSet;
    private static Set<String,Integer> attitudes = new Set<>();
    private static Network network;
    private static boolean uvbrEnabled;
    private static boolean automaticHandling;

    
    
    private static ArrayList<ArrayList<Integer>> consistentAttitudes;
    
    public static void setUp(Set<String,Integer> attitudeNames, ArrayList<ArrayList<Integer>> consistentAttitudes, boolean uvbrEnabled){
        network = new Network();
        ContextController.attitudes = attitudeNames;
        ContextController.uvbrEnabled = uvbrEnabled;
        ContextController.consistentAttitudes = consistentAttitudes;
        ContextController.automaticHandling = false;
        //TODO: wael update the setup method
        contextSet = new ContextSet();
        
    }
    public static void setCurrContext(String currContext) {
        Context c = contextSet.get(currContext);
        if(c == null){
            throw new RuntimeException("no such context exists");
        }
        ContextController.currContext = c;
    }
    
    public int getAttitudeNumber(String attitudeName) {
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
    
    public static void createNewContext(String name){
        if(contextSet.contains(name)){
            throw new RuntimeException("context Already Exists");
        }
        Context c = new Context(name, ContextController.attitudes);
        contextSet.add(name,c);
    }

    public static Set<String,Integer> getAttitudes() {
        return attitudes;
    }

    public static boolean isAutomaticHandling() {
        return automaticHandling;
    }

    public static void addToContext(String contextName, int attitudeNumber, int nodeId) {
        Context c = ContextController.getContext(contextName);
        PropositionNode n = (PropositionNode) Network.getNodeById(nodeId);
        Context.addHypothesisToContext(c,attitudeNumber, n);
    }

    public static void removeFromContext(String contextName, int attitudeNumber, int nodeId) {
        Context c = ContextController.getContext(contextName);
        PropositionNode n = (PropositionNode) Network.getNodeById(nodeId);
        Context.removeHypothesisFromContext(c,attitudeNumber, n);
    }

}
