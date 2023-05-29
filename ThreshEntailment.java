package inferenceRules;

import java.util.HashSet;

import javax.naming.Binding;

import cables.DownCable;
import cables.DownCableSet;
import components.Support;
import matching.Substitutions;
import nodes.Node;
import requests.Channel;
import requests.InferenceType;
import requests.KnownInstance;
import requests.Report;
import set.NodeSet;
import set.PropositionSet;
import java.util.*;

public class ThreshEntailment extends RuleNode{
	private int min, max, args;
	private int pos=0;
	private int neg=0;
	private static boolean sign = false;
	
	public int getThreshMin() {
		return min;
	}

	public int getThreshMax() {
		return max;
	}

	public int getThreshArgs() {
		return args;
	}
	
	public void setThreshMin(int min) {
		this.min = min;
	}

	public void setThreshMax(int max) {
		this.max = max;
	}

	public void setThreshArgs(int args) {
		this.args = args;
	}


public ThreshEntailment (DownCableSet r) {
	super(r);

}
public void applyRuleHandler(Report report, Node signature) {
		
		String contextID = report.getContextName();
		RuleUseInfo rui;
		
		if(report.isSign()) {
			pos++;
		}
		if(!report.isSign()) {
			neg++;
		}
		
		int rem = args-(pos+neg);
		if(pos>min && pos<max && max-pos>rem) {
			
			PropositionSet propSet = report.getSupport();
			FlagNodeSet fns = new FlagNodeSet();
			fns.addFlagNode(new FlagNode(signature, propSet, 1));
			rui = new RuleUseInfo(report.getSubstitutions(),
					pos, neg, fns);
			applyRuleOnRui(rui, contextID);
			
		}
		
		if(neg+pos==args) {
			
			PropositionSet propSet = report.getSupport();
			FlagNodeSet fns = new FlagNodeSet();
			fns.addFlagNode(new FlagNode(signature, propSet, 1));
			rui = new RuleUseInfo(report.getSubstitutions(),
					pos, neg, fns);
			applyRuleOnRui(rui, contextID);
			
		}
		
	}

protected void applyRuleOnRui(RuleUseInfo tRui, String contextID) {
	
	if (tRui.getPositiveCount() < min || tRui.getPositiveCount()>max)
		sign = true;
	else if (tRui.getPositiveCount()>= min && tRui.getPositiveCount() <= max)
		sign = false;
	
	int rem = args-(tRui.getPositiveCount()+tRui.getNegativeCount());
	if(tRui.getPositiveCount()>min && tRui.getPositiveCount()<max && max-tRui.getPositiveCount()>rem) {
		sign=false;
	}
	
	Set<Integer> nodesSentReports = new HashSet<Integer>();
	for (FlagNode fn : tRui.getFlagNodeSet()) {
		nodesSentReports.add(fn.getNode().getId());
	}
	
	
	Substitutions sub = tRui.getSubstitutions();
	FlagNodeSet justification = new FlagNodeSet();
	for (FlagNode fx : tRui.getFlagNodeSet()) {
		justification.addFlagNode(fx);
	}
	
	PropositionSet supports = new PropositionSet();

	for(FlagNode fn : justification){
		
			supports = supports.union(fn.getSupport());
	
	}


		supports = supports.union(tRui.getsupport());
	

	if(this.isOpen()){
		//knownInstances check this.free vars - > bound
		NodeSet freeVars = this.getFreeVariables();
		Substitutions ruiSub = tRui.getSubstitutions();
		boolean allBound = true;

		for(KnownInstance report : knownInstances){
			//Bound to same thing(if bound)
			for(Node var : freeVars.getValues()){
				if(!report.getSubstitutions().isVariableBound(var)){
					allBound = false;
					break;
				}
			}
			if(allBound){//if yes
				Substitutions instanceSub = report.getSubstitutions();

			    if (ruiSub.getUnifiers().equals(instanceSub.getUnifiers())) {
		        	   allBound = false;
						
		                }
				if(allBound){
					//combine known with rui
					Substitutions newSub = new Substitutions();
					newSub.merge(instanceSub);
					newSub.merge(ruiSub);

				}
			}
		}
	}
	
	
	int i =1;
	Report forwardReport = new Report(sub, supports,i,true,InferenceType.FORWARD,  null);
	
	for (Channel outChannel : outgoingChannels) {
		if(!nodesSentReports.contains(outChannel.getRequesterNode().getId()))
		//outChannel.addReport(forwardReport);
		sendReport(forwardReport,outChannel);
	}
	
}


private Support getsSupport() {
	// TODO Auto-generated method stub
	return null;
}

protected RuisHandler createRuisHandler(String contextName) {
	SIndex index = new SIndex(contextName, getSharedVarsNodes(antNodesWithVars), (byte) 0);
	return this.addContextRUIS(contextName, index);
}


@Override
public NodeSet getDownAntNodeSet() {
	DownCableSet r =this.getDownCableSet();
	DownCable ant =r.get("arg");
  return ant.getNodeSet();
}

public boolean getSign() {
	return sign;
}


public int getPos() {
	return pos;
}

public int getNeg() {
	return neg;
}

/**
 * Clears all the variables.
 * Used in testing
 */
public void clrAll() {
	min=0;
	max=0;
	pos=0;
	neg=0;
	
}
}

