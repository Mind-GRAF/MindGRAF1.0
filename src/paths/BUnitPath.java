package paths;

import java.util.ArrayList;
import java.util.LinkedList;

import relations.Relation;
import set.NodeSet;
import cables.DownCable;
import cables.UpCable;
import nodes.Node;
import context.Context;

public class BUnitPath extends Path {
	private Relation relation;
	public BUnitPath(Relation relation) {
		this.relation=relation;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		LinkedList<Object[]> result = new LinkedList<Object[]>();
		if(node.isMolecular()){
			UpCable d = node.getUpCable(relation.getName());
			if(d != null){
					NodeSet nodeSet = d.getNodeSet();
					for (Node n : nodeSet.getValues()) {
						PathTrace t = trace.clone();
						t.compose(new FUnitPath(relation));
						Object [] arr = {n,t} ;
						result.add(arr);
					}
			}
		}
		return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		// TODO Auto-generated method stub
		return new FUnitPath(this.relation).follow(node, trace, context);
	}

	@Override
	public Path clone() {
		return new BUnitPath(this.relation);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof BUnitPath && ((BUnitPath) obj).getRelation().equals(this.getRelation()))
			return true;
		return false;
	}

	@Override
	public Path converse() {
		// TODO Auto-generated method stub
		return new FUnitPath(this.relation);
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}
	public String toString(){
		return "BUnitPath("+this.relation.toString()+")";
	}
}
