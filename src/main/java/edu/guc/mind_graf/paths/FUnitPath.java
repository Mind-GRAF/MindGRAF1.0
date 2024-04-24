package edu.guc.mind_graf.paths;

import java.util.LinkedList;

import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;

public class FUnitPath extends Path {
	private Relation relation;

	public FUnitPath(Relation relation) {
		super();
		this.relation = relation;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		LinkedList<Object[]> result = new LinkedList<Object[]>();
		if (node.isMolecular()) {
			DownCable d = node.getDownCable(relation.getName());
			if (d != null) {
				NodeSet nodeSet = d.getNodeSet();
				for (Node n : nodeSet.getValues()) {
					PathTrace t = trace.clone();
					t.compose(new FUnitPath(relation));
					Object[] arr = { n, t };
					result.add(arr);
				}
			}
		}
		return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		return new BUnitPath(this.relation).follow(node, trace, context);

	}

	@Override
	public Path clone() {
		return new FUnitPath(this.relation);
	}

	@Override
	public boolean equals(Object obj) {
        return obj instanceof FUnitPath && ((FUnitPath) obj).getRelation().equals(this.getRelation());
    }

	@Override
	public Path converse() {
		return new BUnitPath(this.relation);
	}

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public String toString() {
		return "FUnitPath(" + this.relation.toString() + ")";
	}

}
