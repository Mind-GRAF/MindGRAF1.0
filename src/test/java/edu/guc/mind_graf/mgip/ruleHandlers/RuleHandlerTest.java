package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RuleHandlerTest {
    Node X;
    Node Y;
    Node Z;
    Node Nemo;
    Node Marlin;
    Node Dory;
    Substitutions subs1;
    Substitutions subs2;
    private Singleton singleton;
    private Linear linear;
    private SIndex sSIndex;
    private SIndex lSIndex;
    private RuleInfo ruleInfo1;
    private RuleInfo ruleInfo2;

    @BeforeEach
    void setUp() throws NoSuchTypeException {

        Network network = new Network();

        X = Network.createVariableNode("X", "propositionnode");
        Y = Network.createVariableNode("Y", "propositionnode");
        Z = Network.createVariableNode("Z", "propositionnode");

        Nemo = Network.createNode("nemo", "propositionnode");
        Marlin = Network.createNode("marlin", "propositionnode");
        Dory = Network.createNode("dory", "propositionnode");

        subs1 = new Substitutions();
        subs1.add(X, Dory);
        subs1.add(Y, Nemo);
        subs2 = new Substitutions();
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
        assertEquals(2, lSIndex.getCommonVariables().size());
        assertEquals(3, sSIndex.getCommonVariables().size());
    }

    @Test
    void createSIndex() throws NoSuchTypeException {
        NodeSet args = new NodeSet();
        args.add(X);
        args.add(Y);
        args.add(Z);
        DownCable cable = new DownCable(Network.getRelations().get("arg"), args);
        Node M1 = Network.createNode("propositionNode", new DownCableSet(cable));
        SIndex result = sSIndex.createSIndex(new PropositionNodeSet(M1));
        assertEquals(sSIndex.getCommonVariables(), result.getCommonVariables());
    }

    @Test
    void customHash() throws InvalidRuleInfoException {
        assertEquals(linear.customHash(subs2), linear.customHash(subs1));
    }

    @Test
    void insertVariableRI() throws InvalidRuleInfoException {
        ruleInfo1.getSubs().add(Z, Dory);
        sSIndex.insertVariableRI(ruleInfo1);
    }

    @Test
    void constructPtree() throws NoSuchTypeException {
        DownCable gMem = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
        Node government = Network.createNode("government", "propositionnode");
        DownCable gov = new DownCable(Network.getRelations().get("class"), new NodeSet(government));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(gMem, gov));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(Y));
        Node civilian = Network.createNode("civilian", "propositionnode");
        DownCable civ = new DownCable(Network.getRelations().get("class"), new NodeSet(civilian));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(cMem, civ));

        DownCable coMem = new DownCable(Network.getRelations().get("member"), new NodeSet(Z));
        Node country = Network.createNode("country", "propositionnode");
        DownCable coun = new DownCable(Network.getRelations().get("class"), new NodeSet(country));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(coMem, coun));

        Relation rule = Network.createRelation("rule", "",
                Adjustability.EXPAND, 2);
        Relation ruled = Network.createRelation("ruled", "",
                Adjustability.EXPAND, 2);
        DownCable ruling = new DownCable(rule, new NodeSet(X));
        DownCable ruledC = new DownCable(ruled, new NodeSet(Z));
        Node M3 = Network.createNode("propositionnode", new DownCableSet(ruling, ruledC));

        Relation living = Network.createRelation("living", "",
                Adjustability.EXPAND, 2);
        Relation in = Network.createRelation("in", "",
                Adjustability.EXPAND, 2);
        DownCable livingC = new DownCable(living, new NodeSet(Y));
        DownCable inC = new DownCable(in, new NodeSet(Z));
        Node M4 = Network.createNode("propositionnode", new DownCableSet(livingC, inC));

        Ptree testing = Ptree.constructPtree(new NodeSet(M0, M1, M2, M3, M4));
        testing.setMinPcount(5);
        testing.setMinNcount(Integer.MAX_VALUE);
        assertNotNull(testing);
        assertNotNull(testing.getVarSetLeafMap());
        assertEquals(5, testing.getVarSetLeafMap().keySet().size());
        System.out.println(testing);

    }

}