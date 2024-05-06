package edu.guc.mind_graf.paths;

import java.util.LinkedList;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;

public class ConversePath extends Path {
	Path path;

	public ConversePath(Path path) {
		this.path = path;

	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context, int attitude) {
		return this.path.followConverse(node, trace, context, attitude);
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context, int attitude) {
		return this.path.follow(node, trace, context, attitude);
	}

	@Override
	public Path clone() {
		return new ConversePath(this.path.clone());

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConversePath && ((ConversePath) obj).getPath().equals(this.getPath()))
			return true;
		return false;
	}

	@Override
	public Path converse() {
		return this.path;
	}

	public String toString() {
		return "Converse(" + this.path.toString() + ")";
	}

}
