package set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nodes.Node;

public class PropositionNodeSet{
	private HashMap<Integer,Node>nodes;	
	private boolean isFinal;
	public PropositionNodeSet(){
		nodes = new HashMap<Integer,Node>();		
	}
	public PropositionNodeSet(HashMap<Integer,Node> nodes){
		this.nodes = nodes;		
	}
	
	public PropositionNodeSet(Node...nodes){
		this.nodes = new HashMap<Integer,Node>();
		for (Node n : nodes) 
			this.nodes.put(n.getId(), n);
	}
	public PropositionNodeSet(HashMap<Integer,Node> list,Node...nodes){
		this.nodes = list;
		for (Node n : nodes) 
			this.nodes.put(n.getId(), n);
	
	}
	public boolean isFinal(){
		return isFinal;
	}
	public void setIsFinal(boolean isFinal){
		this.isFinal = isFinal;
	}
	
	   public PropositionNodeSet union(PropositionNodeSet otherSet) {
			   
		   PropositionNodeSet result = new PropositionNodeSet();
	        result.putAll(this.nodes);
	        if(!isFinal){
	        otherSet.addAllTo(result);
		   }
	        return result;
	   }
	    public PropositionNodeSet intersection(NodeSet otherSet) {
	    	PropositionNodeSet result = new PropositionNodeSet();
	        for (Node entry : this.nodes.values()) {
	        	if(otherSet.contains(entry)){
	        		result.add(entry);
	        	}
	        }
	        if(!isFinal)
	        return result;
	        else return this;
	    }

	  public void putAll(HashMap<Integer,Node> Set){
		  if(!isFinal)
			  this.nodes.putAll(Set);
		  
	  }
	  public void addAllTo (PropositionNodeSet nodeSet){
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
		if(!isFinal)
		nodes.put(n.getId(),n);
	}
	public int size(){
		return nodes.size();
	}
	public Node remove(Node n){
		if(!isFinal)
			return nodes.remove(n.getName());
		else
			return null;
	}
	public void removeAll(){
		if(!isFinal)
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
