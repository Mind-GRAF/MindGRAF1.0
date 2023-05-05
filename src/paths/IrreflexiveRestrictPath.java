package paths;

import java.util.ArrayList;
import java.util.LinkedList;
import nodes.Node;
import context.Context;

public class IrreflexiveRestrictPath extends Path {
	private Path path;

	public IrreflexiveRestrictPath(Path path) {
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
		LinkedList<Object[]> result = this.path.follow(node, trace, context);
		for (int i = 0; i < result.size(); i++){
			if(((Node)result.get(i)[0]).equals(node)){
				result.remove(i);
				i--;
			}
		}
		return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
			LinkedList<Object[]> result = this.path.followConverse(node, trace, context);
			for (int i = 0; i < result.size(); i++){
				if(((Node)result.get(i)[0]).equals(node)){
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
		if(! (obj instanceof IrreflexiveRestrictPath)) 
			return false;
		if(!((IrreflexiveRestrictPath)obj).getPath().equals(this.path)){
			return false;
		}
		return true;
	}

	public String toString(){
		return "IrreflexiveRestrict(" + this.path.toString() + ")";
	}

	@Override
	public Path converse() {
		return new IrreflexiveRestrictPath(this.path.converse());
	}

}
