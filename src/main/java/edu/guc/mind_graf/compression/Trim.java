package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.ContextSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.context.*;
import edu.guc.mind_graf.exceptions.CannotRemoveNodeException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.exceptions.NodeNotInNetworkException;

public class Trim {
  String context;

  public Trim(String context) {
    this.context = context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public void contextTrim() {
    Context currentContext = ContextController.getContext(context);
    Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
    for (int attitudeID : attitudes) {
      PropositionNodeSet originSet = currentContext.getOriginHypotheses(attitudeID);
      processAttitudeHypsSet(originSet, attitudeID);
    }

  }

  public void processAttitudeHypsSet(PropositionNodeSet hypotheses, int attitudeID) {
    for (int nodeID : hypotheses.getProps()) {
      boolean hasDependents = hasDependents(nodeID, attitudeID);
      PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
      if (!hasDependents) {
        if (!node.isHypInAnyContextAtAnyAttitude(context, 0) && !node.isHypAtOtherAttitudes(context, attitudeID, 0)) {
          try {
            removeHypFromNetwork(nodeID, attitudeID);
          } catch (NodeNotInNetworkException e) {
            e.printStackTrace();
          } catch (CannotRemoveNodeException e) {
            e.printStackTrace();
          }
        } else {
          // remove only from this context
          removeHypFromContext(nodeID, attitudeID);
        }
      }
    }
  }

  public boolean hasDependents(int nodeID, int supportingAttitudeID) {
    PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
    PropositionNodeSet AssumptionSupportDependents = node.getAssumptionSupportDependents();
    Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
    for (int dependentNodeID : AssumptionSupportDependents.getProps()) {
      for (int supportedAttitude : attitudes) {
        boolean isDependent = isDependent(dependentNodeID, supportedAttitude, nodeID, supportingAttitudeID);
        if (isDependent) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isDependent(int supportedNodeID, int supportedAttitudeID, int supportingNodeID,
      int supportingAttitudeID) {
    Context currentContext = ContextController.getContext(context);
    PropositionNode dependentNode = (PropositionNode) Network.getNodeById(supportedNodeID);
    PropositionNodeSet originSet = currentContext.getOriginHypotheses(supportedAttitudeID);
    if (!originSet.contains(supportedNodeID)) {
      return false;
    }
    ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = dependentNode
        .getDerivingSupport(context, supportedAttitudeID, 0);
    for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : supports) {
      if (support.getFirst().get(supportingAttitudeID) == null) {
        continue;
      }
      if (support.getFirst().get(supportingAttitudeID).getFirst().contains(supportingNodeID)) {
        return true;
      }
    }
    return false;
  }

  public void removeHypFromNetwork(int nodeID, int attitudeID)
      throws NodeNotInNetworkException, CannotRemoveNodeException {
    PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
    removeHypFromContext(nodeID, attitudeID);
    Network.RemoveNode(node);
    System.out.println("node removed " + nodeID);
  }

  public void removeHypFromContext(int nodeID, int attitudeID) {
    PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
    Context currentContext = ContextController.getContext(context);
    currentContext.removeHypothesisFromContext(0, attitudeID, node);
    if (!node.isHypInAnyContext(context, attitudeID, 0)) {
      node.removeHypSupport(0, attitudeID);
    }
    System.out.println("node removed " + nodeID);
  }

  // public void contextTrim() {
  // ContextSet contextSet = ContextController.getContextSet();
  // HashMap<String, Context> contexts = contextSet.getSet();
  // Context currentContext = ContextController.getContext(context);
  // HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]>
  // currentContextHypotheses = currentContext
  // .getHypotheses();
  // Set<Integer> levels = currentContextHypotheses.keySet();
  // HashMap<Integer, HashMap<Integer, HashSet<Integer>>> result = new
  // HashMap<>();
  // for (Integer level : levels) {
  // for (int attitudeID = 0; attitudeID <
  // (currentContextHypotheses.get(level)).length; attitudeID++) {
  // int[] originHypothesis =
  // currentContextHypotheses.get(level)[attitudeID].getFirst().getProps();
  // for (int j = 0; j < originHypothesis.length; j++) {
  // boolean hasDependents = hasDependents(context, attitudeID, level,
  // originHypothesis[j]);
  // if (!hasDependents) {
  // if (result.get(level) != null) {
  // if (result.get(level).get(attitudeID) != null) {
  // result.get(level).get(attitudeID).add(originHypothesis[j]);
  // } else {
  // HashSet<Integer> nodesToBeRemoved = new HashSet<>();
  // nodesToBeRemoved.add(originHypothesis[j]);
  // result.get(level).put(attitudeID, nodesToBeRemoved);
  // }
  // } else {
  // HashSet<Integer> nodesToBeRemoved = new HashSet<>();
  // nodesToBeRemoved.add(originHypothesis[j]);
  // HashMap<Integer, HashSet<Integer>> nodesToBeRemovedAtittudes = new
  // HashMap<>();
  // nodesToBeRemovedAtittudes.put(attitudeID, nodesToBeRemoved);
  // result.put(level, nodesToBeRemovedAtittudes);
  // }
  // }
  // }
  // }
  // }
  // for (int level : result.keySet()) {
  // for (int attitudeID : result.get(level).keySet()) {
  // for (int nodeID : result.get(level).get(attitudeID)) {
  // PropositionNode nodeToBeRemoved = (PropositionNode)
  // Network.getNodeById(nodeID);
  // currentContext.removeHypothesisFromContext(level,
  // attitudeID, nodeToBeRemoved);
  // boolean isHypothesisInOtherContext = false;
  // for (Context context : contexts.values()) {
  // isHypothesisInOtherContext = context.isHypothesis(level, attitudeID,
  // nodeToBeRemoved);
  // if (isHypothesisInOtherContext) {
  // break;
  // }
  // }
  // if (!isHypothesisInOtherContext) {
  // HashMap<Integer, HashSet<Integer>> isHyp =
  // nodeToBeRemoved.getSupport().getIsHyp();
  // isHyp.get(level).remove(attitudeID);
  // if (isHyp.get(level).isEmpty()) {
  // isHyp.remove(level);
  // if (isHyp.isEmpty()) {
  // // remove the node from the network
  // }
  // }
  // }
  // }
  // }
  // }
  // }

  // public static boolean hasDependents(String context, int
  // SupportingNodeAttitude, int level, int nodeID) {
  // PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
  // int[] assumptionDependentNodes =
  // node.getAssumptionSupportDependents().getProps();
  // Collection<Integer> attitudes =
  // ContextController.getAttitudes().getSet().values();
  // for (int i = 0; i < assumptionDependentNodes.length; i++) {
  // PropositionNode dependentNode = (PropositionNode)
  // Network.getNodeById(assumptionDependentNodes[i]);
  // for (Integer attitude : attitudes) {
  // boolean isSupported = dependentNode.supported(context, attitude, level);
  // if (isSupported) {
  // ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,
  // PropositionNodeSet>>, PropositionNodeSet>> supports = dependentNode
  // .getSupport().getAssumptionBasedSupport().get(level).get(attitude);
  // for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>,
  // PropositionNodeSet> support : supports) {
  // PropositionNodeSet originSet =
  // support.getFirst().get(SupportingNodeAttitude).getFirst();
  // boolean hasDependents = originSet.contains(nodeID);
  // if (hasDependents)
  // return true;
  // }
  // }

  // }
  // }
  // int[] justificationDependentNodes =
  // node.getAssumptionSupportDependents().getProps();
  // for (int i = 0; i < justificationDependentNodes.length; i++) {
  // PropositionNode dependentNode = (PropositionNode)
  // Network.getNodeById(justificationDependentNodes[i]);
  // for (Integer attitude : attitudes) {
  // boolean isSupported = dependentNode.supported(context, attitude, level);
  // if (isSupported) {
  // ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,
  // PropositionNodeSet>>, PropositionNodeSet>> supports = dependentNode
  // .getSupport().getAssumptionBasedSupport().get(level).get(attitude);
  // for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>,
  // PropositionNodeSet> support : supports) {
  // PropositionNodeSet originSet =
  // support.getFirst().get(SupportingNodeAttitude).getFirst();
  // boolean hasDependents = originSet.contains(nodeID);
  // if (hasDependents)
  // return true;
  // }
  // }

  // }
  // }
  // return false;
  // }

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

    Trim trim = new Trim("guc");
    trim.contextTrim();
    System.out.print(ContextController.getContext("guc").toString());

  }

}