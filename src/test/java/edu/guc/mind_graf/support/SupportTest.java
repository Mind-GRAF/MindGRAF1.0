package edu.guc.mind_graf.support;

import edu.guc.mind_graf.compression.KZeroCompression;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.stream;

class SupportTest {



    @BeforeEach
    public void setUp() {
        new Network();
    }


    @Test
    public void testAddJustificationSupportForNewAttitudeAndLevelForAnEmptySupport() throws NoSuchTypeException{
        PropositionNode node1 = (PropositionNode) Network.createNode("1", "propositionnode");
        int attitudeID = 1;
        int level = 1;
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
        hash.put(1, new Pair<>(new PropositionNodeSet(),new PropositionNodeSet()));

        node1.addJustificationBasedSupport(attitudeID, level, hash, new PropositionNodeSet());

        assertFalse(node1.getSupport().getJustificationBasedSupport().containsKey(level));
        assertTrue(node1.getSupport().getJustificationBasedSupport().isEmpty());
        assertFalse(node1.getSupport().getAssumptionBasedSupport().containsKey(level));
        assertTrue(node1.getSupport().getAssumptionBasedSupport().isEmpty());
    }

    @Test
    public void testAddJustificationSupportForExistingAttitudeAndLevel() throws NoSuchTypeException {
        PropositionNode node2 = (PropositionNode) Network.createNode("2", "propositionnode");
        PropositionNode node3 = (PropositionNode) Network.createNode("3", "propositionnode");
        PropositionNode node4 = (PropositionNode) Network.createNode("4", "propositionnode");
        // First, add some initial support data
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
        hash.put(0, new Pair<>(new PropositionNodeSet(node3.getId()), new PropositionNodeSet()));
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> initialSupport = new ArrayList<>();
        initialSupport.add(new Pair<>(new HashMap<>(hash), new PropositionNodeSet()));
        // Add test data to initialSupport
        int attitudeID = 1;
        int level = 1;
        node4.addJustificationBasedSupports(attitudeID, level, initialSupport);

        // Now add additional support for the same attitude at the same level
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> additionalSupport = new ArrayList<>();
        hash.put(0, new Pair<>(new PropositionNodeSet(node2.getId()), new PropositionNodeSet()));
        additionalSupport.add(new Pair<>(new HashMap<>(hash), new PropositionNodeSet()));
        // Add test data to additionalSupport

        node4.addJustificationBasedSupports(attitudeID, level, additionalSupport);

        assertEquals(1, node4.getSupport().getJustificationBasedSupport().size());
        assertEquals(1, node4.getSupport().getJustificationBasedSupport().get(level).size());
        assertEquals(2, node4.getSupport().getJustificationBasedSupport().get(level).get(attitudeID).size());
        assertTrue(node4.getSupport().getJustificationBasedSupport().get(level).get(attitudeID).containsAll(initialSupport));
        assertTrue(node4.getSupport().getJustificationBasedSupport().get(level).get(attitudeID).containsAll(additionalSupport));
        assertTrue(node2.getJustificationSupportDependents().contains(node4.getId()));
        assertTrue(node4.getSupport().getAssumptionBasedSupport().isEmpty());
    }

    @Test
    public void testAddJustificationSupportWithDirectCycle() throws NoSuchTypeException {
        PropositionNode node5 = (PropositionNode) Network.createNode("5", "propositionnode");

        int attitudeID = 1;
        int level = 1;

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
        hash.put(attitudeID, new Pair<>(new PropositionNodeSet(node5.getId()), new PropositionNodeSet()));

        node5.addJustificationBasedSupport(attitudeID, level, hash, new PropositionNodeSet());

        assertTrue(node5.getSupport().getJustificationBasedSupport().isEmpty());
    }

