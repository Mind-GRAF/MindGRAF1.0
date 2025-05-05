package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.CannotRemoveNodeException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.exceptions.NodeNotInNetworkException;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

public class ReverseTrim {

    String context;

    public ReverseTrim(String context) {
        this.context = context;
    }

    public void reverseTrim() {
        // get maxHyps
        // remove from this context
        // for all dependents remove it from the support ?
        MaximalHypotheses maxHyps = new MaximalHypotheses(context);
        maxHyps.computeMaximalHypotheses();
        PropositionNodeSet[] contextMaxHyps = maxHyps.getMaximalHypotheses();
        boolean nodeRemoved = removeMaxHyps(contextMaxHyps);
        if (nodeRemoved) {
            reverseTrim();
        }

    }

    public void removeMaxHypFromNetwork(int nodeID, int attitudeID)
            throws NodeNotInNetworkException, CannotRemoveNodeException {
        PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
        removeMaxHypFromContext(nodeID, attitudeID);
        Network.RemoveNode(node);
        System.out.println("node removed from network " + nodeID);
    }

    public void removeMaxHypFromContext(int nodeID, int attitudeID) {
        PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
        Context currentContext = ContextController.getContext(context);
        currentContext.removeHypothesisFromContext(0, attitudeID, node);
        if (!node.isHypInAnyContext(context, attitudeID, 0)) {
            node.removeHypSupport(0, attitudeID);
        }
        System.out.println("node removed " + nodeID);
    }

    public boolean removeMaxHyps(PropositionNodeSet[] maxHyps) {
        boolean anyNodeRemoved = false;
        for (int attitudeID = 0; attitudeID < maxHyps.length; attitudeID++) {
            PropositionNodeSet originHyps = maxHyps[attitudeID];
            boolean nodeRemoved = processMaxHypsAtAttitude(originHyps, attitudeID);
            if (nodeRemoved) {
                anyNodeRemoved = true;
            }
        }
        return anyNodeRemoved;

    }

    public boolean processMaxHypsAtAttitude(PropositionNodeSet MaxHyps, int attitudeID) {
        boolean nodeRemoved = false;
        for (int nodeID : MaxHyps.getProps()) {
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            if (!node.isHypInAnyContextAtAnyAttitude(context, 0)
                    && !node.isHypAtOtherAttitudes(context, attitudeID, 0)) {
                // remove from the network
                // remove from all supports
                node.removeNodeFromOtherNodesSupport();
                try {
                    nodeRemoved = true;
                    removeMaxHypFromNetwork(nodeID, attitudeID);
                  } catch (NodeNotInNetworkException e) {
                    e.printStackTrace();
                  } catch (CannotRemoveNodeException e) {
                    e.printStackTrace();
                  }

            } else {
                nodeRemoved = true;
                removeMaxHypFromContext(nodeID,attitudeID);
                node.removeNodeFromOtherSupportsInContext(context, attitudeID);
                // remove from the context only
                 // go to the dependents and remove any support that contains this node in the
            // attitude
            }
           
        }

        return nodeRemoved;
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
        contextToBeTested.addHypothesisToContext(0, 2, node1);
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
        // ex1
        // support = new HashMap<>();
        // support.put(0, new Pair<>( new PropositionNodeSet(2), new
        // PropositionNodeSet()));
        // supports.add(new Pair<>(support, new PropositionNodeSet()));
        // node1.addJustificationBasedSupports(0, Network.currentLevel, new
        // ArrayList<>(supports));
        // supports.clear();
        // support = new HashMap<>();
        // support.put(0, new Pair<>( new PropositionNodeSet(1), new
        // PropositionNodeSet()));
        // supports.add(new Pair<>(support, new PropositionNodeSet()));
        // node1.addJustificationBasedSupports(1, Network.currentLevel, new
        // ArrayList<>(supports));
        // supports.clear();
        // support = new HashMap<>();
        // support.put(1, new Pair<>( new PropositionNodeSet(1), new
        // PropositionNodeSet()));
        // supports.add(new Pair<>(support, new PropositionNodeSet()));
        // node1.addJustificationBasedSupports(2, Network.currentLevel, new
        // ArrayList<>(supports));
        // supports.clear();
        // support = new HashMap<>();
        // support.put(2, new Pair<>( new PropositionNodeSet(1), new
        // PropositionNodeSet()));
        // supports.add(new Pair<>(support, new PropositionNodeSet()));
        // node1.addJustificationBasedSupports(0, Network.currentLevel, new
        // ArrayList<>(supports));

        // ex2
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1_5, new PropositionNodeSet()));
        support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));

        support.clear();
        support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));
        node4.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1_2, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        node3.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        supports.clear();

        supports.clear();
        support = new HashMap<>();
        support.put(2, new Pair<>(ps_1_4, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet(10)));
        node2.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));

        System.out.println(node1.getAssumptionSupportDependents() + "node1 assumptionBasedSupport");
        System.out.println(node2.getAssumptionSupportDependents() + "node2 assumptionBasedSupport");
        System.out.println(node3.getAssumptionSupportDependents() + "node3 assumptionBasedSupport");
        System.out.println(node4.getAssumptionSupportDependents() + "node4 assumptionBasedSupport");
        System.out.println(node5.getAssumptionSupportDependents() + "node5 assumptionBasedSupport");
      //  node3.removeNodeFromOtherNodesSupport();
        System.out.println(node1.getSupport() + "node1 assumptionBasedSupport");
        System.out.println(node2.getSupport() + "node2 assumptionBasedSupport");
        System.out.println(node3.getSupport() + "node3 assumptionBasedSupport");
        System.out.println(node4.getSupport() + "node4 assumptionBasedSupport");
        System.out.println(node5.getSupport() + "node5 assumptionBasedSupport");
        ReverseTrim rTrim = new ReverseTrim("guc");
        rTrim.reverseTrim();
        System.out.print(ContextController.getContext("guc").toString());
        System.out.println(node1.getAssumptionSupportDependents() + "node1 assumptionBasedSupport");
        System.out.println(node2.getAssumptionSupportDependents() + "node2 assumptionBasedSupport");
        System.out.println(node3.getAssumptionSupportDependents() + "node3 assumptionBasedSupport");
        System.out.println(node4.getAssumptionSupportDependents() + "node4 assumptionBasedSupport");
        System.out.println(node5.getAssumptionSupportDependents() + "node5 assumptionBasedSupport");

    }

}