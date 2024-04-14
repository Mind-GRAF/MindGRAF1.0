package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.*;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

class PtreeNode {

    private PtreeNode parent; // important for upward traversal (propagation of RIs)
    private PtreeNode sibling; // important for combining RIs
    private PtreeNode leftChild;
    private PtreeNode rightChild;
    private SIndex sIndex; // linear or singleton based on siblingIntersection
    //private Set<Integer> pats; // antecedents node stores info abt
    private NodeSet vars; // free vars in propositions node represents
    private FreeVariableSet siblingIntersection; // shared vars between sibling and this node  (parent.getCommonVariables)
    private final int min = 0; // should initialize and consider moving

    public PtreeNode(PtreeNode parent, PtreeNode sibling, PtreeNode leftChild, PtreeNode rightChild, SIndex sIndex,
                     NodeSet vars, FreeVariableSet siblingIntersection) {
        this.parent = parent;
        this.sibling = sibling;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.sIndex = sIndex;
        this.vars = new NodeSet();
        for(Node var : vars){   // shalllow cloning so that changing in set would destroy nothing
            this.vars.add(var);
        }
        vars.setIsFinal(true);
        this.siblingIntersection = siblingIntersection;
    }

    public PtreeNode getParent() {
        return parent;
    }

    public void setParent(PtreeNode parent) {
        this.parent = parent;
    }

    public PtreeNode getSibling() {
        return sibling;
    }

    public void setSibling(PtreeNode sibling) {
        this.sibling = sibling;
    }

    public PtreeNode getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(PtreeNode leftChild) {
        this.leftChild = leftChild;
    }

    public PtreeNode getRightChild() {
        return rightChild;
    }

    public void setRightChild(PtreeNode rightChild) {
        this.rightChild = rightChild;
    }

    public SIndex getSIndex() {
        return sIndex;
    }

    public void setSIndex(SIndex sIndex) {
        this.sIndex = sIndex;
    }

    public NodeSet getVars() {
        return vars;
    }

    public void setVars(NodeSet vars) {
        this.vars = vars;
    }

    public FreeVariableSet getSiblingIntersection() {
        return siblingIntersection;
    }

    public void setSiblingIntersection(FreeVariableSet siblingIntersection) {
        this.siblingIntersection = siblingIntersection;
    }

