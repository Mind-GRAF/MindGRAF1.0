package set;
import java.util.HashSet;


import network.Network;
import nodes.Node;

public class PropositionNodeSet{
	private HashSet<Integer>nodes;	
	private boolean isFinal;
	public PropositionNodeSet(){
		nodes = new HashSet<Integer>();		
	}
	public PropositionNodeSet(HashSet<Integer> nodes){
		this.nodes = nodes;		
	}
	
	public PropositionNodeSet(Integer...nodeIDs){
		this.nodes = new HashSet<Integer>();
		for (Integer id : nodeIDs) 
			this.nodes.add(id);
	}
	public PropositionNodeSet(HashSet<Integer> list,Integer...nodeIDs){
		this.nodes = list;
		for (Integer id : nodeIDs) 
		this.nodes.add(id);
	
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
	        for (Integer entry : this.nodes) {
	        	if(otherSet.contains(entry)){
	        		result.add(entry);
	        	}
	        }
	        if(!isFinal)
	        return result;
	        else return this;
	    }

	  public void putAll(HashSet<Integer> Set){
		  if(!isFinal)
			  this.nodes.addAll(Set);
		  
	  }
	  public void addAllTo (PropositionNodeSet nodeSet){
		  nodeSet.putAll(this.nodes);
	  }
	public String toString (){
		String s = "[";
		int i = 1 ;
		for (int n : nodes) {
			s+=n+ (i==nodes.size() ?"":",");
			i++;
		} 
		s+="]";
		return s;
		
	}
	public void add(int id){
		if(!isFinal)
		nodes.add(id);
	}
	public int size(){
		return nodes.size();
	}
	public boolean remove(int i){
		if(!isFinal)
			return nodes.remove(i);
		else
			return false;
	}
	public void removeAll(){
		if(!isFinal)
		nodes.clear();
	}
	public HashSet<Integer> getValues(){
		return this.nodes;
	}
	public boolean isEmpty(){
		return this.nodes.size()==0;
	}
	public Node get(int id){
		return Network.getNodeById(id);
	}
	public boolean contains(Object s){
		return this.nodes.contains(s);
	}

	public boolean equals(PropositionNodeSet n){ 
		return this.getValues().equals(n.getValues());
	}
}
