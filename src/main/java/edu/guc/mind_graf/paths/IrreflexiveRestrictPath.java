package edu.guc.mind_graf.paths;

import java.util.LinkedList;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.mgip.matching.Match;

public class IrreflexiveRestrictPath extends Path {
	private Path path;

	public IrreflexiveRestrictPath(Path path) {
		// TODO Auto-generated constructor stub
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public boolean passFirstCheck(Node node, Match match) {
		return path.passFirstCheck(node, match);
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context, int attitude) {
		LinkedList<Object[]> result = this.path.follow(node, trace, context, attitude);
		for (int i = 0; i < result.size(); i++) {
			if (((Node) result.get(i)[0]).equals(node)) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context, int attitude) {
		LinkedList<Object[]> result = this.path.followConverse(node, trace, context, attitude);
		for (int i = 0; i < result.size(); i++) {
			if (((Node) result.get(i)[0]).equals(node)) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}

	@Override
	public IrreflexiveRestrictPath clone() {
		return new IrreflexiveRestrictPath(this.path.clone());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IrreflexiveRestrictPath))
			return false;
		return ((IrreflexiveRestrictPath) obj).getPath().equals(this.path);
	}

	public String toString() {
		return "IrreflexiveRestrict(" + this.path.toString() + ")";
	}

	@Override
	public Path converse() {
		return new IrreflexiveRestrictPath(this.path.converse());
	}

}
