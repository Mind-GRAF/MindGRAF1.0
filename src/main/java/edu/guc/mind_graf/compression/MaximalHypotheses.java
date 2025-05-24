package edu.guc.mind_graf.compression;

import java.util.HashSet;


public class MaximalHypotheses {
    public static HashSet<Integer> computeMaximalHypotheses(BipartiteGraph g){
          HashSet<Integer> max = new HashSet<>();
        for (int i = 0; i < g.sizeV1; i++) {
            if (!g.removed[i] && g.indeg(i) == 0) {
                max.add(i);
            }
        }
        return max;
    }

}
