package nodes;

import network.Network;
import cables.DownCableSet;

public class RuleNode extends PropositionNode {

	public RuleNode(String name, Boolean isVariable) {
		super(name, isVariable);
		// TODO Auto-generated constructor stub
	}
	public RuleNode(DownCableSet downCableSet) {
		super(downCableSet);
		this.name =  "P" + (Network.MolecularCount) ;
		// TODO Auto-generated constructor stub
	}
	

	

}
