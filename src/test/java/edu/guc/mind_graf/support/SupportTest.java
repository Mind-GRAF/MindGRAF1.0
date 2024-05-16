package edu.guc.mind_graf.support;

import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SupportTest {

    private PropositionNode node1;
    private PropositionNode node2;
    private PropositionNode node3;
    private PropositionNode node4;
    private PropositionNode node5;
    private PropositionNode node6;
    private PropositionNode node7;
    private PropositionNode node8;

    @BeforeEach
    public void setUp() {
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
    }

    @Test
    public void testAddJustificationSupportForNewAttitudeAndLevelForAnEmptySupport(){
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> justificationSupport = new ArrayList<>();
        // Add test data to justificationSupport
        int attitudeID = 1;
        int level = 1;

        node1.addJustificationBasedSupports(attitudeID, level, justificationSupport);

        assertFalse(node1.getSupport().getJustificationSupport().containsKey(level));
        assertTrue(node1.getSupport().getJustificationSupport().isEmpty());
        assertFalse(node1.getSupport().getAssumptionSupport().containsKey(level));
        assertTrue(node1.getSupport().getAssumptionSupport().isEmpty());
    }

    @Test
    public void testAddJustificationSupportForExistingAttitudeAndLevel() {
        // First, add some initial support data
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
        hash.put(0, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> initialSupport = new ArrayList<>();
        initialSupport.add(new Pair<>(new HashMap<>(hash), new PropositionNodeSet()));
        // Add test data to initialSupport
        int attitudeID = 1;
        int level = 1;
        node1.addJustificationBasedSupports(attitudeID, level, initialSupport);

        // Now add additional support for the same attitude at the same level
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> additionalSupport = new ArrayList<>();
        hash.put(0, new Pair<>(new PropositionNodeSet(2), new PropositionNodeSet()));
        additionalSupport.add(new Pair<>(new HashMap<>(hash), new PropositionNodeSet()));
        // Add test data to additionalSupport

        node1.addJustificationBasedSupports(attitudeID, level, additionalSupport);

        assertEquals(1, node1.getSupport().getJustificationSupport().size());
        assertEquals(1, node1.getSupport().getJustificationSupport().get(level).size());
        assertEquals(2, node1.getSupport().getJustificationSupport().get(level).get(attitudeID).size());
        assertTrue(node1.getSupport().getJustificationSupport().get(level).get(attitudeID).containsAll(initialSupport));
        assertTrue(node1.getSupport().getJustificationSupport().get(level).get(attitudeID).containsAll(additionalSupport));
        assertTrue(node2.getJustificationSupportDependents().contains(1));
        assertTrue(node1.getSupport().getAssumptionSupport().isEmpty());
    }

    @Test
    public void testAddJustificationSupportWithDirectCycle() {

        int attitudeID = 1;
        int level = 1;

        Pair<PropositionNodeSet, PropositionNodeSet> innerpair = new Pair<>(new PropositionNodeSet(1), new PropositionNodeSet());
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
        hash.put(1, innerpair);
        Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> outerpair = new Pair<>(hash,new PropositionNodeSet());
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> justificationSupport = new ArrayList<>();
        justificationSupport.add(outerpair);

        node1.addJustificationBasedSupports(attitudeID, level, justificationSupport);

        assertTrue(node1.getSupport().getJustificationSupport().isEmpty());
    }

    @Test
    public void testAddJustificationSupportForAssumptionSupportWithNoGradesCrossProduct() {
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6,7), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node8JustificationInnerHash = new HashMap<>();
        node8JustificationInnerHash.put(1, supports);
        node8.addJustificationBasedSupports(1,Network.currentLevel, supports);

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        node8JustificationInnerHash.put(2, supports);
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node8Justification = new HashMap<>();
        node8Justification.put(Network.currentLevel, node8JustificationInnerHash);
        node8.addJustificationBasedSupports(2,Network.currentLevel, supports);

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> innerHash = new HashMap<>();
        innerHash.put(1, new ArrayList<>(supports));

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support2 = new HashMap<>();

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        innerHash.put(2, new ArrayList<>(supports2));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> assumptionSupport = new HashMap<>();
        assumptionSupport.put(Network.currentLevel, innerHash);

        assertEquals(node8Justification, node8.getSupport().getJustificationSupport());
        assertEquals(assumptionSupport, node8.getSupport().getAssumptionSupport());
    }


    @Test
    void testAddJustificationSupportForAssumptionSupportWithNoGradesCrossProductInDifferentLevels() {
        Network.currentLevel = 1;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        Network.currentLevel = 0;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6,7), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node8JustificationInnerHash = new HashMap<>();
        node8JustificationInnerHash.put(1, new ArrayList<>(supports));
        node8.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node8JustificationInnerHash.put(2, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node8Justification = new HashMap<>();
        node8Justification.put(Network.currentLevel, node8JustificationInnerHash);
        node8.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> innerHash = new HashMap<>();
        innerHash.put(1, supports);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support2 = new HashMap<>();

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet()));
        innerHash.put(2,supports2);

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> assumptionSupport = new HashMap<>();
        assumptionSupport.put(Network.currentLevel, innerHash);
        assumptionSupport.put(1, innerHash);

        assertEquals(node8Justification, node8.getSupport().getJustificationSupport());
        assertEquals(assumptionSupport, node8.getSupport().getAssumptionSupport());

    }

    @Test
    void testRemoveNodeFromJustifications() {
        Network.currentLevel = 1;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        Network.currentLevel = 0;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        node1.removeNodeFromOtherNodesSupport();

        assertTrue(node6.getSupport().getJustificationSupport().isEmpty());

        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node7InnerHash = new HashMap<>();

        support.clear();
        supports.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7InnerHash.put(1, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7InnerHash.put(3, new ArrayList<>(supports));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node7Support = new HashMap<>();
        node7Support.put(0, node7InnerHash);

        assertEquals(node7Support, node7.getSupport().getJustificationSupport());
    }

    @Test
    void testRemoveNodeFromAssumptions() {

        Network.currentLevel = 1;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        Network.currentLevel = 0;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6,7), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node8JustificationInnerHash = new HashMap<>();
        node8JustificationInnerHash.put(1, new ArrayList<>(supports));
        node8.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(7), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(6), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node8JustificationInnerHash.put(2, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node8Justification = new HashMap<>();
        node8Justification.put(Network.currentLevel, node8JustificationInnerHash);
        node8.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        node1.removeNodeFromOtherNodesSupport();

        assertTrue(node6.getSupport().getAssumptionSupport().isEmpty());

        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node7InnerHash = new HashMap<>();

        support.clear();
        supports.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7InnerHash.put(1, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7InnerHash.put(3, new ArrayList<>(supports));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node7Support = new HashMap<>();
        node7Support.put(0, node7InnerHash);
        node7Support.put(1, node7InnerHash);

        assertEquals(node7Support, node7.getSupport().getAssumptionSupport());


        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node8InnerHash = new HashMap<>();
        support.clear();
        supports.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node8InnerHash.put(1, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node8Support = new HashMap<>();
        node8Support.put(0, node8InnerHash);
        node8Support.put(1, node8InnerHash);

        assertEquals(node8Support, node8.getSupport().getAssumptionSupport());

    }

    @Test
    void testUnion(){

        Network.currentLevel = 0;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> testSupport = new HashMap<>();
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> testSupportInnerHash = new HashMap<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        testSupportInnerHash.put(1, new ArrayList<>(supports));
        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupportInnerHash.put(2, new ArrayList<>(supports));
        testSupport.put(0, new HashMap<>(testSupportInnerHash));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        testSupportInnerHash.clear();
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupport.get(0).get(1).addAll(new ArrayList<>(supports));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupport.get(0).put(3, new ArrayList<>(supports));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        node7.unionSupport(node6.getSupport());
        assertTrue(node7.getSupport().getJustificationSupport().get(0).get(1).containsAll(testSupport.get(0).get(1)));
        assertTrue(node7.getSupport().getJustificationSupport().get(0).get(2).containsAll(testSupport.get(0).get(2)));
        assertTrue(node7.getSupport().getJustificationSupport().get(0).get(3).containsAll(testSupport.get(0).get(3)));

    }

    @Test
    void testCombine(){

        Network.currentLevel = 0;
        node1.setHyp(1);
        node1.setHyp(2);
        node1.setHyp(3);

        node2.setHyp(1);

        node3.setHyp(2);

        node4.setHyp(3);

        node5.setHyp(1);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> testSupports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(1,4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(1,2,5), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(1,3), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));



        node7.combineSupport(1, node6.getSupport());
        assertTrue(node7.getSupport().getJustificationSupport().get(0).get(1).containsAll(testSupports));

    }

}