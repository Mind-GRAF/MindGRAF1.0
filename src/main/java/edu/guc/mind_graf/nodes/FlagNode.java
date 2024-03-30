package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.set.PropositionNodeSet;

public class FlagNode {

    private Node node; // would normally be an antecedent
    private boolean flag;
    private PropositionNodeSet support; //the support supporting the report the node sent

    public FlagNode(Node node, boolean flag, PropositionNodeSet support) {
        this.node = node;
        this.flag = flag;
        this.support = support;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public PropositionNodeSet getSupport() {
        return support;
    }

    public void setSupport(PropositionNodeSet support) {
        this.support = support;
    }

    public boolean equals(FlagNode fn) {
        // maybe throw exception here if nodes equal but flags different
        return this.node.equals(fn.getNode());
    }
    
}
