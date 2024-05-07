package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AndEntailmentTest {

    @Test
    void mayInfer() throws NoSuchTypeException, InvalidRuleInfoException, DirectCycleException {
        Network network = new Network();
        Node G = Network.createVariableNode("G", "propositionnode");
        Node C = Network.createVariableNode("C", "propositionnode");
        Node Co = Network.createVariableNode("Co", "propositionnode");

        DownCable gMem = new DownCable(Network.getRelations().get("member"), new NodeSet(G));
        Node government = Network.createNode("government", "propositionnode");
        DownCable gov = new DownCable(Network.getRelations().get("class"), new NodeSet(government));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(gMem, gov));

        DownCable cMem = new DownCable(Network.getRelations().get("member"), new NodeSet(C));
        Node civilian = Network.createNode("civilian", "propositionnode");
        DownCable civ = new DownCable(Network.getRelations().get("class"), new NodeSet(civilian));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(cMem, civ));

        DownCable coMem = new DownCable(Network.getRelations().get("member"), new NodeSet(Co));
        Node country = Network.createNode("country", "propositionnode");
        DownCable coun = new DownCable(Network.getRelations().get("class"), new NodeSet(country));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(coMem, coun));

        Relation rule = Network.createRelation("rule", "",
                Adjustability.EXPAND, 2);
        Relation ruled = Network.createRelation("ruled", "",
                Adjustability.EXPAND, 2);
        DownCable ruling = new DownCable(rule, new NodeSet(G));
        DownCable ruledC = new DownCable(ruled, new NodeSet(Co));
        Node M3 = Network.createNode("propositionnode", new DownCableSet(ruling, ruledC));

        Relation living = Network.createRelation("living", "",
                Adjustability.EXPAND, 2);
        Relation in = Network.createRelation("in", "",
                Adjustability.EXPAND, 2);
        DownCable livingC = new DownCable(living, new NodeSet(C));
        DownCable inC = new DownCable(in, new NodeSet(Co));
        Node M4 = Network.createNode("propositionnode", new DownCableSet(livingC, inC));

        Relation brainwash = Network.createRelation("brainwash", "propositionnode",
                Adjustability.EXPAND, 2);
        Relation brainwashed = Network.createRelation("brainwashed", "propositionnode",
                Adjustability.EXPAND, 2);
        DownCable brainwashG = new DownCable(brainwash, new NodeSet(G));
        DownCable brainwashedC = new DownCable(brainwashed, new NodeSet(C));
        Node M5 = Network.createNode("propositionnode", new DownCableSet(brainwashG, brainwashedC));

        Node P0 = Network.createNode("andentailment", new DownCableSet(new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1, M2, M3, M4)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M5))));

        FlagNode govFlag = new FlagNode(M0, true, new Support(-1));
        Substitutions govSubs = new Substitutions();
        Node henry = Network.createNode("henry", "individualnode");
        govSubs.add(G, henry);
        RuleInfo ruleInfo0 = new RuleInfo("", 0, 1, 0, govSubs, new FlagNodeSet(govFlag), new Support(-1));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo0);

        FlagNode civFlag = new FlagNode(M1, true, new Support(-1));
        Substitutions civSubs = new Substitutions();
        Node anne = Network.createNode("anne", "individualnode");
        civSubs.add(C, anne);
        RuleInfo ruleInfo1 = new RuleInfo("", 0, 1, 0, civSubs, new FlagNodeSet(civFlag), new Support(-1));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo1);

        FlagNode coFlag = new FlagNode(M2, true, new Support(-1));
        Substitutions coSubs = new Substitutions();
        Node england = Network.createNode("england", "individualnode");
        coSubs.add(Co, england);
        RuleInfo ruleInfo2 = new RuleInfo("", 0, 1, 0, coSubs, new FlagNodeSet(coFlag), new Support(-1));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo2);

        FlagNode rFlag = new FlagNode(M3, true, new Support(-1));
        Substitutions rSubs = new Substitutions();
        rSubs.add(Co, england);
        rSubs.add(G, henry);
        RuleInfo ruleInfo3 = new RuleInfo("", 0, 1, 0, rSubs, new FlagNodeSet(rFlag), new Support(-1));
        ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo3);

        FlagNode lFlag = new FlagNode(M4, true, new Support(-1));
        Substitutions lSubs = new Substitutions();
        lSubs.add(Co, england);
        lSubs.add(C, anne);
        RuleInfo ruleInfo4 = new RuleInfo("", 0, 1, 0, lSubs, new FlagNodeSet(lFlag), new Support(-1));
        RuleInfoSet inserted = ((RuleNode)P0).getRuleInfoHandler().insertRI(ruleInfo4);
        ((RuleNode)P0).setRootRuleInfos(inserted);

        RuleInfoSet[] inferrable = ((RuleNode)P0).mayInfer();
        assertEquals(1, inferrable[0].size());
    }
}