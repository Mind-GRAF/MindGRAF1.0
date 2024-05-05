package edu.guc.mind_graf.network;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import edu.guc.mind_graf.mgip.rules.*;
import edu.guc.mind_graf.paths.AndPath;
import edu.guc.mind_graf.paths.ComposePath;
import edu.guc.mind_graf.paths.FUnitPath;
import edu.guc.mind_graf.paths.KPlusPath;
import edu.guc.mind_graf.paths.Path;
import edu.guc.mind_graf.paths.PathTrace;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.CustomClass;
import edu.guc.mind_graf.components.CustomConstructor;
import edu.guc.mind_graf.components.CustomMethod;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.CannotRemoveNodeException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.exceptions.NodeNotInNetworkException;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.IndividualNode;
import edu.guc.mind_graf.nodes.MolecularType;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;

public class Network {
	private static HashMap<Integer, Node> nodes;
	private static HashMap<String, HashMap<String, Node>> molecularNodes;
	private static HashMap<String, Node> baseNodes;
	private static HashMap<String, Relation> relations;
	private static HashMap<Integer, Node> propositionNodes;
	public static HashMap<String, String> quantifiers = new HashMap<String, String>();
	public static HashMap<String, CustomClass> userDefinedClasses = new HashMap<String, CustomClass>();
	public static int MolecularCount;

