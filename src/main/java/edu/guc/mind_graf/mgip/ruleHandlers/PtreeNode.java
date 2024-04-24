package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class PtreeNode {

    private PtreeNode parent; // important for upward traversal (propagation of RIs)
    private PtreeNode sibling; // important for combining RIs
    private SIndex sIndex; // linear or singleton based on siblingIntersection
    //private Set<Integer> pats; // antecedents node stores info abt
    private NodeSet vars; // free vars in propositions node represents
    private FreeVariableSet siblingIntersection; // shared vars between sibling and this node  (parent.getCommonVariables)

    public PtreeNode(PtreeNode parent, PtreeNode sibling, PtreeNode leftChild, PtreeNode rightChild, SIndex sIndex,
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
        if(newRuleInfoSet == null || newRuleInfoSet.size() == 0)
            return result;
        for(RuleInfo newRuleInfo : newRuleInfoSet){
            if(newRuleInfo.getPcount() >= sIndex.getMin() && isPropagating){
                if(parent != null) {
                    RuleInfoSet combinedWithSibling = combineWithSibling(newRuleInfo);
                    if(combinedWithSibling.size() > 0) {
                        result = result.union(parent.insertIntoNode(combinedWithSibling, true));
                    }
                    else {
                        result = result.union(parent.insertIntoNode(newRuleInfo.addNullSubs(siblingIntersection), true));
                    }
                } else{
                    result.addRuleInfo(newRuleInfo);
                }
            }
        }
        return result;
    }

    RuleInfoSet insertIntoNode(RuleInfoSet ris, boolean isPropagating) throws InvalidRuleInfoException{
        RuleInfoSet result = new RuleInfoSet();
        for(RuleInfo ri: ris) {
            RuleInfoSet inserted = insertIntoNode(ri, isPropagating);
            if(inserted != null){
                result = result.union(inserted);
            }
        }
        return result;
    }

    private RuleInfoSet combineWithSibling(RuleInfo newRI) {
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
                "has parent=" + (parent != null) +
                ",has sibling=" + (sibling != null) +
//                ", leftChild=" + leftChild +
//                ", rightChild=" + rightChild +
                ", sIndex=" + (sIndex instanceof Singleton ? "Singleton" : "Linear") +
                ", vars=" + vars +
                ", siblingIntersection=" + siblingIntersection +
                ", min=" + sIndex.getMin() +
                '}';
    }

    public void setMin(int min) {
        sIndex.setMin(min);
    }

    public int getMin() {
        return sIndex.getMin();
    }
}
