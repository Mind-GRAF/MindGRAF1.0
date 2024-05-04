package edu.guc.mind_graf.paths;

import java.util.HashSet;
import java.util.LinkedList;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;

public class KStarPath extends Path {
	Path path;

	public KStarPath(Path path) {
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
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context, int attitude) {
		LinkedList<Object[]> visited = new LinkedList<>();
		HashSet<Node> seen = new HashSet<>();
		LinkedList<Object[]> queue = new LinkedList<>();
		queue.add(new Object[] { node, trace });

		while (!queue.isEmpty()) {
			Object[] current = queue.removeFirst();
			Node current_node = (Node) current[0];
			PathTrace current_trace = (PathTrace) current[1];

			if (!seen.contains(current_node)) {
				visited.add(current);
				seen.add(current_node);

				LinkedList<Object[]> neighbors = this.path.follow(current_node, current_trace, context, attitude);
				queue.addAll(neighbors);
			}
		}

		return visited;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace, Context context, int attitude) {
		LinkedList<Object[]> visited = new LinkedList<>();
		HashSet<Node> seen = new HashSet<>();
		LinkedList<Object[]> queue = new LinkedList<>();
		queue.add(new Object[] { node, trace });

		while (!queue.isEmpty()) {
			Object[] current = queue.removeFirst();
			Node current_node = (Node) current[0];
			PathTrace current_trace = (PathTrace) current[1];

			if (!seen.contains(current_node)) {
				visited.add(current);
				seen.add(current_node);

				LinkedList<Object[]> neighbors = this.path.followConverse(current_node, current_trace, context, attitude);
				queue.addAll(neighbors);
			}
		}

		return visited;
	}

	@Override
	public Path clone() {
		return new KStarPath(this.path.clone());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KStarPath && ((KStarPath) obj).getPath().equals(this.getPath()))
			return true;
		return false;
	}

	@Override
	public Path converse() {
		return new KStarPath(path.converse());

	}

	public String toString() {
		return "KStar Path(" + this.path.toString() + ")";
	}

}
