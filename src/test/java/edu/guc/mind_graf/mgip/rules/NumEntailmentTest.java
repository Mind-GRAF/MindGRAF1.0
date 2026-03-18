package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.mgip.ruleHandlers.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumEntailmentTest {

    @Test
    void mayInfer() throws NoSuchTypeException, InvalidRuleInfoException, DirectCycleException {
        Network network = new Network();
        Node one = Network.createNode("1", "propositionnode");

        Node Patroclus = Network.createNode("Patroclus", "propositionnode");
        Node Achilles = Network.createNode("Achilles", "propositionnode");
        Node Hector = Network.createNode("Hector", "propositionnode");
        Node Alive = Network.createNode("Alive", "propositionnode");

        DownCable pMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Patroclus));
        DownCable pClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(pMember, pClass));

        DownCable hMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Hector));
        DownCable hClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(hMember, hClass));

        DownCable aMember = new DownCable(Network.getRelations().get("member"), new NodeSet(Achilles));
        DownCable aClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Alive));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(aMember, aClass));

        Node P0 = Network.createNode("numentailment", new DownCableSet(new DownCable(Network.getRelations().get("i"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("ant"), new NodeSet(M0, M1)),
                new DownCable(Network.getRelations().get("cq"), new NodeSet(M2))));

        FlagNodeSet flags = new FlagNodeSet(new FlagNode(M0, true, new Support(-1)));
        RuleInfoSet inserted = ((RuleNode)P0).getRuleInfoHandler().insertRI(new RuleInfo("", 0, 1, 0, new Substitutions(), flags, new Support(-1)));
        ((RuleNode)P0).setRootRuleInfos(inserted);

        RuleInfoSet[] inferrable = ((RuleNode)P0).mayInfer();
        assertEquals(1, inferrable[0].size());
    }
}