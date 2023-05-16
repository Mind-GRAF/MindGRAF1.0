package mindG.network.cables;

import mindG.network.NodeSet;
import mindG.network.relations.Relation;

abstract public class Cable {
    private Relation relation;
    private NodeSet nodeSet;

    public Cable(Relation relation, NodeSet nodeSet) {
        super();
        this.relation = relation;
        this.nodeSet = nodeSet;
    }

    public Relation getRelation() {
        return relation;
    }

    public NodeSet getNodeSet() {
        // TODO Auto-generated method stub
        return this.nodeSet;
    }

    protected void setNodeSet(NodeSet nodeSet) {
        // TODO Auto-generated method stub
        this.nodeSet = nodeSet;
    }

    abstract public String toString();

}
