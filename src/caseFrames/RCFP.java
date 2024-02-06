package caseFrames;

import relations.Relation;

public class RCFP {
	
	private Relation relation;
	private Adjustability adjust;
	private int limit;
	
	public RCFP(Relation relation, Adjustability adjust, int limit) {
		super();
		this.relation = relation;
		this.adjust = adjust;
		this.limit = limit;
	}
	

	public Relation getRelation() {
		return relation;
	}

	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	public Adjustability getAdjust() {
		return adjust;
	}
	
	public void setAdjust(Adjustability adjust) {
		this.adjust = adjust;
	}

	public int getLimit() {
		return limit;
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	}
