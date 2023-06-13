package cables;

import relations.Relation;
import set.NodeSet;

public class DownCable extends Cable{

	public DownCable(Relation relation, NodeSet nodeSet) {
		super(relation,nodeSet);
		nodeSet.setIsFinal(true);
	}


	public String toString(){
		return "downCable : {"+"relation: "+this.getRelation() + ", NodeSet :"+this.getNodeSet() +"}";
	}
	
	
}
