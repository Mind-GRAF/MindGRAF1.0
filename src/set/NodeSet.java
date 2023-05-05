package set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nodes.Node;

public class NodeSet{
	private HashMap<String,Node>nodes;
	
	public NodeSet(){
		nodes = new HashMap<String,Node>();		
	}
	public NodeSet(HashMap<String,Node> nodes){
		this.nodes = nodes;		
	}
	
	public NodeSet(Node...nodes){
		this.nodes = new HashMap<String,Node>();
		for (Node n : nodes) 
			this.nodes.put(n.getName(), n);
	}
	public NodeSet(HashMap<String,Node> list,Node...nodes){
		this.nodes = list;
		for (Node n : nodes) 
			this.nodes.put(n.getName(), n);
	
	}
	   public NodeSet union(NodeSet otherSet) {
		   NodeSet result = new NodeSet();
	        result.putAll(this.nodes);
	        otherSet.addAllTo(result);
	        return result;
	   }
	    public NodeSet intersection(NodeSet otherSet) {
	        NodeSet result = new NodeSet();
	        for (Node entry : this.nodes.values()) {
	        	if(otherSet.contains(entry)){
	        		result.add(entry);
	        	}
	        }
	        
	        return result;
	    }

	  public void putAll(HashMap<String,Node> Set){
		  this.nodes.putAll(Set);
		  
	  }
	  public void addAllTo (NodeSet nodeSet){
		  nodeSet.putAll(this.nodes);
	  }
	public String toString (){
		String s = "[";
		int i = 1 ;
		for (Node n : nodes.values()) {
			s+=n.getName()+ (i==nodes.values().size() ?"":",");
			i++;
		} 
		s+="]";
		return s;
		
	}
	public void add(Node n){
		nodes.put(n.getName(),n);
	}
	public int size(){
		return nodes.size();
	}
	public Node remove(Node n){
		return nodes.remove(n.getName());
	}
	public void removeAll(){
		nodes.clear();
	}
	public Collection<Node> getValues(){
		return this.nodes.values();
	}
	public boolean isEmpty(){
		return this.nodes.size()==0;
	}
	public Node get(String name){
		return nodes.get(name);
	}
	public boolean contains(Object s){
		return this.nodes.containsKey(s) || this.nodes.containsValue(s);
	}
	public ArrayList<String> getNames(){
		ArrayList<String> result = new ArrayList<String>(); 
		for (Node node : this.nodes.values()) {
			result.add(node.getName());
		}
		return result;
	}
	public boolean equals(NodeSet n){
		return this.getNames().equals(n.getNames());
	}
}
