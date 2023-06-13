package paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import nodes.Node;
import context.Context;

public class OrPath extends Path{
	private LinkedList<Path> paths ;

	public OrPath() {
		paths = new LinkedList<>();
	}
	public OrPath(LinkedList<Path> paths){
		LinkedList<Path> pList = new LinkedList<Path>();
		pList.addAll(paths);
		for(int i = 0; i < pList.size()-1; i++){
			Path p = pList.get(i);
			for(int j = i+1; j < pList.size(); j++){
				if(pList.get(j).equals(p)){
					pList.remove(j);
					j--;
				}
			}
		}
		
		this.paths = pList;
	}
	public OrPath(Path... paths){
		LinkedList<Path> pList = new LinkedList<Path>(java.util.Arrays.asList(paths));
		this.paths = pList;		
	}
	public LinkedList<Path> getPaths() {
		return paths;
	}

	public void setPaths(LinkedList<Path> paths) {
		this.paths = paths;
	}


	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		if (this.paths.isEmpty())
			return new LinkedList<Object[]>();
			LinkedList<Path> pList = new LinkedList<Path>();
			pList.addAll(this.paths);
			Path p = pList.removeFirst();
			AndPath andPath = new AndPath(pList);
			if(pList.size() > 0)
				return union(p.follow(node, trace, context), andPath.follow(node, trace, context));
			else
				return p.follow(node, trace, context);

	}
	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		if (this.paths.isEmpty())
			return new LinkedList<Object[]>();
			LinkedList<Path> pList = new LinkedList<Path>();
			pList.addAll(this.paths);
			Path p = pList.removeFirst();
			AndPath andPath = new AndPath(pList);
			if(pList.size() > 0)
				return union(p.followConverse(node, trace, context), andPath.followConverse(node, trace, context));
			else
				return p.followConverse(node, trace, context);

	}
	private LinkedList<Object[]> union(LinkedList<Object[]> list1, LinkedList<Object[]> list2){
		LinkedList<Object[]> unionList = new LinkedList<Object[]>();
		unionList.addAll(list1);
		unionList.addAll(list2);
		return unionList;
	}	
	
	@Override
	public Path clone() {
		LinkedList<Path> pList = new LinkedList<Path>();
		for (Path path : this.paths)
			pList.add(path.clone());	

		return new AndPath(pList);
	}

	@Override
	public boolean equals(Object obj) {
			if (!(obj instanceof AndPath))
				return false;
			AndPath andPath = (AndPath) obj;
			if (this.paths.size() != andPath.getPaths().size())
				return false;
			
			HashMap<String, Path> HashedResult = new HashMap<String,Path>();
			for (Path path : paths) 
				HashedResult.put(path.toString(), path);
			
			for (Path path : andPath.getPaths()) {
				boolean found = false ; 
				if(HashedResult.containsKey(path.toString()))
					found = true ;
			
					if(!found) 
					return false;
			}
			return true;
		
	}
	public String toString(){
		String id = "Or Path(" ;
		int i = 1 ;
		for (Path path : paths)
			{
			id+=path.toString() + (i==paths.size() ? "": "_");
			i++;
			}
		id+=")";
		return id; 
	}

	@Override
	public Path converse() {
		LinkedList<Path> result = new LinkedList<Path>();
		
		for (Path path : paths)
			result.add(path.converse());
		
		AndPath and = new AndPath(result);
		return and;
	}

}
