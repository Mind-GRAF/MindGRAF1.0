package edu.guc.mind_graf.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.MGIfNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class MGIfNodeTest {
    @BeforeEach
    void setUp() {
        new Network();
        System.out.println("Testing MGIf Node");
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

        NetworkController.setUp(attitudeNames,consistentAttitudes ,false,false,false,1);
        ContextController.createNewContext("guc");
    }

    @Test
    void test1(){
        try {
            System.out.println("Testing example 1");
            ContextController.setCurrContext("guc");

			Node act = Network.createNode("act", "actnode");
            Node guard = Network.createNode("guard", "propositionnode");

            Relation actR = Network.createRelation("act", "", Adjustability.NONE, 0);
            Relation guardR = Network.createRelation("guard", "", Adjustability.NONE, 0);
            Relation obj = Network.createRelation("obj", "", Adjustability.NONE, 0);

            ContextController.getContext(ContextController.getCurrContextName()).addHypothesisToContext(0, 0, (PropositionNode)guard);

            DownCable d1 = new DownCable(actR, new NodeSet(act));
            DownCable d2 = new DownCable(guardR, new NodeSet(guard));

            Node guardedAct = Network.createNode("actnode", new DownCableSet(d1, d2));

            DownCable d3 = new DownCable(obj, new NodeSet(guardedAct));

            MGIfNode M0 = new MGIfNode(new DownCableSet(d3));

            Scheduler.initiate();

            Scheduler.addToActQueue(M0);

            Scheduler.schedule();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
