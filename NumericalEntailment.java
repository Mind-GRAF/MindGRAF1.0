package inferenceRules;

import java.util.List;

import javax.naming.Binding;

import cables.DownCable;
import cables.DownCableSet;
import components.Support;
import matching.Substitutions;
import nodes.Node;
import requests.ChannelType;
import requests.InferenceType;
import requests.KnownInstance;
import requests.Report;
import set.NodeSet;
import set.PropositionSet;
import set.Set;
import set.SupportSet;

public class NumericalEntailment extends RuleNode{
private int i;

public NumericalEntailment(DownCableSet r) {
	super(r);
	this.i=i;
}
public NodeSet getDownAntNodeSet(){
	
	DownCableSet r =this.getDownCableSet();
	DownCable ant =r.get("ant");
  return ant.getNodeSet();

	
}

public void applyRuleHandler(Report report, Node signature) {
	String contxt = report.getContextName();
	if (report.isSign()) {
		PropositionSet propSet = report.getSupport();
		FlagNodeSet fns = new FlagNodeSet();
		fns.addFlagNode(new FlagNode(signature, propSet, 1));
		RuleUseInfo rui = new RuleUseInfo(report.getSubstitutions(),
				1, 0, fns);
		addNotSentRui(rui, contxt, signature);
	}
	int curPos = contextRuisSet.getByContext(contxt).getPositiveNodes().size();
	int n = antNodesWithoutVars.size()+antNodesWithVars.size();
	if ((curPos >= i) && ((curPos < n-i+1) || (curPos < n-1) ) )
		sendSavedRUIs(report.getContextName());
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
 * Prepares the appropriate SIndex and all its root RuleUseInfo for broadcasting  
 * @param contextID
 */
private void sendSavedRUIs(String contextID) {
	RuleUseInfo addedConstant = getConstantRUI(contextID);
	if (addedConstant == null && antNodesWithoutVars.size() != 0)
		return;

	if (antNodesWithoutVars.size() != addedConstant.getPositiveCount())
		return;

	RuleUseInfoSet ruis = ((PTree)contextRuisSet.getByContext(contextID)).getAllRootRuis();
	if (ruis == null) {
		applyRuleOnRui(addedConstant, contextID);
		return;
	}
	RuleUseInfo combined;
	for (RuleUseInfo info : ruis) {
		combined = info.combine(addedConstant);
		if (combined != null)
			applyRuleOnRui(combined, contextID);
	}
}


protected void applyRuleOnRui(RuleUseInfo Rui, String contextID) {
	if (Rui.getPositiveCount() >= i){
		Substitutions sub = Rui.getSubstitutions();
		FlagNodeSet justification = new FlagNodeSet();
		
		for (FlagNode f: Rui.getFlagNodeSet()) {
			justification.addFlagNode(f);
		}
	
		PropositionSet supports = new PropositionSet();

		for(FlagNode fn : justification){
			
		supports.addAllTo(fn.getSupport());
		
		}
	
			supports.addAllTo(Rui.getsupport());
		

		if(this.isOpen()){
			//knownInstances check this.free vars - > bound
			NodeSet freeVars = this.getFreeVariables();
			Substitutions ruiSub = Rui.getSubstitutions();
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

						//add to new support and send
						int i=1;
						Report reply = new Report(newSub, supports,i,true,InferenceType.FORWARD,  null);
						sendReportToConsequents(reply);
						return;
					}
				}
			}
		}
		
int i=1;
		Report reply = new Report(sub, supports,i,true,InferenceType.FORWARD,  null);
		sendReportToConsequents(reply);
			
	}
		}
			

public RuisHandler createRuisHandler(String contextName) {
	SIndex index = new SIndex(contextName, getSharedVarsNodes(antNodesWithVars), (byte) 0);
	return this.addContextRUIS(contextName, index);
}
public int getI() {
	return i;
}
public void setI(int i) {
	this.i = i;
}
}
