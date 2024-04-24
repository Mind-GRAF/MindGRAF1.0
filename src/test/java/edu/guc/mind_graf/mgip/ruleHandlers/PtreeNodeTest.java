package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PtreeNodeTest {

    private PtreeNode ptreeNode;
    Network network = new Network();
    Node X;
    Node Y;
    Node Z;
    Node Nemo;
    Node Marlin;
    Node Dory;

    @BeforeEach
    public void setUp() throws NoSuchTypeException {
        X = Network.createVariableNode("X", "propositionnode");
        Y = Network.createVariableNode("Y", "propositionnode");
        Z = Network.createVariableNode("Z", "propositionnode");
        Nemo = Network.createNode("nemo", "propositionnode");
        Marlin = Network.createNode("marlin", "propositionnode");
        Dory = Network.createNode("dory", "propositionnode");
        NodeSet commonVariables = new NodeSet(X, Y);
        NodeSet vars = new NodeSet(X, Y, Z);
        FreeVariableSet siblingIntersection = new FreeVariableSet(X, Y);
        ptreeNode = new PtreeNode(null, null, null, null, new Linear(commonVariables), vars, siblingIntersection);
    }

    @AfterEach
    public void tearDown() {
        ((Linear)ptreeNode.getSIndex()).clear();
    }

    @Test
    public void testInsertIntoNode() throws InvalidRuleInfoException {

        Substitutions subs1 = new Substitutions();
        subs1.add(X, Nemo);
        subs1.add(Y, Marlin);

        RuleInfo ri = new RuleInfo(1, 2, subs1, new FlagNodeSet());
        RuleInfoSet result = ptreeNode.insertIntoNode(ri, true);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testInsertIntoNodeSet() throws InvalidRuleInfoException {
        Substitutions subs1 = new Substitutions();
        subs1.add(X, Nemo);
        subs1.add(Y, Marlin);
        Substitutions subs2 = new Substitutions();
        subs2.add(X, Nemo);
        subs2.add(Y, Marlin);
        RuleInfo ri1 = new RuleInfo(1, 2, subs1, new FlagNodeSet());
        RuleInfo ri2 = new RuleInfo(2, 1, subs2, new FlagNodeSet());
        RuleInfoSet ris = new RuleInfoSet(ri1, ri2);
        RuleInfoSet result = ptreeNode.insertIntoNode(ris, true);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, ((Linear)ptreeNode.getSIndex()).getRuleInfoMap().size());
        assertEquals(1, ((Linear)ptreeNode.getSIndex()).getRuleInfoMap().keySet().size());
    }
}