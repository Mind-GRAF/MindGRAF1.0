package nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import network.Network;
import relations.Relation;
import set.NodeSet;
import cables.Cable;
import cables.DownCable;
import cables.DownCableSet;
import cables.UpCable;
import cables.UpCableSet;
import components.Substitutions;
import exceptions.NoSuchTypeException;

public abstract class Node {

	private int id;
	protected String name;
	private UpCableSet upCableSet;
	private final Syntactic syntacticType;
	private DownCableSet downCableSet;
	private NodeSet freeVariableSet;
	private static int count = 0;

	public Node(String name, Boolean isVariable) { // constructor for base and
													// variable nodes

		this.id = count;
		this.name = name;
		this.upCableSet = new UpCableSet();

		if (isVariable) {
			syntacticType = Syntactic.VARIABLE;
		} else
			syntacticType = Syntactic.BASE;

		count++;
	}

	public Node(DownCableSet downCables) { // constructor for molecular nodes

		this.id = count;
		this.name = "M" + Network.MolecularCount;
		syntacticType = Syntactic.MOLECULAR;
		if (downCables == null || downCables.size() == 0)
			return;
		this.upCableSet = new UpCableSet();

		this.downCableSet = downCables;
		freeVariableSet = new NodeSet();
		System.out.println(freeVariableSet.getValues());
		for (Cable c : downCables.getValues())
			for (Node node : c.getNodeSet().getValues())
				node.getUpCableSet().updateCables(c.getRelation(), this);
		count++;
	}

	public DownCable getDownCable(String relation) {
		return this.getDownCableSet().get(relation);
	}

	public UpCable getUpCable(String relation) {
		return this.getUpCableSet().get(relation);
	}

	public NodeSet getParents(String relation) {
		return this.getUpCable(relation).getNodeSet();
	}

	public NodeSet getDirectParents() {
		NodeSet nodeSet = new NodeSet();
		for (Cable c : this.getUpCableSet().getValues()) {
			nodeSet.union(c.getNodeSet());
		}

		return nodeSet;
	}

	public NodeSet getDirectChildren() {
		NodeSet nodeSet = new NodeSet();
		for (DownCable c : this.getDownCableSet().getValues()) {
			nodeSet = nodeSet.union(c.getNodeSet());
		}

		return nodeSet;
	}

	public Relation getRelation(Node n) {
		for (Cable cable : this.getDownCableSet().getValues()) {
			if (cable.getNodeSet().contains(n.getName())) {
				return cable.getRelation();
			}
		}
		for (Cable cable : this.getUpCableSet().getValues()) {
			if (cable.getNodeSet().contains(n.getName())) {
				return cable.getRelation();
			}
		}
		return null;
	}

	public ArrayList<Relation> getRelations() {
		ArrayList<Relation> result = new ArrayList<Relation>();
		for (DownCable d : this.getDownCableSet().getValues())
			result.add(d.getRelation());

		return result;
	}

	public NodeSet fetchFreeVariables() {
		NodeSet freeVariables = new NodeSet();
		HashSet<String> invalidPairs = new HashSet<>();
		LinkedList<Node> pathTrace = new LinkedList<>();

		findFreeVariables(freeVariables, invalidPairs, pathTrace);

		setFreeVariableSet(freeVariables);
		return freeVariables;
	}

	private void findFreeVariables(NodeSet freeVariables, HashSet<String> invalidPairs, LinkedList<Node> pathTrace) {
		pathTrace.addLast(this);
		if (this.isVariable()) {
			for (Node node : pathTrace) {
				String inValidPair = node.getName() + "_" + this.getName();
				if (invalidPairs.contains(inValidPair)) {
					return;
				}
			}
			freeVariables.add(this);
			return;
		}

		if (this.isBase()) {
			return;
		}

		for (Cable cable : this.getDownCableSet().getValues()) {
			if (cable.getRelation().isQuantifier()) {
				for (Node child : cable.getNodeSet().getValues()) {
					if (child.isVariable()) {
						invalidPairs.add(this.getName() + '_' + child.getName());
					}
				}
			}
		}

		for (Cable cable : this.getDownCableSet().getValues()) {
			if (!cable.getRelation().isQuantifier()) {
				for (Node child : cable.getNodeSet().getValues()) {
					child.findFreeVariables(freeVariables, invalidPairs, pathTrace);
					pathTrace.removeLast();
				}
			}
		}
	}

	public Node getNegation() {

		NodeSet negation = this.getParents("arg");
		if (negation != null && negation.size() > 0) {
			for (Node node : negation.getValues()) {
				DownCable min = node.getDownCable("min");
				DownCable max = node.getDownCable("max");
				if (min != null && min.getNodeSet().contains("0")
						&& max != null && max.getNodeSet().contains("0")) {
					return node;
				}
			}
		}
		return null;
	}

	public boolean isFree(Node Node) {
		return Node.getFreeVariables().contains(this);
	}

	public boolean isOpen() {
		return !this.getFreeVariables().isEmpty();
	}

	public Node clone() {
		HashMap<String, Node> builtNodes = new HashMap<String, Node>();
		return this.cloneHelper(builtNodes);
	}

