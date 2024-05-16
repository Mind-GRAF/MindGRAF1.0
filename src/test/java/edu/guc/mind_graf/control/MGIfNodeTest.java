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
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.MGIfNode;
import edu.guc.mind_graf.nodes.Node;
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
            Node maro = Network.createNode("doit", "actnode");
            Node snif = Network.createNode("snif", "actnode");
            Node guard1 = Network.createNode("guard", "propositionnode");
            Relation obj = Network.createRelation("obj", "", Adjustability.NONE,2);
            Relation guard = Network.createRelation("guard", "", Adjustability.NONE,2);
            Relation act = Network.createRelation("act", "", Adjustability.NONE,2);
            Relation action = Network.createRelation("action", "", Adjustability.NONE,2);

            NodeSet ns1 = new NodeSet();
            ns1.add(maro);
            NodeSet ns2 = new NodeSet();
            ns2.add(guard1);
            NodeSet ns4 = new NodeSet();
            ns4.add(snif);

            DownCable downCableMemM0 = new DownCable(act, ns1);
            DownCable downCableMemM1 = new DownCable(guard, ns2);
            DownCable downCableClassM0 = new DownCable(action, ns4);

            DownCableSet downCableSetM0 = new DownCableSet(downCableMemM0,downCableMemM1);

            ActNode guardedAct = new ActNode(downCableSetM0);

            NodeSet ns3 = new NodeSet();
            ns3.add(guardedAct);

            DownCable downCableMemM2 = new DownCable(obj, ns3);

            DownCableSet downCableSetM1 = new DownCableSet(downCableMemM2,downCableClassM0);

            MGIfNode M0 = new MGIfNode(downCableSetM1);

            Scheduler.initiate();

            Scheduler.addToActQueue(M0);

            Scheduler.schedule();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
