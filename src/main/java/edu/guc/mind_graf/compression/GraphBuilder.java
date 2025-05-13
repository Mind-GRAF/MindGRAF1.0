package edu.guc.mind_graf.compression;

import java.util.ArrayList;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

public class GraphBuilder {
    String context;
    HashMap<Integer, HypNode> hypothesesNodes;
    HashMap<Integer, SupportNode> supportNodes;
    int sizeV1;
    int sizeV2;
    HashMap<Integer, HashSet<Integer>> hypAdj;
    HashMap<Integer, HashSet<Integer>> hypAdjRev;
    HashMap<Integer, HashSet<Integer>> supportAdj;
    HashMap<Integer, HashSet<Integer>> supportAdjRev;
    HashMap<HypNode, Integer> hypothesesNodesRev;
    HashMap<SupportNode, Integer> supportNodesRev;

    public GraphBuilder(String context) {
        this.context = context;
        hypothesesNodes = new HashMap<>();
        supportNodes = new HashMap<>();
        sizeV1 = 0;
        sizeV2 = 0;
        hypAdj = new HashMap<>();
        hypAdjRev = new HashMap<>();
        supportAdj = new HashMap<>();
        supportAdjRev = new HashMap<>();
        hypothesesNodesRev = new HashMap<>();
        supportNodesRev = new HashMap<>();
    }

    public BipartiteGraph buildBaseSupportGraph() {
        Context currentContext = ContextController.getContext(context);
        Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
        for (int attitudeID : attitudes) {
            int[] originSet = currentContext.getOriginHypotheses(0, attitudeID).getProps();
            processHypothesesSet(originSet, attitudeID);
        }
        HashSet<Integer>[] adjList = createAdjList(hypAdj, supportAdj);
        HashSet<Integer>[] adjRevList = createAdjList(hypAdjRev, supportAdjRev);
        return new BipartiteGraph(sizeV1, sizeV2, adjList, adjRevList);
    }

    public HashSet<Integer>[] createAdjList(HashMap<Integer, HashSet<Integer>> hypAdjList,
            HashMap<Integer, HashSet<Integer>> supportAdjList) {
        HashSet<Integer>[] adj = new HashSet[sizeV1 + sizeV2];
        for (int i = 0; i < sizeV1; i++) {
            if (hypAdjList.get(i) != null) {
                adj[i] = new HashSet<>();
                for (Integer value : hypAdjList.get(i)) {
                    adj[i].add(value + sizeV1);
                }
            } else {
                adj[i] = new HashSet<>();
            }
        }
        for (int i = 0; i < sizeV2; i++) {
            adj[i + sizeV1] = supportAdjList.get(i) != null ? new HashSet<>(supportAdjList.get(i)) : new HashSet<>();
        }
        return adj;
    }

    public void processHypothesesSet(int[] hyps, int attitudeID) {
        for (int i = 0; i < hyps.length; i++) {
            createHypNode(hyps[i], attitudeID);
            PropositionNode node = (PropositionNode) Network.getNodeById(hyps[i]);
            boolean isSupportedAtLevel0 = node.getSupport().getAssumptionBasedSupport().get(0) != null;
            if (isSupportedAtLevel0) {
                boolean isSupportedAtLevel0InAttitude = node.getSupport().getAssumptionBasedSupport().get(0)
                        .get(attitudeID) != null;
                if (isSupportedAtLevel0InAttitude) {
                    ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supportedLevel = node
                            .getSupport().getAssumptionBasedSupport().get(0).get(attitudeID);
                    for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : supportedLevel) {
                        createSupport(hyps[i], attitudeID, support);
                    }
                }
            }

        }
    }

    public int createHypNode(int nodeID, int attitudeID) {
        HypNode newNode = new HypNode(nodeID, attitudeID);
        if (hypothesesNodesRev.get(newNode) != null) {
            return hypothesesNodesRev.get(newNode);
        }
        int baseSupportGraphHypNodeID = sizeV1;
        sizeV1++;
        hypothesesNodes.put(baseSupportGraphHypNodeID, newNode);
        hypothesesNodesRev.put(newNode, baseSupportGraphHypNodeID);
        return baseSupportGraphHypNodeID;
    }

    public int createSupportNode(ArrayList<HypNode> hypNodes) {
        SupportNode supportingNode = new SupportNode(hypNodes);
        if (supportNodesRev.get(supportingNode) != null) {
            return supportNodesRev.get(supportingNode);
        }
        int baseSupportGraphSupportNodeID = sizeV2;
        supportNodes.put(baseSupportGraphSupportNodeID, supportingNode);
        supportNodesRev.put(supportingNode, baseSupportGraphSupportNodeID);
        sizeV2++;
        for (HypNode node : hypNodes) {
            int nodeId = hypothesesNodesRev.get(node);
            hypAdj.computeIfAbsent(nodeId, k -> new HashSet<>()).add(baseSupportGraphSupportNodeID);
            supportAdjRev.computeIfAbsent(baseSupportGraphSupportNodeID, k -> new HashSet<>()).add(nodeId);
        }

        return baseSupportGraphSupportNodeID;
    }

    public void processSupportingSet(int[] supportingNodes, int supportingAttitude, ArrayList<HypNode> hypNodes) {
        for (int j = 0; j < supportingNodes.length; j++) {
            int baseSupportGraphHypNodeID = createHypNode(supportingNodes[j], supportingAttitude);
            hypNodes.add(hypothesesNodes.get(baseSupportGraphHypNodeID));
        }
    }

    public void createSupport(int nodeID, int attitudeID,
            Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support) {
        if (Support.isDefaultHypSupport(attitudeID, nodeID, support)) {
            return;
        }
        ArrayList<HypNode> hypNodes = new ArrayList<>();
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportingNodes = support.getFirst();
        for (int supportingAttitude : supportingNodes.keySet()) {
            Pair<PropositionNodeSet, PropositionNodeSet> supportingNodesInAttitude = supportingNodes
                    .get(supportingAttitude);
            int[] originSupportingNodesInAttitude = supportingNodesInAttitude.getFirst().getProps();
            int[] gradedSupportingNodesInAttitude = supportingNodesInAttitude.getSecond().getProps();
            processSupportingSet(originSupportingNodesInAttitude, supportingAttitude, hypNodes);
            processSupportingSet(gradedSupportingNodesInAttitude, supportingAttitude, hypNodes);
        }
        int baseSupportGraphSupportNodeID = createSupportNode(hypNodes);
        int baseSupportGraphSupportedNodeID = hypothesesNodesRev.get(new HypNode(nodeID, attitudeID));
        hypAdjRev.computeIfAbsent(baseSupportGraphSupportedNodeID, k -> new HashSet<>())
                .add(baseSupportGraphSupportNodeID);
        supportAdj.computeIfAbsent(baseSupportGraphSupportNodeID, k -> new HashSet<>())
                .add(baseSupportGraphSupportedNodeID);
    }

    public void printHypothesesNodes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hypotheses Nodes Mapping:\n");
        for (Map.Entry<Integer, HypNode> entry : hypothesesNodes.entrySet()) {
            sb.append("  ID ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        System.out.println(sb.toString());
    }

    public void printSupportNodes() {
        StringBuilder sb = new StringBuilder();
        sb.append("Support Nodes Mapping:\n");
        for (Map.Entry<Integer, SupportNode> entry : supportNodes.entrySet()) {
            sb.append("  ID ").append(entry.getKey() + sizeV1).append(" -> ").append(entry.getValue()).append("\n");
        }
        System.out.println(sb.toString());

    }

    public static void main(String[] args) throws NoSuchTypeException {
 
    }
}
