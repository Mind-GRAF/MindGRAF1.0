package relations;

import caseFrames.Adjustability;
import network.Network;
import paths.*;
public class Relation {
	
	private String  name;
	private String  type ;
	private Adjustability adjust;
	private int     limit;
	private Path    path;
	private boolean isQuantifier;
	
	public Relation(String name, String type, Adjustability adjust, int limit) {
		super();
		
		this.name 	= name;
		this.type 	= type;
		this.adjust = adjust;
		this.limit 	= limit;
		this.path 	= null;
		
		if(Network.quantifiers.containsValue(name)){
			isQuantifier = true;
		
		}
//		Network.getRelations().put(name, this);
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public boolean isQuantifier() {
		return isQuantifier;
	}
	
	
	public void setQuantifier(boolean isQuantifier) {
		this.isQuantifier = isQuantifier;
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public Adjustability getAdjust() {
		return adjust;
	}
	public int getLimit() {
		return limit;
	}	
	public String toString(){
		return name;
	}
	public boolean equals(Relation r){
		return r.getName().equals(this.getName())  ;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setAdjust(Adjustability adjust) {
		this.adjust = adjust;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}

}
