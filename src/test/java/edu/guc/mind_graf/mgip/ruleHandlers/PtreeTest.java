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
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PtreeTest {

    Network network = new Network();
    Node G;
    Node C;
    Node Co;
    Node M0;
    Node M1;
    Node M2;
    Node M3;
    Node M4;
    Ptree testing;

    @BeforeEach
    void setUp() throws NoSuchTypeException {
        G = Network.createVariableNode("G", "propositionnode");
        C = Network.createVariableNode("C", "propositionnode");
        Co = Network.createVariableNode("Co", "propositionnode");

        DownCable gMem = new DownCable(Network.getRelations().get("member"), new NodeSet(G));
        Node government = Network.createNode("government", "propositionnode");
        DownCable gov = new DownCable(Network.getRelations().get("class"), new NodeSet(government));
        M0 = Network.createNode("propositionnode", new DownCableSet(gMem, gov));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(C));
        Node civilian = Network.createNode("civilian", "propositionnode");
        DownCable civ = new DownCable(Network.getRelations().get("class"), new NodeSet(civilian));
        M1 = Network.createNode("propositionnode", new DownCableSet(cMem, civ));

        DownCable coMem = new DownCable(Network.getRelations().get("member"), new NodeSet(Co));
        Node country = Network.createNode("country", "propositionnode");
        DownCable coun = new DownCable(Network.getRelations().get("class"), new NodeSet(country));
        M2 = Network.createNode("propositionnode", new DownCableSet(coMem, coun));

        Relation rule = Network.createRelation("rule", "",
                Adjustability.EXPAND, 2);
        Relation ruled = Network.createRelation("ruled", "",
                Adjustability.EXPAND, 2);
        DownCable ruling = new DownCable(rule, new NodeSet(G));
        DownCable ruledC = new DownCable(ruled, new NodeSet(Co));
        M3 = Network.createNode("propositionnode", new DownCableSet(ruling, ruledC));

        Relation living = Network.createRelation("living", "",
                Adjustability.EXPAND, 2);
        Relation in = Network.createRelation("in", "",
                Adjustability.EXPAND, 2);
        DownCable livingC = new DownCable(living, new NodeSet(C));
        DownCable inC = new DownCable(in, new NodeSet(Co));
        M4 = Network.createNode("propositionnode", new DownCableSet(livingC, inC));

        testing = Ptree.constructPtree(new PropositionNodeSet(M0, M1, M2, M3, M4), 5, Integer.MAX_VALUE, 2);
    }

    @Test
    void constructPtree() {
        assertNotNull(testing);
        assertNotNull(testing.getVarSetLeafMap());
        assertEquals(5, testing.getVarSetLeafMap().keySet().size());
        assertEquals(9, testing.arrayOfNodes().size());
        assertEquals(1, testing.arrayOfNodes().get(0).getMin());
    }

    @Test
    void insertVariableRI() throws NoSuchTypeException, InvalidRuleInfoException {
        FlagNode govFlag = new FlagNode(M0, true, new PropositionNodeSet());
        Substitutions govSubs = new Substitutions();
        Node henry = Network.createNode("henry", "individualnode");
        govSubs.add(G, henry);
        RuleInfo ruleInfo0 = new RuleInfo("", 0, 1, 0, govSubs, new FlagNodeSet(govFlag));
        testing.insertVariableRI(ruleInfo0);

        FlagNode civFlag = new FlagNode(M1, true, new PropositionNodeSet());
        Substitutions civSubs = new Substitutions();
        Node anne = Network.createNode("anne", "individualnode");
        civSubs.add(C, anne);
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 1, 0, civSubs, new FlagNodeSet(civFlag));
        testing.insertVariableRI(ruleInfo1);

        FlagNode coFlag = new FlagNode(M2, true, new PropositionNodeSet());
        Substitutions coSubs = new Substitutions();
        Node england = Network.createNode("england", "individualnode");
        coSubs.add(Co, england);
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 1, 0, coSubs, new FlagNodeSet(coFlag));
        testing.insertVariableRI(ruleInfo2);

        FlagNode rFlag = new FlagNode(M3, true, new PropositionNodeSet());
        Substitutions rSubs = new Substitutions();
        rSubs.add(Co, england);
        rSubs.add(G, henry);
        RuleInfo ruleInfo3 = new RuleInfo("", 0, 1, 0, rSubs, new FlagNodeSet(rFlag));
        testing.insertVariableRI(ruleInfo3);

        FlagNode lFlag = new FlagNode(M4, true, new PropositionNodeSet());
        Substitutions lSubs = new Substitutions();
        lSubs.add(Co, england);
        lSubs.add(C, anne);
        RuleInfo ruleInfo4 = new RuleInfo("", 0, 1, 0, lSubs, new FlagNodeSet(lFlag));
        RuleInfoSet result = testing.insertVariableRI(ruleInfo4);
        assertNotNull(result);
        assertEquals(1, result.size());

        Ptree testing2 = Ptree.constructPtree(new PropositionNodeSet(M0, M1, M2, M3, M4), 5, Integer.MAX_VALUE, 2);
        Substitutions govSubs2 = new Substitutions();
        Node khaleesi = Network.createNode("khaleesi", "individualnode");
        govSubs2.add(G, khaleesi);
        RuleInfo ruleInfo5 = new RuleInfo("", 0, 1, 0, govSubs2, new FlagNodeSet(govFlag));
        testing2.insertVariableRI(ruleInfo5);
        testing2.insertVariableRI(ruleInfo1);
        testing2.insertVariableRI(ruleInfo2);
        testing2.insertVariableRI(ruleInfo3);
        testing2.insertVariableRI(ruleInfo4);
        RuleInfoSet result2 = testing2.insertVariableRI(ruleInfo0);
        assertNotNull(result2);
        assertEquals(1, result2.size());
    }

    @Test
    void arrayOfNodes() {
        assertNotNull(testing.arrayOfNodes());
        assertEquals(9, testing.arrayOfNodes().size());
    }

}