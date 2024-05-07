package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RevisionTest {
    Network n;

    @BeforeEach
    void setUp(){
        n = new Network();
        System.out.println("Testing Revision");
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
    void filterAttitudes() {

        ArrayList<ArrayList<Integer>> output = Revision.filterAttitudes(ContextController.getConsistentAttitudes(),0);
        ArrayList< ArrayList< Integer>> expected = new ArrayList<>();
        expected.add(new ArrayList<>(List.of(0)));
        expected.add(new ArrayList<>(List.of(0,1)));
        expected.add(new ArrayList<>(List.of(0,2)));
        expected.add(new ArrayList<>(List.of(0,2,3)));

        Assertions.assertEquals(expected,output);
    }

    @Test
    void testDetectingContradictions1() throws NoSuchTypeException {
        System.out.println("contradiction detection test 1");
        ContextController.setCurrContext("guc");
        PropositionNode p = (PropositionNode) Network.createNode("p", "propositionnode");
        HashMap<String, Relation> relations = Network.getRelations();

        NodeSet ns1 = new NodeSet();
        ns1.add(p);
        NodeSet ns2 = new NodeSet();
        ns2.add(Network.getBaseNodes().get("0"));

        DownCable downCable1 = new DownCable(relations.get("arg"), ns1);
        DownCable downCable2 = new DownCable(relations.get("min"), ns2);
        DownCable downCable3 = new DownCable(relations.get("max"), ns2);

        DownCableSet downCableSet = new DownCableSet(downCable1,downCable2,downCable3);

        PropositionNode notP = (PropositionNode) Network.createNode("propositionnode", downCableSet);

        ContextController.addHypothesisToContext("guc",0, p);
        ContextController.addHypothesisToContext("guc",0,  notP);
        Assertions.assertEquals(0,Revision.checkContradiction(ContextController.getContext("guc"),0, p).size());
    }
}