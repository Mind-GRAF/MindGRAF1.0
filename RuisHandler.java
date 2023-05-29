package inferenceRules;

import set.NodeSet;

//
public abstract class RuisHandler {
	protected String context;
	private NodeSet positiveNodes;

	public RuisHandler(){
		positiveNodes = new NodeSet();
	}
	public RuisHandler(String contextID) {
		this();
		this.context = contextID;
	}

	public String getContext() {
		return context;
	}

	abstract public RuleUseInfoSet insertRUI(RuleUseInfo rui);
	public NodeSet getPositiveNodes() {
		return positiveNodes;
	}
	public void setPositiveNodes(NodeSet positiveNodes) {
		this.positiveNodes = positiveNodes;
	}
}