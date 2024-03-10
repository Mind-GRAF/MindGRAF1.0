package cables;

import java.util.Collection;
import java.util.HashMap;

import relations.Relation;
import set.NodeSet;
import nodes.Node;

public class UpCableSet {
	private HashMap<String, UpCable> upCables;

	public UpCableSet(HashMap<String, UpCable> cables) {
		
		this.upCables = cables;
	}

	public UpCableSet() {
		super();
		this.upCables = new HashMap<String,UpCable>();
	}

	public void updateCables(Relation r, Node Molecular) {

		try {
			((UpCable) this.get(r.getName())).addNode(Molecular);
		} catch (Exception e) {
			UpCable c = new UpCable(r,new NodeSet(Molecular));

			this.addCable(c);
		}
	}
	public boolean isEmpty(){
		return upCables.isEmpty();
	}
	public void removeNode(Relation r, Node n) {
		String relationName = r.getName();
		if (this.contains(relationName)) {
			((UpCable) this.get(relationName)).removeNode(n);

			if (this.get(relationName).getNodeSet().isEmpty()) {
				this.removeCable(relationName);
			}
		}
	}

	public void addNode(Relation r, Node n) {
		String relationName = r.getName();
		if (this.contains(relationName)) {
			((UpCable) this.get(relationName)).addNode(n);
			;
		} else {
			UpCable upCable = new UpCable(r,new NodeSet(n));
			this.addCable(upCable);
		}
	}

	public UpCable get(String key) {
		return upCables.get(key);
	}

	public void addCable(UpCable c) {
		upCables.put(c.getRelation().getName(), c);
	}

	public Cable removeCable(UpCable c) {
		return upCables.remove(c.getRelation().getName());
	}

	public Cable removeCable(String key) {
		return upCables.remove(key);
	}

	public boolean contains(Object s) {
		return this.upCables.containsKey(s) || this.upCables.containsValue(s);
	}

	public int size() {
		return upCables.size();
	}
	public  Collection<UpCable> getValues(){
		return this.upCables.values();
	}
	public String toString(){
		String result = "[";
		if(upCables!=null&& upCables.size()>0){
		int i = 0 ;
			
		for (UpCable d : upCables.values()) {
			result += d + (i==upCables.size()-1 ? "]":", ");
		}
		}else{
			result = "Up Cable Set is Empty";
		}
		return result;
	}

}
