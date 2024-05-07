package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.*;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinearTest {

    private Network network;
    private Node X;
    private Node Y;

    @BeforeEach
    public void setUp() throws NoSuchTypeException {
        network = new Network();

        X = Network.createVariableNode("X", "propositionnode");
        Y = Network.createVariableNode("Y", "propositionnode");
    }

    @Test
    public void testInsertIntoMap() throws DirectCycleException {
        NodeSet commonVariables = new NodeSet(X, Y);
        Linear linearWithCommonVars = new Linear(commonVariables);
        RuleInfo ri = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet(), new Support(-1));
        RuleInfoSet inserted = linearWithCommonVars.insertIntoMap(ri, 1);
        assertNotNull(inserted);
        assertEquals(1, inserted.size());
    }

    @Test
    public void testGetAllRuleInfos() throws DirectCycleException {
        NodeSet commonVariables = new NodeSet(X, Y);
        Linear linearWithCommonVars = new Linear(commonVariables);
        RuleInfo ri1 = new RuleInfo("", 0, 1, 2, new Substitutions(), new FlagNodeSet(), new Support(-1));
        RuleInfo ri2 = new RuleInfo("", 0, 2, 1, new Substitutions(), new FlagNodeSet(), new Support(-1));
        linearWithCommonVars.insertIntoMap(ri1, 1);
        linearWithCommonVars.insertIntoMap(ri2, 2);
        RuleInfoSet allRuleInfos = linearWithCommonVars.getAllRuleInfos();
        assertEquals(2, allRuleInfos.size());
    }
}