package nodes;

import network.Network;
import cables.DownCableSet;

public class ActNode extends Node{

	   public ActNode(String name , Boolean isVariable) {
	        super(name,isVariable);
	    }

	    public ActNode( DownCableSet downCableSet ) {
	        super(downCableSet);
	    }

	  
}
