package cables;

import relations.Relation;
import set.NodeSet;
import nodes.Node;

public class UpCable extends Cable {
	 private NodeSet nodeSet ;

	public UpCable(Relation relation) {
		super(relation);
		nodeSet = new NodeSet();
	}
	
	public void addNode(Node node){
		this.getNodeSet().add(node);
	}
	public void removeNode(Node node){
		this.getNodeSet().remove(node);
	}

	@Override
	public NodeSet getNodeSet() {
		// TODO Auto-generated method stub
		return this.nodeSet;
	}

	public void setNodeSet(NodeSet nodeSet) {
		// TODO Auto-generated method stub
		this.nodeSet=nodeSet;
	}

	@Override
	public String toString(){
		return "UpCable : {"+"relation: "+this.getRelation() + "_"+this.nodeSet +"}";
	}
	
}
