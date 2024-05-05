package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.FlagNodeSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreshTest {

    @Test
    void mayInfer() throws NoSuchTypeException, InvalidRuleInfoException {
//        Network network = new Network();
//        Node X = Network.createVariableNode("X", "propositionnode");
//        Node one = Network.createNode("1", "individualnode");
//        Node three = Network.createNode("3", "individualnode");
//        Node idealistic = Network.createNode("Idealistic", "individualnode");
//        Node moral = Network.createNode("Moral", "individualnode");
//        Node brave = Network.createNode("Brave", "individualnode");
//
//        DownCable iMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
//        DownCable mMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
//        DownCable bMember = new DownCable(Network.getRelations().get("member"), new NodeSet(X));
//        DownCable iClass = new DownCable(Network.getRelations().get("class"), new NodeSet(idealistic));
//        DownCable mClass = new DownCable(Network.getRelations().get("class"), new NodeSet(moral));
//        DownCable bClass = new DownCable(Network.getRelations().get("class"), new NodeSet(brave));
//        Node M0 = Network.createNode("propositionnode", new DownCableSet(iMember, iClass));
//        Node M1 = Network.createNode("propositionnode", new DownCableSet(mMember, mClass));
//        Node M2 = Network.createNode("propositionnode", new DownCableSet(bMember, bClass));
//
//        Node P0 = Network.createNode("thresh", new DownCableSet(new DownCable(Network.getRelations().get("thresh"), new NodeSet(one)),
//                new DownCable(Network.getRelations().get("threshmax"), new NodeSet(three)),
//                new DownCable(Network.getRelations().get("arg"), new NodeSet(M0, M1, M2))));
//
//        Substitutions subs = new Substitutions();
//        subs.add(X, Network.createNode("Patroclus", "individualnode"));
//        //TODO: sara, changed by wael to merge supports
////        FlagNodeSet flags = new FlagNodeSet(new FlagNode(M0, true, new Support()));
////        RuleInfoSet inserted = ((RuleNode)P0).getRuleInfoHandler().insertRI(new RuleInfo(1, 0, subs, flags));
////        ((RuleNode)P0).setRootRuleInfos(inserted);
//        RuleInfoSet[] inferrable = ((RuleNode)P0).mayInfer();
//        assertEquals(1, inferrable[0].size());
//        assertEquals(0, inferrable[1].size());
    }
}