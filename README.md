"# MindGRAF1.0" 

## steps for creating a Node
    !! DO NOT CREATE NODES DIRECTLY FROM THE CONSTRUCTOR !! 
    !! YOU MUST USE THE NETWORK CLASS FOR CREATING NODES !!
    Base Nodes :
        Network.createNode(String name, String SemanticType): 
        e.g.
            Node base1 = Network.createNode("base", "propositionnode"); //Proposition Node
            Node base2 = Network.createNode("base", "actnode"); //Act Node
            Node base3 = Network.createNode("base", "individualnode"); // Indv Node
    
    Variable Nodes  :
        Network.createVariableNode(String name, String SemanticType):
        e.g.
            Node X = Network.createVariableNode("X", "propositionnode");

    Molecular Nodes: 
        you need to define :

            1. downCables
                1.1. to define a down cable you need to define a Relation for it and a NodeSet of nodes having this relation with the moleclar node.
                    1.1.0. to define a Relation it's name, semanticType, adjust, and limit should be specified
                        e.g.  
                                        Relation relation = new Relation("forall", "proposition", Adjustability.EXPAND, 2);
                                        
                    1.1.1. to define a NodeSet you can pass to it's constructor a series of nodes or a hash map containing the nodes 
                        e.g.  
                                        NodeSet nodeSet = new NodeSet(base1,X,base2);
                e.g. 
                    DownCable downCable = new DownCable(relation,nodeSet) ;

            2. DownCableSet :  new DownCableSet(DownCable...downCables)
            3. Create the node through the network class with  :     
                Network.createNode(DownCableSet downCables) 

            # === A full scenario for creating the molecular node : 
            
                Node X = Network.createVariableNode("X", "propositionnode");
                Node Y = Network.createVariableNode("Y", "propositionnode");
            	Node Base = Network.createNode("base", "propositionnode");

                Relation relation = new Relation("forall", "proposition", Adjustability.EXPAND, 2);
                Relation relation2 = new Relation("super", "proposition", Adjustability.EXPAND, 2);
                    
                NodeSet nodeSet1 = new NodeSet(X,Y);
                NodeSet nodeSet2 = new NodeSet(Base);

                DownCable downCable1 = new DownCable(relation2, nodeSet1);
                DownCable downCable2 = new DownCable(relation2, nodeSet2);

                DownCableSet downCableSet = new DownCableSet(downCable1, downCable2);

                Network.createNode(downCableSet);