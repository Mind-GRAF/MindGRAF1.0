package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;

public class GraphBuilder {
    String context;
    HashMap<Integer, HypNode> hypothesesNodes;
    HashMap<Integer, SupportNode> supportNodes;
    int sizeV1;
    int sizeV2;
    ArrayList<HashSet<Integer>> hypAdj;
    ArrayList<HashSet<Integer>> hypAdjRev;
    ArrayList<HashSet<Integer>> supportAdj;
    ArrayList<HashSet<Integer>> supportAdjRev;
    HashMap<HypNode, Integer> hypothesesNodesRev;
    HashMap<SupportNode, Integer> supportNodesRev;

    public GraphBuilder(String context) {
        this.context = context;
        hypothesesNodes = new HashMap<>();
        supportNodes = new HashMap<>();
        sizeV1 = 0;
        sizeV2 = 0;
        hypAdj = new ArrayList<>();
        hypAdjRev = new ArrayList<>();
        supportAdj = new ArrayList<>();
        supportAdjRev = new ArrayList<>();
        hypothesesNodesRev = new HashMap<>();
        supportNodesRev = new HashMap<>();
    }

    public BipartiteGraph createBaseSupportGraph() {
        Context currentContext = ContextController.getContext(context);
        Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
        for (int attitudeID : attitudes) {
            int[] originSet = currentContext.getOriginHypotheses(attitudeID).getProps();
            int[] gradedSet = currentContext.getGradedHypotheses(0, attitudeID).getProps();
            for (int i = 0; i < originSet.length; i++) {
                createHypNode(originSet[i], attitudeID);
                PropositionNode node = (PropositionNode) Network.getNodeById(originSet[i]);
                boolean isSupportedAtLevel0 = node.getSupport().getAssumptionBasedSupport().get(0) != null;
                if (isSupportedAtLevel0) {
                    boolean isSupportedAtLevel0InAttitude = node.getSupport().getAssumptionBasedSupport().get(0)
                            .get(attitudeID) != null;
                    if (isSupportedAtLevel0InAttitude) {
                        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supportedLevel = node
                                .getSupport().getAssumptionBasedSupport().get(0).get(attitudeID);
                        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : supportedLevel) {
                            createSupport(originSet[i], attitudeID, support);
                        }
                    }
                }

            }
            for (int i = 0; i < gradedSet.length; i++) {
                createHypNode(gradedSet[i], attitudeID);
                PropositionNode node = (PropositionNode) Network.getNodeById(gradedSet[i]);
                boolean isSupportedAtLevel0 = node.getSupport().getAssumptionBasedSupport().get(0) != null;
                if (isSupportedAtLevel0) {
                    boolean isSupportedAtLevel0InAttitude = node.getSupport().getAssumptionBasedSupport().get(0)
                            .get(attitudeID) != null;
                    if (isSupportedAtLevel0InAttitude) {
                        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supportedLevel = node
                                .getSupport().getAssumptionBasedSupport().get(0).get(attitudeID);
                        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : supportedLevel) {
                            createSupport(gradedSet[i], attitudeID, support);
                        }
                    }
                }

            }
        }
        return null;
    }

    public int createHypNode(int nodeID, int attitudeID) {
        HypNode newNode = new HypNode(nodeID, attitudeID);
        int baseSupportGraphHypNodeID = -1;
        if (hypothesesNodesRev.get(newNode) == null) {
            baseSupportGraphHypNodeID = sizeV1;
            sizeV1++;
            hypothesesNodes.put(baseSupportGraphHypNodeID, newNode);
            hypothesesNodesRev.put(newNode, baseSupportGraphHypNodeID);
        } else {
            baseSupportGraphHypNodeID = hypothesesNodesRev.get(newNode);
        }
        return baseSupportGraphHypNodeID;
    }

    public int createSupportNode(ArrayList<HypNode> hypNodes) {
        int baseSupportGraphSupportNodeID = -1;
        SupportNode supportingNode = new SupportNode(hypNodes);
        if (supportNodesRev.containsKey(supportingNode)) {
            baseSupportGraphSupportNodeID = supportNodesRev.get(supportingNode);
        } else {
            baseSupportGraphSupportNodeID = sizeV2;
            supportNodes.put(baseSupportGraphSupportNodeID, supportingNode);
            supportNodesRev.put(supportingNode, baseSupportGraphSupportNodeID);
            sizeV2++;
            for (HypNode node : hypNodes) {
                int nodeId = hypothesesNodesRev.get(node);
                if (hypAdj.get(nodeId) == null) {
                    hypAdj.add(nodeId, new HashSet<>());
                }
                hypAdj.get(nodeId).add(baseSupportGraphSupportNodeID);
                if (supportAdjRev.get(baseSupportGraphSupportNodeID) == null) {
                    supportAdjRev.add(baseSupportGraphSupportNodeID, new HashSet<>());
                }
                supportAdjRev.get(baseSupportGraphSupportNodeID).add(nodeId);
            }
        }
        return baseSupportGraphSupportNodeID;
    }

    public void createSupport(int nodeID, int attitudeID,
            Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support) {
        ArrayList<HypNode> hypNodes = new ArrayList<>();
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportingNodes = support.getFirst();
        for (int supportingAttitude : supportingNodes.keySet()) {
            Pair<PropositionNodeSet, PropositionNodeSet> supportingNodesInAttitude = supportingNodes
                    .get(supportingAttitude);
            int[] originSupportingNodesInAttitude = supportingNodesInAttitude.getFirst().getProps();
            int[] gradedSupportingNodesInAttitude = supportingNodesInAttitude.getSecond().getProps();
            for (int j = 0; j < originSupportingNodesInAttitude.length; j++) {
                int baseSupportGraphHypNodeID = createHypNode(originSupportingNodesInAttitude[j], supportingAttitude);
                hypNodes.add(hypothesesNodes.get(baseSupportGraphHypNodeID));
            }
            for (int j = 0; j < gradedSupportingNodesInAttitude.length; j++) {
                int baseSupportGraphHypNodeID = createHypNode(originSupportingNodesInAttitude[j], supportingAttitude);
                hypNodes.add(hypothesesNodes.get(baseSupportGraphHypNodeID));
            }
        }
        int baseSupportGraphSupportNodeID = createSupportNode(hypNodes);
        int baseSupportGraphSupportedNodeID = hypothesesNodesRev.get(new HypNode(nodeID, attitudeID));
        if (hypAdjRev.get(baseSupportGraphSupportedNodeID) == null) {
            hypAdjRev.add(baseSupportGraphSupportedNodeID, new HashSet<>());
        }
        hypAdjRev.get(baseSupportGraphSupportedNodeID).add(baseSupportGraphSupportNodeID);
        if (supportAdj.get(baseSupportGraphSupportNodeID) == null) {
            supportAdj.add(baseSupportGraphSupportNodeID, new HashSet<>());
        }
        supportAdj.get(baseSupportGraphSupportNodeID).add(baseSupportGraphSupportedNodeID);
    }

    public static void main(String[] args) throws NoSuchTypeException {
        // Set<String, Integer> attitudeNames = new Set<>();
        // attitudeNames.add("beliefs", 0);
        // attitudeNames.add("obligations", 1);
        // attitudeNames.add("fears", 2);
        // attitudeNames.add("hate", 3);

        // ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        // consistentAttitudes.add(new ArrayList<>(List.of(0)));
        // consistentAttitudes.add(new ArrayList<>(List.of(1)));
        // consistentAttitudes.add(new ArrayList<>(List.of(0, 2)));
        // consistentAttitudes.add(new ArrayList<>(List.of(0, 2, 3)));

        // Network n = NetworkController.setUp(attitudeNames, consistentAttitudes,
        // false, false, false, 1);
        // ContextController.createNewContext("guc");
        // ContextController.setCurrContext("guc");

        // PropositionNode p = (PropositionNode) Network.createNode("p",
        // "propositionnode");
        // HashMap<String, Relation> relations = Network.getRelations();

        // NodeSet ns1 = new NodeSet();
        // ns1.add(p);
        // NodeSet ns2 = new NodeSet();
        // ns2.add(Network.getBaseNodes().get("0"));

        // DownCable downCable1 = new DownCable(relations.get("arg"), ns1);
        // DownCable downCable2 = new DownCable(relations.get("min"), ns2);
        // DownCable downCable3 = new DownCable(relations.get("max"), ns2);

        // DownCableSet downCableSet = new DownCableSet(downCable1, downCable2,
        // downCable3);

        // PropositionNode notP = (PropositionNode)
        // Network.createNode("propositionnode", downCableSet);(

    }

}
