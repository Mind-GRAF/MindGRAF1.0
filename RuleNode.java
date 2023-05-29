package inferenceRules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.naming.Binding;

import cables.DownCable;
import cables.DownCableSet;
import context.Context;
import matching.Substitutions;
import network.Controller;
import network.Network;
import nodes.Node;
import nodes.PropositionNode;
import requests.Channel;
import requests.ChannelType;
import requests.Report;
import requests.Request;
import requests.RuleToConsequentChannel;
import requests.Scheduler;
import set.NodeSet;
import set.PropositionSet;
import set.Set;

public abstract class RuleNode extends PropositionNode {
	private NodeSet consequents;
	protected NodeSet antecedents;
	protected NodeSet antNodesWithVars;
	protected NodeSet antNodesWithoutVars;

	protected Collection<Integer> antNodesWithVarsIDs;
	protected Collection<Integer> antNodesWithoutVarsIDs;
	protected boolean shareVars;
	protected NodeSet sharedVars;
	protected ContextRuisSet contextRuisSet;
	private Hashtable<Context, RuleUseInfo> contextConstantRUI;
    

	public RuleNode(DownCableSet r){
		super(r);
		consequents = new NodeSet();
		antecedents = new NodeSet();
		antNodesWithoutVars = new NodeSet();
		antNodesWithoutVarsIDs = new HashSet<Integer>();
		antNodesWithVars = new NodeSet();
		antNodesWithVarsIDs = new HashSet<Integer>();
		shareVars = false;
		sharedVars = new NodeSet();
		contextRuisSet = new ContextRuisSet();
		contextConstantRUI = new Hashtable<Context, RuleUseInfo>();
	}
	public NodeSet getSharedVarsNodes(NodeSet nodes) {
		NodeSet res = new NodeSet();
		NodeSet temp = new NodeSet();
		if (nodes.isEmpty())
			return res;

		for(Node curNode : nodes.getValues()){
			if(curNode.isVariable()==true){
				if(temp.contains(curNode))
					res.add(curNode);
				else
					temp.add( curNode);
			}
			if (curNode.isOpen())
			
			{
				NodeSet free = curNode.getFreeVariables();
				for(Node var : free.getValues())
					if(temp.contains(var))
						res.add(var);
					else
						temp.add(var);
			}
		}
		return res;
	}
	
  

	protected void processNodes(NodeSet antNodes) {
		this.splitToNodesWithVarsAndWithout(antNodes, antNodesWithVars, antNodesWithoutVars);
		for (Node n : antNodesWithVars.getValues()) {
			antNodesWithVarsIDs.add(n.getId());
		}
		for (Node n : antNodesWithoutVars.getValues()) {
			antNodesWithoutVarsIDs.add(n.getId());
		}
		this.shareVars = this.allShareVars(antNodesWithVars);
		sharedVars = getSharedVarsNodes(antNodesWithVars);
	}

    public void splitToNodesWithVarsAndWithout(NodeSet allNodes, NodeSet withVars, NodeSet withoutVars) {

		for (Node n: allNodes.getValues()) {
			if (n.isVariable())
				withVars.add(n);
			else
				withoutVars.add(n);
		}
	}
	public boolean allShareVars(NodeSet nodes) {
		if (nodes.isEmpty())
			return false;

		
		boolean res = true;
		for (Node node : nodes.getValues()) {
			for (Node node2 : nodes.getValues()) {
				if(! node.getFreeVariables().equals(node2.getFreeVariables())){
					return false;
				}
			}
		}
		return res;
	}
	public int getAntSize(){
		return antNodesWithoutVars.size() + antNodesWithVars.size();
	}
	public RuleUseInfo getConstantRui(Context con) {
		RuleUseInfo tRui = contextConstantRUI.get(con);
		return tRui;
	}
	public RuleUseInfo getConstantRUI(String context) {
		return contextConstantRUI.get(context);
	}


	public void applyRuleHandler(Report report, Channel currentChannel) {
		Node currentChannelReporter = currentChannel.getRequesterNode();
		String contextID = currentChannel.getContextName();
		RuleUseInfo rui;
		if (report.isSign()==true) {
			FlagNode fn = new FlagNode(currentChannelReporter, report.getSupport(), 1);
			FlagNodeSet fns = new FlagNodeSet();
			fns.addFlagNode(fn);
			rui = new RuleUseInfo(report.getSubstitutions(), 1, 0, fns);
		} else {
			FlagNode fn = new FlagNode(currentChannelReporter, report.getSupport(), 2);
			FlagNodeSet fns = new FlagNodeSet();
			fns.addFlagNode(fn);
			rui = new RuleUseInfo(report.getSubstitutions(), 0, 1, fns);
		}
	

	}
	public void applyRuleHandler(Report report, Node signature) {
		String contextID = report.getContextName();
		RuleUseInfo rui;
		if (report.isSign()==true) {
			PropositionSet propSet = report.getSupport();
			FlagNodeSet fns = new FlagNodeSet();
			fns.addFlagNode(new FlagNode(signature, propSet, 1));
			rui = new RuleUseInfo(report.getSubstitutions(),
					1, 0, fns);
		} else {
			PropositionSet propSet = report.getSupport();
			FlagNodeSet fns = new FlagNodeSet();
			fns.addFlagNode(new FlagNode(signature, propSet, 2));
			rui = new RuleUseInfo(report.getSubstitutions(), 0, 1, fns);
		}
		RuisHandler crtemp = contextRuisSet.getByContext(contextID);
		if(crtemp == null){
			crtemp = addContextRUIS(contextID);
		}

		RuleUseInfoSet res = crtemp.insertRUI(rui);
		if (res == null)
			res = new RuleUseInfoSet();
		for (RuleUseInfo tRui : res) {
			applyRuleOnRui(tRui, contextID);
		}
	}
	
