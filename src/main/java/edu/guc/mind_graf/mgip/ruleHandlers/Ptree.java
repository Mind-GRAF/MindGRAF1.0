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

public class Ptree extends RuleInfoHandler {

    private final HashMap <Integer, PtreeNode> varSetLeafMap;
    private int minPcount; // minimum number of positive RIs needed to be sent
    private int minNcount; // minimum number of negative RIs needed to be sent
    // either minPcount or minNcount needs to be satisfied for propagation to start
    private int pcount = 0; // number of antecedents that reported positively
    private int ncount = 0; // number of antecedents that reported negatively
    private boolean isPropagating = false;
    private HashMap <Integer, int[]> antecedentRIcount = new HashMap<>(); // keeps track of how many positive/negative report was received from each antecedent
    ArrayDeque <PtreeNode> roots;

    public HashMap<Integer, PtreeNode> getVarSetLeafMap() {
        return varSetLeafMap;
    }

    public Ptree (int minPcount, int minNcount){ // should edit: shouldn't have constructors I don't need/want
        varSetLeafMap = new HashMap<>();
        this.minPcount = minPcount;
        this.minNcount = minNcount;
    }

    public static Ptree constructPtree(PropositionNodeSet antecedents, int minPcount, int minNcount, int ptreeNodeMin){
        Ptree ptree = new Ptree(minPcount, minNcount);
        HashMap <Node, HashSet<PtreeNode>> vpList = ptree.processAntecedents(antecedents, ptreeNodeMin);
        ArrayDeque <PtreeNode> pSequence = ptree.processVariables(vpList);
        ptree.buildPtree(pSequence, ptreeNodeMin);
        ptree.roots = pSequence;
        return ptree;
    }

    private HashMap <Node, HashSet<PtreeNode>> processAntecedents(PropositionNodeSet antecedents, int ptreeNodeMin){
        HashMap <Node, HashSet<PtreeNode>> vpList = new HashMap<>();
        for(PropositionNode ant : antecedents){
            antecedentRIcount.put(ant.getId(), new int[]{0, 0});
            NodeSet vars = ant.getFreeVariables();
            // insert in varSetLeafMap
            int hash = ant.getFreeVariablesHash();
            if(!varSetLeafMap.containsKey(hash)) {
                Singleton sIndex = new Singleton(ant.getFreeVariables());
                varSetLeafMap.put(hash, new PtreeNode(null, null, null, null, sIndex, vars, null));
            }
            // insert in vpList
            for(Node n : vars){
                HashSet<PtreeNode> ps = vpList.getOrDefault(n, new HashSet<>());
                ps.add(varSetLeafMap.get(hash));
                vpList.put(n, ps);
            }
            // setting the min of each leaf
            if(ptreeNodeMin < 2)
                varSetLeafMap.get(hash).setMin(ptreeNodeMin);
            else
                varSetLeafMap.get(hash).setMin(varSetLeafMap.get(hash).getMin() + 1);
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

    private void buildPtree(ArrayDeque <PtreeNode> pSequence, int ptreeNodeMin){
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
                if(ptreeNodeMin < 2) // it's not andentail, 2 is for andentail
                    parent.setMin(ptreeNodeMin);
                else
                    parent.setMin(p1.getMin() + p2.getMin());
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
        SIndex parentSIndex = new Linear(intersection);
        NodeSet vars = p1.getVars().union(p2.getVars());
        PtreeNode parent = new PtreeNode(null, null, p1, p2, parentSIndex, vars, null);
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

            int[] count = antecedentRIcount.get(n.getId());  // till the end of the else is really unnecessary after start of propagation but code was moved up bcz it's nice info to keep track of. could move down for after return since it's usually unnecessary info later
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

            int hash = n.getFreeVariablesHash();
            RuleInfoSet mayInfer = varSetLeafMap.get(hash).insertIntoNode(ri, isPropagating);
            if(mayInfer != null && mayInfer.size() > 0){
                return mayInfer;
            }

            if((pcount >= minPcount || ncount >= minNcount) && !isPropagating){
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
                PtreeNode parent = p.getParent();
                if(parent != null){
                    visited.add(p.getSibling());
                    ris = ris.combineAdd(p.getSibling().getSIndex().getAllRuleInfos());
                    parent.getSIndex().insertVariableRI(ris);
                    queue.addLast(parent);
                }
                else{
                    rootRuleInfos = rootRuleInfos.addAll(p.getSIndex().getAllRuleInfos());
                }
            }
        }
        return rootRuleInfos;
    }

    public String BFString(){
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
                "ptreeNodes=" + BFString() +
                ", minPcount=" + minPcount +
                ", minNcount=" + minNcount +
                '}';
    }

    public ArrayList<PtreeNode> arrayOfNodes(){  //for testing
        ArrayList<PtreeNode> arr = new ArrayList<>();
        ArrayDeque <PtreeNode> queue = new ArrayDeque<>();
        queue.addAll(varSetLeafMap.values());
        while(!queue.isEmpty()){
            PtreeNode p = queue.pollFirst();
            arr.add(p);
            if(p.getParent() != null && !queue.contains(p.getParent())){
                queue.addLast(p.getParent());
            }
        }
        return arr;
    }

    public ArrayDeque<PtreeNode> getRoots() {
        return roots;
    }

    public void setRoots(ArrayDeque<PtreeNode> roots) {
        this.roots = roots;
    }
}
