package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class PtreeNode {

    private PtreeNode parent; // important for upward traversal (propagation of RIs)
    private PtreeNode sibling; // important for combining RIs
    private final SIndex sIndex; // A linear or singleton SIndex based on the position of the PtreeNode in the tree.
    private final NodeSet vars; // free vars in propositions node represents
    private FreeVariableSet siblingIntersection; // shared vars between sibling and this node  (parent.getCommonVariables)

    public PtreeNode(PtreeNode parent, PtreeNode sibling, SIndex sIndex,
                     NodeSet vars, FreeVariableSet siblingIntersection) {
        this.parent = parent;
        this.sibling = sibling;
        this.sIndex = sIndex;
        this.vars = new NodeSet();
        for(Node var : vars){   // shallow cloning so that changing in set would destroy nothing
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

    public SIndex getSIndex() {
        return sIndex;
    }

    public NodeSet getVars() {
        return vars;
    }

    public void setSiblingIntersection(FreeVariableSet siblingIntersection) {
        this.siblingIntersection = siblingIntersection;
    }

    public RuleInfoSet insertIntoNode(RuleInfo ri, boolean isPropagating) throws InvalidRuleInfoException, DirectCycleException {
        RuleInfoSet newRuleInfoSet = sIndex.insertVariableRI(ri);
        RuleInfoSet result = new RuleInfoSet();
        if(newRuleInfoSet == null || newRuleInfoSet.isEmpty())
            return result;
        for(RuleInfo newRuleInfo : newRuleInfoSet){
            if(newRuleInfo.getPcount() >= sIndex.getMin() && isPropagating){
                System.out.println("Propagating up the RuleInfo: " + newRuleInfo);
                if(parent != null) {
                    RuleInfoSet combinedWithSibling = combineWithSibling(newRuleInfo);
                    if(!combinedWithSibling.isEmpty()) {
                        System.out.println("The RuleInfo found compatible RuleInfos in Sibling.");
                        result = result.union(parent.insertIntoNode(combinedWithSibling, true));
                    }
                    else {
                        System.out.println("There were no compatible RuleInfos in Sibling.");
                        result = result.union(parent.insertIntoNode(newRuleInfo.addNullSubs(siblingIntersection), true));
                    }
                } else{
                    result.addRuleInfo(newRuleInfo);
                }
            }
        }
        return result;
    }

    RuleInfoSet insertIntoNode(RuleInfoSet ris, boolean isPropagating) throws InvalidRuleInfoException, DirectCycleException {
        RuleInfoSet result = new RuleInfoSet();
        for(RuleInfo ri: ris) {
            RuleInfoSet inserted = insertIntoNode(ri, isPropagating);
            if(inserted != null){
                result = result.union(inserted);
            }
        }
        return result;
    }

    private RuleInfoSet combineWithSibling(RuleInfo newRI) throws DirectCycleException {
        RuleInfoSet allRuleInfos = new RuleInfoSet();
        for(RuleInfo ri: sibling.getSIndex().getAllRuleInfos()){
            RuleInfo combined = newRI.combine(ri);
            if(combined != null)
                allRuleInfos.addRuleInfo(combined);
        }
        return allRuleInfos;
    }

    @Override
    public String toString() {
        return "PtreeNode{" +
                " has " + (parent != null ? "a" : "no") + " parent, " +
                " has " + (sibling != null ? "a" : "no") + " sibling, " +
                " sIndex is " + (sIndex instanceof Singleton ? "Singleton" : "Linear") +
                ", vars =" + vars +
                (sibling != null ? ", siblingIntersection =" + siblingIntersection : "")+
                ", min =" + sIndex.getMin() +
                '}';
    }

    public void setMin(int min) {
        sIndex.setMin(min);
    }

    public int getMin() {
        return sIndex.getMin();
    }
}