	public Network() {
		nodes = new HashMap<Integer, Node>();
		molecularNodes = new HashMap<String, HashMap<String, Node>>();
		baseNodes = new HashMap<String, Node>();
		propositionNodes = new HashMap<Integer, Node>();
		relations = new HashMap<String, Relation>();
		quantifiers.put("forall", "forall");
		addBasicRelations();
		try{
			addBasicNodes();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void addBasicRelations() {
		Network.createRelation("forall", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("min", "individualnode", Adjustability.NONE, 1);
		Network.createRelation("max", "individualnode", Adjustability.NONE, 1);
		Network.createRelation("arg", "propositionnode", Adjustability.NONE, 1);
		Network.createRelation("object", "", Adjustability.EXPAND, 2);
		Network.createRelation("member", "", Adjustability.EXPAND, 2);
		Network.createRelation("class", "", Adjustability.EXPAND, 2);
		Network.createRelation("i", "individualnode", Adjustability.EXPAND, 2);
		Network.createRelation("cq", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("ant", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("&ant", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("thresh", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("threshmax", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("threshmax", "propositionnode", Adjustability.EXPAND, 2);
		Network.createRelation("grade", "individualNode", Adjustability.NONE, 0);
		Network.createRelation("prop", "pro positionnode", Adjustability.NONE, 0);

	}

	public void addBasicNodes() throws NoSuchTypeException {
		Network.createNode("0", "individualNode");
	}

	// first constructor for molecular nodes
	public static Node createNode(String SemanticType, DownCableSet downCableSet)
			throws NoSuchTypeException {
		if (downCableSet.size() == 0) {
			return null;
		}
		String downCablesKey = downCableSet.getMolecularSetKey();
		String molecularKey = downCableSet.getMolecularNodeKey();
		Node node;

		if (!molecularNodes.containsKey(downCablesKey)
				|| ((molecularNodes.containsKey(downCablesKey) && !molecularNodes
						.get(downCablesKey).containsKey(molecularKey)))) {

			switch (SemanticType.toLowerCase()) {
				case "propositionnode":
					node = new PropositionNode(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;

				case "actnode":
					node = new ActNode(downCableSet);
					break;
				case "individualnode":
					node = new IndividualNode(downCableSet);
					break;
//				case "rulenode":
//					node = new RuleNode(downCableSet);
//					break;
				case "andor":
					node = new AndOr(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;
				case "andentailment":
					node = new AndEntailment(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;
				case "orentailment":
					node = new OrEntailment(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;
				case "thresh":
					node = new Thresh(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;
				case "numentailment":
					node = new NumEntailment(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;
				case "bridgerule":
					node = new BridgeRule(downCableSet);
					propositionNodes.put(node.getId(), node);
					break;
				default:
					if (userDefinedClasses.containsKey(SemanticType)) {
						CustomClass customClass = userDefinedClasses
								.get(SemanticType);
						Class<?> createdClass = customClass.getNewClass();
						try {
							node = (Node) customClass.createInstance(createdClass,
									downCableSet);
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return null;
						}

					} else {
						throw new NoSuchTypeException(
								"No such Semantic Type, do you want to create a new one ?");
					}
			}

			node.fetchFreeVariables();
			if (node.getMolecularType() == MolecularType.CLOSED) {

				for (HashMap<String, Node> molecularSet : molecularNodes
						.values()) {

					for (Node molecular : molecularSet.values()) {
						String MolecularKey = molecular.getDownCableSet()
								.getMolecularNodeKeyWithoutVars();
						String newNodeKey = downCableSet
								.getMolecularNodeKeyWithoutVars();
						if (MolecularKey.equals(newNodeKey)) {
							return molecular;
						}
					}
				}
			}
			nodes.put(node.getId(), node);
			if (molecularNodes.containsKey(downCablesKey)) {
				molecularNodes.get(downCablesKey).put(
						downCableSet.getMolecularNodeKey(), node);
			} else {
				HashMap<String, Node> newSet = new HashMap<String, Node>();
				newSet.put(downCableSet.getMolecularNodeKey(), node);
				molecularNodes.put(downCablesKey, newSet);
			}
			MolecularCount++;
			return node;
		}

		return molecularNodes.get(downCablesKey).get(molecularKey);

	}

	// second constructor for base nodes
	public static Node createNode(String name, String SemanticType)
			throws NoSuchTypeException {
		Node node;
		switch (SemanticType.toLowerCase()) {
			case "propositionnode":
				node = new PropositionNode(name, false);
				propositionNodes.put(node.getId(), node);
				break;
			case "actnode":
				node = new ActNode(name, false);
				break;
			case "individualnode":
				node = new IndividualNode(name, false);
				break;
//			case "rulenode":
//				node = new RuleNode(name, false);
//				break;
			default:
				if (userDefinedClasses.containsKey(SemanticType)) {
					CustomClass customClass = userDefinedClasses.get(SemanticType);
					Class<?> createdClass = customClass.getNewClass();
					try {
						node = (Node) customClass.createInstance(createdClass,
								name, false);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;

					}

				} else {
					throw new NoSuchTypeException(
							"No such Semantic Type, do you want to create a new one ?");
				}
		}
		if (node != null) {
			if (nodes.containsKey(node.getId()))
				return nodes.get(node.getId());

			nodes.put(node.getId(), node);
			baseNodes.put(node.getName(), node);
		}

		return node;
	}

	// third constructor for base nodes
	public static Node createVariableNode(String name, String SemanticType)
			throws NoSuchTypeException {
		Node node;
		switch (SemanticType.toLowerCase()) {
			case "propositionnode":
				node = new PropositionNode(name, true);
				propositionNodes.put(node.getId(), node);
				break;
			case "actnode":
				node = new ActNode(name, true);
				break;
			case "individualnode":
				node = new IndividualNode(name, true);
				break;
//			case "rulenode":
//				node = new RuleNode(name, true);
//				break;
			default:
				if (userDefinedClasses.containsKey(SemanticType)) {
					CustomClass customClass = userDefinedClasses.get(SemanticType);
					Class<?> createdClass = customClass.getNewClass();
					try {
						node = (Node) customClass.createInstance(createdClass,
								name, true);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}

				} else {
					throw new NoSuchTypeException(
							"No such Semantic Type, do you want to create a new one ?");
				}
		}
		if (node != null) {
			if (nodes.containsKey(node.getId()))
				return nodes.get(node.getId());
			nodes.put(node.getId(), node);
		}

		return node;
	}

	public static Relation createRelation(String name, String type,
			Adjustability adjust, int limit) {
		Relation r = new Relation(name, type, adjust, limit);
		relations.put(r.getName(), r);
		return r;
	}

	public static CustomClass createNewSemanticType(String name,
			String SuperClass, ArrayList<CustomMethod> methods)
			throws Exception {
		CustomClass c = new CustomClass(name, SuperClass);
		Class<?>[] params = { String.class, Boolean.class };
		String[] arguments = { "name", "isVariable" };
		Class<?>[] params2 = { DownCableSet.class };
		String[] arguments2 = { "downCableSet" };
		CustomConstructor constructor = new CustomConstructor(name, params,
				arguments);
		CustomConstructor constructor2 = new CustomConstructor(name, params2,
				arguments2);
		ArrayList<CustomConstructor> constructors = new ArrayList<>();
		constructors.add(constructor);
		constructors.add(constructor2);
		Class<?> newClass = c.createClass(methods, constructors);
		userDefinedClasses.put(name, c);
		return c;
	}

	public static void RemoveNode(Node node) throws NodeNotInNetworkException,
			CannotRemoveNodeException {
		if (!Network.nodes.containsValue(node)) {
			throw new NodeNotInNetworkException("Node " + node.getName()
					+ " does not exist in the network");
		}
		if (node.getUpCableSet().size() > 0) {
			throw new CannotRemoveNodeException(
					"Node "
							+ node.getName()
							+ " is part of the structure of other molecular nodes, so it cannot be removed unless all its parents are removed");
		}
		System.out.println(nodes.size());
		nodes.remove(node.getId());
		System.out.println(nodes.size());
		if (node.isMolecular()) {
			molecularNodes.get(node.getDownCableSet().getMolecularSetKey())
					.remove(node.getDownCableSet().getMolecularNodeKey());
			for (DownCable d : node.getDownCableSet().getValues()) {
				for (Node n : d.getNodeSet().getValues()) {
					n.getUpCableSet().removeNode(d.getRelation(), node);
				}
			}
		} else if (node.isBase())
			baseNodes.remove(node.getName());
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

	public static HashMap<String, HashMap<String, Node>> getMolecularNodes() {
		return molecularNodes;
	}

	public static void setMolecularNodes(
			HashMap<String, HashMap<String, Node>> MolecularNodes) {
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

	public static HashMap<Integer, Node> getPropositionNodes() {
		return propositionNodes;
	}

	public static void setQuantifiers(HashMap<String, String> quantifiers) {
		Network.quantifiers = quantifiers;
	}

	public Node find(DownCableSet downCableSet) {
		String setKey = downCableSet.getMolecularSetKey();
		String key = downCableSet.getMolecularNodeKey();
		return molecularNodes.get(setKey).get(key);
	}

	public HashMap<String, Node> findMolecularSet(DownCableSet downCableSet) {
		String setKey = downCableSet.getMolecularSetKey();
		return molecularNodes.get(setKey);
	}

	public void printNodes() {
		String result = "";
		for (Node node : nodes.values()) {
			result += node.toString() + "\n";
		}
		System.out.println(result);
	}

	public static void main(String[] args) throws Exception {
		Network Net = new Network();
		// Node Z = createvariableNode("Z", "propositionnode");
		// Node Y = createvariableNode("Y", "propositionnode");
		// Node X = createvariableNode("X", "propositionnode");
		// Node Base = createNode("base", "propositionnode");
		// quantifiers.put("forall","forall");
		//
		// Relation relation = createRelation ("forall", "",
		// Adjustability.EXPAND, 2);
		// Relation relation2 = createRelation("b", "", Adjustability.EXPAND,
		// 2);
		//
		// NodeSet nodeSetX = new NodeSet();
		// NodeSet nodeSetZ = new NodeSet();
		// NodeSet nodeSetXZ = new NodeSet();
		// NodeSet nodeSetY = new NodeSet();
		//
		// nodeSetZ.add(Z);
		// nodeSetX.add(X);
		// nodeSetXZ.add(X);
		// nodeSetXZ.add(Z);
		// nodeSetY.add(Y);
		//
		// // M0
		// DownCable d2 = new DownCable(relation2, nodeSetXZ);
		// HashMap<String, DownCable> Cables = new HashMap<>();
		// Cables.put(d2.getRelation().getName(), d2);
		// DownCableSet downCableSet = new DownCableSet(Cables);
		// Node M0 = createNode("propositionnode", downCableSet);
		//
		// //M1
		// NodeSet nodeSetM0 = new NodeSet();
		// nodeSetM0.add(M0);
		// DownCable d = new DownCable(relation, nodeSetZ);
		// DownCable d3 = new DownCable(relation2,nodeSetM0);
		// HashMap<String, DownCable> Cables2 = new HashMap<>();
		// Cables2.put(d3.getRelation().getName(), d3);
		// Cables2.put(d.getRelation().getName(), d);
		// DownCableSet downCableSet2 = new DownCableSet(Cables2);
		// Node M1 = createNode("propositionnode", downCableSet2);
		//
		// NodeSet nodeSetM1 = new NodeSet();
		// nodeSetM1.add(M1);
		//
		// //M2
		// DownCable dM2 = new DownCable(relation2, nodeSetY.union(nodeSetM1));
		// HashMap<String, DownCable> CablesM2 = new HashMap<>();
		// CablesM2.put(dM2.getRelation().getName(), dM2);
		// DownCableSet downCableSetM2 = new DownCableSet(CablesM2);
		// Node M2 = createNode("propositionnode", downCableSetM2);
		//
		// //M3
		// DownCable dM3 = new DownCable(relation2,
		// nodeSetX.union(nodeSetZ.union(nodeSetY)));
		//
		// HashMap<String, DownCable> CablesM3 = new HashMap<>();
		// CablesM3.put(dM3.getRelation().getName(), dM3);
		//
		// DownCableSet downCableSetM3 = new DownCableSet(CablesM3);
		// Node M3 = createNode("propositionnode", downCableSetM3);
		//
		// //M4
		// NodeSet nodeSetM3 = new NodeSet();
		// nodeSetM3.add(M3);
		// NodeSet nodeSetM2 = new NodeSet();
		// nodeSetM3.add(M2);
		//
		// NodeSet nodeSetM23 = new NodeSet();
		// nodeSetM23.add(M2);
		// nodeSetM23.add(M3);
		//
		// DownCable dM4 = new DownCable(relation, nodeSetX);
		// DownCable dM4_2 = new DownCable(relation2,nodeSetM23);
		// // DownCable dM4_3 = new DownCable(relation2,nodeSetM3);
		//
		// HashMap<String, DownCable> CablesM4 = new HashMap<>();
		// CablesM4.put(dM4.getRelation().getName(), dM4);
		// CablesM4.put(dM4_2.getRelation().getName()+1, dM4_2);
		//
		// DownCableSet downCableSetM4 = new DownCableSet(CablesM4);
		// Node M4 = createNode("propositionnode", downCableSetM4);
		//
		// System.out.println("-----------------------------------------------------");
		// System.out.println("free vars" + M4.getFreeVariables());
		//
		//
		// Substitution s = new Substitution(Base, Z);
		// ArrayList<Substitution> substitutionArr = new ArrayList<>();
		// substitutionArr.add(s);
		// System.out.println(M4);
		// // System.out.println(M4.clone());
		//
		// System.out.println(M4.substitute(substitutionArr));
		// printNodes();

		// ======================================================================================================================

		// Node X = createvariableNode("X", "propositionnode");
		// Node Y = createvariableNode("Y", "propositionnode");
		// Node Base = createNode("base", "propositionnode", false);
		// Node Bob = createNode("bob", "propositionnode", false);
		//
		// quantifiers.put("forall","forall");
		//
		// Relation Qrelation = createRelation ("forall", "",
		// Adjustability.EXPAND, 2);
		// Relation relation = createRelation("relation", "",
		// Adjustability.EXPAND, 2);
		//
		// NodeSet nodeSetX = new NodeSet();
		// NodeSet nodeSetY = new NodeSet();
		//
		// nodeSetX.add(X);;
		// nodeSetY.add(Y);
		//
		// // M0
		// DownCable d2 = new DownCable(relation, nodeSetX.union(nodeSetY));
		// HashMap<String, DownCable> Cables = new HashMap<>();
		// Cables.put(d2.getRelation().getName(), d2);
		// DownCableSet downCableSet = new DownCableSet(Cables);
		// Node M0 = createNode("propositionnode", downCableSet);
		//
		// //M1
		// NodeSet nodeSetM0 = new NodeSet();
		// nodeSetM0.add(M0);
		// DownCable d = new DownCable(relation, nodeSetM0.union(nodeSetY));
		// HashMap<String, DownCable> Cables2 = new HashMap<>();
		// Cables2.put(d.getRelation().getName(), d);
		// DownCableSet downCableSet2 = new DownCableSet(Cables2);
		// Node M1 = createNode("propositionnode", downCableSet2);
		//
		// NodeSet nodeSetM1 = new NodeSet();
		// nodeSetM1.add(M1);
		//
		// //M2
		// NodeSet nodeSetBob = new NodeSet(); nodeSetBob.add(Bob);
		// DownCable dM2 = new DownCable(relation, nodeSetY.union(nodeSetBob));
		// HashMap<String, DownCable> CablesM2 = new HashMap<>();
		// CablesM2.put(dM2.getRelation().getName(), dM2);
		// DownCableSet downCableSetM2 = new DownCableSet(CablesM2);
		// Node M2 = createNode("propositionnode", downCableSetM2);
		//
		// //M3
		// NodeSet NodeSetM2 = new NodeSet(); NodeSetM2.add(M2);
		// DownCable dM3 = new DownCable(Qrelation, (nodeSetY));
		// DownCable dM3_2 = new DownCable(relation,
		// (nodeSetM1.union(NodeSetM2)));
		//
		// HashMap<String, DownCable> CablesM3 = new HashMap<>();
		// CablesM3.put(dM3.getRelation().getName(), dM3);
		// CablesM3.put(dM3_2.getRelation().getName(), dM3_2);
		//
		// DownCableSet downCableSetM3 = new DownCableSet(CablesM3);
		// Node M3 = createNode("propositionnode", downCableSetM3);
		//
		// //M4
		// NodeSet nodeSetM3 = new NodeSet();
		// nodeSetM3.add(M3);
		//
		// NodeSet nodeSetM03 = new NodeSet();
		// nodeSetM03.add(M0);
		// nodeSetM03.add(M3);
		//
		// DownCable dM4_2 = new DownCable(relation,nodeSetM03);
		// // DownCable dM4_3 = new DownCable(relation2,nodeSetM3);
		//
		// HashMap<String, DownCable> CablesM4 = new HashMap<>();
		// CablesM4.put(dM4_2.getRelation().getName()+1, dM4_2);
		//
		// DownCableSet downCableSetM4 = new DownCableSet(CablesM4);
		// Node M4 = createNode("propositionnode", downCableSetM4);
		//
		// System.out.println("-----------------------------------------------------");
		// System.out.println("free vars" + M4.getFreeVariables());
		//
		//
		// Substitution s = new Substitution(Base, Y);
		// ArrayList<Substitution> substitutionArr = new ArrayList<>();
		// substitutionArr.add(s);
		// System.out.println(M4);
		// System.out.println(M4.clone());

		// System.out.println(M4.substitute(substitutionArr));
		// printNodes();
		// =============================================================================================================
//		Node cs = createNode("cs", "propositionnode");
//		Node fun = createNode("fun", "propositionnode");
//		Node mary = createNode("mary", "propositionnode");
//		Node believe = createNode("believe", "propositionnode");
//		Node bob = createNode("bob", "propositionnode");
//		Node know = createNode("know", "propositionnode");
//
//		Relation agent = createRelation("agent", "", Adjustability.EXPAND, 2);
//		Relation act = createRelation("act", "", Adjustability.EXPAND, 2);
//		Relation obj = createRelation("obj", "", Adjustability.EXPAND, 2);
//		Relation prop = createRelation("prop", "", Adjustability.EXPAND, 2);
//
//		DownCable d1 = new DownCable(obj, new NodeSet(cs));
//		DownCable d2 = new DownCable(prop, new NodeSet(fun));
//
//		Node M1 = createNode("propositionnode", new DownCableSet(d1, d2));
//
//		DownCable d3 = new DownCable(obj, new NodeSet(M1));
//		DownCable d4 = new DownCable(act, new NodeSet(believe));
//		DownCable d5 = new DownCable(agent, new NodeSet(mary));
//
//		Node M2 = createNode("propositionnode", new DownCableSet(d3, d4, d5));
//
//		DownCable d6 = new DownCable(obj, new NodeSet(M2));
//		DownCable d7 = new DownCable(act, new NodeSet(know));
//		DownCable d8 = new DownCable(agent, new NodeSet(bob));
//
//		Node M3 = createNode("propositionnode", new DownCableSet(d6, d7, d8));
//
//		FUnitPath p1 = new FUnitPath(agent);
//		FUnitPath p2 = new FUnitPath(act);
//		FUnitPath p3 = new FUnitPath(obj);
//
//		ComposePath pCompose = new ComposePath(p2, p3);
//
//		FUnitPath pF4 = new FUnitPath(agent);
//		FUnitPath pF5 = new FUnitPath(act);
//		FUnitPath pF6 = new FUnitPath(obj);
//
//		ComposePath pCompose2 = new ComposePath(pF5, pF6);
//
//		LinkedList<Object[]> s = p3.follow(M3, new PathTrace(), new Context());
//		Path p4 = new KPlusPath(p3);
//		LinkedList<Object[]> s2 = p4.follow(M3, new PathTrace(), new Context());
//
//		for (Object[] object : s2) {
//			System.out.println(object[0]);
//		}
//
//		AndPath and = new AndPath(pCompose);
//		AndPath and2 = new AndPath(pCompose2);
//
//		NodeSet oss = new NodeSet();

	}

}