	private Node cloneHelper(HashMap<String, Node> builtNodes) {
		if (this.isBase() || this.isVariable()) {
			String Semantic = this.getClass().getSimpleName();
			if (builtNodes.containsKey(this.getName() + "temp")) {
				return builtNodes.get(this.getName() + "temp");
			}
			Node n;
			if (isVariable()) {

				try {
					n = Network.createVariableNode(this.getName() + "temp",
							Semantic);
					return n;
				} catch (NoSuchTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			} else {

				try {
					n = Network.createNode(this.getName() + "temp", Semantic);
					builtNodes.put(n.getName(), n);
					return n;
				} catch (NoSuchTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

		} else {
			String Semantic = this.getClass().getSimpleName();

			HashMap<String, DownCable> downCables = new HashMap<String, DownCable>();
			for (DownCable downCable : this.getDownCableSet().getValues()) {
				NodeSet n = new NodeSet();
				for (Node node : downCable.getNodeSet().getValues()) {
					n.add(node.cloneHelper(builtNodes));
				}

				DownCable d = new DownCable(downCable.getRelation(), n);
				downCables.put(d.getRelation().getName(), d);
			}
			DownCableSet dc = new DownCableSet(downCables);
			String dcKey = dc.getMolecularNodeKey();
			if (builtNodes.containsKey(dcKey)) {
				return builtNodes.get(dcKey);
			}
			Node n;
			try {
				n = Network.createNode(Semantic, dc);
				builtNodes.put(dcKey, n);
				return n;
			} catch (NoSuchTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}

	public Node applySubstitution(Substitutions substitutions)
			throws NoSuchTypeException {
		HashMap<String, Node> builtNodes = new HashMap<String, Node>();
		LinkedList<Node> pathTrace = new LinkedList<Node>();
		return this.substituteHelper(this, substitutions, builtNodes, pathTrace);
	}

	private Node substituteHelper(Node parent,
			Substitutions substitutions,
			HashMap<String, Node> builtNodes, LinkedList<Node> pathTrace)
			throws NoSuchTypeException {
		pathTrace.add(this);
		String Semantic = this.getClass().getSimpleName();
		if (this.isBase() || this.isVariable()) {

			for (Entry<Node, Node> sub : substitutions.getMap().entrySet()) {
				if (this.isVariable()) {
					if (this.equals(sub.getKey()) && this.isFree(parent)) {
						boolean flag = true;
						for (Node node : pathTrace) {
							if (node.isMolecular() && !this.isFree(node)) {
								flag = false;
							}
						}
						if (flag) {
							builtNodes.put(sub.getValue().getName(),
									sub.getValue());
							pathTrace.removeLast();
							return sub.getValue();
						}
					}
				}

			}

			if (builtNodes.containsKey(this.getName() + "temp")) {
				pathTrace.removeLast();
				return builtNodes.get(this.getName() + "temp");
			}
			Node n;
			if (isVariable())
				n = Network.createVariableNode(this.getName() + "temp",
						Semantic);
			else
				n = Network.createNode(this.getName() + "temp", Semantic);

			builtNodes.put(n.getName(), n);
			pathTrace.removeLast();
			return n;
		} else {

			HashMap<String, DownCable> downCables = new HashMap<String, DownCable>();
			for (DownCable downCable : this.getDownCableSet().getValues()) {
				NodeSet n = new NodeSet();
				for (Node node : downCable.getNodeSet().getValues()) {
					n.add(node.substituteHelper(parent, substitutions,
							builtNodes, pathTrace));

				}

				DownCable d = new DownCable(downCable.getRelation(), n);
				downCables.put(d.getRelation().getName(), d);
			}
			DownCableSet dc = new DownCableSet(downCables);
			String dcKey = dc.getMolecularNodeKey();
			if (builtNodes.containsKey(dcKey)) {
				pathTrace.removeLast();
				return builtNodes.get(dcKey);
			}
			Node n = Network.createNode(Semantic, dc);
			builtNodes.put(dcKey, n);
			pathTrace.removeLast();
			return n;
		}
	}

	public String toString() {
		if (this.isMolecular())
			return "{" + this.name + ", " + this.syntacticType + ", "
					+ this.downCableSet + "}";

		return "{" + this.name + ", " + this.syntacticType + "}";
	}

	public MolecularType getMolecularType() {
		if (this.getFreeVariables() == null
				|| this.getFreeVariables().size() == 0)
			return MolecularType.CLOSED;

		return MolecularType.OPEN;

	}

	public int getId() {
		return id;
	}

	public boolean isMolecular() {
		if (this.syntacticType == Syntactic.MOLECULAR)
			return true;
		return false;
	}

	public boolean isBase() {
		if (this.syntacticType == Syntactic.BASE)
			return true;
		return false;
	}

	public boolean isVariable() {
		if (this.syntacticType == Syntactic.VARIABLE)
			return true;
		return false;
	}

	public static int getCount() {
		return count;
	}

	public String getName() {
		return name;
	}

	public UpCableSet getUpCableSet() {
		return upCableSet;
	}

	public void setUpCableSet(UpCableSet upCableSet) {
		this.upCableSet = upCableSet;
	}

	public DownCableSet getDownCableSet() {
		return downCableSet;
	}

	public void setDownCableSet(DownCableSet downCableSet) {
		this.downCableSet = downCableSet;
	}

	public Syntactic getSyntacticType() {
		return syntacticType;
	}

	public void getVariableType(Node node) {

	}

	public NodeSet getFreeVariables() {
		return freeVariableSet;
	}

	public void setFreeVariableSet(NodeSet freeVariableSet) {
		this.freeVariableSet = freeVariableSet;
	}

	public boolean equals(Node n) {
		if (n.isMolecular() && this.isMolecular()) {
			String nKey = n.getDownCableSet().getMolecularNodeKey();
			String myKey = this.getDownCableSet().getMolecularNodeKey();
			return this.getId() == n.getId() || (nKey.equals(myKey));
		} else {
			return this.getId() == n.getId()
					|| (this.getName().equals(n.getName())
							&& this.getSyntacticType() == n.getSyntacticType());
		}

	}

	public void processReports() {
		// TODO Auto-generated method stub

	}

	public void processRequests() {
		// TODO Auto-generated method stub

	}

}
