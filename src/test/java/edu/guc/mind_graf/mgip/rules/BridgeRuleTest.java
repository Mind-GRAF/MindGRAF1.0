package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BridgeRuleTest {

    @BeforeEach
    void setup(){
        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("beliefs", 0);
        attitudeNames.add("obligations", 1);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0, 1)));

        Network network = NetworkController.setUp(attitudeNames,consistentAttitudes,false,false,false,1);
        ContextController.createNewContext("Mythology");
        ContextController.setCurrContext("Mythology");
    }

    @Test
    void applyRuleHandler() throws NoSuchTypeException {
        Scheduler.initiate();

        // belief
        Relation evil = Network.createRelation("evil", "", Adjustability.EXPAND, 2);
        Node X = Network.createVariableNode("X", "propositionnode");
        Node M0 = Network.createNode("propositionnode", new DownCableSet(new DownCable(evil, new NodeSet(X))));

        // capability
        Relation kill = Network.createRelation("kill", "", Adjustability.EXPAND, 2);
        Node M1 = Network.createNode("propositionnode", new DownCableSet(new DownCable(kill, new NodeSet(X))));

        // obligation
        Node M2 = Network.createNode("propositionnode", new DownCableSet(new DownCable(kill, new NodeSet(X))));

        // bridge rule
        DownCable belief = new DownCable(Network.createRelation("1-ant", "", Adjustability.EXPAND, 2), new NodeSet(M0));
        DownCable capability = new DownCable(Network.createRelation("2-ant", "", Adjustability.EXPAND, 2), new NodeSet(M1));
        DownCable obligation = new DownCable(Network.createRelation("3-cq", "", Adjustability.EXPAND, 2), new NodeSet(M2));
        Node P0 = Network.createNode("bridgerule", new DownCableSet(belief, capability, obligation));

        Node paris = Network.createNode("paris", "propositionnode");
        Substitutions subs1 = new Substitutions();
        subs1.add(X, paris);
        Report bReport = new Report(subs1, new Support(-1), 1, true, InferenceType.BACKWARD, P0, M0);
        bReport.setContextName("Mythology");

        Substitutions subs2 = new Substitutions();
        subs2.add(X, paris);
        Report cReport = new Report(subs2, new Support(-1), 2, true, InferenceType.BACKWARD, P0, M1);
        cReport.setContextName("Mythology");

        /*System.out.println(X);
        System.out.println(M0);
        System.out.println(M1);
        System.out.println(M2);
        System.out.println(P0);*/

        ((RuleNode)P0).applyRuleHandler(bReport);
        ((RuleNode)P0).applyRuleHandler(cReport);
        assertEquals(1, Scheduler.getHighQueue().size());

    }
}