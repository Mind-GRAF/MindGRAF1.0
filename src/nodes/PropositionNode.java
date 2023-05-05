package nodes;


import network.Network;
import cables.DownCableSet;

public class PropositionNode extends Node {

    public PropositionNode(String name , Boolean isVariable,Network network) {
        super(name,isVariable, network);
    }

    public PropositionNode( DownCableSet downCableSet,Network network) {
    	super(downCableSet,network);
    }

    
   
  
}
