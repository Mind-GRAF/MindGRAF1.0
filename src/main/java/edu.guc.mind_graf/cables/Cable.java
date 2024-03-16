package edu.guc.mind_graf.cables;

import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;

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
		return this.nodeSet;
	}

	protected void setNodeSet(NodeSet nodeSet) {
		this.nodeSet = nodeSet;
	}

	abstract public String toString();

}
