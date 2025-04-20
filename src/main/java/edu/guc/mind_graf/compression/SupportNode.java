package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SupportNode {
    ArrayList<HypNode> supportingNodes;

    public SupportNode(ArrayList<HypNode> hypNodes, HypNode... hypNode){
        this.supportingNodes = new ArrayList<HypNode>();
        this.supportingNodes.addAll(hypNodes);
        this.supportingNodes.addAll(java.util.Arrays.asList(hypNode));
    }
    public SupportNode() {
        supportingNodes =  new ArrayList<HypNode>();

    }

    public SupportNode(ArrayList<HypNode> hypNodes) {
        this.supportingNodes = hypNodes;

    }

    public SupportNode(HypNode... hypNode) {
        this.supportingNodes = new ArrayList<HypNode>(java.util.Arrays.asList(hypNode));
    }
    public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    SupportNode other = (SupportNode) obj;
    return  Objects.equals(supportingNodes, other.supportingNodes);
    }

}