    @Test
    public void testAddJustificationSupportForAssumptionSupportWithoutGradesCrossProduct() throws NoSuchTypeException {
        //1->13
        //2->14
        //3->15
        //4- 16
        //5- 17
        PropositionNode node6 = (PropositionNode) Network.createNode("6", "propositionnode");
        PropositionNode node7 = (PropositionNode) Network.createNode("7", "propositionnode");
        PropositionNode node8 = (PropositionNode) Network.createNode("8", "propositionnode");
        PropositionNode node9 = (PropositionNode) Network.createNode("9", "propositionnode");
        PropositionNode node10 = (PropositionNode) Network.createNode("10", "propositionnode");
        PropositionNode node11 = (PropositionNode) Network.createNode("11", "propositionnode");
        PropositionNode node12 = (PropositionNode) Network.createNode("12", "propositionnode");
        PropositionNode node13 = (PropositionNode) Network.createNode("13", "propositionnode");
        PropositionNode node14 = (PropositionNode) Network.createNode("14", "propositionnode");
        PropositionNode node15 = (PropositionNode) Network.createNode("15", "propositionnode");
        PropositionNode node16 = (PropositionNode) Network.createNode("16", "propositionnode");
        PropositionNode node17 = (PropositionNode) Network.createNode("17", "propositionnode");
        node13.setHyp(1);
        node13.setHyp(2);
        node13.setHyp(3);

        node14.setHyp(1);

        node15.setHyp(2);

        node16.setHyp(3);

        node17.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node9.getId())));

        node6.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node13.getId(),node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node10.getId())));
        node6.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId(),node17.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node12.getId())));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node12.getId())));
        node7.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node17.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node15.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node12.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node15.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node12.getId())));
        node7.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node6.getId(),node7.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node6.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node11.getId())));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node7.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node11.getId())));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node8JustificationInnerHash = new HashMap<>();
        node8JustificationInnerHash.put(1, new ArrayList<>(supports));
        node8.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node6.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node7.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node11.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node6.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node11.getId())));
        node8JustificationInnerHash.put(2, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node8Justification = new HashMap<>();
        node8Justification.put(Network.currentLevel, node8JustificationInnerHash);
        node8.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId(),node17.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node13.getId(),node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node10.getId(),node11.getId(),node12.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node13.getId(),node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node10.getId(),node11.getId(),node12.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId(),node17.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node13.getId(),node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node9.getId(),node10.getId(),node11.getId(),node12.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node13.getId(),node16.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node9.getId(),node10.getId(),node11.getId(),node12.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node17.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node15.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node11.getId(),node12.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node15.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node11.getId(),node12.getId())));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> innerHash = new HashMap<>();
        innerHash.put(1, new ArrayList<>(supports));

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support2 = new HashMap<>();

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();
        support2.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId(),node17.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node15.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node11.getId(),node12.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node15.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node11.getId(),node12.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node17.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node16.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node9.getId(),node11.getId(),node12.getId())));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node16.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node9.getId(),node11.getId(),node12.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node13.getId(),node14.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node11.getId())));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(node13.getId(),node15.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node16.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node9.getId(),node11.getId())));
        innerHash.put(2, new ArrayList<>(supports2));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> assumptionSupport = new HashMap<>();
        assumptionSupport.put(Network.currentLevel, new HashMap<>(innerHash));


        assertEquals(node8Justification, node8.getSupport().getJustificationBasedSupport());
        assertEquals(assumptionSupport, node8.getSupport().getAssumptionBasedSupport());
    }

    @Test
    public void testAddJustificationSupportForAssumptionSupportWithGradesCrossProduct() throws NoSuchTypeException {
        // 1-> 18
        // 2-> 19
        // 3-> 20
        // 4-> 21
        // 5-> 22
        // 6-> 23
        // 7-> 24
        // 8-> 25
        // 9-> 26
        // 10-> 27
        // 11-> 28
        // 12-> 29
        // 13-> 30
        PropositionNode node18 = (PropositionNode) Network.createNode("18", "propositionnode");
        PropositionNode node19 = (PropositionNode) Network.createNode("19", "propositionnode");
        PropositionNode node20 = (PropositionNode) Network.createNode("20", "propositionnode");
        PropositionNode node21 = (PropositionNode) Network.createNode("21", "propositionnode");
        PropositionNode node22 = (PropositionNode) Network.createNode("22", "propositionnode");
        PropositionNode node23 = (PropositionNode) Network.createNode("23", "propositionnode");
        PropositionNode node24 = (PropositionNode) Network.createNode("24", "propositionnode");
        PropositionNode node25 = (PropositionNode) Network.createNode("25", "propositionnode");
        PropositionNode node26 = (PropositionNode) Network.createNode("26", "propositionnode");
        PropositionNode node27 = (PropositionNode) Network.createNode("27", "propositionnode");
        PropositionNode node28 = (PropositionNode) Network.createNode("28", "propositionnode");
        PropositionNode node29 = (PropositionNode) Network.createNode("29", "propositionnode");
        PropositionNode node30 = (PropositionNode) Network.createNode("30", "propositionnode");
       
        node18.setHyp(1);
        node18.setHyp(2);
        node18.setHyp(3);

        node19.setHyp(1);

        node20.setHyp(2);

        node21.setHyp(3);

        node22.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node26.getId())));

        node23.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node18.getId(),node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node27.getId())));
        node23.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId(),node22.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node29.getId())));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node29.getId())));
        node24.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node22.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node20.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node29.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node20.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node29.getId())));
        node24.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> node30Hash = new HashMap<>();
        node30Hash.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId()), new PropositionNodeSet()));
        node30.addJustificationBasedSupport(1,Network.currentLevel, node30Hash, new PropositionNodeSet());

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node23.getId(),node24.getId()), new PropositionNodeSet(node30.getId())));
        support.put(2, new Pair<>(new PropositionNodeSet(node23.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node28.getId())));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node24.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node28.getId())));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node25JustificationInnerHash = new HashMap<>();
        node25JustificationInnerHash.put(1, new ArrayList<>(supports));
        node25.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node23.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node24.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node28.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node23.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node28.getId())));
        node25JustificationInnerHash.put(2, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node25Justification = new HashMap<>();
        node25Justification.put(Network.currentLevel, node25JustificationInnerHash);
        node25.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId(),node22.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node18.getId(),node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node27.getId(),node28.getId(),node29.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node18.getId(),node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node27.getId(),node28.getId(),node29.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId(),node22.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node18.getId(),node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node26.getId(),node27.getId(),node28.getId(),node29.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node18.getId(),node21.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node26.getId(),node27.getId(),node28.getId(),node29.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node22.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node20.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node28.getId(),node29.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node20.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node28.getId(),node29.getId())));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> innerHash = new HashMap<>();
        innerHash.put(1, new ArrayList<>(supports));

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support2 = new HashMap<>();

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();
        support2.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId(),node22.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node20.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node28.getId(),node29.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node20.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node28.getId(),node29.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node22.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node21.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node26.getId(),node28.getId(),node29.getId())));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node21.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node26.getId(),node28.getId(),node29.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node18.getId(),node19.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node28.getId())));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(node18.getId(),node20.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node21.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node26.getId(),node28.getId())));
        innerHash.put(2, new ArrayList<>(supports2));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> assumptionSupport = new HashMap<>();
        assumptionSupport.put(Network.currentLevel, new HashMap<>(innerHash));

        assertEquals(node25Justification, node25.getSupport().getJustificationBasedSupport());
       assertEquals(assumptionSupport, node25.getSupport().getAssumptionBasedSupport());
    }

    @Test
    void testAddJustificationSupportForAssumptionSupportWithoutGradesCrossProductInDifferentLevels() throws NoSuchTypeException {
       //1->31
       //2->32
       //3->33
       //4->34
       //5->35
       //6->36
       //7->37
       //8->38
       //9->39
       //10->40
       //11->41
       //12->42
       
       PropositionNode node31 = (PropositionNode) Network.createNode("31", "propositionnode");
       PropositionNode node32 = (PropositionNode) Network.createNode("32", "propositionnode");
       PropositionNode node33 = (PropositionNode) Network.createNode("33", "propositionnode");
       PropositionNode node34 = (PropositionNode) Network.createNode("34", "propositionnode");
       PropositionNode node35 = (PropositionNode) Network.createNode("35", "propositionnode");
       PropositionNode node36 = (PropositionNode) Network.createNode("36", "propositionnode");
       PropositionNode node37 = (PropositionNode) Network.createNode("37", "propositionnode");
       PropositionNode node38 = (PropositionNode) Network.createNode("38", "propositionnode");
       PropositionNode node39 = (PropositionNode) Network.createNode("39", "propositionnode");
       PropositionNode node40 = (PropositionNode) Network.createNode("40", "propositionnode");
       PropositionNode node41 = (PropositionNode) Network.createNode("41", "propositionnode");
       PropositionNode node42 = (PropositionNode) Network.createNode("42", "propositionnode");

        Network.currentLevel = 1;
        node31.setHyp(1);
        node31.setHyp(2);
        node31.setHyp(3);

        node32.setHyp(1);

        node33.setHyp(2);

        node34.setHyp(3);

        node35.setHyp(1);

        Network.currentLevel = 0;
        node31.setHyp(1);
        node31.setHyp(2);
        node31.setHyp(3);

        node32.setHyp(1);

        node33.setHyp(2);

        node34.setHyp(3);

        node35.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node39.getId())));

        node36.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node31.getId(),node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node40.getId())));
        node36.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId(),node35.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node42.getId())));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node42.getId())));
        node37.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node35.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node33.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node42.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node33.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node42.getId())));
        node37.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node36.getId(),node37.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node36.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node41.getId())));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node37.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node41.getId())));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node38JustificationInnerHash = new HashMap<>();
        node38JustificationInnerHash.put(1, new ArrayList<>(supports));
        node38.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node36.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node37.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node41.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node36.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node41.getId())));
        node38JustificationInnerHash.put(2, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node38Justification = new HashMap<>();
        node38Justification.put(Network.currentLevel, node38JustificationInnerHash);
        node38.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId(),node35.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node31.getId(),node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node40.getId(),node41.getId(),node42.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node31.getId(),node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node40.getId(),node41.getId(),node42.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId(),node35.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node31.getId(),node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node39.getId(),node40.getId(),node41.getId(),node42.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node31.getId(),node34.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node39.getId(),node40.getId(),node41.getId(),node42.getId())));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node35.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node33.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node41.getId(),node42.getId())));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node33.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(node41.getId(),node42.getId())));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> innerHash = new HashMap<>();
        innerHash.put(1, new ArrayList<>(supports));

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support2 = new HashMap<>();

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports2 = new ArrayList<>();
        support2.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId(),node35.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node33.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node41.getId(),node42.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node33.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node41.getId(),node42.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node35.getId()), new PropositionNodeSet()));
        support2.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node34.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node39.getId(),node41.getId(),node42.getId())));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node34.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node39.getId(),node41.getId(),node42.getId())));
        support2.clear();
        support2.put(1, new Pair<>(new PropositionNodeSet(node31.getId(),node32.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node41.getId())));
        support2.clear();
        support2.put(2, new Pair<>(new PropositionNodeSet(node31.getId(),node33.getId()), new PropositionNodeSet()));
        support2.put(3, new Pair<>(new PropositionNodeSet(node34.getId()), new PropositionNodeSet()));
        supports2.add(new Pair<>(new HashMap<>(support2), new PropositionNodeSet(node39.getId(),node41.getId())));
        innerHash.put(2, new ArrayList<>(supports2));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> assumptionSupport = new HashMap<>();
        assumptionSupport.put(Network.currentLevel, new HashMap<>(innerHash));
        assumptionSupport.put(1, new HashMap<>(innerHash));

        assertEquals(node38Justification, node38.getSupport().getJustificationBasedSupport());
        assertEquals(assumptionSupport, node38.getSupport().getAssumptionBasedSupport());

    }

    @Test
    void testRemoveNodeFromJustifications() throws NoSuchTypeException {
       //1->43
       //2->44
       //3->45
       //4->46
       //5->47
       //6->48
       //7->49
      

       PropositionNode node43 = (PropositionNode) Network.createNode("43", "propositionnode");
       PropositionNode node44 = (PropositionNode) Network.createNode("44", "propositionnode");
       PropositionNode node45 = (PropositionNode) Network.createNode("45", "propositionnode");
       PropositionNode node46 = (PropositionNode) Network.createNode("46", "propositionnode");
       PropositionNode node47 = (PropositionNode) Network.createNode("47", "propositionnode");
       PropositionNode node48 = (PropositionNode) Network.createNode("48", "propositionnode");
       PropositionNode node49 = (PropositionNode) Network.createNode("49", "propositionnode");
       
        Network.currentLevel = 1;
        node43.setHyp(1);
        node43.setHyp(2);
        node43.setHyp(3);

        node44.setHyp(1);

        node45.setHyp(2);

        node46.setHyp(3);

        node47.setHyp(1);

        Network.currentLevel = 0;
        node43.setHyp(1);
        node43.setHyp(2);
        node43.setHyp(3);

        node44.setHyp(1);

        node45.setHyp(2);

        node46.setHyp(3);

        node47.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node43.getId(),node44.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node43.getId(),node45.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node46.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node48.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node43.getId(),node46.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node48.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node43.getId(),node44.getId(),node47.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node43.getId(),node45.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node46.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node49.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node43.getId(),node47.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node45.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node45.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node49.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        node43.removeNodeFromOtherNodesSupport();

        assertTrue(node48.getSupport().getJustificationBasedSupport().isEmpty());

        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node49InnerHash = new HashMap<>();

        support.clear();
        supports.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node46.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node49InnerHash.put(1, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node45.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node49InnerHash.put(3, new ArrayList<>(supports));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node49Support = new HashMap<>();
        node49Support.put(0, node49InnerHash);

        assertEquals(node49Support, node49.getSupport().getJustificationBasedSupport());
    }

    @Test
    void testRemoveNodeFromAssumptions() throws NoSuchTypeException {
       //1->50
       //2->51
       //3->52
       //4->53
       //5->54
       //6->55
       //7->56
       //8->57
       
       PropositionNode node50 = (PropositionNode) Network.createNode("50", "propositionnode");
       PropositionNode node51 = (PropositionNode) Network.createNode("51", "propositionnode");
       PropositionNode node52 = (PropositionNode) Network.createNode("52", "propositionnode");
       PropositionNode node53 = (PropositionNode) Network.createNode("53", "propositionnode");
       PropositionNode node54 = (PropositionNode) Network.createNode("54", "propositionnode");
       PropositionNode node55 = (PropositionNode) Network.createNode("55", "propositionnode");
       PropositionNode node56 = (PropositionNode) Network.createNode("56", "propositionnode");
       PropositionNode node57 = (PropositionNode) Network.createNode("57", "propositionnode");

        Network.currentLevel = 1;
        node50.setHyp(1);
        node50.setHyp(2);
        node50.setHyp(3);

        node51.setHyp(1);

        node52.setHyp(2);

        node53.setHyp(3);

        node54.setHyp(1);

        Network.currentLevel = 0;
        node50.setHyp(1);
        node50.setHyp(2);
        node50.setHyp(3);

        node51.setHyp(1);

        node52.setHyp(2);

        node53.setHyp(3);

        node54.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node50.getId(),node51.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node50.getId(),node52.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node53.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node55.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node50.getId(),node53.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node55.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node50.getId(),node51.getId(),node54.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node50.getId(),node52.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node53.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node56.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node50.getId(),node54.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node52.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node52.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node56.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node55.getId(),node56.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node55.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node56.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node57JustificationInnerHash = new HashMap<>();
        node57JustificationInnerHash.put(1, new ArrayList<>(supports));
        node57.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node55.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node56.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node55.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node57JustificationInnerHash.put(2, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node57Justification = new HashMap<>();
        node57Justification.put(Network.currentLevel, node57JustificationInnerHash);
        node57.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        node50.removeNodeFromOtherNodesSupport();

        assertTrue(node55.getSupport().getAssumptionBasedSupport().isEmpty());

        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node56InnerHash = new HashMap<>();

        support.clear();
        supports.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node53.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node56InnerHash.put(1, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node52.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node56InnerHash.put(3, new ArrayList<>(supports));

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node56Support = new HashMap<>();
        node56Support.put(0, node56InnerHash);
        node56Support.put(1, node56InnerHash);

        assertEquals(node56Support, node56.getSupport().getAssumptionBasedSupport());


        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> node57InnerHash = new HashMap<>();
        support.clear();
        supports.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node52.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node57InnerHash.put(1, new ArrayList<>(supports));
        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> node57Support = new HashMap<>();
        node57Support.put(0, node57InnerHash);
        node57Support.put(1, node57InnerHash);

        assertEquals(node57Support, node57.getSupport().getAssumptionBasedSupport());

    }
    @Test
    void testUnion() throws NoSuchTypeException{
         //1->58
       //2->59
       //3->60
       //4->61
       //5->62
       //6->63
       //7->64
       PropositionNode node58 = (PropositionNode) Network.createNode("58", "propositionnode");
       PropositionNode node59 = (PropositionNode) Network.createNode("59", "propositionnode");
       PropositionNode node60 = (PropositionNode) Network.createNode("60", "propositionnode");
       PropositionNode node61 = (PropositionNode) Network.createNode("61", "propositionnode");
       PropositionNode node62 = (PropositionNode) Network.createNode("62", "propositionnode");
       PropositionNode node63 = (PropositionNode) Network.createNode("63", "propositionnode");
       PropositionNode node64 = (PropositionNode) Network.createNode("64", "propositionnode");

        Network.currentLevel = 0;
        node58.setHyp(1);
        node58.setHyp(2);
        node58.setHyp(3);

        node59.setHyp(1);

        node60.setHyp(2);

        node61.setHyp(3);

        node62.setHyp(1);

        HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> testSupport = new HashMap<>();
        HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> testSupportInnerHash = new HashMap<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node58.getId(),node59.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node58.getId(),node60.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node61.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        testSupportInnerHash.put(1, new ArrayList<>(supports));
        node63.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node58.getId(),node61.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupportInnerHash.put(2, new ArrayList<>(supports));
        testSupport.put(0, new HashMap<>(testSupportInnerHash));
        node63.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        testSupportInnerHash.clear();
        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node58.getId(),node59.getId(),node62.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node58.getId(),node60.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node61.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupport.get(0).get(1).addAll(new ArrayList<>(supports));
        node64.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node58.getId(),node62.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node60.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node60.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupport.get(0).put(3, new ArrayList<>(supports));
        node64.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        node64.unionSupport(node63.getSupport());
        assertTrue(node64.getSupport().getJustificationBasedSupport().get(0).get(1).containsAll(testSupport.get(0).get(1)));
        assertTrue(node64.getSupport().getJustificationBasedSupport().get(0).get(2).containsAll(testSupport.get(0).get(2)));
        assertTrue(node64.getSupport().getJustificationBasedSupport().get(0).get(3).containsAll(testSupport.get(0).get(3)));

    }

    

    @Test
    void testCombine() throws NoSuchTypeException{
      //1->65
       //2->66
       //3->67
       //4->68
       //5->69
       //6->70
       //7->71
       //8->72
      
       PropositionNode node65 = (PropositionNode) Network.createNode("65", "propositionnode");
       PropositionNode node66 = (PropositionNode) Network.createNode("66", "propositionnode");
       PropositionNode node67 = (PropositionNode) Network.createNode("67", "propositionnode");
       PropositionNode node68 = (PropositionNode) Network.createNode("68", "propositionnode");
       PropositionNode node69 = (PropositionNode) Network.createNode("69", "propositionnode");
       PropositionNode node70 = (PropositionNode) Network.createNode("70", "propositionnode");
       PropositionNode node71 = (PropositionNode) Network.createNode("71", "propositionnode");
       PropositionNode node72 = (PropositionNode) Network.createNode("72", "propositionnode");

        Network.currentLevel = 0;
        node65.setHyp(1);
        node65.setHyp(2);
        node65.setHyp(3);

        node66.setHyp(1);

        node67.setHyp(2);

        node68.setHyp(3);

        node69.setHyp(1);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> testSupports = new ArrayList<>();

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(node65.getId(),node66.getId()), new PropositionNodeSet()));

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node65.getId(),node67.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node68.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        node70.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node65.getId(),node68.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node70.addJustificationBasedSupports(2,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node65.getId(),node66.getId(),node69.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node65.getId(),node67.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(3, new Pair<>(new PropositionNodeSet(node68.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node71.addJustificationBasedSupports(1,Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node65.getId(),node69.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node67.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node67.getId()), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node71.addJustificationBasedSupports(3,Network.currentLevel, new ArrayList<>(supports));

        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node65.getId(),node66.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node68.getId()), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        support.clear();
        support.put(1, new Pair<>(new PropositionNodeSet(node65.getId(),node66.getId(),node69.getId()), new PropositionNodeSet()));
        support.put(2, new Pair<>(new PropositionNodeSet(node65.getId(),node67.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node68.getId()), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));

        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(node65.getId(),node67.getId()), new PropositionNodeSet()));
        support.put(3, new Pair<>(new PropositionNodeSet(node68.getId()), new PropositionNodeSet()));
        testSupports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));



        node71.combineSupport(1, node70.getSupport());
        assertTrue(node71.getSupport().getJustificationBasedSupport().get(0).get(1).containsAll(testSupports));

    }
    @Test
    void testConstructor() throws NoSuchTypeException{
        //createNodes();
        //1->73
       //2->74
       //3->75
       //4->76
       //5->77
      
       PropositionNode node73 = (PropositionNode) Network.createNode("73", "propositionnode");
       PropositionNode node74 = (PropositionNode) Network.createNode("74", "propositionnode");
       PropositionNode node75 = (PropositionNode) Network.createNode("75", "propositionnode");
       PropositionNode node76 = (PropositionNode) Network.createNode("76", "propositionnode");
       PropositionNode node77 = (PropositionNode) Network.createNode("77", "propositionnode");
    
   
        Network.currentLevel = 0;
        node73.setHyp(1);
        node73.setHyp(2);
        node73.setHyp(3);

        node74.setHyp(1);

        node75.setHyp(2);

        node76.setHyp(3);

        node77.setHyp(1);

        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportHash = new HashMap<>();
        supportHash.put(1, new Pair<>(new PropositionNodeSet(node73.getId(),node74.getId()), new PropositionNodeSet()));

        Support support = new Support(node73.getId(), 1, Network.currentLevel, supportHash, new PropositionNodeSet());
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> list = new ArrayList<>();
        list.add(new Pair<>(supportHash, new PropositionNodeSet()));
        assertTrue(support.getJustificationBasedSupport().get(Network.currentLevel).get(1).containsAll(list));
        assertTrue(support.getAssumptionBasedSupport().get(Network.currentLevel).get(1).containsAll(list));
    }


}