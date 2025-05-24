package edu.guc.mind_graf.compression;

import java.util.LinkedList;
import java.util.Queue;


public class Trim {

  public static boolean nodeIsBad(BipartiteGraph g, int u) {
    return g.outdeg(u) == 0;
  }

  public static void trim(BipartiteGraph g) {
    Queue<Integer> bad = new LinkedList<>();
    for (int i = 0; i < g.sizeV1 + g.sizeV2; i++) {
      if (!g.removed[i] && nodeIsBad(g, i)) {
        bad.add(i);
        g.removed[i] = true;
      }
    }
    while (!bad.isEmpty()) {
      int u = bad.poll();
      for (int v : g.adjRev[u]) { // v -> u
        g.adj[v].remove(u);
        if (!g.removed[v] && nodeIsBad(g, v)) {
          bad.add(v);
          g.removed[v] = true;
        }
      }
      g.adj[u].clear();
      g.adjRev[u].clear();
    }
  }

}