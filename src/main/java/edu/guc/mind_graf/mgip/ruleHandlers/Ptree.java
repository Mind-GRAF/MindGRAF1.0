package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.FreeVariableSet;
import edu.guc.mind_graf.set.NodeSet;

class PtreeNode {

    private PtreeNode parent; // important for upward traversal (propagation of RIs)
    private PtreeNode sibling; // important for combining RIs
    private PtreeNode leftChild;
    private PtreeNode rightChild;
    private SIndex sIndex; // linear or singleton based on siblingIntersection
    private Set<Integer> pats; // antecedents node stores info abt
    private FreeVariableSet vars; // free vars in propositions node represents
    private FreeVariableSet siblingIntersection; // shared vars between sibling and this node

    public PtreeNode(PtreeNode parent, PtreeNode sibling, PtreeNode leftChild, PtreeNode rightChild, SIndex sIndex,
            Set<Integer> pats, FreeVariableSet vars, FreeVariableSet siblingIntersection) {
        this.parent = parent;
        this.sibling = sibling;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.sIndex = sIndex;
        this.pats = pats;
        this.vars = vars;
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

    public SIndex getsIndex() {
        return sIndex;
    }

    public void setsIndex(SIndex sIndex) {
        this.sIndex = sIndex;
    }

    public Set<Integer> getPats() {
        return pats;
    }

    public void setPats(Set<Integer> pats) {
        this.pats = pats;
    }

    public FreeVariableSet getVars() {
        return vars;
    }

    public void setVars(FreeVariableSet vars) {
        this.vars = vars;
    }

    public FreeVariableSet getSiblingIntersection() {
        return siblingIntersection;
    }

    public void setSiblingIntersection(FreeVariableSet siblingIntersection) {
        this.siblingIntersection = siblingIntersection;
    }

    public void insertIntoNode(RuleInfo ri){

    }

}

public class Ptree extends RuleInfoHandler {

    private PtreeNode root;
    private NodeSet antecedents;
    private NodeSet FreeVariableSet;

    public HashMap<String, NodeSet> computePatternVariableList(){
        HashMap <String, NodeSet> pvList = new HashMap<String, NodeSet>(); //map pattern node name to its nodeset of variables
        for (Node n : antecedents){
            NodeSet vars = n.getFreeVariables();
            pvList.put(n.getName(), vars);
        }
        return pvList;
    }

    public HashMap<String, List<String>> computeVariablePatternList(HashMap<String, NodeSet> pvList){
        HashMap <String, List<String>> vpList = new HashMap<String, List<String>>(); //map variable name to its nodeset of patterns
        for (String pattern : vpList.keySet()){
            NodeSet vars = pvList.get(pattern);
            for (Node n : vars){
                if (vpList.containsKey(n.getName())){
                    vpList.get(n.getName()).add(pattern);
                }
                else{
                    List<String> ps = new ArrayList<String>();
                    ps.add(pattern);
                    vpList.put(n.getName(), ps);
                }
            }
            
        }
        return vpList;
    }

    @Override
    public void insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertVariableRI'");
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }

}
