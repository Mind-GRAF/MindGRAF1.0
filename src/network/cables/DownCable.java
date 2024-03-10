package mindG.network.cables;

import mindG.network.NodeSet;
import mindG.network.relations.Relation;

public class DownCable extends Cable {

    public DownCable(Relation relation, NodeSet nodeSet) {
        super(relation, nodeSet);
        nodeSet.setIsFinal(true);
    }

    public String toString() {
        return "downCable : {" + "relation: " + this.getRelation() + ", NodeSet :" + this.getNodeSet() + "}";
    }

}
