package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    Network n;


    @BeforeEach
    void setUp() {
        System.out.println("Testing Nodes");
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

        n = NetworkController.setUp(attitudeNames, consistentAttitudes, false,false,false,1);
        ContextController.createNewContext("guc");
        ContextController.setCurrContext("guc");
    }

    @Test
    void getNegation() throws NoSuchTypeException {
        System.out.println("negation test 1");
        Node p = Network.createNode("p", "propositionnode");
        HashMap<String, Relation> relations = Network.getRelations();

        NodeSet ns1 = new NodeSet();
        ns1.add(p);
        NodeSet ns2 = new NodeSet();
        ns2.add(Network.getBaseNodes().get("0"));

        DownCable downCable1 = new DownCable(relations.get("arg"), ns1);
        DownCable downCable2 = new DownCable(relations.get("min"), ns2);
        DownCable downCable3 = new DownCable(relations.get("max"), ns2);

        DownCableSet downCableSet = new DownCableSet(downCable1,downCable2,downCable3);

        Node notP = Network.createNode("propositionnode", downCableSet);

        assertEquals(notP,p.getNegation());
        assertEquals(p,notP.getNegation());

    }
}