    public RuleInfoSet insertIntoNode(RuleInfo ri, boolean isPropagating) throws InvalidRuleInfoException {
        RuleInfoSet newRuleInfoSet = sIndex.insertVariableRI(ri);
        RuleInfoSet result = new RuleInfoSet();
        for(RuleInfo newRuleInfo : newRuleInfoSet){
            if(newRuleInfo.getPcount() >= min && isPropagating){
                if(parent != null) {
                    parent.insertIntoNode(newRuleInfo, true);
                }
                else{
                    result.addRuleInfo(newRuleInfo);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "PtreeNode{" +
                "has parent=" + (parent != null) +
                ",has sibling=" + (sibling != null) +
//                ", leftChild=" + leftChild +
//                ", rightChild=" + rightChild +
                ", sIndex=" + sIndex +
                ", vars=" + vars +
                ", siblingIntersection=" + siblingIntersection +
                ", min=" + min +
                '}';
    }
}

public class Ptree extends RuleInfoHandler {

    private final HashMap <Integer, PtreeNode> antLeafMap; // depricated
    private final HashMap <Integer, PtreeNode> varSetLeafMap;
    private int minPcount; // minimum number of positive RIs needed to be sent
    private int minNcount; // minimum number of negative RIs needed to be sent
    // either minPcount or minNcount needs to be satisfied for propagation to start
    private int pcount = 0;
    private int ncount = 0;
    private boolean isPropagating = false;
    private HashMap <Integer, int[]> antecedentRIcount = new HashMap<>();

    public Ptree(){
        antLeafMap = new HashMap<>();
        varSetLeafMap = new HashMap<>();
    }

    public HashMap<Integer, PtreeNode> getVarSetLeafMap() {
        return varSetLeafMap;
    }

    public Ptree (PropositionNodeSet antecedents, int minPcount, int minNcount){
        this();
        this.minPcount = minPcount;
        this.minNcount = minNcount;
        constructPtree(antecedents, minPcount, minNcount);
    }

    public static Ptree constructPtree(PropositionNodeSet antecedents, int minPcount, int minNcount){
        Ptree ptree = new Ptree();
        ptree.minPcount = minPcount;
        ptree.minNcount = minNcount;
        HashMap <Node, HashSet<PtreeNode>> vpList = ptree.processAntecedents(antecedents);
        ArrayDeque <PtreeNode> pSequence = ptree.processVariables(vpList);
        ptree.buildPtree(pSequence);
        return ptree;
    }

    private HashMap <Node, HashSet<PtreeNode>> processAntecedents(PropositionNodeSet antecedents){
        HashMap <Node, HashSet<PtreeNode>> vpList = new HashMap<>();
        for(PropositionNode ant : antecedents){
            antecedentRIcount.put(ant.getId(), new int[]{0, 0});
            NodeSet vars = ant.getFreeVariables();
            if(vars == null){
                vars = ant.fetchFreeVariables();
            }
            // insert in varSetLeafMap
            int hash = ant.getFreeVariablesHash();
            if(!varSetLeafMap.containsKey(hash)) {
                Singleton sIndex = new Singleton();
                varSetLeafMap.put(hash, new PtreeNode(null, null, null, null, sIndex, vars, null));
            }
            // insert in vpList
            for(Node n : vars){
                HashSet<PtreeNode> ps = vpList.getOrDefault(n, new HashSet<>());
                ps.add(varSetLeafMap.get(hash));
                vpList.put(n, ps);
            }
        }
        return vpList;
    }

    private ArrayDeque <PtreeNode> processVariables(HashMap <Node, HashSet<PtreeNode>> vpList){
        HashSet <PtreeNode> processed = new HashSet<>();
        ArrayDeque <PtreeNode> pSequence = new ArrayDeque<>(); // arraydeque cz that's the queue i could think of but any queue should work
        for(Node var : vpList.keySet()){ // process each variable
            HashSet<PtreeNode> ps = vpList.get(var);
            for(PtreeNode p : ps){
                if(!processed.contains(p)){
                    pSequence.addLast(p); // insert to end of deque
                    processed.add(p); // mark as processed
                }
            }
        }
        return pSequence;
    }

    private void buildPtree(ArrayDeque <PtreeNode> pSequence){
        PtreeNode firstMismatched = null;
        while(pSequence.size() > 1){ // would stop when only one node is left
            PtreeNode p1 = pSequence.pollFirst();
            PtreeNode p2 = pSequence.peekFirst();
            NodeSet intersection = p1.getVars().intersection(p2.getVars());
            if(intersection.size() == 0){
                pSequence.addLast(p1);
                if(firstMismatched == p1){ // if the first mismatched node is reached again, then the tree is complete, every node left in the pSequence is a disjoint tree
                    break;
                }
                else if(firstMismatched == null){
                    firstMismatched = p1;
                }
            }
            else {
                firstMismatched = null;
                p2 = pSequence.pollFirst();
                PtreeNode parent = matchSiblings(p1, p2, intersection);
                pSequence.add(parent);
            }
        }
    }

    private PtreeNode matchSiblings(PtreeNode p1, PtreeNode p2, NodeSet intersection){
        FreeVariableSet siblingIntersection = new FreeVariableSet();
        for(Node n : intersection){
            siblingIntersection.add(n);
        }
        p1.setSibling(p2);
        p1.setSiblingIntersection(siblingIntersection);
        p2.setSibling(p1);
        p2.setSiblingIntersection(siblingIntersection);
        // setup parent
        SIndex sIndex = new Linear(intersection);
        NodeSet vars = p1.getVars().union(p2.getVars());
        PtreeNode parent = new PtreeNode(null, null, p1, p2, sIndex, vars, null);
        p1.setParent(parent);
        p2.setParent(parent);
        return parent;
    }

    @Override
    public RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        // when inserting a rule info into the tree, it should only have one flag node (that of the antecedent that caused it to be sent)
        if (ri.getFns().size() != 1){
            throw new InvalidRuleInfoException("RuleInfo should only have one flag node when being inserted in tree");
        }
        FlagNode fn = ri.getFns().getFlagNodes().iterator().next();
        Node n = fn.getNode();

        if(antecedentRIcount.containsKey(n.getId())){

            int hash = n.getFreeVariablesHash();
            RuleInfoSet mayInfer = varSetLeafMap.get(hash).insertIntoNode(ri, isPropagating);
            if(mayInfer != null && mayInfer.size() > 0){
                return mayInfer;
            }

            int[] count = antecedentRIcount.get(n.getId());
            if(fn.isFlag()){
                if(count[0] == 0)
                    pcount++;
                count[0]++;
            }
            else{
                if(count[1] == 0)
                    ncount++;
                count[1]++;
            }
            if((count[0] >= minPcount || count[1] >= minNcount) && !isPropagating){
                isPropagating = true;
                return startPropagation();
            }
        }
        else{
            throw new InvalidRuleInfoException("RuleInfo should come from one of the antecedents of the tree");
        }

        return null;
    }

    private RuleInfoSet startPropagation() throws InvalidRuleInfoException {
        HashSet <PtreeNode> visited = new HashSet<>();
        ArrayDeque <PtreeNode> queue = new ArrayDeque<>();
        queue.addAll(varSetLeafMap.values());
        RuleInfoSet rootRuleInfos = new RuleInfoSet();
        while(!queue.isEmpty()){
            PtreeNode p = queue.pollFirst();
            if(!visited.contains(p)){
                visited.add(p);
                RuleInfoSet ris = new RuleInfoSet();
                ris = ris.addAll(p.getSIndex().getAllRuleInfos());
                if(p.getParent() != null){
                    visited.add(p.getSibling());
                    ris = ris.combineAdd(p.getSibling().getSIndex().getAllRuleInfos());
                }
                PtreeNode parent = p.getParent();
                if(parent != null){
                    parent.getSIndex().insertVariableRI(ris);
                    queue.addLast(parent);
                }
                else{
                    rootRuleInfos.addAll(p.getSIndex().getAllRuleInfos());
                }
            }
        }
        return rootRuleInfos;
    }

    public String BFS(){
        StringBuilder sb = new StringBuilder();
        ArrayDeque <PtreeNode> queue = new ArrayDeque<>();
        queue.addAll(varSetLeafMap.values());
        while(!queue.isEmpty()){
            PtreeNode p = queue.pollFirst();
            sb.append(p.toString());
            sb.append("\n");
            if(p.getParent() != null && !queue.contains(p.getParent())){
                queue.addLast(p.getParent());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Ptree{" +
                "ptreeNodes=" + BFS() +
                ", minPcount=" + minPcount +
                ", minNcount=" + minNcount +
                '}';
    }
}
