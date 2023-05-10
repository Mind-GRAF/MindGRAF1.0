package cables;

import java.util.Collection;
import java.util.HashMap;

import nodes.Node;



public class DownCableSet {
	private final HashMap<String,DownCable> downCables ;

	public DownCableSet(HashMap<String,DownCable> cables) {
			this.downCables=cables;
	}
	
	public DownCableSet(DownCable...cables) {
		super();
		HashMap<String,DownCable> cablesTemp = new HashMap<String,DownCable>();
		for (DownCable cable : cables) {
			cablesTemp.put(cable.getRelation().getName(),cable);
			
		}
		
		this.downCables=cablesTemp;
	}
	
	public DownCable get(String key){
		return this.downCables.get(key);
	}
	
	public boolean contains(Object s){
		return this.downCables.containsKey(s) || this.downCables.containsValue(s);
	}
	public boolean isEmpty(){
		return downCables.isEmpty();
	}
	public Collection<DownCable> getValues(){
		return this.downCables.values();
	}
	
	public int size(){
		return downCables.size();
	}
	public String toString(){
		String result = "[";
		if(downCables!=null&& downCables.size()>0){
		int i = 0 ;
		for (DownCable d : downCables.values())
			{
			result += d + (i==downCables.size()-1 ? "]":", ");
			i++;
			}
		}else{
			result = "Down Cable Set is Empty";
		}
		return result;
	}
	public String getMolecularNodeKey() {
		String result = "";
		int counter = 1;
		for (DownCable c : this.getValues()) {
		
			result += c.getRelation().getName() + (counter == this.size() ? "" : "_");
			for (Node node : c.getNodeSet().getValues()) {
				result+=node.getId();
			}
			counter++;
		}
		return result;
	}
	public String getMolecularNodeKeyWithoutVars() {
		String result = "";
		int counter = 1;
		for (DownCable c : this.getValues()) {
			result += c.getRelation().getName() + (counter == this.size() ? "" : "_");
			for (Node node : c.getNodeSet().getValues()) {
				if(node.isVariable())
					result+="_VAR_";
				else
				result+=node.getId();
			}
			counter++;
		}
		return result;
	}
}
