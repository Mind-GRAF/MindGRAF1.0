package paths;

import java.util.LinkedList;

import nodes.Node;
import context.Context;

public class ConversePath extends Path {
	Path path;
	public ConversePath(Path path) {
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
		return this.path.followConverse(node, trace, context);
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		// TODO Auto-generated method stub
		return this.path.follow(node, trace, context);
	}

	@Override
	public Path clone() {
		return new ConversePath(this.path.clone());

	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ConversePath && ((ConversePath) obj).getPath().equals(this.getPath()))
			return true;
		return false;
	}

	@Override
	public Path converse() {
		return new ConversePath(this.path);
	}
	public String toString(){
		return "Converse("+this.path.toString()+")";
	}


}
