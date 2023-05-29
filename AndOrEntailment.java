package inferenceRules;

import java.util.HashSet;
import java.util.Set;

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



public class AndOrEntailment extends RuleNode{
	boolean sign = false;
	private int min, max, args;
	private int received=0;
	private static int pos=0;
	private static int neg=0;
public AndOrEntailment(DownCableSet r) {
	super(r);
	this.min=min;
	this.max=max;
	this.args=args;
	//this.arguments=new NodeSet();
}
public int getAndOrMin() {
	return min;
}

public int getAndOrMax() {
	return max;
}

public int getAndOrArgs() {
	return args;
}

public void setArguments(NodeSet s) {
	DownCable x;
	x=getDownCableSet().get("arguments");
    x.getNodeSet().addAllTo(s);
    
}
public void applyRuleHandler(Report report, Node signature) {
	String contextID = report.getContextName();
	RuleUseInfo rui;
	if (report.isSign()) {
		pos++;
	} else {
		neg++;
	}
	
	int rem = args-(pos+neg);
	if(rem<min && (min-pos)>rem) {
		PropositionSet propSet = report.getSupport();
		FlagNodeSet fns = new FlagNodeSet();
		fns.addFlagNode(new FlagNode(signature, propSet, 1));
		rui = new RuleUseInfo(report.getSubstitutions(),
				pos, neg, fns);
		applyRuleOnRui(rui, contextID);
	}
	
	
	
	if(pos+neg==args) {
	PropositionSet propSet = report.getSupport();
	FlagNodeSet fns = new FlagNodeSet();
	fns.addFlagNode(new FlagNode(signature, propSet, 1));
	rui = new RuleUseInfo(report.getSubstitutions(),
			pos, neg, fns);
	applyRuleOnRui(rui, contextID);
	}
}
protected void applyRuleOnRui(RuleUseInfo tRui, String contextID) {
	
	if(tRui.getPositiveCount()>=min&&tRui.getPositiveCount()<=max) {
		sign=true;
	}else if(tRui.getPositiveCount()>max||tRui.getPositiveCount()<min) {
		sign=false;
	}
	
	int rem = args-(tRui.getPositiveCount()+tRui.getNegativeCount());
	if(rem<min && (min-tRui.getPositiveCount())>rem) {
		sign=false;
	}
	
	
	Set<Integer> nodesSentReports = new HashSet<Integer>();
	for (FlagNode fn : tRui.getFlagNodeSet()) {
		nodesSentReports.add(fn.getNode().getId());
	}
	


	
	
	Substitutions sub = tRui.getSubstitutions();
	FlagNodeSet justification = new FlagNodeSet();
	for (FlagNode n: tRui.getFlagNodeSet()) {
		justification.addFlagNode(n);
	}
	//justification.add(tRui.getFlagNodeSet());
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

		for(KnownInstance report :knownInstances){
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
	//Report forwardReport = new Report(sub, supports, sign, contextID);
	Report forwardReport = new Report(sub, supports,i,true,InferenceType.FORWARD,  null);
	
	for (Channel outChannel : outgoingChannels) {
		if(!nodesSentReports.contains(outChannel.getRequesterNode().getId()))
		//outChannel.addReport(forwardReport);
		sendReport(forwardReport,outChannel);
	}
	
}
public NodeSet getDownAntNodeSet() {
	DownCableSet r =this.getDownCableSet();
	DownCable ant =r.get("ant");
     return ant.getNodeSet();
}

/**
 * Create the SIndex within the context
 * @param ContextName
 */
protected RuisHandler createRuisHandler(String contextName) {
	SIndex index = new SIndex(contextName, getSharedVarsNodes(antNodesWithVars), (byte) 0);
	return this.addContextRUIS(contextName, index);
}

public void addNotSentRui(RuleUseInfo rui, String contxt, Node signature){
	SIndex set = (SIndex) contextRuisSet.getByContext(contxt);
	if (set == null) 
		set = new SIndex(contxt, getSharedVarsNodes(antNodesWithVars), (byte) 0);
	set.insertRUI(rui);
	if(!set.getPositiveNodes().contains(signature))
		set.getPositiveNodes().add(signature);
	contextRuisSet.addHandlerSet(contxt, set);
}
/**
 * Getters of positive, negative, sign
 * used in testing
 */

public static int getPos() {
	return pos;
}

public static int getNeg() {
	return neg;
}

public boolean isSign() {
	return sign;
}

/**
 * Clear all the values
 * Used in testing
 */
public void clrAll() {
	min=0;
	max=0;
	pos=0;
	neg=0;
}

private Support getsSupport() {
	// TODO Auto-generated method stub
	return null;
}


}
