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
import edu.guc.mind_graf.nodes.DoAllNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;


public class DoAllNodeTest {
    Network n;
    @BeforeEach
    void setUp() {
        n = new Network();
        System.out.println("Testing DoAll Node");
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
            Node doall = Network.createNode("doall", "actnode");
            Relation obj = Network.createRelation("obj", "", Adjustability.NONE,2);
            Relation action = Network.createRelation("action", "", Adjustability.NONE,2);

            NodeSet ns1 = new NodeSet();
            ns1.add(maro);
            ns1.add(maro2);
            ns1.add(maro3);
            NodeSet ns4 = new NodeSet();
            ns4.add(doall);

            DownCable downCableMemM0 = new DownCable(obj, ns1);
            DownCable downCableClassM0 = new DownCable(action, ns4);

            DownCableSet downCableSetM0 = new DownCableSet(downCableMemM0,downCableClassM0);

            DoAllNode M0 = new DoAllNode(downCableSetM0);

            Scheduler.initiate();

            M0.runActuator();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
