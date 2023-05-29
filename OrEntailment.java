package inferenceRules;

import cables.DownCable;
import cables.DownCableSet;
import nodes.Node;
import requests.Report;
import set.NodeSet;
import set.PropositionSet;
import requests.Channel;
import requests.ChannelType;
import requests.InferenceType;

public class OrEntailment extends RuleNode {
	
 ChannelType AntRule ;

	boolean sign = false;
	private int ant,cq;
public OrEntailment(DownCableSet r) {
	super(r);
}
public void applyRuleHandler(Report report, Node node) {
	
	if(report.isSign()) {
		
	boolean	sign = true;
		
		PropositionSet propSet = report.getSupport();
		FlagNodeSet fns = new FlagNodeSet();
		fns.addFlagNode(new FlagNode(node, propSet, 1));
		int i=1;

		Report reply = new Report(report.getSubstitutions(), propSet,  i,sign,InferenceType.FORWARD, null);
		
		for (Channel outChannel : outgoingChannels)
			sendReport(report,outChannel);
		
	}
	

}
public NodeSet getDownAntNodeSet() {
	DownCableSet r =this.getDownCableSet();
	DownCable ant =r.get("ant");
  return ant.getNodeSet();
}

@Override
protected void applyRuleOnRui(RuleUseInfo tRui, String contextID) {
	
}

@Override
protected RuisHandler createRuisHandler(String contextName) {
	// TODO Auto-generated method stub
	return null;
}

public boolean getReply() {
	return sign;
}
}
