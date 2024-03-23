package edu.guc.mind_graf.paths;

import java.util.LinkedList;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.nodes.Node;

abstract public class Path {

	abstract public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context);

	abstract public LinkedList<Object[]> followConverse(Node node, PathTrace trace, Context context);

	abstract public Path clone();

	abstract public boolean equals(Object obj);

	abstract public Path converse();

}
