package edu.guc.mind_graf.compression;
import java.util.*;

public class FourAlgorithm {
    static HashSet<Integer> vis;
    static LinkedList<Integer> path = new LinkedList<>();

    static int pop(HashSet<Integer> hs) {
        int res = -1;
        for (int x : hs) {
            res = x;
            break;
        }
        hs.remove(res);
        return res;
    }

    static int[] depth;

    public static boolean findCycle(Graph g, int u, int p, int root) {
        depth[root] = 0;
        vis.add(u);
        path.addLast(u);
        int myX = -1;
        for (int x : g.adj[u]) {
            if (vis.contains(x) && p != x) {
                if (myX == -1 || depth[myX] < depth[x]) {
                    myX = x;
                }
            }
        }
        if (myX != -1) {
            while (path.peekFirst() != myX)
                path.pollFirst();
            return true;
        }
        for (int x : g.adj[u]) {
            if (!vis.contains(x)) {
                depth[x] = 1 + depth[u];
                if (findCycle(g, x, u, root))
                    return true;
            }
        }

        path.removeLast();
        return false;
    }

    static HashSet<Integer> pathHS;

    public static boolean findPathToAdd(Graph g, Graph h, int u, int p, int root) {
        vis.add(u);
        path.addLast(u);
        pathHS.add(u);
        int cntBack = 0;
        for (int x : g.adj[u]) {
            if (u != root && g.adj[root].contains(x))
                continue;
            if (x != p && (pathHS.contains(x) || !h.removed[x])) {
                cntBack++;
            }
        }
        if (cntBack > 2) {
            pathHS.remove(u);
            path.removeLast();
            return false;
        }
        if (cntBack == 0) {
            for (int x : g.adj[u]) {
                if (u != root && g.adj[root].contains(x))
                    continue;
                if (!vis.contains(x)) {
                    if (findPathToAdd(g, h, x, u, root)) {
                        g.removeEdge(x, u);
                        h.addEdge(x, u);
                        h.removed[u] = false;
                        pathHS.remove(u);
                        return true;
                    }
                }
            }
        } else {

            ArrayList<Integer> backEdges = new ArrayList<>();
            for (int x : g.adj[u]) {
                if (u != root && g.adj[root].contains(x))
                    continue;
                if (x != p && (pathHS.contains(x) || !h.removed[x])) {
                    backEdges.add(x);
                }
            }
            for (int x : backEdges) {
                g.removeEdge(x, u);
                h.addEdge(x, u);
                path.add(x);
            }
            h.removed[u] = false;
            pathHS.remove(u);
            return true;
        }
        pathHS.remove(u);
        path.removeLast();
        return false;
    }

    static void removeLeaves(Graph g, Queue<Integer> leaves) {
        while (!leaves.isEmpty()) {
            int cur = leaves.poll();
            g.removed[cur] = true;
            for (int x : g.adj[cur]) {
                g.adj[x].remove(cur);
                if (g.adj[x].size() == 1) {
                    leaves.add(x);
                }
            }
            g.adj[cur].clear();
        }
    }

    public static Graph SubGraph23(Graph g) {

        depth = new int[g.size];
        Graph h = new Graph(g.size);

        vis = new HashSet<>();
        Arrays.fill(h.removed, true);
        Queue<Integer> leaves = new LinkedList<>();
        for (int i = 0; i < g.size; i++) {
            if (g.adj[i].size() == 1) {
                leaves.add(i);
            }
        }
        for (int i = 0; i < g.size; ) {
            removeLeaves(g, leaves);
            if (g.removed[i]) {
                i++;
                continue;
            }
            ArrayList<Integer> addedToH = new ArrayList<>();
            Queue<Integer> r = new LinkedList<>();
            path = new LinkedList<>();
            vis = new HashSet<>();
            findCycle(g, i, -1, i);
//            System.out.println(path + " Cycle");
//            System.out.println(g);
            if (path.isEmpty()) {
                i++;
                continue;
            }
            for (int j = 0; j < path.size(); j++) {
                addedToH.add(path.get(j));
                r.add(path.get(j));
                h.removed[path.get(j)] = false;
                h.addEdge(path.get(j), path.get((j + 1) % path.size()));
                g.removeEdge(path.get(j), path.get((j + 1) % path.size()));
            }
            ArrayList<Integer> cc = new ArrayList<>(path);
            while (!r.isEmpty()) {
                int u = r.poll();
                if (h.adj[u].size() != 2) {
                    continue;
                }
                Queue<Integer> deg3 = new LinkedList<>();
                path = new LinkedList<>();
                vis = new HashSet<>();
                pathHS = new HashSet<>();
                findPathToAdd(g, h, u, -1, u);
                for (int x : path) {
                    if (h.adj[x].size() == 3) {
                        deg3.add(x);
                    } else if (h.adj[x].size() == 2) {
                        r.add(x);
                    }
                }

                ArrayList<Integer> toBeRemoved = new ArrayList<>();
//                toBeRemoved.addAll(withEdgesInH[4]);
//                withEdgesInH[4].clear();
                for (int x : deg3) {
                    for (int y : g.adj[x]) {
                        toBeRemoved.add(y);
                    }
                }
                for (int x : toBeRemoved) {
                    for (int y : g.adj[x]) {
                        if (g.adj[y].size() == 2) {
                            leaves.add(y);
                        }
                    }
                    g.removeNode(x);
                }
            }
            for (int x : addedToH) {
                for (int y : g.adj[x]) {
                    if (g.adj[y].size() == 2) {
                        leaves.add(y);
                    }
                }
                g.removeNode(x);
            }
        }
        return h;
    }

