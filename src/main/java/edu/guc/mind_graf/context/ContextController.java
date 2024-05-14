package edu.guc.mind_graf.context;

import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.ContextSet;
import edu.guc.mind_graf.set.Set;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.IntBinaryOperator;

public class ContextController {
    private static Context currContext;
    private static ContextSet contextSet = new ContextSet();;
    private static Set<String, Integer> attitudes = new Set<String, Integer>();
    private static boolean automaticHandlingEnabled;
    private static boolean cacheEnabled;
    private static int mergeFunctionNumber;
    private static ArrayList<ArrayList<Integer>> consistentAttitudes;

    public static void setUp(Set<String, Integer> attitudeNames, ArrayList<ArrayList<Integer>> consistentAttitudes, boolean automaticHandlingEnabled, boolean cacheEnabled, int mergeFunctionNumber) {
        ContextController.attitudes = attitudeNames;
        ContextController.consistentAttitudes = consistentAttitudes;
        ContextController.automaticHandlingEnabled = false;
        ContextController.mergeFunctionNumber = 0;
        contextSet = new ContextSet();
    }

    public static void setCurrContext(String currContext) {
        Context c = contextSet.get(currContext);
        if (c == null) {
            throw new RuntimeException("no such context exists");
        }
        ContextController.currContext = c;
    }

    public static int getAttitudeNumber(String attitudeName) {
        return attitudes.get(attitudeName);
    }

    public static String getAttitudeName(int attitudeNumber) {
        for (Map.Entry<String, Integer> entry : attitudes.getSet().entrySet()) {
            if (entry.getValue() == attitudeNumber) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Context getContext(String contextName) {
        Context c = contextSet.get(contextName);
        if (c == null) {
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

    public static void createNewContext(String name) {
        if (contextSet.contains(name)) {
            throw new RuntimeException("context Already Exists");
        }
        Context c = new Context(name, ContextController.attitudes);
        contextSet.add(name, c);
    }

    public static Set<String, Integer> getAttitudes() {
        return attitudes;
    }

    public static boolean automaticHandlingEnabled() {
        return automaticHandlingEnabled;
    }

    public static void addHypothesisToContext(String contextName, int level, int attitudeId, PropositionNode node) {
        Context c = ContextController.getContext(contextName);
        c.addHypothesisToContext(level, attitudeId, node);
        //TODO: wael uncomment this it's removed to test
//        Revision.checkContradiction(c,attitudeId,node);
    }

    public static void addHypothesisToContext(int attitudeNumber, PropositionNode node) {
        ContextController.addHypothesisToContext(ContextController.currContext.getName(), 0, attitudeNumber, node);
    }

    public static void removeHypothesisFromContext(String contextName, int level, int attitudeId, int nodeId) {
        Context c = ContextController.getContext(contextName);
        PropositionNode n = (PropositionNode) Network.getNodeById(nodeId);
        c.removeHypothesisFromContext(level, attitudeId, n);
    }

    public static void removeHypothesisFromContext(int attitudeId, int nodeId) {
        ContextController.removeHypothesisFromContext(ContextController.getCurrContextName(), 0, attitudeId, nodeId);
    }

    public static IntBinaryOperator mergeGrades() {
        return switch (ContextController.mergeFunctionNumber) {
            case 2 -> Math::min;
            case 3 -> (a, b) -> (a + b) / 2;
            default -> Math::max;
        };
    }

    

}