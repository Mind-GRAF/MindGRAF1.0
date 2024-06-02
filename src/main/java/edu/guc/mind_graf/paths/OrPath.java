package edu.guc.mind_graf.paths;

import java.util.HashMap;
import java.util.LinkedList;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.mgip.matching.Match;

public class OrPath extends Path {
	private LinkedList<Path> paths;

	public OrPath() {
		paths = new LinkedList<>();
	}

	public OrPath(LinkedList<Path> paths) {
		LinkedList<Path> pList = new LinkedList<Path>();
		pList.addAll(paths);
		for (int i = 0; i < pList.size() - 1; i++) {
			Path p = pList.get(i);
			for (int j = i + 1; j < pList.size(); j++) {
				if (pList.get(j).equals(p)) {
					pList.remove(j);
					j--;
				}
			}
		}

		this.paths = pList;
	}

	public OrPath(Path... paths) {
		LinkedList<Path> pList = new LinkedList<Path>(java.util.Arrays.asList(paths));
		this.paths = pList;
	}

	public LinkedList<Path> getPaths() {
		return paths;
	}

	public void setPaths(LinkedList<Path> paths) {
		this.paths = paths;
	}

	@Override
	public boolean passFirstCheck(Node node, Match match) {
		for (Path p : paths) {
			if (p.passFirstCheck(node, match)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context, int attitude) {
		if (this.paths.isEmpty())
			return new LinkedList<Object[]>();
		LinkedList<Path> pList = new LinkedList<Path>();
		pList.addAll(this.paths);
		Path p = pList.removeFirst();
		OrPath orPath = new OrPath(pList);
		if (pList.size() > 0)
			return union(p.follow(node, trace, context, attitude), orPath.follow(node, trace, context, attitude));
		else
			return p.follow(node, trace, context, attitude);

	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context, int attitude) {
		if (this.paths.isEmpty())
			return new LinkedList<Object[]>();
		LinkedList<Path> pList = new LinkedList<Path>();
		pList.addAll(this.paths);
		Path p = pList.removeFirst();
		OrPath orPath = new OrPath(pList);
		if (pList.size() > 0)
			return union(p.followConverse(node, trace, context, attitude),
					orPath.followConverse(node, trace, context, attitude));
		else
			return p.followConverse(node, trace, context, attitude);

	}

	private LinkedList<Object[]> union(LinkedList<Object[]> list1, LinkedList<Object[]> list2) {
		LinkedList<Object[]> unionList = new LinkedList<Object[]>();
		unionList.addAll(list1);
		unionList.addAll(list2);
		return unionList;
	}

	@Override
	public Path clone() {
		LinkedList<Path> pList = new LinkedList<Path>();
		for (Path path : this.paths)
			pList.add(path.clone());

		return new OrPath(pList);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OrPath))
			return false;
		OrPath orPath = (OrPath) obj;
		if (this.paths.size() != orPath.getPaths().size())
			return false;

		HashMap<String, Path> HashedResult = new HashMap<String, Path>();
		for (Path path : paths)
			HashedResult.put(path.toString(), path);

		for (Path path : orPath.getPaths())
			if (!HashedResult.containsKey(path.toString()))
				return false;

		return true;

	}

	public String toString() {
		String id = "Or Path(";
		int i = 1;
		for (Path path : paths) {
			id += path.toString() + (i == paths.size() ? "" : "_");
			i++;
		}
		id += ")";
		return id;
	}

	@Override
	public Path converse() {
		LinkedList<Path> result = new LinkedList<Path>();

		for (Path path : paths)
			result.add(path.converse());

		OrPath or = new OrPath(result);
		return or;
	}

}