    public static boolean[] findCriticalPoints(Graph g, Graph h) {
        boolean[] isCriticalPoint = new boolean[h.size];
        // mark components in G - H then check if adding i forms a cycle
        int[] comp = new int[g.size];
        int curComp = 1;
        for (int i = 0; i < g.size; i++) {
            if (!h.removed[i] || comp[i] != 0)
                continue;
            Queue<Integer> q = new LinkedList<>();
            q.add(i);
            comp[i] = curComp;
            while (!q.isEmpty()) {
                int cur = q.poll();
                for (int x : g.adj[cur]) {
                    if (h.removed[x] && comp[x] == 0) {
                        comp[x] = curComp;
                        q.add(x);
                    }
                }
            }
            curComp++;
        }
        for (int i = 0; i < g.size; i++) {
            if (h.removed[i]) continue;
            HashSet<Integer> prevComps = new HashSet<>();
            isCriticalPoint[i] = false;
            for (int x : g.adj[i]) {
                if (h.removed[x]) {
                    isCriticalPoint[i] |= prevComps.add(comp[x]);
                }
            }
        }
        return isCriticalPoint;
    }

    public static ArrayList<Integer> findIsolatedCycles(Graph h, boolean[] isCriticalPoint) {
        ArrayList<Integer> result = new ArrayList<>();
        boolean[] visited = new boolean[h.size];
        for (int i = 0; i < h.size; i++) {
            if (h.removed[i] || visited[i])
                continue;
            Queue<Integer> q = new LinkedList<>();
            q.add(i);
            visited[i] = true;
            int maxDegree = 0;
            boolean criticalPointFound = false;
            while (!q.isEmpty()) {
                int cur = q.poll();
                maxDegree = Math.max(maxDegree, h.adj[cur].size());
                criticalPointFound |= isCriticalPoint[cur];
                for (int x : h.adj[cur]) {
                    if (!visited[x]) {
                        visited[x] = true;
                        q.add(x);
                    }
                }
            }
            if (maxDegree == 2 && !criticalPointFound) {
                result.add(i);
            }
        }
        return result;
    }

    public static HashSet<Integer> findFVS(Graph g) {
        int numberOfNodes = 0;
        for (int i = 0; i < g.size; i++) {
            if (!g.removed[i]) {
                numberOfNodes++;
            }
        }
        HashSet<Integer> f = new HashSet<>();

        for (int i = 0; i < g.size; i++) {
            int other = -1;
            int myDeg = 0;
            for (int x : g.adj[i]) {
                if (g.adj[i].count(x) == 2) {
                    other = x;
                    myDeg++;
                }
            }
            if (other != -1) {
                int otherDeg = 0;
                for (int x : g.adj[other]) {
                    if (g.adj[other].count(x) == 2) {
                        otherDeg++;
                    }
                }
                if (myDeg > 1) {
                    f.add(i);
                    g.removeNode(i);
                }
                if (otherDeg > 1) {
                    f.add(other);
                    g.removeNode(other);
                }
                if (otherDeg == 1 && myDeg == 1) {
                    f.add(i);
                    g.removeNode(i);
                    f.add(other);
                    g.removeNode(other);
                }
            }
        }

        Graph h = SubGraph23(g.copy());
        boolean[] isCriticalPoint = findCriticalPoints(g, h);
        for (int i = 0; i < h.size; i++) {
            if (h.adj[i].size() == 3 || isCriticalPoint[i]) {
                f.add(i);
            }
        }
        f.addAll(findIsolatedCycles(h, isCriticalPoint));

        if (f.size() == numberOfNodes) {
            pop(f);
        }
        return f;
    }

    public static void main(String[] args) {
        Graph g = new Graph(6);
        g.addEdge(0, 1);
        g.addEdge(1, 2);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 4);
        g.addEdge(4, 2);
        g.addEdge(5, 1);
        Graph h = SubGraph23(g);
        System.out.println(g);
        System.out.println(h);
    }
}
