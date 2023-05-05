package paths;

import java.util.ArrayList;
import java.util.LinkedList;
import nodes.Node;
import context.Context;

public class RangeRestrictPath extends Path {
	private Path q;
	private Path p;
	private Node zNode;

	public RangeRestrictPath(Path p, Path q, Node zNode) {
		// TODO Auto-generated constructor stub
		this.q = q;
		this.p = p;
		this.zNode = zNode;
	}

	public Path getQ() {
		return q;
	}

	public void setQ(Path q) {
		this.q = q;
	}

	public Path getP() {
		return p;
	}

	public void setP(Path p) {
		this.p = p;
	}

	public Node getzNode() {
		return zNode;
	}

	public void setzNode(Node zNode) {
		this.zNode = zNode;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		 LinkedList<Object[]> result = new LinkedList<Object[]>();
			LinkedList<Object[]> pFollow = this.p.follow(node, trace, context);
			for (Object[] objects : pFollow) {
        
                Node nodeP = (Node) objects[0];
                PathTrace pathTraceP = (PathTrace) objects[1];
                LinkedList<Object[]> qFollow = this.q.follow(nodeP,pathTraceP,context);
    			for (Object[] obj : qFollow) {
                
                        Node nodeQ = (Node) obj[0];
                        PathTrace pathTraceQ = (PathTrace) obj[1];
                        if(nodeQ.equals(this.zNode))
                        {
                                Object[] r = new Object[2];
                                r[0] = nodeQ;
                                PathTrace ptrace = pathTraceP.clone();
                                ptrace.addAllSupports(pathTraceQ.getSupports());
                                r[1] = ptrace;
                                result.add(r);
                        }

                }
        }
       
        return result;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		return new DomainRestrictPath(new ConversePath(p),q,zNode).follow(node,trace,context);
	}

	@Override
	public RangeRestrictPath clone() {
		return new RangeRestrictPath(this.p.clone(), this.q.clone(), this.zNode);
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof RangeRestrictPath))
			return false;
		RangeRestrictPath RPath = (RangeRestrictPath) obj;

		if(!this.p.equals(RPath.getP()) || !this.q.equals(RPath.getQ()) || !this.zNode.equals(RPath.getzNode()) ){
			return false;
		}
		return true ;
	}
	public String toString(){
		return "RangeRestrict(" + this.q.toString() + ", " + this.zNode.toString() + ", "
				+ this.p.toString() + ")";
	}

	@Override
	public Path converse() {
		return new DomainRestrictPath(p.converse(), q, zNode);
	}

}
