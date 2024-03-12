package paths;

import java.util.LinkedList;
import nodes.Node;
import context.Context;

public class BangPath extends Path {
//	Attitude attitude;
	

	public BangPath() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		// TODO Auto-generated method stub
		LinkedList<Object[]> result = new LinkedList<Object[]> ();
//		if(node instanceof PropositionNode && node.isSupported(attitude)){
//			PathTrace pt = trace.clone();
//			pt.addSupport(node);
//			Object [] o = {node,pt};
//			result.add(o);
//		}
		return result ;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		// TODO Auto-generated method stub
		LinkedList<Object[]> result = new LinkedList<Object[]> ();
//		if(node instanceof PropositionNode && node.isSupported(attitude)){
//			PathTrace pt = trace.clone();
//			pt.addSupport(node);
//			Object [] o = {node,pt};
//			result.add(o);
//		}
		return result ;
		}

	@Override
	public Path clone() {
//		return new BangPath(Attitude attitude);
		return null;
	}

	@Override
	public boolean equals(Object obj) {
//		if(!(obj instanceof BangPath) || ! this.attitude.equals(((BangPath)obj).attitude))
//		return false;
		return true ;
	}

	@Override
		public Path converse() {
			return this;
		}
	public String toString(){
		return "!";
	}

}
