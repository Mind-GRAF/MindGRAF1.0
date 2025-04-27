package edu.guc.mind_graf.compression;
import com.google.common.collect.HashMultiset;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class Graph { // undirected
    public int size;
    public boolean[] removed;
    public HashMultiset<Integer>[] adj;

    public void addEdge(int u, int v) {
        if (adj[u].count(v) >= 2)
            return;
        adj[u].add(v);
        adj[v].add(u);
    }

    public void removeEdge(int u, int v) {
        adj[u].remove(v);
        adj[v].remove(u);
    }

    public void removeNode(int u) {
        removed[u] = true;
        for (int x : adj[u]) {
            adj[x].remove(u);
        }
        adj[u].clear();
    }

    public Graph(int size, boolean[] removed, HashMultiset<Integer>[] adj) {
        this.size = size;
        this.removed = removed;
        this.adj = adj;
    }

    public Graph(int size) {
        this.size = size;
        removed = new boolean[size];
        adj = new HashMultiset[size];
        for (int i = 0; i < size; i++) {
            adj[i] = HashMultiset.create();
        }
    }

    public Graph(BipartiteGraph g) {
        this(g.sizeV1);
        for (int i = g.sizeV1; i < g.sizeV1 + g.sizeV2; i++) {
            for (int u : g.adjRev[i]) {
                for (int v : g.adj[i]) {
                    if (u != v) {
                        addEdge(u, v);
                    }
                }
            }
        }
        for (int i = 0; i < g.sizeV1; i++) {
            if (g.removed[i]) {
                removed[i] = true;
            }
        }
    }
//    public void findCriticalPoints() {
//        // mark components in G - H then check if adding i forms a cycle
//        int[] comp = new int[size];
//        int curComp = 1;
//        for (int i = 0; i < size; i++) {
//            if (inSubGraph[i] || comp[i] != 0)
//                continue;
//            Queue<Integer> q = new LinkedList<>();
//            q.add(i);
//            comp[i] = curComp;
//            while (!q.isEmpty()) {
//                int cur = q.poll();
//                for (int x : adj[cur]) {
//                    if (!inSubGraph[x] && comp[x] == 0) {
//                        comp[x] = curComp;
//                        q.add(x);
//                    }
//                }
//            }
//            curComp++;
//        }
//        for (int i = 0; i < size; i++) {
//            if (!inSubGraph[i]) continue;
//            HashSet<Integer> prevComps = new HashSet<>();
//            inResult[i] = true;
//            for (int x : adj[i]) {
//                if (!inSubGraph[x]) {
//                    inResult[i] &= prevComps.add(comp[x]);
//                }
//            }
//        }
//    }
//
//    public void getBranchPoints() {
//        for (int i = 0; i < size; i++) {
//            if (inSubGraph[i] && edgesInSubGraph[i] == 3) {
//                inResult[i] = true;
//            }
//        }
//    }


    @Override
    public String toString() {
        return "Graph{" +
                "size=" + size +
                ", removed=" + Arrays.toString(removed) +
                ", adj=" + Arrays.toString(adj) +
                '}';
    }

    public Graph copy() {
        HashMultiset<Integer>[] x = new HashMultiset[size];
        for (int i = 0; i < x.length; i++) {
            x[i] = HashMultiset.create(adj[i]);
        }
        return new Graph(size, this.removed.clone(), x);
    }
}
