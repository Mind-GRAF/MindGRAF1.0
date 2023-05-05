package paths;

import java.util.LinkedList;

import nodes.Node;
import context.Context;

public class ComposePath extends Path {
	private LinkedList<Path> paths ;
	public ComposePath() {
		// TODO Auto-generated constructor stub
		paths = new LinkedList<>();

	}
	public ComposePath(LinkedList<Path> paths){
		this.paths = paths;
		
	}
	public ComposePath(Path...paths){
		this.paths = new LinkedList<Path>(java.util.Arrays.asList(paths));
	}
	
	public ComposePath(LinkedList<Path> list, Path...paths){
		this.paths = new LinkedList<Path>();
		this.paths.addAll(list);
		this.paths.addAll(java.util.Arrays.asList(paths));
	}

	public LinkedList<Path> getPaths() {
		return paths;
	}
	
	public void setPaths(LinkedList<Path> paths) {
		this.paths = paths;
	}

	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		if(this.paths.isEmpty())
			return (new LinkedList<Object[]>());	

		LinkedList<Path> paths = new LinkedList<Path>(this.paths);
		Path p = paths.removeFirst();
		ComposePath cPath = new ComposePath(p);
		LinkedList<Object[]> pathList = cPath.follow(node, trace, context);
		return follow(pathList,cPath,context);
	}
	
	private LinkedList<Object[]> follow(LinkedList<Object[]> pathList, ComposePath cPath ,  Context context){
		if(cPath.getPaths().isEmpty())
			return pathList;
		LinkedList<Object[]> result = new LinkedList<Object[]>();
			for (Object[] objects : pathList) {
				Node node = (Node) objects[0];
				PathTrace pt = (PathTrace) objects[1];
				LinkedList<Object[]> fList = cPath.follow(node, pt, context);
				result.addAll(fList);		
		}
		return result;
	}


	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,Context context) {
		if(this.paths.isEmpty())
			return (new LinkedList<Object[]>());	

		LinkedList<Path> paths = new LinkedList<Path>(this.paths);
		Path p = paths.removeLast();
		ComposePath cPath = new ComposePath(p);
		LinkedList<Object[]> pathList = cPath.followConverse(node, trace, context);
		return followConverse(pathList,cPath,context);

	}
	private LinkedList<Object[]> followConverse(LinkedList<Object[]> pathList, ComposePath cPath ,  Context context){
		if(cPath.getPaths().isEmpty())
			return pathList;
		LinkedList<Object[]> result = new LinkedList<Object[]>();
			for (Object[] objects : pathList) {
				Node node = (Node) objects[0];
				PathTrace pt = (PathTrace) objects[1];
				LinkedList<Object[]> fList = cPath.followConverse(node, pt, context);
				result.addAll(fList);
				
		}
		return result;
	}

	@Override
	public Path clone() {
		return new ComposePath(paths);		
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ComposePath))
			return false;
		
		for (Path path : paths) 
			for (Path path2 : ((ComposePath)obj).getPaths()) 
				if(!path.equals(path2))
					return false;
			
		return true;
	}

	@Override
	public Path converse() {
		LinkedList<Path> lPaths = new LinkedList<Path>();
		for (Path path : paths)
			lPaths.add(path.converse());
		
		return new ComposePath(lPaths);
	}

	public String toString(){
		String id = "Compose Path(" ;
		int i = 1 ;
		for (Path path : paths)
			{
			id+=path.toString() + (i==paths.size() ? "": "_");
			i++;
			}
		id+=")";
		return id; 
	}

}
