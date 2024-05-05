package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;
import edu.guc.mind_graf.network.Network;

import static org.junit.jupiter.api.Assertions.*;

class AndOrTest {

    @Test
    void mayInfer() throws NoSuchTypeException, InvalidRuleInfoException {
        Network network = new Network();
        Node A = Network.createVariableNode("A", "propositionnode");
        Node one = Network.createNode("1", "propositionnode");
        Node Fish = Network.createNode("Fish", "propositionnode");
        Node Cat = Network.createNode("Cat", "propositionnode");
        Node Dog = Network.createNode("Dog", "propositionnode");
        DownCable fMember = new DownCable(Network.getRelations().get("member"), new NodeSet(A));
        DownCable cMember = new DownCable(Network.getRelations().get("member"), new NodeSet(A));
        DownCable dMember = new DownCable(Network.getRelations().get("member"), new NodeSet(A));
        DownCable fClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Fish));
        DownCable cClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Cat));
        DownCable dClass = new DownCable(Network.getRelations().get("class"), new NodeSet(Dog));
        Node M0 = Network.createNode("propositionnode", new DownCableSet(fMember, fClass));
        Node M1 = Network.createNode("propositionnode", new DownCableSet(cMember, cClass));
        Node M2 = Network.createNode("propositionnode", new DownCableSet(dMember, dClass));
        Node P0 = Network.createNode("AndOr", new DownCableSet(new DownCable(Network.getRelations().get("min"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("max"), new NodeSet(one)),
                new DownCable(Network.getRelations().get("arg"), new NodeSet(M0, M1, M2))));

        Substitutions subs = new Substitutions();
        subs.add(A, Network.createNode("Nemo", "propositionnode"));
        FlagNodeSet flags = new FlagNodeSet(new FlagNode(M0, true, new Support(-1)));
        RuleInfoSet inserted = ((RuleNode)P0).getRuleInfoHandler().insertRI(new RuleInfo("", 0, 1, 0, subs, flags, new Support(-1)));
        ((RuleNode)P0).setRootRuleInfos(inserted);

        RuleInfoSet[] inferrable = ((RuleNode)P0).mayInfer();
        assertEquals(0, inferrable[0].size());
        assertEquals(1, inferrable[1].size());

    }
}