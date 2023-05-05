package paths;

import java.util.ArrayList;
import java.util.LinkedList;

import relations.Relation;
import caseFrames.CaseFrame;
import nodes.Node;
import context.Context;

public class CFResFUnitPath extends Path {
		private Relation relation ;
		private CaseFrame caseFrame;
		
	
	public CFResFUnitPath(Relation relation, CaseFrame caseFrame) {
		super();
		this.relation = relation;
		this.caseFrame = caseFrame;
	}
	

	public Relation getRelation() {
		return relation;
	}


	public void setRelation(Relation relation) {
		this.relation = relation;
	}


	public CaseFrame getCaseFrame() {
		return caseFrame;
	}


	public void setCaseFrame(CaseFrame caseFrame) {
		this.caseFrame = caseFrame;
	}


	@Override
	public LinkedList<Object[]> follow(Node node, PathTrace trace, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedList<Object[]> followConverse(Node node, PathTrace trace,
			Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Path converse() {
		// TODO Auto-generated method stub
		return null;
	}

}
