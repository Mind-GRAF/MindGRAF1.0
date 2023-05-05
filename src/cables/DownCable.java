package cables;

import relations.Relation;
import set.NodeSet;

public class DownCable extends Cable{
	 private final NodeSet nodeSet ;

	public DownCable(Relation relation, NodeSet nodeSet) {
		super(relation);
		this.nodeSet = nodeSet;
	}
	public NodeSet getNodeSet(){
		return nodeSet;
	}

	public String toString(){
		return "downCable : {"+"relation: "+this.getRelation() + ", NodeSet :"+this.nodeSet +"}";
	}
	
	
}
