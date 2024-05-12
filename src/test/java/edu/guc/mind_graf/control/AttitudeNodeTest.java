package edu.guc.mind_graf.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.AttitudeNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;

public class AttitudeNodeTest {
    Network n;
    @BeforeEach
    void setUp() {
        n = new Network();
        System.out.println("Testing Attitude Node");
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

        ContextController.setUp(attitudeNames,consistentAttitudes ,false);
        ContextController.createNewContext("guc");
    }

    @Test
    void test1(){
        try {
            System.out.println("Testing example 1");
            ContextController.setCurrContext("guc");
            Node maro = Network.createNode("maro", "propositionnode");
            Node beliefs = Network.createNode("beliefs", "actnode");
            Relation obj = Network.createRelation("obj", "", Adjustability.NONE,2);
            Relation action = Network.createRelation("action", "", Adjustability.NONE,2);

            NodeSet ns1 = new NodeSet();
            ns1.add(maro);
            NodeSet ns2 = new NodeSet();
            ns2.add(beliefs);

            DownCable downCableMemM0 = new DownCable(obj, ns1);
            DownCable downCableClassM0 = new DownCable(action, ns2);

            DownCableSet downCableSetM0 = new DownCableSet(downCableMemM0,downCableClassM0);

            AttitudeNode M0 = new AttitudeNode(downCableSetM0);

            M0.runActuator();

//			Node ruleNode1 = Network.createNode("ruleNode1", "rulenode");
        }catch (NoSuchTypeException e){
            e.printStackTrace();
        }
    }
}
