package edu.guc.mind_graf.compression;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphBuilderTest {

    private static final String reality = "Reality";
    private static final String fiction = "Fiction";

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        // Initialize fresh network for each test
        Set<String, Integer> attitudes = new Set<>();
        attitudes.add("Desire", 0);
        attitudes.add("Belief", 1);
        attitudes.add("Promise", 2);
        NetworkController.setUp(attitudes, new ArrayList<>(), false, false, false, 1);
        ContextController.createNewContext(reality);
        ContextController.createNewContext(fiction);
    }

    @Test
    void shouldBuildEmptyGraphForEmptyContext() {
        GraphBuilder builder = new GraphBuilder(reality);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        assertAll(
                () -> assertEquals(0, graph.getSizeV1()),
                () -> assertEquals(0, graph.getSizeV2()),
                () -> assertEquals(0, graph.getAdjList().length),
                () -> assertEquals(0, graph.getAdjRevList().length));
    }

    @Test
    void shouldCreateHypothesisNodesForOriginHyps() throws Exception {
        // Setup
        Node man = Network.createNode("Man", "individualnode");
        Node fly = Network.createNode("Fly", "individualnode");
        Node bird = Network.createNode("Bird", "individualnode");
        Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        NodeSet ns1 = new NodeSet();
        ns1.add(man);
        NodeSet ns2 = new NodeSet();
        ns2.add(fly);
        NodeSet ns3 = new NodeSet();
        ns2.add(bird);
        DownCable downCable1 = new DownCable(Network.getRelations().get("verb"), ns2);
        DownCable downCable2 = new DownCable(Network.getRelations().get("agent"), ns1);
        DownCable downCable3 = new DownCable(Network.getRelations().get("verb"), ns2);
        DownCable downCable4 = new DownCable(Network.getRelations().get("agent"), ns3);

        DownCableSet downCableSet = new DownCableSet(downCable1, downCable2);
        DownCableSet downCableSet1 = new DownCableSet(downCable3, downCable4);

        PropositionNode m1Node = (PropositionNode) Network.createNode("propositionnode", downCableSet);
        PropositionNode m2Node = (PropositionNode) Network.createNode("propositionnode", downCableSet1);
        ContextController.getContext(reality).addHypothesisToContext(0, 0, m1Node);
        ContextController.getContext(reality).addHypothesisToContext(0, 1, m2Node);

        // Execute
        GraphBuilder builder = new GraphBuilder(reality);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        int node1ID = builder.hypothesesNodesRev.get(new HypNode(m1Node.getId(), 0));
        int node2ID = builder.hypothesesNodesRev.get(new HypNode(m2Node.getId(), 1));
        // Verify
        assertAll(
                () -> assertEquals(2, graph.getSizeV1()),
                () -> assertEquals(0, graph.getSizeV2()),
                () -> assertTrue(graph.getAdjList()[node1ID].isEmpty()),
                () -> assertTrue(graph.getAdjRevList()[node1ID].isEmpty()),
                () -> assertTrue(graph.getAdjList()[node2ID].isEmpty()),
                () -> assertTrue(graph.getAdjRevList()[node2ID].isEmpty()));
    }

    @Test
    void shouldCreateSupportNodesForSupportedHyps() throws Exception {
        // Setup
        Node x= Network.createVariableNode("X", "individualnode");
        Node man = Network.createNode("Man", "individualnode");
        Node fly = Network.createNode("Fly", "individualnode");
        Node creature = Network.createNode("Creature", "individualnode");
         Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        DownCable classCreature = new DownCable(Network.getRelations().get("class"), new NodeSet(creature));
        DownCable memberX = new DownCable(Network.getRelations().get("member"), new NodeSet(x));
        DownCable agentX = new DownCable(Network.getRelations().get("agent"), new NodeSet(x));
        DownCable verbFly = new DownCable(Network.getRelations().get("verb"), new NodeSet(fly));
        DownCable classCreature2 = new DownCable(Network.getRelations().get("class"), new NodeSet(creature));
        DownCable verbFly2 = new DownCable(Network.getRelations().get("verb"), new NodeSet(fly));
        DownCable memberMan = new DownCable(Network.getRelations().get("member"), new NodeSet(man));
        DownCable agentMan = new DownCable(Network.getRelations().get("agent"), new NodeSet(man));
        
        PropositionNode p1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(classCreature,memberX));
        PropositionNode p2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet( agentX, verbFly));
        DownCable forallX = new DownCable(Network.getRelations().get("forall"), new NodeSet(x));
        DownCable antP1 = new DownCable(Network.getRelations().get("ant"), new NodeSet(p1Node));
        DownCable cqP2 = new DownCable(Network.getRelations().get("cq"), new NodeSet(p2Node));
        PropositionNode m1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(antP1,cqP2,forallX));
        PropositionNode m2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(memberMan,classCreature2));
        PropositionNode m3Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet( agentMan, verbFly2));
        ContextController.getContext(fiction).addHypothesisToContext(0, 1, m1Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 1, m2Node);
        ContextController.getContext(fiction).addHypothesisToContext(0, 1, m3Node);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        PropositionNodeSet originSupport = new PropositionNodeSet();
        originSupport.add(m1Node.getId());
        originSupport.add(m2Node.getId());

        supportMap.put(1, new Pair<>(originSupport, new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet()));

        m3Node.addJustificationBasedSupports(1, 0, supports);
       


        // Execute
        GraphBuilder builder = new GraphBuilder(fiction);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        int supportedNodeID = builder.hypothesesNodesRev.get(new HypNode(m3Node.getId(), 1));
        HypNode hypNode1 = new HypNode(m1Node.getId(), 1);
        HypNode hypNode2 = new HypNode(m2Node.getId(), 1);
        int supportingNodeID1 = builder.hypothesesNodesRev.get(hypNode1);
        int supportingNodeID2 = builder.hypothesesNodesRev.get(hypNode2);
        ArrayList<HypNode> supportingNodes = new ArrayList<>();
        supportingNodes.add(hypNode1);
        supportingNodes.add(hypNode2);
        int supportID = builder.supportNodesRev.get(new SupportNode(supportingNodes)) + graph.getSizeV1();
        // Verify
        assertAll(
                () -> assertEquals(3, graph.getSizeV1()), // A and B
                () -> assertEquals(1, graph.getSizeV2()), // One support node
                () -> assertEquals(0, graph.getAdjList()[supportedNodeID].size()),
                () -> assertEquals(1, graph.getAdjRevList()[supportedNodeID].size()),
                () -> assertEquals(1, graph.getAdjList()[supportingNodeID1].size()),
                () -> assertEquals(0, graph.getAdjRevList()[supportingNodeID1].size()), 
                 () -> assertEquals(1, graph.getAdjList()[supportingNodeID2].size()),
                () -> assertEquals(0, graph.getAdjRevList()[supportingNodeID2].size()), 
                () -> assertEquals(1, graph.getAdjList()[supportID].size()),
                () -> assertEquals(2, graph.getAdjRevList()[supportID].size()) // Support connected to B
        );
    }

    @Test
    void shouldHandleMultiAttitudeSupports() throws Exception {
        // Setup
        Node x= Network.createVariableNode("X", "individualnode");
        Node avenge = Network.createNode("Avenge", "individualnode");
        Node murder = Network.createNode("Murder", "individualnode");
        Node thor = Network.createNode("Thor", "individualnode");
          Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        Relation objectRelation = Network.createRelation("object", "individualnode",
                Adjustability.NONE, 1);
        DownCable agentX = new DownCable(Network.getRelations().get("agent"), new NodeSet(x));
        DownCable verbAvenge = new DownCable(Network.getRelations().get("verb"), new NodeSet(avenge));
        DownCable objectMurder = new DownCable(Network.getRelations().get("object"), new NodeSet(murder));
        DownCable forallX = new DownCable(Network.getRelations().get("forall"), new NodeSet(x));
        PropositionNode p1Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(agentX, verbAvenge, objectMurder));
        DownCable antP1 = new DownCable(Network.createRelation("2-ant", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        DownCable cqP1 = new DownCable(Network.createRelation("1-cq", "", Adjustability.NONE, 1), new NodeSet(p1Node));
        PropositionNode m1Node = (PropositionNode)Network.createNode("bridgerule", new DownCableSet(forallX, antP1, cqP1));
        //inferred
        DownCable agentThor = new DownCable(Network.getRelations().get("agent"), new NodeSet(thor));
        DownCable verbAvenge1 = new DownCable(Network.getRelations().get("verb"), new NodeSet(avenge));
        DownCable objectMurder1 = new DownCable(Network.getRelations().get("object"), new NodeSet(murder));
        PropositionNode m2Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(agentThor, verbAvenge1, objectMurder1));
        ContextController.getContext(reality).addHypothesisToContext(0, 2, m2Node);
        ContextController.getContext(reality).addHypothesisToContext(0, 1, m2Node);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        PropositionNodeSet originSupport = new PropositionNodeSet();
        originSupport.add(m2Node.getId());

        supportMap.put(2, new Pair<>(originSupport, new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet(m1Node.getId())));

        m2Node.addJustificationBasedSupports(1, 0, supports);
       
       
        GraphBuilder builder = new GraphBuilder(reality);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        builder.printHypothesesNodes();
        int supportedNodeID = builder.hypothesesNodesRev.get(new HypNode(m2Node.getId(), 1));
        HypNode hypNode = new HypNode(m2Node.getId(), 2);
        int supportingNodeID = builder.hypothesesNodesRev.get(hypNode);
        ArrayList<HypNode> supportingNodes = new ArrayList<>();
        supportingNodes.add(hypNode);
        int supportID = builder.supportNodesRev.get(new SupportNode(supportingNodes)) + graph.getSizeV1();
        // Verify
        assertAll(
                () -> assertEquals(2, graph.getSizeV1()),
                () -> assertEquals(1, graph.getSizeV2()),
                () -> assertTrue(graph.getAdjList()[supportedNodeID].isEmpty()),
                () -> assertEquals(1, graph.getAdjRevList()[supportedNodeID].size()),
                () -> assertTrue(graph.getAdjRevList()[supportedNodeID].contains(supportID)),
                () -> assertTrue(graph.getAdjList()[supportingNodeID].contains(supportID)),
                () -> assertEquals(1, graph.getAdjList()[supportingNodeID].size()),
                () -> assertTrue(graph.getAdjRevList()[supportingNodeID].isEmpty()),
                () -> assertTrue(graph.getAdjList()[supportID].contains(supportedNodeID)),
                () -> assertTrue(graph.getAdjRevList()[supportID].contains(supportingNodeID)),
                () -> assertEquals(1, graph.getAdjList()[supportID].size()),
                () -> assertEquals(1, graph.getAdjRevList()[supportID].size()));
    }

    // @Test
    // void shouldHandleComplexSupportStructures() throws Exception {
    //     // Setup
    //     Node x= Network.createVariableNode("X", "individualnode");
    //     Node save = Network.createNode("Save", "individualnode");
    //     Node universe = Network.createNode("Universe", "individualnode");
    //     Node fight = Network.createNode("Fight", "individualnode");
    //     Node die = Network.createNode("Die", "individualnode");
    //     Node avengers = Network.createNode("Avengers", "individualnode");
    //     Node capAmerica= Network.createNode("Captain America", "individualnode");
    //     Node zero = Network.getBaseNodes().get("0");
    //     Relation agentRelation = Network.createRelation("agent", "individualnode",
    //             Adjustability.NONE, 1);
    //     Relation verbRelation = Network.createRelation("verb", "individualnode",
    //             Adjustability.NONE, 1);
    //     Relation objectRelation = Network.createRelation("object", "individualnode",
    //             Adjustability.NONE, 1);
    //     DownCable forall = new DownCable(Network.getRelations().get("forall"), new NodeSet(x));
    //     DownCable verbDie = new DownCable(Network.getRelations().get("verb"), new NodeSet(die));
    //     DownCable agentX = new DownCable(Network.getRelations().get("agent"), new NodeSet(x));
    //     PropositionNode p1Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(agentX, verbDie));
    //     DownCable arg = new DownCable(Network.getRelations().get("arg"), new NodeSet(p1Node));
    //     DownCable min0 = new DownCable(Network.getRelations().get("min"), new NodeSet(zero));
    //     DownCable max0 = new DownCable(Network.getRelations().get("max"), new NodeSet(zero));
    //     PropositionNode p2Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(arg, min0, max0));
    //     DownCable verbSave = new DownCable(Network.getRelations().get("verb"), new NodeSet(save));
    //     DownCable objectUniverse = new DownCable(Network.getRelations().get("object"), new NodeSet(universe));
    //     DownCable agentX1 = new DownCable(Network.getRelations().get("agent"), new NodeSet(x));
    //     PropositionNode p3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(agentX1, verbSave, objectUniverse));
    //     DownCable verbFight = new DownCable(Network.getRelations().get("verb"), new NodeSet(fight));
    //     DownCable objectAvenger = new DownCable(Network.getRelations().get("object"), new NodeSet(avengers));
    //     DownCable agentX2 = new DownCable(Network.getRelations().get("agent"), new NodeSet(x));
    //     PropositionNode p4Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(agentX2, verbFight, objectAvenger));
    //     Node m1Node = Network.createNode("andentailment", new DownCableSet(new DownCable(Network.getRelations().get("ant"), new NodeSet(p2Node,p3Node)),
    //             new DownCable(Network.getRelations().get("cq"), new NodeSet(p4Node))));
         
    //     PropositionNode m2Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(die)),new DownCable(Network.getRelations().get("agent"), new NodeSet(capAmerica))));
    //     PropositionNode m3Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(save)),new DownCable(Network.getRelations().get("object"), new NodeSet(universe)),new DownCable(Network.getRelations().get("agent"), new NodeSet(capAmerica))));
    //     PropositionNode m4Node = (PropositionNode)Network.createNode("propositionnode", new DownCableSet(new DownCable(Network.getRelations().get("verb"), new NodeSet(fight)),new DownCable(Network.getRelations().get("object"), new NodeSet(avengers)),new DownCable(Network.getRelations().get("agent"), new NodeSet(capAmerica))));
       
    //      ContextController.getContext(reality).addHypothesisToContext(0, 1, m1Node);
    //     ContextController.getContext(reality).addHypothesisToContext(0, 2, m2Node);
    //     ContextController.getContext(reality).addHypothesisToContext(0, 1, m2Node);




       

    //     addHypothesis(TEST_CONTEXT, 0, 0, node);
    //     addHypothesis(TEST_CONTEXT, 0, 0, sup1);
    //     addHypothesis(TEST_CONTEXT, 0, 0, sup2);
    //     addSupport(node, 0, 0, new int[] { sup1.getId(), sup2.getId() });

    //     // Execute
    //     GraphBuilder builder = new GraphBuilder(TEST_CONTEXT);
    //     BipartiteGraph graph = builder.buildBaseSupportGraph();
    //     int supportedNodeID = builder.hypothesesNodesRev.get(new HypNode(node.getId(), 0));
    //     HypNode hypNode1 = new HypNode(sup1.getId(), 0);
    //     HypNode hypNode2 = new HypNode(sup2.getId(), 0);
    //     int supportingNodeID1 = builder.hypothesesNodesRev.get(hypNode1);
    //     int supportingNodeID2 = builder.hypothesesNodesRev.get(hypNode2);
    //     ArrayList<HypNode> supportingNodes = new ArrayList<>();
    //     supportingNodes.add(hypNode1);
    //     supportingNodes.add(hypNode2);
    //     int supportID = builder.supportNodesRev.get(new SupportNode(supportingNodes)) + graph.getSizeV1();
    //     // Verify
    //     assertAll(
    //             () -> assertEquals(3, graph.getSizeV1()),
    //             () -> assertEquals(1, graph.getSizeV2()),
    //             () -> assertEquals(0, graph.getAdjList()[supportedNodeID].size()),
    //             () -> assertEquals(1, graph.getAdjRevList()[supportedNodeID].size()),
    //             () -> assertEquals(1, graph.getAdjList()[supportingNodeID1].size()),
    //             () -> assertEquals(0, graph.getAdjRevList()[supportingNodeID1].size()),
    //             () -> assertEquals(1, graph.getAdjList()[supportingNodeID2].size()),
    //             () -> assertEquals(0, graph.getAdjRevList()[supportingNodeID2].size()),
    //             () -> assertEquals(1, graph.getAdjList()[supportID].size()),
    //             () -> assertEquals(2, graph.getAdjRevList()[supportID].size()),
    //             () -> assertTrue(graph.getAdjRevList()[supportID].contains(supportingNodeID1)),
    //             () -> assertTrue(graph.getAdjRevList()[supportID].contains(supportingNodeID2)),
    //             () -> assertTrue(graph.getAdjList()[supportID].contains(supportedNodeID)),
    //             () -> assertTrue(graph.getAdjRevList()[supportedNodeID].contains(supportID)),
    //             () -> assertTrue(graph.getAdjList()[supportingNodeID1].contains(supportID)),
    //             () -> assertTrue(graph.getAdjList()[supportingNodeID2].contains(supportID)));
    // }





}