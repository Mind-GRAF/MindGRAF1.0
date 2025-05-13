package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.CannotRemoveNodeException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.exceptions.NodeNotInNetworkException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

public class KZeroCompression {
    //
    // remove the hyps in all levels then for each node in the network if it is not
    // a hyp at level zero remove
    // for (int nodeID:)
    // removeDerivedNodes();
    // what about removing the node from the network
    // if hyp in contexts and inferred in others
    // ask about network current level
    //
    public void kZeroCompress() {
        HashMap<Integer, Node> propositionNodes = Network.getPropositionNodes();
        removeLowerLevelsHyps();
        PropositionNodeSet nodesToBeRemoved = new PropositionNodeSet();
        for (int nodeID : propositionNodes.keySet()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            if (!node.isOriginHypInAnyContext()) {
                node.removeNodeFromOtherNodesSupport();
                node.removeFromOthersdependents();
                nodesToBeRemoved.add(nodeID);
            }

        }
        removeNodesFromNetwork(nodesToBeRemoved);

    }

    public void removeLowerLevelsHyps() {
        HashMap<String, Context> contexts = ContextController.getContextSet().getSet();
        for (String context : contexts.keySet()) {
            Context currentContext = ContextController.getContext(context);
            ArrayList<Integer> contextLevels = currentContext.getLevels();
            for (int level : contextLevels) {
                if (level == 0) {
                    continue;
                }
                removeOriginHypsAtLevel(context, level);
                removeGradedHypsAtLevel(context, level);
            }
        }
    }

    public void removeNodes(String contextName, int attitude, int level, PropositionNodeSet nodeSet, boolean isGraded) {
        Context context = ContextController.getContext(contextName);
        for (int nodeID : nodeSet.getProps()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            if (isGraded) {
                context.removeGradedHypothesisFromContext(level, attitude, node);
                if (!node.isHypInOtherContexts(contextName, attitude, level)) {
                    node.removeHypSupport(0, attitude);
                }
            } else {
                context.removeHypothesisFromContext(level, attitude, node);
                if (!node.isHypInOtherContexts(contextName, attitude, level)) {
                    node.removeHypSupport(0, attitude);
                }
            }
        }
    }

    public void removeGradedHypsAtLevel(String contextName, int level) {
        Context context = ContextController.getContext(contextName);
        Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
        for (int attitudeID : attitudes) {
            PropositionNodeSet gradedNodeSet = context.getGradedHypotheses(level, attitudeID);
            removeNodes(contextName, attitudeID, level, gradedNodeSet, true);
        }
    }

    public void removeOriginHypsAtLevel(String contextName, int level) {
        Context context = ContextController.getContext(contextName);
        Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
        for (int attitudeID : attitudes) {
            PropositionNodeSet originNodeSet = context.getOriginHypotheses(level, attitudeID);
            removeNodes(contextName, attitudeID, level, originNodeSet, false);
        }
    }

    public void removeNodesFromNetwork(PropositionNodeSet nodes) {
        for (int nodeID : nodes.getProps()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            try {
                Network.RemoveNode(node);
            } catch (NodeNotInNetworkException e) {
                e.printStackTrace();
            } catch (CannotRemoveNodeException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws NoSuchTypeException {
        Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("a1", 0);
        attitudeNames.add("a2", 1);
        attitudeNames.add("a3", 2);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(Arrays.asList(0, 1))); // a1 and a2
        consistentAttitudes.add(new ArrayList<>(Arrays.asList(1, 2))); // a2 and a3

        Network n = NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

        ContextController.createNewContext("guc");
        ContextController.setCurrContext("guc");
        Context contextToBeTested = ContextController.getContext("guc");

        PropositionNode node1 = (PropositionNode) Network.createNode("1", "propositionnode");
        PropositionNode node2 = (PropositionNode) Network.createNode("2", "propositionnode");
        PropositionNode node3 = (PropositionNode) Network.createNode("3", "propositionnode");
        PropositionNode node4 = (PropositionNode) Network.createNode("4", "propositionnode");
        PropositionNode node5 = (PropositionNode) Network.createNode("5", "propositionnode");

        contextToBeTested.addHypothesisToContext(0, 0, node1);
        contextToBeTested.addHypothesisToContext(0, 1, node1);
        // contextToBeTested.addHypothesisToContext(0, 2, node1);
        contextToBeTested.addHypothesisToContext(0, 0, node2);
        contextToBeTested.addHypothesisToContext(0, 1, node3);
        contextToBeTested.addHypothesisToContext(0, 2, node4);
        contextToBeTested.addHypothesisToContext(0, 0, node5);
        System.out.print(ContextController.getContext("guc").toString());

        PropositionNodeSet ps_1_2 = new PropositionNodeSet(1, 2);
        PropositionNodeSet ps_1_3 = new PropositionNodeSet(1, 3);
        PropositionNodeSet ps_1_2_5 = new PropositionNodeSet(1, 2, 5);
        PropositionNodeSet ps_1_4 = new PropositionNodeSet(1, 4);
        PropositionNodeSet ps_1_5 = new PropositionNodeSet(1, 5);
        PropositionNodeSet ps_3 = new PropositionNodeSet(3);
        PropositionNodeSet ps_4 = new PropositionNodeSet(4);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support;

        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1_2, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        node3.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        supports.clear();

        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1_5, new PropositionNodeSet()));
        support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));

        support.clear();
        support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));
        node4.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support = new HashMap<>();
        support.put(2, new Pair<>(ps_1_4, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet(10)));
        node2.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));

        System.out.println(node1.getSupport() + "node1 assumptionBasedSupport");
        System.out.println(node2.getSupport() + "node2 assumptionBasedSupport");
        System.out.println(node3.getSupport() + "node3 assumptionBasedSupport");
        System.out.println(node4.getSupport() + "node4 assumptionBasedSupport");
        System.out.println(node5.getSupport() + "node5 assumptionBasedSupport");

    }

}