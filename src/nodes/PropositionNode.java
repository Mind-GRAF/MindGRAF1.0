package nodes;
import cables.DownCableSet;

public class PropositionNode extends Node {

    public PropositionNode(String name , Boolean isVariable) {
        super(name,isVariable);
    }

    public PropositionNode( DownCableSet downCableSet) {
    	super(downCableSet);
    }

    
   
  
}
