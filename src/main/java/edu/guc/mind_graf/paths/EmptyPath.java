package edu.guc.mind_graf.paths;

import java.util.LinkedList;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.context.Context;

public class EmptyPath extends Path {

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context, int attitude) {
		// TODO Auto-generated method stub
		LinkedList<Object[]> result = new LinkedList<Object[]>();
		Object[] obj = { node, trace };
		result.add(obj);
		return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context, int attitude) {
		// TODO Auto-generated method stub
		LinkedList<Object[]> result = new LinkedList<Object[]>();
		Object[] obj = { node, trace };
		result.add(obj);
		return result;
	}

	@Override
	public Path clone() {
		// TODO Auto-generated method stub
		EmptyPath empty = new EmptyPath();
		return empty;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
        return obj instanceof EmptyPath;
    }

	@Override
	public Path converse() {
		// TODO Auto-generated method stub
		return this;

	}

}
