package nodes;

import network.Network;
import cables.DownCableSet;

public class IndividualNode extends Node {

	   public IndividualNode(String name , Boolean isVariable) {
	        super(name,isVariable);
	    }

	    public IndividualNode( DownCableSet downCableSet) {
	    	super(downCableSet);
	    }

   
}
