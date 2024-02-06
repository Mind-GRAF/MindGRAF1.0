package cables;

import relations.Relation;
import set.NodeSet;
import nodes.Node;

public class UpCable extends Cable {
	public UpCable(Relation relation,NodeSet nodeSet) {
		super(relation,nodeSet);
	}
	
	public void addNode(Node node){
		this.getNodeSet().add(node);
	}
	public void removeNode(Node node){
		this.getNodeSet().remove(node);
	}
	public void setNodeSet(NodeSet nodeSet){
		this.setNodeSet(nodeSet);
	}
	
	

	@Override
	public String toString(){
		return "UpCable : {"+"relation: "+this.getRelation() + "_"+this.getNodeSet() +"}";
	}
	
}
