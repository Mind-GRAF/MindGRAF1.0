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
import java.util.Arrays;
import java.util.HashMap;


public class KZeroCompressionIntegrationTest {

    private KZeroCompression compressor;
    private Context testContext1;
    private Context testContext2;

    @BeforeEach
    void setup() throws NoSuchTypeException {
        // Initialize fresh network for each test
        Set<String, Integer> attitudes = new Set<>();
        attitudes.add("att1", 0);
        attitudes.add("att2", 1);

        NetworkController.setUp(attitudes, new ArrayList<>(), false, false, false, 1);
        ContextController.createNewContext("test1");
        ContextController.createNewContext("test2");

        testContext1 = ContextController.getContext("test1");
        testContext2 = ContextController.getContext("test2");

        compressor = new KZeroCompression();
    }
    @Test
    void shouldKeepNodesUsedAsDerivedNodeInOneContextAndUsedAsOriginHypothesisInOther() throws Exception {
        // Setup
        PropositionNode node1 = createNode("context1Hyp");
        PropositionNode node2 = createNode("context2Hyp");

        // node1 is origin hypothesis in test1
        testContext1.addHypothesisToContext(0, 0, node1);
        testContext2.addHypothesisToContext(0, 0, node2);

        // node2 has no origin hypothesis references
        Network.currentLevel = 0;
        node1.addJustificationBasedSupports(0, 0, 
            new ArrayList<>(Arrays.asList(
                createSupportEntry(node2.getId())
            )));

        // Execute
        compressor.kZeroCompress();

        // Verify
        assertNotNull(Network.getNodeById(node2.getId()));
        assertNotNull(Network.getNodeById(node1.getId()));
    }

    @Test
    void shouldRemoveNodesNotUsedAsOriginHypothesis() throws Exception {
        // Setup
        PropositionNode node1 = createNode("1");
        PropositionNode node2 = createNode("2");

        // node1 is origin hypothesis in test1
        testContext1.addHypothesisToContext(0, 0, node1);

        // node2 has no origin hypothesis references
        Network.currentLevel = 0;
        node2.addJustificationBasedSupports(0, 0, 
            new ArrayList<>(Arrays.asList(
                createSupportEntry(node1.getId())
            )));

        // Execute
        compressor.kZeroCompress();

        // Verify
        assertNull(Network.getNodeById(node2.getId()));
        assertNotNull(Network.getNodeById(node1.getId()));
    }

    @Test
    void shouldKeepNodesUsedInMultipleContexts() throws Exception {
        // Setup
        PropositionNode sharedNode = createNode("shared");
        testContext1.addHypothesisToContext(0, 0, sharedNode);
        testContext2.addHypothesisToContext(0, 1, sharedNode);

        // Execute
        compressor.kZeroCompress();

        // Verify
        assertNotNull(Network.getNodeById(sharedNode.getId()));
    }

    @Test
    void shouldRemoveHypsFromNonZeroLevels() throws NoSuchTypeException {
        // Setup
        PropositionNode node = createNode("test");
        testContext1.addHypothesisToContext(1, 0, node);
        testContext1.addHypothesisToContext(0, 0, node);

        // Execute
        compressor.removeLowerLevelsHypsFromContexts();

        // Verify
        assertNull(testContext1.getOriginHypotheses(1, 0));
        assertNotNull(testContext1.getOriginHypotheses(0, 0));
    }



    private PropositionNode createNode(String id) throws NoSuchTypeException {
        return (PropositionNode) Network.createNode(id, "propositionnode");

    }

    private Pair createSupportEntry(int nodeId) {
        // Implementation matching your support structure
        // Explicit map creation
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        supportMap.put(0, new Pair<>(
                new PropositionNodeSet(nodeId),
                new PropositionNodeSet()));

        return new Pair<>(supportMap, new PropositionNodeSet());
    }
}