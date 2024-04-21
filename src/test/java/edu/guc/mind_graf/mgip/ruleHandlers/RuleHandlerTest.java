package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.*;
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
    void constructPtree() throws NoSuchTypeException, InvalidRuleInfoException {
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

        Ptree testing = Ptree.constructPtree(new PropositionNodeSet(M0, M1, M2, M3, M4), 5, Integer.MAX_VALUE, 2);
        assertNotNull(testing);
        assertNotNull(testing.getVarSetLeafMap());
        assertEquals(5, testing.getVarSetLeafMap().keySet().size());
        assertEquals(9, testing.arrayOfNodes().size());
        assertEquals(1, testing.arrayOfNodes().get(0).getMin());
        assertEquals(5, testing.arrayOfNodes().get(8).getMin());

        // test inserting into ptree
        FlagNode govFlag = new FlagNode(M0, true, new PropositionNodeSet());
        Substitutions govSubs = new Substitutions();
        Node henry = Network.createNode("henry", "individualnode");
        govSubs.add(X, henry);
        RuleInfo ruleInfo0 = new RuleInfo(1, 0, govSubs, new FlagNodeSet(govFlag));
//        testing.insertVariableRI(ruleInfo0);

        FlagNode civFlag = new FlagNode(M1, true, new PropositionNodeSet());
        Substitutions civSubs = new Substitutions();
        Node anne = Network.createNode("anne", "individualnode");
        civSubs.add(Y, anne);
        RuleInfo ruleInfo1 = new RuleInfo(1, 0, civSubs, new FlagNodeSet(civFlag));
//        testing.insertVariableRI(ruleInfo1);

        FlagNode coFlag = new FlagNode(M2, true, new PropositionNodeSet());
        Substitutions coSubs = new Substitutions();
        Node england = Network.createNode("england", "individualnode");
        coSubs.add(Z, england);
        RuleInfo ruleInfo2 = new RuleInfo(1, 0, coSubs, new FlagNodeSet(coFlag));
//        testing.insertVariableRI(ruleInfo2);

        FlagNode rFlag = new FlagNode(M3, true, new PropositionNodeSet());
        Substitutions rSubs = new Substitutions();
        rSubs.add(Z, england);
        rSubs.add(X, henry);
        RuleInfo ruleInfo3 = new RuleInfo(1, 0, rSubs, new FlagNodeSet(rFlag));
//        testing.insertVariableRI(ruleInfo3);

        FlagNode lFlag = new FlagNode(M4, true, new PropositionNodeSet());
        Substitutions lSubs = new Substitutions();
        lSubs.add(Z, england);
        lSubs.add(Y, anne);
        RuleInfo ruleInfo4 = new RuleInfo(1, 0, lSubs, new FlagNodeSet(lFlag));
//        RuleInfoSet result = testing.insertVariableRI(ruleInfo4);
//        assertNotNull(result);
//        assertEquals(1, result.size());

        Ptree testing2 = Ptree.constructPtree(new PropositionNodeSet(M0, M1, M2, M3, M4), 5, Integer.MAX_VALUE, 2);
        Substitutions govSubs2 = new Substitutions();
        Node khaleesi = Network.createNode("khaleesi", "individualnode");
        govSubs2.add(X, khaleesi);
        RuleInfo ruleInfo6 = new RuleInfo(1, 0, govSubs2, new FlagNodeSet(govFlag));
        testing2.insertVariableRI(ruleInfo6);
        testing2.insertVariableRI(ruleInfo1);
        testing2.insertVariableRI(ruleInfo2);
        testing2.insertVariableRI(ruleInfo3);
        testing2.insertVariableRI(ruleInfo4);
        RuleInfoSet result2 = testing2.insertVariableRI(ruleInfo0);
        assertNotNull(result2);
        assertEquals(1, result2.size());

    }

}