package network;

import java.util.HashMap;
import java.util.LinkedList;

import paths.AndPath;
import paths.ComposePath;
import paths.FUnitPath;
import paths.KPlusPath;
import paths.Path;
import paths.PathTrace;
import relations.Relation;
import set.NodeSet;
import cables.DownCable;
import cables.DownCableSet;
import caseFrames.Adjustability;
import components.MyClassCreator;
import context.Context;
import nodes.ActNode;
import nodes.IndividualNode;
import nodes.MolecularType;
import nodes.Node;
import nodes.PropositionNode;

public class Network {
	private static HashMap<Integer, Node> nodes;
	private static HashMap<String, Node> molecularNodes;
	private static HashMap<String, Node> baseNodes;
	private static HashMap<String,Relation> relations; 
	private static HashMap<Integer,PropositionNode> propositionNodes ; 
	private static MyClassCreator classCreator;
	public static HashMap<String, String> quantifiers = new HashMap<String, String>();

	public Network() {
		nodes = new HashMap<Integer, Node>();
		molecularNodes = new HashMap<String, Node>();
		classCreator = new MyClassCreator();
		baseNodes = new HashMap<String,Node>();
		propositionNodes = new HashMap<Integer,PropositionNode>();
	}
	
	// first constructor for molecular nodes
	public static Node createNode(String SemanticType,DownCableSet downCableSet) {
		if(downCableSet.size()==0){
			return null;
		}
		String downCablesKey = downCableSet.getMolecularNodeKey();
		Node node;
	
		if (!molecularNodes.containsKey(downCablesKey)) {
			
			switch (SemanticType.toLowerCase()) {
			case "propositionnode":
			
				PropositionNode pnode = new PropositionNode(downCableSet);
				node = new PropositionNode(downCableSet);
				propositionNodes.put(Integer.valueOf(pnode.getId()),pnode);
				break;
			
			case "actnode":
				node = new ActNode(downCableSet);
				break;
			case "individualnode":
				node = new IndividualNode(downCableSet);
				break;
			default:
				System.out.println("Invalid node type.");
				return null;
			}
			
			node.fetchFreeVariables();
			if(node.getMolecularType()==MolecularType.CLOSED){
				
				for (Node molecular : molecularNodes.values()) {
					String MolecularKey = molecular.getDownCableSet().getMolecularNodeKeyWithoutVars();
					String newNodeKey   = downCableSet.getMolecularNodeKeyWithoutVars();
					if(MolecularKey.equals(newNodeKey)){
						return molecular;
					}
				}
				}
			nodes.put(node.getId(), node);
			molecularNodes.put(downCablesKey, node);
			return node;
		}
		return molecularNodes.get(downCablesKey);

	}
	// second constructor for base nodes
	public static Node createNode(String name, String SemanticType) {
		Node node;
		switch (SemanticType.toLowerCase()) {
		case "propositionnode":
			node = new PropositionNode(name, false);
			propositionNodes.put(node.getId(),(PropositionNode)node);
			break;
		case "actnode":
			node = new ActNode(name, false);
			break;
		case "individualnode":
			node = new IndividualNode(name, false);
			break;
		default:
			System.out.println("Invalid node type.");
			return null;
		}
		if(node!=null){	
			if(nodes.containsKey(node.getId()))
				return nodes.get(node.getId());
		
			nodes.put(node.getId(), node);
			if( node.isBase())
				baseNodes.put(node.getName(), node);
		}
		
		return node;
	}
	// third constructor for base nodes
		public static Node createVariableNode(String name, String SemanticType) {
			Node node;
			switch (SemanticType.toLowerCase()) {
			case "propositionnode":
				node = new PropositionNode(name, true);
				propositionNodes.put(node.getId(),(PropositionNode)node);
				break;
			case "actnode":
				node = new ActNode(name, true);
				break;
			case "individualnode":
				node = new IndividualNode(name, true);
				break;
			default:
				System.out.println("Invalid node type.");
				return null;
			}
			if(node!=null){	
				if(nodes.containsKey(node.getId()))
					return nodes.get(node.getId());	
				nodes.put(node.getId(), node);
			}
			
			return node;
		}
		
	public static HashMap<String, Relation> getRelations() {
			return relations;
		}

