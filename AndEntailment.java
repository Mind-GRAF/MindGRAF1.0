package inferenceRules;

import java.util.HashSet;

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

public class AndEntailment extends RuleNode{
	
public AndEntailment(DownCableSet r) {
	super(r);
}


public void applyRuleHandler(Report report, Node signature) {
	String contxt = report.getContextName();
	if (report.isSign()==true) {
		PropositionSet propSet = report.getSupport();
		FlagNodeSet fns = new FlagNodeSet();
		fns.addFlagNode(new FlagNode(signature, propSet, 1));
		RuleUseInfo rui = new RuleUseInfo(report.getSubstitutions(),
				1, 0, fns);
		//addNotSentRui(rui, contxt, signature);
	}
	if (contextRuisSet.getByContext(contxt).getPositiveNodes().size() >= getAntSize()) {}
		
		sendSavedRUIs(report.getContextName());
}
		
protected void applyRuleOnRui(RuleUseInfo Rui, String contextID) {
	if (Rui.getPositiveCount() >= getAntSize()){
		Substitutions sub = Rui.getSubstitutions();
		FlagNodeSet justification = new FlagNodeSet();
		//justification.addAll(Rui.getFlagNodeSet());
		PropositionSet supports = new PropositionSet();

		for(FlagNode fn : justification){
			
				supports = supports.union(fn.getSupport());
			} 
		

		
			supports = supports.union(Rui.getsupport());
		 

		if(this.isOpen()){
			//knownInstances check this.free vars - > bound
			NodeSet freeVars = this.getFreeVariables();
			Substitutions ruiSub = Rui.getSubstitutions();
			boolean allBound = true;

			for(KnownInstance r : knownInstances){
				//Bound to same thing(if bound)
				for(Node var : freeVars.getValues()){
					if(!r.getSubstitutions().isVariableBound(var)){
						allBound = false;
						break;
					}
				}
				if(allBound){//if yes
					Substitutions instanceSub = r.getSubstitutions();

					if (ruiSub.getUnifiers().equals(instanceSub.getUnifiers())) {
	   allBound = false;
		
                  }

				
					
					if(allBound){
						//combine known with rui
						Substitutions newSub = new Substitutions();
					
						newSub.merge(instanceSub);
						newSub.merge(ruiSub);

						//add to new support and send
						int id =1;
						Report reply = new Report(newSub, supports,id, true,InferenceType.BACKWARD, null);
						sendReportToConsequents(reply);
						return;
					}
				}
			}
			}
		
	int id2 =1;
		Report reply = new Report(sub, supports, id2, true,InferenceType.BACKWARD, null);
		sendReportToConsequents(reply);
	}
	}
	

public RuisHandler createRuisHandler(String context) {
	PTree tree = new PTree();
	NodeSet ants = antNodesWithoutVars;
	ants.addAllTo(antNodesWithVars);
	tree.buildTree(ants);
	this.addContextRUIS(context, tree);
	return tree;
}

public void addNotSentRui(RuleUseInfo rui, String contxt, Node signature){
	PTree tree = (PTree) contextRuisSet.getByContext(contxt);
	if (tree == null)
		tree = (PTree) createRuisHandler(contxt);
	tree.insertRUI(rui);
	if(!tree.getPositiveNodes().contains(signature))
		tree.getPositiveNodes().add(signature);
	contextRuisSet.addHandlerSet(contxt, tree);
}
private void sendSavedRUIs(String contextID) {
	RuleUseInfo addedConstant = getConstantRUI(contextID);
	if (addedConstant == null && antNodesWithoutVars.size() != 0)
		return;

	if( (addedConstant != null) &&(antNodesWithoutVars.size() != addedConstant.getPositiveCount()))
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


public NodeSet getDownAntNodeSet() {
	return this.getDownNodeSet("&ant");
}
private Support getSupport() {
	// TODO Auto-generated method stub
	return null;
}





}


