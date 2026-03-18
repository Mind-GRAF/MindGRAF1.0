package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.guc.mind_graf.network.Network;

import static org.junit.jupiter.api.Assertions.*;

class SIndexTest {

    Network network;
    private Node X;
    private Node Y;
    SIndex linear;

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        network = new Network();
        X = Network.createVariableNode("X", "propositionnode");
        Y = Network.createVariableNode("Y", "propositionnode");
        NodeSet commonVariables = new NodeSet(X, Y);
        linear = new Linear(commonVariables);
    }

    @AfterEach
    void tearDown() {
        ((Linear)linear).clear();
    }

    @Test
    void customHash() throws InvalidRuleInfoException, NoSuchTypeException {

        Node Nemo = Network.createNode("nemo", "propositionnode");
        Node Marlin = Network.createNode("marlin", "propositionnode");
        Node Dory = Network.createNode("dory", "propositionnode");

        Substitutions subs1 = new Substitutions();
        subs1.add(X, Dory);
        subs1.add(Y, Nemo);
        Substitutions subs2 = new Substitutions();
        subs2.add(X, Dory);
        subs2.add(Y, Marlin);

        assertNotEquals(linear.customHash(subs2), linear.customHash(subs1));

        subs2.add(Y, Nemo);
        assertEquals(linear.customHash(subs2), linear.customHash(subs1));
    }

    @Test
    void testInsertVariableRI() throws InvalidRuleInfoException, DirectCycleException {
        Substitutions subs1 = new Substitutions();
        subs1.add(X, null);
        subs1.add(Y, null);
        RuleInfo ri = new RuleInfo("", 0, 1, 2, subs1, new FlagNodeSet(), new Support(-1));
        linear.setMin(5);
        RuleInfoSet inserted = linear.insertVariableRI(ri);
        assertNull(inserted);

        ri.setPcount(5);
        inserted = linear.insertVariableRI(ri);
        assertNotNull(inserted);
    }

    @Test
    void testInsertIntoMapSingleton() throws DirectCycleException {
        RuleInfo ri = new RuleInfo("", 0 );
        int hash = 1;
        Singleton singleton = new Singleton();
        singleton.insertIntoMap(ri, hash);

        RuleInfo result = singleton.getRuleInfoMap().get(hash);

        assertNotNull(result);
        assertEquals(ri, result);
        assertEquals(1, singleton.getRuleInfoMap().size());
        assertEquals(ri, singleton.getRuleInfoMap().get(hash));
    }


    @Test
    void insertIntoMapLinear() throws DirectCycleException {
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet(), new Support(-1));
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));
        linear.insertIntoMap(ruleInfo1, 0);
        assertNotNull(((Linear)linear).getRuleInfoMap().get(0));
        RuleInfoSet afterInsertion = linear.insertIntoMap(ruleInfo2, 0);
        assertEquals(1, afterInsertion.size());
        assertEquals(3, ((Linear)linear).getRuleInfoMap().get(0).size());
    }

}