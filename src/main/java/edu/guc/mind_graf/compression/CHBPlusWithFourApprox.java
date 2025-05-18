package edu.guc.mind_graf.compression;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

public class CHBPlusWithFourApprox {
    static int pop(HashSet<Integer> hs) {
        int res = -1;
        for (int x : hs) {
            res = x;
            break;
        }
        hs.remove(res);
        return res;
    }

    public static HashSet<Integer> CHBPlus(BipartiteGraph g) {
        HashSet<Integer> max = MaximalHypotheses.computeMaximalHypotheses(g);
        Trim.trim(g);
        ReverseTrim.reverseTrim(g);
        HashSet<Integer> result = FourAlgorithm.findFVS(new Graph(g));
        return red(g, result, max);
    }

    public static HashSet<Integer> red(BipartiteGraph g, HashSet<Integer> h, HashSet<Integer> b) {
        boolean v1IsEmpty = true;
        for (int n = 0; n < g.sizeV1; n++) {
            v1IsEmpty &= g.removed[n];
        }

        if (v1IsEmpty) {
            return b;
        }
        for (int n = 0; n < g.sizeV1; n++) {
            if (h.contains(n) || g.removed[n]) {
                continue;
            }
            int ns = -1;
            for (int x : g.adjRev[n]) {
                if (ns == -1 || g.indeg(x) < g.indeg(ns)) {
                    ns = x;
                }
            }
            if (ns == -1) {
               continue;
            }
            ArrayList<Integer> toBeRemoved = new ArrayList<>();
            for (int v : g.adj[n]) {
                boolean remove = true;
                for (int u : g.adj[v]) {
                    if (!g.adjRev[ns].contains(u)) {
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    toBeRemoved.add(v);
                } else {
                    for (int x : g.adjRev[ns]) {
                        g.addEdge(x, v);
                    }
                    ArrayList<Integer> inNs = new ArrayList<>();
                    for (int x : g.adj[v]) {
                        if (g.adjRev[ns].contains(x)) {
                            inNs.add(x);
                        }
                    }
                    for (int x : inNs) {
                        g.adj[v].remove(x);
                        g.adjRev[x].remove(v);
                    }
                }
            }
            for (int np : g.adjRev[n]) {
                if (g.adj[np].size() == 1) {
                    toBeRemoved.add(np);
                }
            }
            toBeRemoved.add(n);
            for (int v : toBeRemoved) {
                g.removeNode(v);
            }
        }
        for (int n = 0; n < g.sizeV1; n++) {
            if (g.removed[n])
                continue;
            if (g.indeg(n) == 0) {
                b.add(n);
            }
        }
        Trim.trim(g);
        ReverseTrim.reverseTrim(g);
        h = FourAlgorithm.findFVS(new Graph(g));
//        System.out.println(h);
//        System.out.println(g);
        return red(g, h, b);
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
        // ContextController.createNewContext("context2");
        // ContextController.createNewContext("context3");
        ContextController.setCurrContext("context1");
        Context context1 = ContextController.getContext("context1");
        // Context context2 = ContextController.getContext("context2");
        // Context context3 = ContextController.getContext("context3");

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
        context1.addHypothesisToContext(0, 2, node4);
        // context1 level 1 hyps
        Network.currentLevel = 1;
        context1.addHypothesisToContext(1, 0, node1);
        context1.addHypothesisToContext(1, 1, node2);
        context1.addHypothesisToContext(1, 2, node3);
        context1.addHypothesisToContext(1, 2, node4);
        // // context1 level 2 hyps
        // Network.currentLevel = 2;
        // context1.addHypothesisToContext(2, 0, node1);
        // context1.addHypothesisToContext(2, 1, node2);
        // context1.addHypothesisToContext(2, 2, node3);
        // context2

        // context2 level0 hyps
        // Network.currentLevel = 0;
        // context2.addHypothesisToContext(0, 0, node1);
        // context2.addHypothesisToContext(0, 1, node2);
        // context2.addHypothesisToContext(0, 2, node4);

        // context2 level1 hyps
        // Network.currentLevel = 1;
        // context2.addHypothesisToContext(1, 0, node1);
        // context2.addHypothesisToContext(1, 1, node2);
        // context2.addHypothesisToContext(1, 2, node4);

        // context3
        // context3 level0 hyps
        // Network.currentLevel = 0;
        // context3.addHypothesisToContext(0, 0, node6);
        // context3.addHypothesisToContext(0, 1, node7);
        // context3.addHypothesisToContext(0, 2, node8);

        // context3 level1 hyps
        // Network.currentLevel = 1;
        // context3.addHypothesisToContext(1, 0, node6);
        // context3.addHypothesisToContext(1, 1, node7);
        // context3.addHypothesisToContext(1, 2, node8);

        System.out.print(ContextController.getContext("context1").toString());
        // System.out.print(ContextController.getContext("context2").toString());
        // System.out.print(ContextController.getContext("context3").toString());

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support;
       // node 1 support
        support = new HashMap<>();
        support.put(2, new Pair<>(new PropositionNodeSet(4), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        support.clear();
        support.put(2, new Pair<>(new PropositionNodeSet(3), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        node1.addJustificationBasedSupports(0,Network.currentLevel, new ArrayList<>(supports));

        // node2 support
        support = new HashMap<>();
        support.put(0, new Pair<>(new PropositionNodeSet(1), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        Network.currentLevel = 0;
        node2.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node2.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        supports.clear();

      //node 3 support
        support = new HashMap<>();
        support.put(1, new Pair<>(new PropositionNodeSet(2), new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet()));
        Network.currentLevel = 0;
        node3.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        Network.currentLevel = 1;
        node3.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));
        supports.clear();

      
        KZeroCompression kzero = new KZeroCompression();
        kzero.kZeroCompress();
        System.out.print(ContextController.getContext("context1").toString());
        BaseSupportGraph graph = new BaseSupportGraph("context1") ;
        BipartiteGraph g = graph.getGraph();
        System.out.println(" the resulted graph"+ g);
        HashSet<Integer> compressionResult = CHBPlus(g);
        System.out.println(" the surviving nodes"+ compressionResult);
        // graph.builder.printHypothesesNodes();
        // graph.builder.printSupportNodes();
        // System.out.print(ContextController.getContext("context2").toString());
        // System.out.print(ContextController.getContext("context3").toString());
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
