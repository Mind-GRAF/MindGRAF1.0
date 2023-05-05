package cables;

import relations.Relation;
import set.NodeSet;

 abstract public class Cable {
	 private String Name;
	 private Relation relation;
	 
	public Cable(Relation relation) {
		super();
		this.relation = relation;
		Name = relation.getName();
	}
	
	public String getName() {
		return Name;
	}

	public Relation getRelation() {
		return relation;
	}
	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	abstract public NodeSet getNodeSet();
	
	abstract public String toString();
	
	
}
