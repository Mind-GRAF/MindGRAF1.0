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
import edu.guc.mind_graf.support.Support;

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
