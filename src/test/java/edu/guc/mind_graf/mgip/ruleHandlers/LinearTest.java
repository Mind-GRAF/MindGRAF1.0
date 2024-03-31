package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.Linear;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.Singleton;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LinearTest {

    private Linear linear;
    private RuleInfo ruleInfo1;
    private RuleInfo ruleInfo2;
    private RuleInfo ruleInfo3;

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        Network network = new Network();

        Node Z = Network.createVariableNode("Z", "propositionnode");
        Node Y = Network.createVariableNode("Y", "propositionnode");
        Node X = Network.createVariableNode("X", "propositionnode");

        Node Nemo = Network.createNode("nemo", "propositionnode");
        Node Marlin = Network.createNode("marlin", "propositionnode");
        Node Dory = Network.createNode("dory", "propositionnode");
        Substitutions subs1 = new Substitutions();
        subs1.add(Y, Nemo);
        subs1.add(X, Dory);
        Substitutions subs2 = new Substitutions();
        subs2.add(X, Dory);
        subs2.add(Y, Marlin);
        // Create RuleInfo objects for testing
        ruleInfo1 = new RuleInfo(2, 3, subs1, new FlagNodeSet());
        ruleInfo2 = new RuleInfo(4, 1, subs2, new FlagNodeSet());
        HashSet <Node> commonVariables = new HashSet<>();
        commonVariables.add(X);
        linear = new Linear(commonVariables);
    }
    @Test
    void insertIntoMap() {
        linear.insertIntoMap(ruleInfo1, 0);
        assertNotNull(linear.getRuleInfoMap().get(0));
        linear.insertIntoMap(ruleInfo2, 0);
        assertEquals(2, linear.getRuleInfoMap().get(0).size());
    }
}