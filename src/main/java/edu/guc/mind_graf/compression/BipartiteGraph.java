package edu.guc.mind_graf.compression;

import java.util.Arrays;
import java.util.HashSet;

public class BipartiteGraph {
    public int sizeV1, sizeV2;
    public boolean[] removed;
    public HashSet<Integer>[] adj, adjRev;

    public void addEdge(int u, int v) {
        adj[u].add(v);
        adjRev[v].add(u);
    }

    public BipartiteGraph(int sizeV1, int sizeV2, boolean[] removed, HashSet<Integer>[] adj, HashSet<Integer>[] adjRev) {
        this.sizeV1 = sizeV1;
        this.sizeV2 = sizeV2;
        this.removed = removed;
        this.adj = adj;
        this.adjRev = adjRev;
    }
    public BipartiteGraph(int sizeV1, int sizeV2, HashSet<Integer>[] adj, HashSet<Integer>[] adjRev) {
        this.sizeV1 = sizeV1;
        this.sizeV2 = sizeV2;
        this.adj = adj;
        this.adjRev = adjRev;
        for (int i = sizeV1; i < sizeV1 + sizeV2; i++) {
            if (indeg(i) == 0 || outdeg(i) == 0) {
                removeNode(i);
            }
        }
    }

    public BipartiteGraph(int sizeV1, int sizeV2, double edgeDensity) {
        this(sizeV1, sizeV2);
        for (int i = 0; i < sizeV1; i++) {
            for (int j = sizeV1; j < sizeV1 + sizeV2; j++) {
                if (Math.random() < edgeDensity) {
                    if (Math.random() < 0.5) {
                        addEdge(i, j);
                    } else {
                        addEdge(j, i);
                    }
                }
            }
        }
        for (int i = sizeV1; i < sizeV1 + sizeV2; i++) {
            if (indeg(i) == 0 || outdeg(i) == 0) {
                removeNode(i);
            }
        }
    }

    public BipartiteGraph(int sizeV1, int sizeV2) { // create empty graph
        this.sizeV1 = sizeV1;
        this.sizeV2 = sizeV2;
        removed = new boolean[sizeV1 + sizeV2];
        adj = new HashSet[sizeV1 + sizeV2];
        adjRev = new HashSet[sizeV1 + sizeV2];
        for (int i = 0; i < sizeV1 + sizeV2; i++) {
            adj[i] = new HashSet<>();
            adjRev[i] = new HashSet<>();
        }
    }

    public int indeg(int u) { // deg-
        return adjRev[u].size();
    }

    public int outdeg(int u) { // deg+
        return adj[u].size();
    }

    public void reverseGraph() {
        HashSet<Integer>[] temp = adj;
        adj = adjRev;
        adjRev = temp;
    }

    public void removeNode(int v) {
        removed[v] = true;
        for (int x : adj[v]) {
            adjRev[x].remove(v);
        }
        for (int x : adjRev[v]) {
            adj[x].remove(v);
        }
        adj[v].clear();
        adjRev[v].clear();
    }

    @Override
    public String toString() {
        return "BipartiteGraph{" +
                "sizeV1=" + sizeV1 +
                ", sizeV2=" + sizeV2 +
                ", removed=" + Arrays.toString(removed) +
                "\n, adj=" + Arrays.toString(adj) +
                "\n, adjRev=" + Arrays.toString(adjRev) +
                '}';
    }

    public BipartiteGraph copy() {
        HashSet<Integer>[] adjclone = new HashSet[adj.length];
        for (int i = 0; i < adjclone.length; i++) {
            adjclone[i] = new HashSet<>(adj[i]);
        }
        HashSet<Integer>[] adjRevclone = new HashSet[adjRev.length];
        for (int i = 0; i < adjRevclone.length; i++) {
            adjRevclone[i] = new HashSet<>(adjRev[i]);
        }
        return new BipartiteGraph(sizeV1, sizeV2, removed.clone(), adjclone, adjRevclone);
    }

    public static void main(String[] args) {
        BipartiteGraph g = new BipartiteGraph(4, 4);
        g.addEdge(0, 7);
        g.addEdge(1, 5);
        g.addEdge(1, 7);
        g.addEdge(2, 4);
        g.addEdge(3, 4);
        g.addEdge(4, 0);
        g.addEdge(4, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(7, 3);
        g.removed[6] = true;
        //        System.out.println(ChordlessCycles.chordlessCycles(g.copy()));
        // System.out.println(new Graph(g.copy()));
        // System.out.println(FourAlgorithm.findFVS(new Graph(g.copy())));
//        System.out.println(FourAlgorithm.SubGraph23(new Graph(g.copy())));
//        System.out.println(Checker.checker(CHBWithFourAprox.CHB(g.copy()), g));
//        System.out.println(CHBWithFourAprox.CHB(g.copy()));
    }
}
