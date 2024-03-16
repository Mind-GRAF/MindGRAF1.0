package edu.guc.mind_graf.cables;

import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;

public class DownCable extends Cable {

	public DownCable(Relation relation, NodeSet nodeSet) {
		super(relation, nodeSet);
		nodeSet.setIsFinal(true);
	}

	public String toString() {
		return "downCable : {" + "relation: " + this.getRelation() + ", NodeSet :" + this.getNodeSet() + "}";
	}

}
