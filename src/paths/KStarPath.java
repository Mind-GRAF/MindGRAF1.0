package paths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import nodes.Node;
import context.Context;

public class KStarPath extends Path {
		Path path;
	public KStarPath(Path path) {
		// TODO Auto-generated constructor stub
		this.path=path;

		}
			
		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}


		@Override
		public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		    LinkedList<Object[]> visited = new LinkedList<>();
		    HashSet<Node> seen = new HashSet<>();
		    visited.add(new Object[] {node, trace});
		    seen.add(node);
		    int index = 0;
		    while (index < visited.size()) {
		        Object[] current = visited.get(index++);
		        Node current_node = (Node) current[0];
		        PathTrace current_trace = (PathTrace) current[1];
		        LinkedList<Object[]> neighbors = this.path.follow(current_node, current_trace, context);
		        for (Object[] neighbor : neighbors) {
		            Node neighbor_node = (Node) neighbor[0];
		            if (!seen.contains(neighbor_node)) {
		                visited.add(neighbor);
		                seen.add(neighbor_node);
		            }
		        }
		    }
		    return visited;
		}


		@Override
		public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
				Context context) {
			 LinkedList<Object[]> visited = new LinkedList<>();
			    HashSet<Node> seen = new HashSet<>();
			    visited.add(new Object[] {node, trace});
			    seen.add(node);
			    int index = 0;
			    while (index < visited.size()) {
			        Object[] current = visited.get(index++);
			        Node current_node = (Node) current[0];
			        PathTrace current_trace = (PathTrace) current[1];
			        LinkedList<Object[]> neighbors = this.path.followConverse(current_node, current_trace, context);
			        for (Object[] neighbor : neighbors) {
			            Node neighbor_node = (Node) neighbor[0];
			            if (!seen.contains(neighbor_node)) {
			                visited.add(neighbor);
			                seen.add(neighbor_node);
			            }
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
		if(obj instanceof KStarPath && ((KStarPath) obj).getPath().equals(this.getPath()))
			return true;
		return false;
	}

	@Override
	public Path converse() {
		return new KStarPath(path.converse());

	}
	public String toString(){
		return "KStar Path("+this.path.toString()+")";
	}

}