		public static void setRelations(HashMap<String, Relation> relations) {
			Network.relations = relations;
		}

	public static Node getNodeById(int id) {
		return nodes.get(id);
	}

	public static boolean isAssignable(Node parent, Node child) {
		if ((parent.getClass()).isAssignableFrom(child.getClass()))
			return true;
		return false;

	}
	public static HashMap<Integer, Node> getNodes() {
		return nodes;
	}

	public void setNodes(HashMap<Integer, Node> Nodes) {
		nodes = Nodes;
	}

	public static HashMap<String, Node> getMolecularNodes() {
		return molecularNodes;
	}

	public static void setMolecularNodes(HashMap<String, Node> MolecularNodes) {
		molecularNodes = MolecularNodes;
	}

	public static HashMap<String, Node> getBaseNodes() {
		return baseNodes;
	}

	public static void setBaseNodes(HashMap<String, Node> BaseNodes) {
		baseNodes = BaseNodes;
	}

	public static HashMap<String, String> getQuantifiers() {
		return quantifiers;
	}
	public static HashMap<Integer,PropositionNode> getPropositionNodes(){
		return propositionNodes;
	}

	public static void setQuantifiers(HashMap<String, String> quantifiers) {
		Network.quantifiers = quantifiers;
	}
	public Node find(DownCableSet downCableSet){
		String key = downCableSet.getMolecularNodeKey();
		return molecularNodes.get(key);
	}
	public void printNodes(){
		String result = "";
		for (Node node : nodes.values()) {
			result += node.toString() +"\n";
		}
		System.out.println( result);
	}

