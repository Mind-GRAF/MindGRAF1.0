package edu.guc.mind_graf.network;

import java.util.Hashtable;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.set.ContextSet;

public class Controller {
    private static String currContext = "Cartoon";
    private static ContextSet contextSet = new ContextSet(currContext);
    private static Hashtable<Integer, String> attitudeNames = new Hashtable<Integer, String>();

    public String getPropositionAttitudeName(Integer prop, Context c) {
        // get the key value from the attitudeNames hashtable
        System.out.println(c.getPropositionAttitude(prop));
        return attitudeNames.get(c.getPropositionAttitude(prop));
    }

    public static Context getContext(String contextName) {
        return contextSet.getContext(contextName);
    }

    public static String getCurrContext() {
        return currContext;
    }

    public static void setCurrContext(String currContext) {
        Controller.currContext = currContext;
    }

    public static ContextSet getContextSet() {
        return contextSet;
    }

    public static void setContextSet(ContextSet contextSet) {
        Controller.contextSet = contextSet;
    }

    public static Hashtable<Integer, String> getAttitudeNames() {
        return attitudeNames;
    }

    public static void setAttitudeNames(Hashtable<Integer, String> attitudeNames) {
        Controller.attitudeNames = attitudeNames;
    }

}
