package edu.guc.mind_graf.network;

import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.set.Set;

import java.util.ArrayList;

public class NetworkController {
    private static Network network;
    private static boolean uvbrEnabled;

    public static Network setUp(Set<String, Integer> attitudeNames, ArrayList<ArrayList<Integer>> consistentAttitudes, boolean uvbrEnabled, boolean automaticHandlingEnabled,boolean cacheEnabled, int mergeFunctionNumber) {
        NetworkController.network = new Network();
        ContextController.setup(attitudeNames,consistentAttitudes,automaticHandlingEnabled,cacheEnabled,mergeFunctionNumber);
        //TODO: wael attitudes closed under conjunction & consequence & telescoping attitudes and uvbrEnabled
        NetworkController.uvbrEnabled = uvbrEnabled;
        return NetworkController.network;
    }

    public static boolean isUvbrEnabled() {
        return uvbrEnabled;
    }
}