	public static void main(String[] args) throws ClassCastException {
		Network Net = new Network();
//		Node Z = createvariableNode("Z", "propositionnode");
//		Node Y = createvariableNode("Y", "propositionnode");
//		Node X = createvariableNode("X", "propositionnode");
//		Node Base = createNode("base", "propositionnode");
//		quantifiers.put("forall","forall");
//		
//		Relation relation = new Relation ("forall", "", Adjustability.EXPAND, 2);
//		Relation relation2 = new Relation("b", "", Adjustability.EXPAND, 2);
//		
//		NodeSet nodeSetX = new NodeSet();
//		NodeSet nodeSetZ = new NodeSet();
//		NodeSet nodeSetXZ = new NodeSet();
//		NodeSet nodeSetY = new NodeSet();
//
//		nodeSetZ.add(Z);
//		nodeSetX.add(X);
//		nodeSetXZ.add(X);
//		nodeSetXZ.add(Z);
//		nodeSetY.add(Y);
//		
//		// M0
//		DownCable d2 = new DownCable(relation2, nodeSetXZ);
//		HashMap<String, DownCable> Cables = new HashMap<>();
//		Cables.put(d2.getRelation().getName(), d2);
//		DownCableSet downCableSet = new DownCableSet(Cables);
//		Node M0 = createNode("propositionnode", downCableSet);
//
//		//M1 
//		NodeSet nodeSetM0 = new NodeSet();
//			nodeSetM0.add(M0);
//		DownCable d = new DownCable(relation, nodeSetZ);
//		DownCable d3 = new DownCable(relation2,nodeSetM0);
//		HashMap<String, DownCable> Cables2 = new HashMap<>();
//			Cables2.put(d3.getRelation().getName(), d3);
//			Cables2.put(d.getRelation().getName(), d);
//		DownCableSet downCableSet2 = new DownCableSet(Cables2);		
//		Node M1 = createNode("propositionnode", downCableSet2);
//		
//		NodeSet nodeSetM1 = new NodeSet();
//		nodeSetM1.add(M1);
//		
//		//M2 
//		DownCable dM2 = new DownCable(relation2, nodeSetY.union(nodeSetM1));
//			HashMap<String, DownCable> CablesM2 = new HashMap<>();
//				CablesM2.put(dM2.getRelation().getName(), dM2);
//			DownCableSet downCableSetM2 = new DownCableSet(CablesM2);		
//			Node M2 = createNode("propositionnode", downCableSetM2);
//	
//			//M3 
//			DownCable dM3 = new DownCable(relation2, nodeSetX.union(nodeSetZ.union(nodeSetY)));
//			
//				HashMap<String, DownCable> CablesM3 = new HashMap<>();
//					CablesM3.put(dM3.getRelation().getName(), dM3);
//					
//				DownCableSet downCableSetM3 = new DownCableSet(CablesM3);		
//				Node M3 = createNode("propositionnode", downCableSetM3);
//		
//				//M4 
//				NodeSet nodeSetM3 = new NodeSet();
//				nodeSetM3.add(M3);
//				NodeSet nodeSetM2 = new NodeSet();
//				nodeSetM3.add(M2);
//				
//				NodeSet nodeSetM23 = new NodeSet();
//				nodeSetM23.add(M2);
//				nodeSetM23.add(M3);
//				
//				DownCable dM4 = new DownCable(relation, nodeSetX);
//				DownCable dM4_2 = new DownCable(relation2,nodeSetM23);
////				DownCable dM4_3 = new DownCable(relation2,nodeSetM3);
//				
//					HashMap<String, DownCable> CablesM4 = new HashMap<>();
//						CablesM4.put(dM4.getRelation().getName(), dM4);
//						CablesM4.put(dM4_2.getRelation().getName()+1, dM4_2);
//						
//					DownCableSet downCableSetM4 = new DownCableSet(CablesM4);		
//					Node M4 = createNode("propositionnode", downCableSetM4);
//					
//		System.out.println("-----------------------------------------------------");
//		System.out.println("free vars" + M4.getFreeVariables());
//		
//		
//		Substitution s = new Substitution(Base, Z);
//		ArrayList<Substitution> substitutionArr = new ArrayList<>();
//		substitutionArr.add(s);
//		System.out.println(M4);
////		System.out.println(M4.clone());
//		
//		System.out.println(M4.substitute(substitutionArr));
//		printNodes();
		
// ======================================================================================================================
		
//		Node X = createvariableNode("X", "propositionnode");
//		Node Y = createvariableNode("Y", "propositionnode");
//		Node Base = createNode("base", "propositionnode", false);
//		Node Bob = createNode("bob", "propositionnode", false);
//		
//		quantifiers.put("forall","forall");
//		
//		Relation Qrelation = new Relation ("forall", "", Adjustability.EXPAND, 2);
//		Relation relation = new Relation("relation", "", Adjustability.EXPAND, 2);
//
//		NodeSet nodeSetX = new NodeSet();
//		NodeSet nodeSetY = new NodeSet();
//
//		nodeSetX.add(X);;
//		nodeSetY.add(Y);
//		
//		// M0
//		DownCable d2 = new DownCable(relation, nodeSetX.union(nodeSetY));
//		HashMap<String, DownCable> Cables = new HashMap<>();
//		Cables.put(d2.getRelation().getName(), d2);
//		DownCableSet downCableSet = new DownCableSet(Cables);
//		Node M0 = createNode("propositionnode", downCableSet);
//
//		//M1 
//		NodeSet nodeSetM0 = new NodeSet();
//		nodeSetM0.add(M0);
//		DownCable d = new DownCable(relation, nodeSetM0.union(nodeSetY));	
//		HashMap<String, DownCable> Cables2 = new HashMap<>();
//			Cables2.put(d.getRelation().getName(), d);
//		DownCableSet downCableSet2 = new DownCableSet(Cables2);		
//		Node M1 = createNode("propositionnode", downCableSet2);
//		
//		NodeSet nodeSetM1 = new NodeSet();
//		nodeSetM1.add(M1);
//		
//		//M2 
//		NodeSet nodeSetBob = new NodeSet(); nodeSetBob.add(Bob);
//		DownCable dM2 = new DownCable(relation, nodeSetY.union(nodeSetBob));
//			HashMap<String, DownCable> CablesM2 = new HashMap<>();
//				CablesM2.put(dM2.getRelation().getName(), dM2);
//			DownCableSet downCableSetM2 = new DownCableSet(CablesM2);		
//			Node M2 = createNode("propositionnode", downCableSetM2);
//			
//			//M3 
//			NodeSet NodeSetM2 = new NodeSet(); NodeSetM2.add(M2);
//			DownCable dM3   = new DownCable(Qrelation, (nodeSetY));
//			DownCable dM3_2 = new DownCable(relation, (nodeSetM1.union(NodeSetM2)));
//			
//				HashMap<String, DownCable> CablesM3 = new HashMap<>();
//					CablesM3.put(dM3.getRelation().getName(), dM3);
//					CablesM3.put(dM3_2.getRelation().getName(), dM3_2);
//					
//				DownCableSet downCableSetM3 = new DownCableSet(CablesM3);		
//				Node M3 = createNode("propositionnode", downCableSetM3);
//		
//				//M4 
//				NodeSet nodeSetM3 = new NodeSet();
//				nodeSetM3.add(M3);
//				
//				NodeSet nodeSetM03 = new NodeSet();
//				nodeSetM03.add(M0);
//				nodeSetM03.add(M3);
//				
//				DownCable dM4_2 = new DownCable(relation,nodeSetM03);
////				DownCable dM4_3 = new DownCable(relation2,nodeSetM3);
//				
//					HashMap<String, DownCable> CablesM4 = new HashMap<>();
//						CablesM4.put(dM4_2.getRelation().getName()+1, dM4_2);
//						
//					DownCableSet downCableSetM4 = new DownCableSet(CablesM4);		
//					Node M4 = createNode("propositionnode", downCableSetM4);
//					
//		System.out.println("-----------------------------------------------------");
//		System.out.println("free vars" + M4.getFreeVariables());
//		
//		
//		Substitution s = new Substitution(Base, Y);
//		ArrayList<Substitution> substitutionArr = new ArrayList<>();
//		substitutionArr.add(s);
//		System.out.println(M4);
//		System.out.println(M4.clone());

//		System.out.println(M4.substitute(substitutionArr));
//		printNodes();
// =============================================================================================================
		Node cs = createNode("cs", "propositionnode");
		Node fun = createNode("fun", "propositionnode");
		Node mary = createNode("mary", "propositionnode");
		Node believe = createNode("believe", "propositionnode");
		Node bob = createNode("bob", "propositionnode");
		Node know = createNode("know", "propositionnode");
			
		Relation agent = new Relation ("agent", "", Adjustability.EXPAND, 2);
		Relation act = new Relation ("act", "", Adjustability.EXPAND, 2);
		Relation obj = new Relation ("obj", "", Adjustability.EXPAND, 2);
		Relation prop = new Relation ("prop", "", Adjustability.EXPAND, 2);
		
		DownCable d1 = new DownCable(obj,new NodeSet(cs));
		DownCable d2 = new DownCable(prop,new NodeSet(fun));
		

		
		
		Node M1 = createNode("propositionnode", new DownCableSet(d1,d2));
				
		DownCable d3 = new DownCable(obj,new NodeSet(M1));
		DownCable d4 = new DownCable(act,new NodeSet(believe));
		DownCable d5 = new DownCable(agent,new NodeSet(mary));
		
		Node M2 = createNode("propositionnode", new DownCableSet(d3,d4,d5));
		
		
		DownCable d6 = new DownCable(obj,new NodeSet(M2));
		DownCable d7 = new DownCable(act,new NodeSet(know));
		DownCable d8 = new DownCable(agent,new NodeSet(bob));
		
		Node M3 = createNode("propositionnode", new DownCableSet(d6,d7,d8));
		
		FUnitPath p1 = new FUnitPath(agent);
		FUnitPath p2 = new FUnitPath(act);
		FUnitPath p3 = new FUnitPath(obj);
		
		ComposePath pCompose = new ComposePath(p2,p3);
		
		FUnitPath pF4 = new FUnitPath(agent);
		FUnitPath pF5 = new FUnitPath(act);
		FUnitPath pF6 = new FUnitPath(obj);
		
		ComposePath pCompose2 = new ComposePath(pF5,pF4);
		
		
//		LinkedList <Object[]> s  = p3.follow(M3,new PathTrace(),new Context());
//		Path p4 = new KPlusPath(p3);
//		LinkedList <Object[]> s2  = p4.follow(M3,new PathTrace(),new Context());
		
//		for (Object[] object : s) {
//			System.out.println(object[0]);
//		}
		
		AndPath and  = new AndPath(pCompose);
		AndPath and2  = new AndPath(pCompose2);
		
		System.out.println(and.equals(and2));

		
		
	}

}
