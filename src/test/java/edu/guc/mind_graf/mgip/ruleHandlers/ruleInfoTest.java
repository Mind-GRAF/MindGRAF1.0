package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;

import static org.junit.jupiter.api.Assertions.*;

class RuleInfoTest {

    Network network;
    Node X;
    Node Y;
    Node Z;


    @BeforeEach
    void setUp() throws NoSuchTypeException {
        network = new Network();

        // Create some variable nodes for testing
        X = Network.createVariableNode("X", "propositionnode");
        Y = Network.createVariableNode("Y", "propositionnode");
        Z = Network.createVariableNode("Z", "propositionnode");
    }

    @Test
    void testEmptyConstructor() {
        RuleInfo ruleInfo = new RuleInfo("", 0 );
        assertEquals(0, ruleInfo.getPcount());
        assertEquals(0, ruleInfo.getNcount());
        assertNotNull(ruleInfo.getSubs());
        assertNotNull(ruleInfo.getFns());
    }

    @Test
    void testCombine() throws NoSuchTypeException, DirectCycleException {

        Node Nemo = Network.createNode("nemo", "propositionnode");
        Node Marlin = Network.createNode("marlin", "propositionnode");
        Node Dory = Network.createNode("dory", "propositionnode");
        Substitutions subs1 = new Substitutions();
        subs1.add(Z, Nemo);
        subs1.add(Y, Marlin);
        Substitutions subs2 = new Substitutions();
        subs2.add(X, Dory);
        subs2.add(Y, Marlin);
        // Create RuleInfo objects for testing
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 2, 3, subs1, new FlagNodeSet(), new Support(-1));
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 4, 1, subs2, new FlagNodeSet(), new Support(-1));

        // Combine the RuleInfo objects
        RuleInfo combinedRuleInfo = ruleInfo1.combine(ruleInfo2);

        // Verify the combined RuleInfo object
        assertNotNull(combinedRuleInfo);
        assertEquals(6, combinedRuleInfo.getPcount());
        assertEquals(4, combinedRuleInfo.getNcount());
        assertNotNull(combinedRuleInfo.getSubs());
        assertEquals(3, combinedRuleInfo.getSubs().size());
        assertNotNull(combinedRuleInfo.getFns());

        ruleInfo1.getSubs().add(X, Nemo);

        combinedRuleInfo = ruleInfo1.combine(ruleInfo2);
        assertNull(combinedRuleInfo);

    }

    @Test
    public void testEquals() {
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));

        assertTrue(ruleInfo1.equals(ruleInfo2));
    }

    @Test
    public void testNotEquals() {
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 3, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));

        assertFalse(ruleInfo1.equals(ruleInfo2));
    }

    @Test
    public void testAddNullSubs() {
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));
        FreeVariableSet freeVariables = new FreeVariableSet();
        freeVariables.add(X);
        freeVariables.add(Y);

        RuleInfo result = ruleInfo1.addNullSubs(freeVariables);
        assertEquals(2, result.getSubs().size());
        assertTrue(result.getSubs().contains(X));
        assertTrue(result.getSubs().contains(Y));
    }

    @Test
    void testClone() {
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));
        RuleInfo result = ruleInfo1.clone();

        assertEquals(ruleInfo1.getPcount(), result.getPcount());
        assertEquals(ruleInfo1.getNcount(), result.getNcount());
        assertEquals(ruleInfo1.getSubs(), result.getSubs());
        assertEquals(ruleInfo1.getFns(), result.getFns());
    }
}