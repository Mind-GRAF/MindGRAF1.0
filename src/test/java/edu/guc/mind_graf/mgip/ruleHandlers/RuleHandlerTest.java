package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
class RuleHandlerTest {
    private Singleton singleton;
    private Linear linear;
    private SIndex sSIndex;
    private SIndex lSIndex;
    private RuleInfo ruleInfo1;
    private RuleInfo ruleInfo2;
    private RuleInfo ruleInfo3;

    @BeforeEach
    void setUp() throws NoSuchTypeException {

        Network network = new Network();

        Node X = Network.createVariableNode("X", "propositionnode");
        Node Y = Network.createVariableNode("Y", "propositionnode");
        Node Z = Network.createVariableNode("Z", "propositionnode");

        Node Nemo = Network.createNode("nemo", "propositionnode");
        Node Marlin = Network.createNode("marlin", "propositionnode");
        Node Dory = Network.createNode("dory", "propositionnode");

        Substitutions subs1 = new Substitutions();
        subs1.add(X, Dory);
        subs1.add(Y, Nemo);
        Substitutions subs2 = new Substitutions();
        subs2.add(X, Dory);
        subs2.add(Y, Marlin);

        ruleInfo1 = new RuleInfo(2, 3, subs1, new FlagNodeSet());
        ruleInfo2 = new RuleInfo(4, 1, subs2, new FlagNodeSet());
        NodeSet commonVariables = new NodeSet();
        commonVariables.add(X);
        linear = new Linear(commonVariables);

        singleton = new Singleton();

        sSIndex = new Singleton();
        lSIndex = new Linear();
        NodeSet commonVariablesS = new NodeSet();
        commonVariablesS.add(X);
        commonVariablesS.add(Y);
        commonVariablesS.add(Z);
        NodeSet commonVariablesL = new NodeSet();
        commonVariablesL.add(X);
        commonVariablesL.add(Y);
        sSIndex.setCommonVariables(commonVariablesS);
        lSIndex.setCommonVariables(commonVariablesL);

    }

    @Test
    void testInsertIntoMapSingleton() {
        RuleInfo ri = new RuleInfo();
        int hash = 1;

        singleton.insertIntoMap(ri, hash);

        RuleInfo result = singleton.getRuleInfoMap().get(hash);

        assertNotNull(result);
        assertEquals(ri, result);
        assertEquals(1, singleton.getRuleInfoMap().size());
        assertEquals(ri, singleton.getRuleInfoMap().get(hash));
    }

    @Test
    void insertIntoMapLinear() {
        linear.insertIntoMap(ruleInfo1, 0);
        assertNotNull(linear.getRuleInfoMap().get(0));
        linear.insertIntoMap(ruleInfo2, 0);
        assertEquals(2, linear.getRuleInfoMap().get(0).size());
    }

    @Test
    void getCommonVariables() {
        assertEquals(2, linear.getCommonVariables().size());
        assertEquals(3, singleton.getCommonVariables().size());
    }

    @Test
    void createSIndex() {

    }

    @Test
    void customHash() {
    }

    @Test
    void insertVariableRI() {
    }

    @Test
    void insertIntoMap() {
    }

    @Test
    void clear() {
    }

}
