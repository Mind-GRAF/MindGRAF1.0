package edu.guc.mind_graf.control;

import java.util.ArrayList;
import java.util.List;

import edu.guc.mind_graf.network.NetworkController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.WithSomeNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class WithSomeNodeTest {
    @BeforeEach
    void setUp() {
        new Network();
        System.out.println("Testing WithSome Node");
        Set<String,Integer> attitudeNames = new Set<>();
        attitudeNames.add( "beliefs",0);
        attitudeNames.add("obligations",1);
        attitudeNames.add("fears",2);
        attitudeNames.add("hate",3);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(List.of(0)));
        consistentAttitudes.add(new ArrayList<>(List.of(1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0,1)));
        consistentAttitudes.add(new ArrayList<>(List.of(0,2)));
        consistentAttitudes.add(new ArrayList<>(List.of(0,2,3)));

        NetworkController.setUp(attitudeNames, consistentAttitudes , false, false, false,1);
        ContextController.createNewContext("guc");
    }

    @Test
    void test1(){
        try {
            System.out.println("Testing example 1");
            ContextController.setCurrContext("guc");

            Node cs = Network.createNode("cs", "propositionnode");
            Node fun = Network.createNode("fun", "propositionnode");
            Node var1 = Network.createVariableNode("var1", "propositionnode");
            Node actionN = Network.createNode("pickup", "individualnode");

            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);
            Relation prop = Network.createRelation("prop", "", Adjustability.NONE, 0);
            Relation action = Network.createRelation("action", "", Adjustability.NONE, 0);
            Relation qualifiers = Network.createRelation("qualifiers", "", Adjustability.NONE, 0);

            DownCable d1 = new DownCable(obj, new NodeSet(cs));
            DownCable d2 = new DownCable(prop, new NodeSet(fun));

            DownCable d3 = new DownCable(obj, new NodeSet(var1));

            Node M0 = Network.createNode("propositionnode", new DownCableSet(d1, d2));
            Node M1 = Network.createNode("propositionnode", new DownCableSet(d3, d2));
            
            DownCable d4 = new DownCable(action, new NodeSet(actionN));
            
            Node actNode = Network.createNode("actnode", new DownCableSet(d4, d1));

            DownCable d5 = new DownCable(qualifiers, new NodeSet(M1));
            DownCable d6 = new DownCable(qualifiers, new NodeSet(M0));
            DownCable d7 = new DownCable(obj, new NodeSet(actNode));

            WithSomeNode M2 = new WithSomeNode(new DownCableSet(d5,d6,d7));

            Scheduler.initiate();

            Scheduler.addToActQueue(M2);

            Scheduler.schedule();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (Exception e){
        }
    }
}
