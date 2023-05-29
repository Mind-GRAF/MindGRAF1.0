package inferenceRules;

import java.util.HashSet;
import java.util.Iterator;

public class RuleUseInfoSet extends RuisHandler implements Iterable<RuleUseInfo> {

	
	private HashSet<RuleUseInfo> ruis;
	private String context;


	public RuleUseInfoSet() {
		ruis = new HashSet<RuleUseInfo>();
	}

	
	
	public RuleUseInfoSet(String context2, boolean b) {
		// TODO Auto-generated constructor stub
	}



	public boolean isEmpty() {
		return ruis.isEmpty();
	}


	public void add(RuleUseInfo r) {
		ruis.add(r);
	}
	   public void addset(RuleUseInfoSet r) {
	        for (RuleUseInfo rule : r) {
	            ruis.add(rule);
	        }
	    }

	

	public RuleUseInfo remove(RuleUseInfo rui) {
		ruis.remove(rui);
		return rui;
	}


	public RuleUseInfoSet combine(RuleUseInfo rui) {
		RuleUseInfoSet res = new RuleUseInfoSet();
		for (RuleUseInfo tRui : ruis) {
			RuleUseInfo tmp = rui.combine(tRui);
			if (tmp != null)
				res.add(tmp);
		}
		return res;
	}


	public RuleUseInfoSet combine(RuleUseInfoSet ruis) {
		RuleUseInfoSet res = new RuleUseInfoSet();
		for (RuleUseInfo rui : this.ruis) {
			RuleUseInfoSet temp = ruis.combine(rui);
			for (RuleUseInfo tRui : temp.ruis) {
				res.add(tRui);
			}
		}
		return res;
	}


	public boolean contains(RuleUseInfo rui) {
	    return ruis.contains(rui);
	}


	
	public int getSize() {
		return ruis.size();
	}

	public Iterator<RuleUseInfo> iterator() {
		return ruis.iterator();
	}
	public String getContext() {
		return context;
	}



	@Override
	public RuleUseInfoSet insertRUI(RuleUseInfo rui) {
		// TODO Auto-generated method stub
		return null;
	}
	
}