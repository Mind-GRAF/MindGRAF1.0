package edu.guc.mind_graf.compression;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphBuilderTest {

    private static final String TEST_CONTEXT = "test-context";

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        // Initialize fresh network for each test
        Set<String, Integer> attitudes = new Set<>();
        attitudes.add("att1", 0);
        attitudes.add("att2", 1);
        NetworkController.setUp(attitudes, new ArrayList<>(), false, false, false, 1);
        ContextController.createNewContext(TEST_CONTEXT);
    }

    @Test
    void shouldBuildEmptyGraphForEmptyContext() {
        GraphBuilder builder = new GraphBuilder(TEST_CONTEXT);
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
        PropositionNode node1 = createNode("1");
        PropositionNode node2 = createNode("2");
        addHypothesis(TEST_CONTEXT, 0, 0, node1);
        addHypothesis(TEST_CONTEXT, 0, 1, node2);

        // Execute
        GraphBuilder builder = new GraphBuilder(TEST_CONTEXT);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        int node1ID = builder.hypothesesNodesRev.get(new HypNode(node1.getId(), 0));
        int node2ID = builder.hypothesesNodesRev.get(new HypNode(node2.getId(), 1));
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
        PropositionNode supportedNode = createNode("A");
        PropositionNode supportingNode = createNode("B");
        addHypothesis(TEST_CONTEXT, 0, 0, supportedNode);
        addHypothesis(TEST_CONTEXT, 0, 0, supportingNode);
        addSupport(supportedNode, 0, 0, new int[] { supportingNode.getId() });

        // Execute
        GraphBuilder builder = new GraphBuilder(TEST_CONTEXT);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        int supportedNodeID = builder.hypothesesNodesRev.get(new HypNode(supportedNode.getId(), 0));
        HypNode hypNode = new HypNode(supportingNode.getId(), 0);
        int supportingNodeID = builder.hypothesesNodesRev.get(hypNode);
        ArrayList<HypNode> supportingNodes = new ArrayList<>();
        supportingNodes.add(hypNode);
        int supportID = builder.supportNodesRev.get(new SupportNode(supportingNodes)) + graph.getSizeV1();
        // Verify
        assertAll(
                () -> assertEquals(2, graph.getSizeV1()), // A and B
                () -> assertEquals(1, graph.getSizeV2()), // One support node
                () -> assertEquals(0, graph.getAdjList()[supportedNodeID].size()),
                () -> assertEquals(1, graph.getAdjRevList()[supportedNodeID].size()),
                () -> assertEquals(1, graph.getAdjList()[supportingNodeID].size()),
                () -> assertEquals(0, graph.getAdjRevList()[supportingNodeID].size()), // A connected to support
                () -> assertEquals(1, graph.getAdjList()[supportID].size()),
                () -> assertEquals(1, graph.getAdjRevList()[supportID].size()) // Support connected to B
        );
    }

    @Test
    void shouldHandleMultiAttitudeSupports() throws Exception {
        // Setup
        PropositionNode node = createNode("X");
        addHypothesis(TEST_CONTEXT, 0, 0, node);
        addHypothesis(TEST_CONTEXT, 0, 1, node);
        addSupport(node, 0, 0, new int[] { node.getId() }, 1); // Support from attitude 1

        // Execute
        GraphBuilder builder = new GraphBuilder(TEST_CONTEXT);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        builder.printHypothesesNodes();
    
        int supportedNodeID = builder.hypothesesNodesRev.get(new HypNode(node.getId(), 0));
        HypNode hypNode = new HypNode(node.getId(), 1);
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
                () -> assertEquals(1, graph.getAdjRevList()[supportID].size())
        );
    }

    @Test
    void shouldHandleComplexSupportStructures() throws Exception {
        // Setup
        PropositionNode node = createNode("Main");
        PropositionNode sup1 = createNode("Sup1");
        PropositionNode sup2 = createNode("Sup2");

        addHypothesis(TEST_CONTEXT, 0, 0, node);
        addHypothesis(TEST_CONTEXT, 0, 0, sup1);
        addHypothesis(TEST_CONTEXT, 0, 0, sup2);
        addSupport(node, 0, 0, new int[] { sup1.getId(), sup2.getId() });

        // Execute
        GraphBuilder builder = new GraphBuilder(TEST_CONTEXT);
        BipartiteGraph graph = builder.buildBaseSupportGraph();
        int supportedNodeID = builder.hypothesesNodesRev.get(new HypNode(node.getId(), 0));
        HypNode hypNode1 = new HypNode(sup1.getId(), 0);
        HypNode hypNode2 = new HypNode(sup2.getId(), 0);
        int supportingNodeID1 = builder.hypothesesNodesRev.get(hypNode1);
        int supportingNodeID2 = builder.hypothesesNodesRev.get(hypNode2);
        ArrayList<HypNode> supportingNodes = new ArrayList<>();
        supportingNodes.add(hypNode1);
        supportingNodes.add(hypNode2);
        int supportID = builder.supportNodesRev.get(new SupportNode(supportingNodes)) + graph.getSizeV1();
        // Verify
        assertAll(
                () -> assertEquals(3, graph.getSizeV1()), 
                () -> assertEquals(1, graph.getSizeV2()),
                () -> assertEquals(0, graph.getAdjList()[supportedNodeID].size()), 
                () -> assertEquals(1,  graph.getAdjRevList()[supportedNodeID].size()), 
                () -> assertEquals(1,  graph.getAdjList()[supportingNodeID1].size()),
                () -> assertEquals(0,  graph.getAdjRevList()[supportingNodeID1].size()), 
                () -> assertEquals(1,  graph.getAdjList()[supportingNodeID2].size()), 
                () -> assertEquals(0, graph.getAdjRevList()[supportingNodeID2].size()),
                () -> assertEquals(1, graph.getAdjList()[supportID].size()), 
                () -> assertEquals(2,  graph.getAdjRevList()[supportID].size()), 
                () -> assertTrue(graph.getAdjRevList()[supportID].contains(supportingNodeID1)),
                () -> assertTrue(graph.getAdjRevList()[supportID].contains(supportingNodeID2)),
                () -> assertTrue(graph.getAdjList()[supportID].contains(supportedNodeID)),
                () -> assertTrue(graph.getAdjRevList()[supportedNodeID].contains(supportID)),
                () -> assertTrue(graph.getAdjList()[supportingNodeID1].contains(supportID)),
                () -> assertTrue(graph.getAdjList()[supportingNodeID2].contains(supportID))
        );
    }

    private PropositionNode createNode(String id) throws NoSuchTypeException {
        return (PropositionNode) Network.createNode(id, "propositionnode");
    }

    private void addHypothesis(String context, int level, int attitude, PropositionNode node) {
        ContextController.getContext(context).addHypothesisToContext(level, attitude, node);
    }

    private void addSupport(PropositionNode node, int level, int attitude, int[] supportingNodes) {
        addSupport(node, level, attitude, supportingNodes, attitude);
    }

    private void addSupport(PropositionNode node, int level, int targetAttitude,
            int[] supportingNodes, int supportAttitude) {
        // Implementation matching your support structure
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        PropositionNodeSet originSupport = new PropositionNodeSet();
        for (int id : supportingNodes) {
            originSupport.add(id);
        }

        supportMap.put(supportAttitude, new Pair<>(originSupport, new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet()));

        Network.currentLevel = level;
        node.addJustificationBasedSupports(targetAttitude, level, supports);
    }
}