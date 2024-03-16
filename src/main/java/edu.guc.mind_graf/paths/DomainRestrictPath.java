package edu.guc.mind_graf.paths;

import java.util.LinkedList;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;

public class DomainRestrictPath extends Path {
	private Path q;
	private Path p;
	private Node zNode;

	public DomainRestrictPath(Path q, Path p, Node zNode) {
		this.q = q;
		this.p = p;
		this.zNode = zNode;
	}

	public Path getQ() {
		return q;
	}

	public void setQ(Path q) {
		this.q = q;
	}

	public Path getP() {
		return p;
	}

	public void setP(Path p) {
		this.p = p;
	}

	public Node getzNode() {
		return zNode;
	}

	public void setzNode(Node zNode) {
		this.zNode = zNode;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace,
			Context context) {
		LinkedList<Object[]> result = new LinkedList<Object[]>();
		LinkedList<Object[]> temp = q.follow(node, trace, context);
		for (Object[] objects : temp) {
			Node n = (Node) objects[0];
			PathTrace pt = (PathTrace) objects[1];
			if (n.equals(this.zNode)) {
				trace.addAllSupports(pt.getSupports());
				result = p.follow(node, trace, context);
			}
		}
		return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		return new RangeRestrictPath(this.q, this.p.converse(),
				this.zNode).follow(node, trace, context);

	}

	@Override
	public DomainRestrictPath clone() {
		return new DomainRestrictPath(this.q.clone(), this.p.clone(),
				this.zNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DomainRestrictPath))
			return false;
		DomainRestrictPath dPath = (DomainRestrictPath) obj;

		if (!this.p.equals(dPath.getP()) || !this.q.equals(dPath.getQ())
				|| !this.zNode.equals(dPath.getzNode())) {
			return false;
		}
		return true;
	}

	public String toString() {
		return "DomainRestrict(" + this.q.toString() + ", "
				+ this.zNode.toString() + ", " + this.p.toString() + ")";
	}

	@Override
	public Path converse() {
		return new RangeRestrictPath(q, p.converse(), zNode);
	}

}
