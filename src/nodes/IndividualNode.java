package nodes;

import network.Network;
import cables.DownCableSet;

public class IndividualNode extends Node {

	   public IndividualNode(String name , Boolean isVariable,Network network) {
	        super(name,isVariable, network);
	    }

	    public IndividualNode( DownCableSet downCableSet, Network network) {
	    	super(downCableSet,network);
	    }

   
}
