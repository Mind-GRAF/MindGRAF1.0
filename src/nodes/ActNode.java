package nodes;

import network.Network;
import cables.DownCableSet;

public class ActNode extends Node{

	   public ActNode(String name , Boolean isVariable, Network network) {
	        super(name,isVariable,network);
	    }

	    public ActNode( DownCableSet downCableSet , Network network) {
	        super(downCableSet,network);
	    }

	  
}
