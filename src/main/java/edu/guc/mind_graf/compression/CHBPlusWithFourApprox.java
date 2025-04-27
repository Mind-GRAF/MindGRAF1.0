package edu.guc.mind_graf.compression;
import java.util.ArrayList;
import java.util.HashSet;

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
        HashSet<Integer> max = new HashSet<>();
        for (int i = 0; i < g.sizeV1; i++) {
            if (!g.removed[i] && g.indeg(i) == 0) {
                max.add(i);
            }
        }
        // Trim.trim(g);
        // Trim.reverseTrim(g);
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
        // Trim.trim(g);
        // Trim.reverseTrim(g);
        h = FourAlgorithm.findFVS(new Graph(g));
//        System.out.println(h);
//        System.out.println(g);
        return red(g, h, b);
    }

    public static void main(String[] args) {
        BipartiteGraph g = new BipartiteGraph(5, 5);
        g.addEdge(0, 5);
        g.addEdge(0, 7);
        g.addEdge(0, 9);
        g.addEdge(1, 5);
        g.addEdge(1, 8);
        g.addEdge(1, 6);
        g.addEdge(2, 7);
        g.addEdge(2, 8);
        g.addEdge(3, 6);
        g.addEdge(4, 7);
        g.addEdge(4, 9);
        g.addEdge(5, 2);
        g.addEdge(6, 4);
        g.addEdge(7, 1);
        g.addEdge(8, 4);
        g.addEdge(9, 3);
        HashSet<Integer> x = CHBPlus(g);
        System.out.println(x);
    }
}
