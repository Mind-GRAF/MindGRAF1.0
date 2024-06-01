package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.*;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class Ptree extends RuleInfoHandler {

    private final HashMap <Integer, PtreeNode> varSetLeafMap;
    private int minPcount;
    private int minNcount;
    // either minPcount or minNcount needs to be satisfied for propagation to start
    private int posAntecedents;
    private int negAntecedents;
    private boolean isPropagating;
    private HashMap <Integer, int[]> antecedentRIcount = new HashMap<>(); // keeps track of how many positive/negative report was received from each antecedent

    public Ptree (int minPcount, int minNcount){ // should edit: shouldn't have constructors I don't need/want
        varSetLeafMap = new HashMap<>();
        this.minPcount = minPcount;
        this.minNcount = minNcount;
    }

    public static Ptree constructPtree(PropositionNodeSet antecedents, int minPcount, int minNcount, int ptreeNodeMin){
        Ptree ptree = new Ptree(minPcount, minNcount);
        if(antecedents.size() == 0){
            System.out.println("There are no open nodes so there is no need for a P-Tree.");
            return ptree;
        }
        System.out.println("Constructing the P-Tree");
        System.out.println("The P-Tree has a minimum of " + minPcount + " positive RIs" + (minNcount < Integer.MAX_VALUE ? (" or " + minNcount + " negative RIs" ) : "")+ " needed for propagation to start.");
        HashMap <Node, HashSet<PtreeNode>> vpList = ptree.processAntecedents(antecedents, ptreeNodeMin);
        System.out.println("The variable-pattern list contructed is:");
        for(Node n : vpList.keySet()){
            System.out.println(n.getName() + " with " + vpList.get(n).size() + " corresponding P-tree nodes.");
        }
        ArrayDeque <PtreeNode> pSequence = ptree.processVariables(vpList);
        System.out.println("The pattern sequence contructed is:");
        for(PtreeNode p : pSequence){
            System.out.println(p);
        }
        System.out.println("The P-Tree will be built using the pattern sequence.");
        ptree.buildPtree(pSequence, ptreeNodeMin);
        return ptree;
    }

    private HashMap <Node, HashSet<PtreeNode>> processAntecedents(PropositionNodeSet antecedents, int ptreeNodeMin){
        System.out.println("Processing the antecedents");
        HashMap <Node, HashSet<PtreeNode>> vpList = new HashMap<>();
        for(PropositionNode ant : antecedents){
            System.out.println("Processing " + ant);
            System.out.println("Its free variables are: " + ant.getFreeVariables());
            antecedentRIcount.put(ant.getId(), new int[]{0, 0});
            NodeSet vars = ant.getFreeVariables();
            // insert in varSetLeafMap
            int hash = ant.getFreeVariablesHash();
            if(!varSetLeafMap.containsKey(hash)) {
                System.out.println("No P-tree node exists for this variable set. A new P-tree node will be created.");
                Singleton sIndex = new Singleton(ant.getFreeVariables());
                varSetLeafMap.put(hash, new PtreeNode(null, null, sIndex, vars, null));
            }
            else{
                System.out.println("A P-tree node already exists for this variable set. The node will be updated.");
            }
            // insert in vpList
            for(Node n : vars){
                HashSet<PtreeNode> ps = vpList.getOrDefault(n, new HashSet<>());
                ps.add(varSetLeafMap.get(hash));
                vpList.put(n, ps);
                System.out.println("The P-tree node is added to the variable-pattern list corresponding to " + n.getName());
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
        System.out.println("Processing the variables");
        HashSet <PtreeNode> processed = new HashSet<>();
        ArrayDeque <PtreeNode> pSequence = new ArrayDeque<>(); // arraydeque cz that's the queue i could think of but any queue should work
        for(Node var : vpList.keySet()){ // process each variable
            System.out.println("Processing " + var.getName());
            HashSet<PtreeNode> ps = vpList.get(var);
            for(PtreeNode p : ps){
                if(!processed.contains(p)){
                    pSequence.addLast(p); // insert to end of deque
                    processed.add(p); // mark as processed
                    System.out.println("The P-tree node " + p + " is added to the pSequence.");
                }
            }
        }
        return pSequence;
    }

    private void buildPtree(ArrayDeque <PtreeNode> pSequence, int ptreeNodeMin){
        PtreeNode firstMismatched = null;
        System.out.println("Building the tree starts with a pattern sequence of " + pSequence.size() + " P-tree nodes");
        while(pSequence.size() > 1){ // would stop when only one node is left
            PtreeNode p1 = pSequence.pollFirst();
            PtreeNode p2 = pSequence.peekFirst();
            System.out.println(p1 + " is polled");
            System.out.println(p2 + " is peeked");
            NodeSet intersection = p1.getVars().intersection(p2.getVars());
            if(intersection.isEmpty()){
                System.out.println("The two nodes are disjoint and the polled node will be added to the end of the sequence.");
                pSequence.addLast(p1);
                if(firstMismatched == p1){ // if the first mismatched node is reached again, then the tree is complete, every node left in the pSequence is a disjoint tree
                    break;
                }
                else if(firstMismatched == null){
                    firstMismatched = p1;
                }
            }
            else {
                System.out.println("The two nodes intersect at the variables: " + intersection);
                firstMismatched = null;
                p2 = pSequence.pollFirst();
                PtreeNode parent = matchSiblings(p1, p2, intersection, ptreeNodeMin);
                pSequence.add(parent);
            }
            System.out.println("The pattern sequence now has " + pSequence.size() + " P-tree " + (pSequence.size() == 1 ? "node." : "nodes."));
        }
        System.out.println("The tree is constructed with " + pSequence.size() + (pSequence.size() == 1 ? " root." : " roots."));
    }

    private PtreeNode matchSiblings(PtreeNode p1, PtreeNode p2, NodeSet intersection, int ptreeNodeMin){
        System.out.println("Matching the siblings");
        // setup siblings
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
        PtreeNode parent = new PtreeNode(null, null, parentSIndex, vars, null);
        p1.setParent(parent);
        p2.setParent(parent);
        if(ptreeNodeMin < 2) // it's not andentail, 2 is for andentail
            parent.setMin(ptreeNodeMin);
        else
            parent.setMin(p1.getMin() + p2.getMin());
        System.out.println("The parent " + parent + " is created with intersection " + intersection + " and variables " + vars);
        return parent;
    }

    @Override
    public RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException, DirectCycleException {
        // when inserting a rule info into the tree, it should only have one flag node (that of the antecedent that caused it to be sent)
        System.out.println("Inserting " + ri + " into the P-Tree");
        if (ri.getFns().size() != 1){
            throw new InvalidRuleInfoException("RuleInfo should only have one flag node when being inserted in tree");
        }
        FlagNode fn = ri.getFns().getFlagNodes().iterator().next();
        Node n = fn.getNode();

        if(antecedentRIcount.containsKey(n.getId())){

            int[] count = antecedentRIcount.get(n.getId());  // till the end of the else is really unnecessary after start of propagation but code was moved up bcz it's nice info to keep track of. could move down for after return since it's usually unnecessary info later
            if(fn.isFlag()){
                if(count[0] == 0)
                    posAntecedents++;
                count[0]++;
            }
            else{
                if(count[1] == 0)
                    negAntecedents++;
                count[1]++;
            }
            System.out.println("The antecedent " + n.getName() + " has " + count[0] + " positive and " + count[1] + " negative reports.");
            System.out.println("So far the P-Tree has " + posAntecedents + " positively reporting antecedents and " + negAntecedents + " negatively reporting antecedents.");
            int hash = n.getFreeVariablesHash();
            RuleInfoSet mayInfer = varSetLeafMap.get(hash).insertIntoNode(ri, isPropagating);
            if(mayInfer != null && !mayInfer.isEmpty()){
                return mayInfer;
            }

            if((posAntecedents >= minPcount || negAntecedents >= minNcount) && !isPropagating){
                isPropagating = true;
                return startPropagation();
            }
        }
        else{
            throw new InvalidRuleInfoException("RuleInfo should come from one of the antecedents of the tree");
        }

        return null;
    }

    private RuleInfoSet startPropagation() throws InvalidRuleInfoException, DirectCycleException {
        // propagating here is bsically a BFS traversal of the tree where both siblings must be visited in order for their RIs to be combined and there parent added to the queue
        System.out.println("Starting propagation in the P-Tree");
        HashSet <PtreeNode> visited = new HashSet<>();
        ArrayDeque <PtreeNode> queue = new ArrayDeque<>(varSetLeafMap.values());
        RuleInfoSet rootRuleInfos = new RuleInfoSet();
        while(!queue.isEmpty()){
            PtreeNode p = queue.pollFirst();
            if(!visited.contains(p)){
                System.out.println("Visiting " + p);
                visited.add(p);
                RuleInfoSet ris = new RuleInfoSet();
                ris = ris.union(p.getSIndex().getAllRuleInfos());
                System.out.println("The RuleInfos in this P-Tree node are:");
                System.out.println(ris);
                PtreeNode parent = p.getParent();
                if(parent != null && visited.contains(p.getSibling())){
                    System.out.println("Both siblings are visited and their RIs will be combined and added to the parent.");
                    ris = ris.combineAdd(p.getSibling().getSIndex().getAllRuleInfos());
                    System.out.println("The combined RuleInfos are:");
                    System.out.println(ris);
                    parent.getSIndex().insertVariableRI(ris);
                    queue.addLast(parent);
                }
                else if(parent == null){
                    System.out.println("This is a root node and its RIs will be added to the root RuleInfoSet.");
                    rootRuleInfos = rootRuleInfos.union(p.getSIndex().getAllRuleInfos());
                }
            }
        }
        return rootRuleInfos;
    }

    public ArrayList<PtreeNode> arrayOfNodes(){  //for testing
        ArrayList<PtreeNode> arr = new ArrayList<>();
        ArrayDeque <PtreeNode> queue = new ArrayDeque<>(varSetLeafMap.values());
        HashSet<PtreeNode> addedToQueue = new HashSet<>(varSetLeafMap.values());
        while(!queue.isEmpty()){
            PtreeNode p = queue.pollFirst();
            arr.add(p);
            if(p.getParent() != null && !addedToQueue.contains(p.getParent())){
                queue.addLast(p.getParent());
                addedToQueue.add(p.getParent());
            }
        }
        return arr;
    }

    public String BFString(){
        StringBuilder sb = new StringBuilder();
        ArrayDeque <PtreeNode> queue = new ArrayDeque<>(varSetLeafMap.values());
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

    public HashMap<Integer, PtreeNode> getVarSetLeafMap() {
        return varSetLeafMap;
    }

}
