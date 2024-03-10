package paths;

import java.util.LinkedList;

import set.NodeSet;
import nodes.*;

public class PathTrace {
	private Path path;
	private NodeSet supports;
	
	public PathTrace(Path path, NodeSet supports) {
		super();
		this.path = path;
		this.supports = supports;
	}
	public PathTrace(){
		this.path = new EmptyPath();
		this.supports = new NodeSet();
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public NodeSet getSupports() {
		return supports;
	}
	public void setSupports(NodeSet supports) {
		this.supports = supports;
	}
	public void addSupport(Node node){
		this.supports.add(node);
	}
	public void addAllSupports(NodeSet supports){
		supports.addAllTo(this.supports);
	}
	
	public void compose(Path path){
		if(path.getClass().getSimpleName().equals("EmptyPath"))
			return;
		if(this.path.getClass().getSimpleName().equals("EmptyPath")){
			this.path = path;
			return;
		}
		LinkedList<Path> pList = new LinkedList<Path>();
		if(this.path.getClass().getSimpleName().equals("ComposePath"))
			pList.addAll(((ComposePath)this.path).getPaths());
		else
			pList.add(this.path);
		if(path.getClass().getSimpleName().equals("ComposePath"))
			pList.addAll(((ComposePath)path).getPaths());
		else
			pList.add(path);
		this.path = new ComposePath(pList);	
	}


	public PathTrace clone(){
		PathTrace t = new PathTrace(path,supports);
		return t;
	}
	public boolean equals(PathTrace p){
		if(p.getPath().equals(this.getPath()))
			if(p.getSupports().equals(this.getSupports()))
				return true;
		return false;
	}
}
