package inferenceRules;

import components.Support;
import nodes.Node;
import set.PropositionSet;
import set.SupportSet;

public class FlagNode {

private	Node node;
private PropositionSet supports;
private int flag;
public FlagNode(Node n,PropositionSet s,int flag) {
	this.node=node;
	supports= new PropositionSet();
	this.flag=flag;
	
}

public boolean isEqual(FlagNode x) {
	if(( x.node ==this.node) && (x.flag==this.flag)&&(x.supports==this.supports))
	
	return true;
	else 
		return false;
}


public Node getNode() {
	return node;
}
public void setNode(Node node) {
	this.node = node;
}
public PropositionSet getSupport() {
	return supports;
}
public void setSupport(PropositionSet support) {
	this.supports = supports;
}
public int getFlag() {
	return flag;
}
public void setFlag(int flag) {
	this.flag = flag;
}

}
