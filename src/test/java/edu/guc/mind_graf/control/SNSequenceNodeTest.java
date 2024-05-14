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
import edu.guc.mind_graf.nodes.SNSequenceNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class SNSequenceNodeTest {
    Network n;
    @BeforeEach
    void setUp() {
        n = new Network();
        System.out.println("Testing SNSequence Node");
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
            Node maro2 = Network.createNode("dontdoit", "actnode");
            Node maro3 = Network.createNode("pleasedontdoit", "actnode");
            Node snsequence = Network.createNode("snsequence", "actnode");
            Relation obj1 = Network.createRelation("obj1", "", Adjustability.NONE,2);
            Relation obj2 = Network.createRelation("obj2", "", Adjustability.NONE,2);
            Relation obj3 = Network.createRelation("obj3", "", Adjustability.NONE,2);
            Relation action = Network.createRelation("action", "", Adjustability.NONE,2);

            NodeSet ns1 = new NodeSet();
            ns1.add(maro);
            NodeSet ns2 = new NodeSet();
            ns2.add(maro2);
            NodeSet ns3 = new NodeSet();
            ns3.add(maro3);
            NodeSet ns4 = new NodeSet();
            ns4.add(snsequence);

            DownCable downCableMemM0 = new DownCable(obj1, ns1);
            DownCable downCableMemM1 = new DownCable(obj2, ns2);
            DownCable downCableMemM2 = new DownCable(obj3, ns3);
            DownCable downCableClassM0 = new DownCable(action, ns4);

            DownCableSet downCableSetM0 = new DownCableSet(downCableMemM0,downCableMemM1,downCableMemM2,downCableClassM0);

            SNSequenceNode M0 = new SNSequenceNode(downCableSetM0);

            Scheduler.initiate();

            M0.runActuator();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