	abstract protected void applyRuleOnRui(RuleUseInfo tRui, String contextID);
	
	public void clear() {
		contextRuisSet.clear();
		contextConstantRUI.clear();
	}

	public RuisHandler addContextRUIS(String contextName) {
		if (sharedVars.size() != 0) {
			SIndex si = null;
			if (shareVars)
				si = new SIndex(contextName, sharedVars, SIndex.SINGLETONRUIS);
			else
				si = new SIndex(contextName, sharedVars, getSIndexContextType());
			return this.addContextRUIS(si);
		} else {
			return this.addContextRUIS(contextName,createRuisHandler(contextName));
		}
	}
	private RuleUseInfoSet addContextRUIS(SIndex si) {
		// TODO Auto-generated method stub
		return null;
	}
	protected byte getSIndexContextType() {
		return SIndex.RUIS;
	}
	
	
	protected abstract RuisHandler createRuisHandler(String contextName);
	
	public RuisHandler addContextRUIS(String cntxt, RuisHandler cRuis) {
		return this.contextRuisSet.addHandlerSet(cntxt, cRuis);
	}
	

	public RuleUseInfo addConstantRuiToContext(String context, RuleUseInfo rui) {
		Context contxt = (Context) Controller.getContext(context);
		RuleUseInfo tRui = contextConstantRUI.get(contxt);
		if (tRui != null)
			tRui = rui.combine(tRui);
		else
			tRui = rui;
		if (tRui == null)
			throw new NullPointerException(
					"The existed RUI could not be merged " + "with the given rui so check your code again");
		contextConstantRUI.put(contxt, tRui);
		return tRui;
	}
	public static boolean isConstantNode(Node n) {
		return !(n.isMolecular() || n.isMolecular());
	}



    // Getter and setter methods for instance variables
    public NodeSet getConsequents() {
    		DownCable x;
    	x=getDownCableSet().get("cosequents");
        antecedents=x.getNodeSet();
        return consequents;
    }

    public void setConsequents(NodeSet consequents) {
    	DownCable x;
    	x=getDownCableSet().get("consequents");
        x.getNodeSet().addAllTo(consequents);;
    }

    public NodeSet getAntecedents() {
        
    	DownCable x;
    	x=getDownCableSet().get("antecedents");
        antecedents=x.getNodeSet();
        return antecedents;

    }

    public void setAntecedents(NodeSet antecedents) {
    	DownCable x;
    	x=this.getDownCableSet().get("antecedents");
        x.getNodeSet().addAllTo(antecedents);;
    }
    

		//	} else {
				//super.processSingleReports(currentChannel
	
	protected abstract NodeSet getDownAntNodeSet();


	
	public NodeSet getDownNodeSet(String name) {
		if(this.isMolecular())
			return this.getDownCableSet().get("ant").getNodeSet();
		return null;
	}
	protected void sendReportToConsequents(Report reply) {
			newInstances.addKnownInstance(reply);
		for (Channel outChannel : outgoingChannels)
			if(outChannel instanceof RuleToConsequentChannel)
				sendReport(reply,outChannel);
		
		
	}
	public void addAntecedent(Node ant){
		if(ant.isVariable()|| ant.isOpen())
			antNodesWithVars.add(ant);
		else
			antNodesWithoutVars.add(ant);
	}
	public RuisHandler getContextRuiHandler(String cntxt) {
		return contextRuisSet.getByContext(cntxt);
	}
	

	protected RuleUseInfoSet createContextRUISNonShared(String contextName) {
		return new RuleUseInfoSet(contextName, false);
	}
	
	/***
     * Report handling in Rule proposition nodes.
     */
    public void processReports() {
        Report reportHasTurn = Scheduler.getHighQueue().poll();
        try {
            processSingleReports(reportHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    public void processRequests() {
        Request requestHasTurn = Scheduler.getLowQueue().poll();
        try {
            processSingleRequests(requestHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }





//	public ContextRuisSet getContextRUISSet() {
//		return contextRuisSet;
//	}


}
    
