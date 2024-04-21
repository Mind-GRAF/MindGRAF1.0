package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingletonTest {

    private Network network;
    private Node X;
    private Node Y;

    @BeforeEach
    public void setUp() throws NoSuchTypeException {
        network = new Network();

        X = Network.createVariableNode("X", "propositionnode");
        Y = Network.createVariableNode("Y", "propositionnode");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testInsertIntoMap() {
        NodeSet commonVariables = new NodeSet(X, Y);
        Singleton singletonWithCommonVars = new Singleton(commonVariables);
        RuleInfo ri = new RuleInfo(1, 2, new Substitutions(), new FlagNodeSet());
        RuleInfoSet inserted = singletonWithCommonVars.insertIntoMap(ri, 1);
        assertNotNull(inserted);
        assertEquals(1, inserted.size());
    }

    @Test
    public void testGetAllRuleInfos() {
        NodeSet commonVariables = new NodeSet(X, Y);
        Singleton singletonWithCommonVars = new Singleton(commonVariables);
        RuleInfo ri1 = new RuleInfo(1, 2, new Substitutions(), new FlagNodeSet());
        RuleInfo ri2 = new RuleInfo(2, 1, new Substitutions(), new FlagNodeSet());
        singletonWithCommonVars.insertIntoMap(ri1, 1);
        singletonWithCommonVars.insertIntoMap(ri2, 2);
        RuleInfoSet allRuleInfos = singletonWithCommonVars.getAllRuleInfos();
        assertEquals(2, allRuleInfos.size());
    }

}