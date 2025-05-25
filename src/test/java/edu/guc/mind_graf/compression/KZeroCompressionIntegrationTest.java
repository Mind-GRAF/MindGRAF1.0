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


public class KZeroCompressionIntegrationTest {

    private Context reality;
    private Context fiction;

    @BeforeEach
    void setup() throws NoSuchTypeException {
        // Initialize fresh network for each test
        Set<String, Integer> attitudes = new Set<>();
        attitudes.add("Desire", 0);
        attitudes.add("Belief", 1);
        attitudes.add("Promise", 2);
        attitudes.add("Fear", 3);

        NetworkController.setUp(attitudes, new ArrayList<>(), false, false, false, 1);
        ContextController.createNewContext("Reality");
        ContextController.createNewContext("Fiction");

        reality = ContextController.getContext("Reality");
        fiction = ContextController.getContext("Fiction");


    }
    @Test
    void shouldKeepNodesUsedAsDerivedNodeInOneContextAndUsedAsOriginHypothesisInOther() throws Exception {
        // Setup
        Node x= Network.createVariableNode("X", "individualnode");
        Node bird = Network.createNode("Bird", "individualnode");
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
        DownCable memberBird = new DownCable(Network.getRelations().get("member"), new NodeSet(bird));
        DownCable agentBird = new DownCable(Network.getRelations().get("agent"), new NodeSet(bird));
        
        PropositionNode p1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(classCreature,memberX));
        PropositionNode p2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet( agentX, verbFly));
        DownCable forallX = new DownCable(Network.getRelations().get("forall"), new NodeSet(x));
        DownCable antP1 = new DownCable(Network.getRelations().get("ant"), new NodeSet(p1Node));
        DownCable cqP2 = new DownCable(Network.getRelations().get("cq"), new NodeSet(p2Node));
        PropositionNode m1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(antP1,cqP2,forallX));
        PropositionNode m2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(memberBird,classCreature2));
        PropositionNode m3Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(agentBird, verbFly2));
        ContextController.getContext("Fiction").addHypothesisToContext(0, 1, m1Node);
        ContextController.getContext("Fiction").addHypothesisToContext(0, 1, m2Node);
        ContextController.getContext("Reality").addHypothesisToContext(0, 1, m3Node);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        PropositionNodeSet originSupport = new PropositionNodeSet();
        originSupport.add(m1Node.getId());
        originSupport.add(m2Node.getId());
        supportMap.put(1, new Pair<>(originSupport, new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet()));
        m3Node.addJustificationBasedSupports(1, 0, supports);
        // Execute
        KZeroCompression.kZeroCompress();
        // Verify
        assertNotNull(Network.getNodeById(m1Node.getId()));
        assertNotNull(Network.getNodeById(m2Node.getId()));
        assertNotNull(Network.getNodeById(m3Node.getId()));
    }

    @Test
    void shouldRemoveNodesNotUsedAsOriginHypothesis() throws Exception {
        // Setup
          Node x= Network.createVariableNode("X", "individualnode");
        Node bird = Network.createNode("Bird", "individualnode");
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
        DownCable memberBird = new DownCable(Network.getRelations().get("member"), new NodeSet(bird));
        DownCable agentBird = new DownCable(Network.getRelations().get("agent"), new NodeSet(bird));
        
        PropositionNode p1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(classCreature,memberX));
        PropositionNode p2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet( agentX, verbFly));
        DownCable forallX = new DownCable(Network.getRelations().get("forall"), new NodeSet(x));
        DownCable antP1 = new DownCable(Network.getRelations().get("ant"), new NodeSet(p1Node));
        DownCable cqP2 = new DownCable(Network.getRelations().get("cq"), new NodeSet(p2Node));
        PropositionNode m1Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(antP1,cqP2,forallX));
        PropositionNode m2Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(memberBird,classCreature2));
        PropositionNode m3Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(agentBird, verbFly2));
        ContextController.getContext("Fiction").addHypothesisToContext(0, 1, m1Node);
        ContextController.getContext("Fiction").addHypothesisToContext(0, 1, m2Node);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportMap = new HashMap<>();
        PropositionNodeSet originSupport = new PropositionNodeSet();
        originSupport.add(m1Node.getId());
        originSupport.add(m2Node.getId());
        supportMap.put(1, new Pair<>(originSupport, new PropositionNodeSet()));
        supports.add(new Pair<>(supportMap, new PropositionNodeSet()));
        m3Node.addJustificationBasedSupports(1, 0, supports);
        // Execute
        KZeroCompression.kZeroCompress();
        // Verify
        assertNotNull(Network.getNodeById(m1Node.getId()));
        assertNotNull(Network.getNodeById(m2Node.getId()));
        assertNull(Network.getNodeById(m3Node.getId()));
    }

    @Test
    void shouldKeepNodesUsedInMultipleContexts() throws Exception {
        // Setup
        Node bird = Network.createNode("Bird", "individualnode");
        Node fly = Network.createNode("Fly", "individualnode");
        Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        DownCable verbFly = new DownCable(Network.getRelations().get("verb"), new NodeSet(fly));
        DownCable agentBird = new DownCable(Network.getRelations().get("agent"), new NodeSet(bird));
        
        PropositionNode m3Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(agentBird, verbFly));
        fiction.addHypothesisToContext(0, 1, m3Node);
        reality.addHypothesisToContext(0, 0, m3Node);
        // Execute
        KZeroCompression.kZeroCompress();

        // Verify
        assertNotNull(Network.getNodeById(m3Node.getId()));
    }

    @Test
    void shouldRemoveHypsFromNonZeroLevels() throws NoSuchTypeException {
        // Setup
                Node bird = Network.createNode("Bird", "individualnode");
        Node fly = Network.createNode("Fly", "individualnode");
        Relation agentRelation = Network.createRelation("agent", "individualnode",
                Adjustability.NONE, 1);
        Relation verbRelation = Network.createRelation("verb", "individualnode",
                Adjustability.NONE, 1);
        DownCable verbFly = new DownCable(Network.getRelations().get("verb"), new NodeSet(fly));
        DownCable agentBird = new DownCable(Network.getRelations().get("agent"), new NodeSet(bird));
        
        PropositionNode m3Node = (PropositionNode) Network.createNode("propositionnode", new DownCableSet(agentBird, verbFly));
        reality.addHypothesisToContext(0, 0, m3Node);
        reality.addHypothesisToContext(1, 0, m3Node);

        // Execute
        KZeroCompression.kZeroCompress();

        // Verify
        assertNull(reality.getOriginHypotheses(1, 0));
        assertNotNull(reality.getOriginHypotheses(0, 0));
    }


}