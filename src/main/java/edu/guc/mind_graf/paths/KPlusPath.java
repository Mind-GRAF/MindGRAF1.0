package edu.guc.mind_graf.paths;

import java.util.HashSet;
import java.util.LinkedList;





import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.nodes.Node;

public class KPlusPath extends Path {

	Path path;
	public KPlusPath(Path path) {
		// TODO Auto-generated constructor stub
		this.path=path;
		
		}
			
		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}

		public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		    LinkedList<Object[]> visited = new LinkedList<>();
		    HashSet<Node> seen = new HashSet<>();
		    LinkedList<Object[]> queue = this.path.follow(node,trace,context);
		 
		    while (!queue.isEmpty()) {
		        Object[] current = queue.removeFirst();
		        Node current_node = (Node) current[0];
		        PathTrace current_trace = (PathTrace) current[1];

		        if (!seen.contains(current_node)) {
		            visited.add(current);
		            seen.add(current_node);

		            LinkedList<Object[]> neighbors = this.path.follow(current_node, current_trace, context);
		            for (Object[] neighbor : neighbors) {
		                Node neighbor_node = (Node) neighbor[0];
		                if (!seen.contains(neighbor_node)) {
		                    queue.add(neighbor);
		                }
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
	public KPlusPath clone() {
		return new KPlusPath(this.path.clone());
	}

	@Override
	public boolean equals(Object obj) {
        return obj instanceof KPlusPath && ((KPlusPath) obj).getPath().equals(this.getPath());
    }

	@Override
	public Path converse() {
		return new KPlusPath(path.converse());
	}
	public String toString(){
		return "KPlus Path("+this.path.toString()+")";
	}

}
