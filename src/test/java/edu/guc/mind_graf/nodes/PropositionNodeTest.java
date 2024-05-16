package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class PropositionNodeTest {

    private PropositionNode node1;
    private PropositionNode node2;
    private PropositionNode node3;
    private PropositionNode node4;
    private PropositionNode node5;
    private PropositionNode node6;
    private PropositionNode node7;
    private PropositionNode node8;
    private Context context;

    @BeforeEach
    void setUp() {
        new Network();
        try {
            node1 = (PropositionNode) Network.createNode("1", "propositionnode");
            node2 = (PropositionNode) Network.createNode("2", "propositionnode");
            node3 = (PropositionNode) Network.createNode("3", "propositionnode");
            node4 = (PropositionNode) Network.createNode("4", "propositionnode");
            node5 = (PropositionNode) Network.createNode("5", "propositionnode");
            node6 = (PropositionNode) Network.createNode("6", "propositionnode");
            node7 = (PropositionNode) Network.createNode("7", "propositionnode");
            node8 = (PropositionNode) Network.createNode("8", "propositionnode");
        } catch (NoSuchTypeException e) {
            throw new RuntimeException(e);
        }

        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("Belief", 0);
        attitudeNames.add("Hate", 1);
        attitudeNames.add("Intend", 2);
        ContextController.setup(attitudeNames, new ArrayList<ArrayList<Integer>>(), false, false, 0);
        ContextController.createNewContext("Context1");

        Network.currentLevel = 1;
        node1.setHyp(0);
        node1.setHyp(1);
        node1.setHyp(2);

        node2.setHyp(0);

        node3.setHyp(1);

        node4.setHyp(2);

        node5.setHyp(0);

        Network.currentLevel = 0;
        node1.setHyp(0);
        node1.setHyp(1);
        node1.setHyp(2);

        node2.setHyp(0);

        node3.setHyp(1);

        node4.setHyp(2);

        node5.setHyp(0);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(0, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node6.addJustificationBasedSupports(0,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(0, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(1, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(0,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(0, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(1, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(0, new Pair<>(new PropositionNodeSet(6,7), new PropositionNodeSet()));
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        node8.addJustificationBasedSupports(0,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(0, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(0, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node8Justification = new HashMap<>();
        node8.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

    }

    @Test
    void testSupportedWithNoHypotheses() {

        assertFalse(node1.supported("Context1",0,0));
        assertFalse(node6.supported("Context1",0,0));
        assertFalse(node7.supported("Context1",0,0));
        assertFalse(node8.supported("Context1",0,0));

    }

    @Test
    void testSupported() {

        Network.currentLevel = 0;
        node1.setHyp("Context1",0);
        node1.setHyp("Context1",1);

        node2.setHyp("Context1",0);

        node3.setHyp("Context1",1);

        node4.setHyp("Context1",2);

        assertTrue(node6.supported("Context1", 0, Network.currentLevel));
        assertFalse(node6.supported("Context1", 1, Network.currentLevel));
        assertTrue(node7.supported("Context1", 0, Network.currentLevel));
        assertTrue(node7.supported("Context1", 2, Network.currentLevel));
        assertTrue(node8.supported("Context1", 1, Network.currentLevel));

    }
}