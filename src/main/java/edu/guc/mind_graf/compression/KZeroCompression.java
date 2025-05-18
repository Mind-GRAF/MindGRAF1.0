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

    public static void kZeroCompress() {
        HashMap<Integer, Node> propositionNodes = Network.getPropositionNodes();
        removeLowerLevelsHypsFromContexts();
        PropositionNodeSet nodesToBeRemoved = new PropositionNodeSet();
        for (int nodeID : propositionNodes.keySet()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            // System.out.println("node is hyp list" + nodeID + " " +
            // node.getSupport().getIsHyp());
            if (!node.isOriginHypInAnyContext()) {
                node.removeNodeFromOtherNodesSupport();
                node.removeFromOthersdependents();
                nodesToBeRemoved.add(nodeID);
            }

        }
        removeNodesFromNetwork(nodesToBeRemoved);
    }

    public static void removeLowerLevelsHypsFromContexts() {
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

    public static void removeNodes(String contextName, int attitude, int level, PropositionNodeSet nodeSet, boolean isGraded) {
        Context context = ContextController.getContext(contextName);
        // System.out.println("inputs to removeNodes" + nodeSet +" contextName "+
        // contextName + " level "+ level + " is graded "+ isGraded);
        for (int nodeID : nodeSet.getProps()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            if (isGraded) {
                context.removeGradedHypothesisFromContext(level, attitude, node);
            } else {
                context.removeHypothesisFromContext(level, attitude, node);
            }
            if (!node.isHypInOtherContexts(contextName, attitude, level)) {
                node.removeHypSupport(level, attitude);
            }
        }
    }

    public static void removeGradedHypsAtLevel(String contextName, int level) {
        Context context = ContextController.getContext(contextName);
        Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
        for (int attitudeID : attitudes) {
            PropositionNodeSet gradedNodeSet = context.getGradedHypotheses(level, attitudeID);
            if (gradedNodeSet != null)
                removeNodes(contextName, attitudeID, level, gradedNodeSet, true);
        }
    }

    public static void removeOriginHypsAtLevel(String contextName, int level) {
        Context context = ContextController.getContext(contextName);
        Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
        for (int attitudeID : attitudes) {
            PropositionNodeSet originNodeSet = context.getOriginHypotheses(level, attitudeID);
            if (originNodeSet != null)
                removeNodes(contextName, attitudeID, level, originNodeSet, false);
        }
    }

    public static void removeNodesFromNetwork(PropositionNodeSet nodes) {
        for (int nodeID : nodes.getProps()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            try {
                Network.RemoveNode(node);
                // System.out.println("node to be removed " + nodeID);
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

        ContextController.createNewContext("context1");
        ContextController.createNewContext("context2");
        ContextController.createNewContext("context3");
        ContextController.setCurrContext("context1");
        Context context1 = ContextController.getContext("context1");
        Context context2 = ContextController.getContext("context2");
        Context context3 = ContextController.getContext("context3");

        PropositionNode node1 = (PropositionNode) Network.createNode("1", "propositionnode");
        PropositionNode node2 = (PropositionNode) Network.createNode("2", "propositionnode");
        PropositionNode node3 = (PropositionNode) Network.createNode("3", "propositionnode");
        PropositionNode node4 = (PropositionNode) Network.createNode("4", "propositionnode");
        PropositionNode node5 = (PropositionNode) Network.createNode("5", "propositionnode");
        PropositionNode node6 = (PropositionNode) Network.createNode("6", "propositionnode");
        PropositionNode node7 = (PropositionNode) Network.createNode("7", "propositionnode");
        PropositionNode node8 = (PropositionNode) Network.createNode("8", "propositionnode");
        PropositionNode node9 = (PropositionNode) Network.createNode("9", "propositionnode");
        PropositionNode node10 = (PropositionNode) Network.createNode("10", "propositionnode");

        // context1
        // context1 level 0 hyps
        context1.addHypothesisToContext(0, 0, node1);
        context1.addHypothesisToContext(0, 1, node2);
        context1.addHypothesisToContext(0, 2, node3);

        // context1 level 1 hyps
        Network.currentLevel = 1;
        context1.addHypothesisToContext(1, 0, node1);
        context1.addHypothesisToContext(1, 1, node2);
        context1.addHypothesisToContext(1, 2, node3);

        // // context1 level 2 hyps
        // Network.currentLevel = 2;
        // context1.addHypothesisToContext(2, 0, node1);
        // context1.addHypothesisToContext(2, 1, node2);
        // context1.addHypothesisToContext(2, 2, node3);
        // context2

        // context2 level0 hyps
        Network.currentLevel = 0;
        context2.addHypothesisToContext(0, 0, node1);
        context2.addHypothesisToContext(0, 1, node2);
        context2.addHypothesisToContext(0, 2, node4);

        // context2 level1 hyps
        Network.currentLevel = 1;
        context2.addHypothesisToContext(1, 0, node1);
        context2.addHypothesisToContext(1, 1, node2);
        context2.addHypothesisToContext(1, 2, node4);

        // context3
        // context3 level0 hyps
        Network.currentLevel = 0;
        context3.addHypothesisToContext(0, 0, node6);
        context3.addHypothesisToContext(0, 1, node7);
        context3.addHypothesisToContext(0, 2, node8);

        // context3 level1 hyps
        Network.currentLevel = 1;
        context3.addHypothesisToContext(1, 0, node6);
        context3.addHypothesisToContext(1, 1, node7);
        context3.addHypothesisToContext(1, 2, node8);

        System.out.print(ContextController.getContext("context1").toString());
        System.out.print(ContextController.getContext("context2").toString());
        System.out.print(ContextController.getContext("context3").toString());

        PropositionNodeSet ps_1 = new PropositionNodeSet(1);
        PropositionNodeSet ps_2 = new PropositionNodeSet(2);
        PropositionNodeSet ps_3 = new PropositionNodeSet(3);
        PropositionNodeSet ps_4 = new PropositionNodeSet(4);
        PropositionNodeSet ps_6 = new PropositionNodeSet(6);
        PropositionNodeSet ps_7 = new PropositionNodeSet(7);
        PropositionNodeSet ps_8 = new PropositionNodeSet(8);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support;
        // node 1 support
        support = new HashMap<>();
        support.put(1, new Pair<>(ps_2, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node1.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node1.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node1.addJustificationBasedSupports(0, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();

        // node2 support
        // support 1 for attitude 1
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1, new PropositionNodeSet()));
        support.put(2, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        // support 2 fro attitude 1
        support.clear();
        support.put(2, new Pair<>(ps_4, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));

        Network.currentLevel = 0;
        node2.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node2.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node2.addJustificationBasedSupports(1, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();

        // node 1 support
        support = new HashMap<>();
        support.put(1, new Pair<>(ps_2, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        // support 2 fro attitude 0
        support.clear();
        support.put(0, new Pair<>(ps_6, new PropositionNodeSet()));
        support.put(1, new Pair<>(ps_7, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));
        Network.currentLevel = 0;
        node1.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node1.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node1.addJustificationBasedSupports(0, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();

        // nod3 support
        support = new HashMap<>();
        support.put(2, new Pair<>(ps_8, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node3.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node3.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node3.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();

        // node4 support
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1, new PropositionNodeSet()));
        support.put(1, new Pair<>(ps_2, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node4.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node4.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node4.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();
        // node5 support
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1, new PropositionNodeSet()));
        support.put(2, new Pair<>(ps_4, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node5.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node5.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node4.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();
        // node8 support
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_6, new PropositionNodeSet()));
        support.put(1, new Pair<>(ps_7, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node8.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node8.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node8.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();

        // node9 support
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_6, new PropositionNodeSet()));
        support.put(2, new Pair<>(ps_8, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node9.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node9.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node9.addJustificationBasedSupports(0, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();
        // node10 support
        support = new HashMap<>();
        support.put(1, new Pair<>(ps_7, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        Network.currentLevel = 0;
        node10.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node10.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        // Network.currentLevel = 2;
        // node10.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));
        supports.clear();

        // support = new HashMap<>();
        // support.put(0, new Pair<>(ps_1_5, new PropositionNodeSet()));
        // support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        // supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));

        // support.clear();
        // support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        // supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));
        // node4.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));

        // supports.clear();
        // support = new HashMap<>();
        // support.put(2, new Pair<>(ps_1_4, new PropositionNodeSet()));
        // supports.add(new Pair<>(support, new PropositionNodeSet(10)));
        // node2.addJustificationBasedSupports(0, Network.currentLevel, new
        // ArrayList<>(supports));

        // System.out.println(node1.getSupport() + "node1 assumptionBasedSupport");
        // System.out.println(node2.getSupport() + "node2 assumptionBasedSupport");
        // System.out.println(node3.getSupport() + "node3 assumptionBasedSupport");
        // System.out.println(node4.getSupport() + "node4 assumptionBasedSupport");
        // System.out.println(node5.getSupport() + "node5 assumptionBasedSupport");
        // System.out.println(node6.getSupport() + "node6 assumptionBasedSupport");
        // System.out.println(node7.getSupport() + "node7 assumptionBasedSupport");
        // System.out.println(node8.getSupport() + "node8 assumptionBasedSupport");
        // System.out.println(node9.getSupport() + "node9 assumptionBasedSupport");
        // System.out.println(node10.getSupport() + "node10 assumptionBasedSupport");
        // System.out.println("node 1 is hyp list "+ node1.getSupport().getIsHyp());
        // System.out.println("node 2 is hyp list "+ node2.getSupport().getIsHyp());
        // System.out.println("node 3 is hyp list "+ node3.getSupport().getIsHyp());
        // System.out.println("node 4 is hyp list "+ node4.getSupport().getIsHyp());
        // System.out.println("node 5 is hyp list "+ node5.getSupport().getIsHyp());
        // System.out.println("node 6 is hyp list "+ node6.getSupport().getIsHyp());
        // System.out.println("node 7 is hyp list "+ node7.getSupport().getIsHyp());
        // System.out.println("node 8 is hyp list "+ node8.getSupport().getIsHyp());
        // System.out.println("node 9 is hyp list "+ node9.getSupport().getIsHyp());
        // System.out.println("node 10 is hyp list "+ node10.getSupport().getIsHyp());
        KZeroCompression kzero = new KZeroCompression();
        kzero.kZeroCompress();
        System.out.print(ContextController.getContext("context1").toString());
        System.out.print(ContextController.getContext("context2").toString());
        System.out.print(ContextController.getContext("context3").toString());
        // Network.printPropositionNodes();
        // System.out.println(node1.getSupport() + "node1 assumptionBasedSupport");
        // System.out.println(node2.getSupport() + "node2 assumptionBasedSupport");
        // System.out.println(node3.getSupport() + "node3 assumptionBasedSupport");
        // System.out.println(node4.getSupport() + "node4 assumptionBasedSupport");
        // System.out.println(node5.getSupport() + "node5 assumptionBasedSupport");
        // System.out.println(node6.getSupport() + "node6 assumptionBasedSupport");
        // System.out.println(node7.getSupport() + "node7 assumptionBasedSupport");
        // System.out.println(node8.getSupport() + "node8 assumptionBasedSupport");
        // System.out.println(node9.getSupport() + "node9 assumptionBasedSupport");
        // System.out.println(node10.getSupport() + "node10 assumptionBasedSupport");

    }

}