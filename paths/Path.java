package paths;

import java.util.ArrayList;
import java.util.LinkedList;

import context.Context;
import nodes.Node;

abstract public class Path {

	abstract public LinkedList<Object[]> follow(Node node, PathTrace trace,Context context);
	abstract public LinkedList<Object[]> followConverse(Node node, PathTrace trace, Context context);
	abstract public Path clone();
	abstract public boolean equals(Object obj);
	abstract public Path converse();
	
}
