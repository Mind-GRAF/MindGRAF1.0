package paths;

import java.util.ArrayList;
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
		return null;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		// TODO Auto-generated method stub
		return null;
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
