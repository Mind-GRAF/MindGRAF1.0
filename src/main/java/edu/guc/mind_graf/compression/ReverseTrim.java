package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.CannotRemoveNodeException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.exceptions.NodeNotInNetworkException;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

public class ReverseTrim {
  public static void reverseTrim(BipartiteGraph g) {
    g.reverseGraph();
    Trim.trim(g);
    g.reverseGraph();
}
   

}