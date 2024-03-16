package cables;

import relations.Relation;
import set.NodeSet;

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
