package edu.guc.mind_graf.mgip.ruleHandlers;

import org.junit.jupiter.api.Test;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;

import static org.junit.jupiter.api.Assertions.*;

class RuleInfoTest {

    @Test
    void testEmptyConstructor() {
        RuleInfo ruleInfo = new RuleInfo();
        assertEquals(0, ruleInfo.getPcount());
        assertEquals(0, ruleInfo.getNcount());
        assertNotNull(ruleInfo.getSubs());
        assertNotNull(ruleInfo.getFns());
    }

    @Test
    void testCombine() throws NoSuchTypeException {

        Network network = new Network();

        // Create some variable nodes for testing
        Node Z = Network.createVariableNode("Z", "propositionnode");
        Node Y = Network.createVariableNode("Y", "propositionnode");
        Node X = Network.createVariableNode("X", "propositionnode");

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
        RuleInfo ruleInfo1 = new RuleInfo(2, 3, subs1, new FlagNodeSet());
        RuleInfo ruleInfo2 = new RuleInfo(4, 1, subs2, new FlagNodeSet());

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

}