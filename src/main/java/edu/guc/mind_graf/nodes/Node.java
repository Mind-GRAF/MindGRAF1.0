package edu.guc.mind_graf.nodes;

import java.util.*;
import java.util.Map.Entry;

import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.support.Support;
import edu.guc.mind_graf.cables.Cable;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.cables.UpCable;
import edu.guc.mind_graf.cables.UpCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.requests.ActChannel;
import edu.guc.mind_graf.mgip.requests.AntecedentToRuleChannel;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.IfToRuleChannel;
import edu.guc.mind_graf.mgip.requests.MatchChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.requests.RuleToConsequentChannel;
import edu.guc.mind_graf.mgip.requests.WhenToRuleChannel;

public abstract class Node {

	private final int id;
	protected String name;
	private UpCableSet upCableSet;
	private final Syntactic syntacticType;
	private DownCableSet downCableSet;
	private NodeSet freeVariableSet;
	private static int count = 0;
	private int freeVariablesHash;

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
//		System.out.println(freeVariableSet.getValues());
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
		UpCable cable = this.getUpCable(relation);
		return cable == null? null : cable.getNodeSet();
	}

	public NodeSet getDirectParents() {
		NodeSet nodeSet = new NodeSet();
		for (Cable c : this.getUpCableSet().getValues()) {
			nodeSet = nodeSet.union(c.getNodeSet());
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

	public int getFreeVariablesHash() {
		return freeVariablesHash;
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
		if (negation != null && !negation.isEmpty()) {
			for (Node node : negation.getValues()) {
				DownCable min = node.getDownCable("min");
				DownCable max = node.getDownCable("max");
				if (min != null && min.getNodeSet().contains("0")
						&& max != null && max.getNodeSet().contains("0")) {
					return node;
				}
			}
		}
		//this handles the case of !(!P) to find P
		if(this.getDownCable("min").getNodeSet().contains("0") && this.getDownCable("max").getNodeSet().contains("0")){
			NodeSet p = this.getDownCable("arg").getNodeSet();
			if(p != null && !p.isEmpty()){
				return p.iterator().next();
			}
		}

		return null;
	}

	public Node createNegation(Node negated) throws NoSuchTypeException {
		NodeSet zeroNode = new NodeSet(Network.getBaseNodes().get("0"));
		DownCable min = new DownCable(Network.getRelations().get("min"), zeroNode);
		DownCable max = new DownCable(Network.getRelations().get("max"), zeroNode);
		NodeSet negatedSet = new NodeSet(negated);
		DownCable arg = new DownCable(Network.getRelations().get("arg"), negatedSet);
		DownCableSet negationDownCableSet = new DownCableSet(min, max, arg);
		return Network.createNode("andor", negationDownCableSet);
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

	public String printShortData(){
		return "{ id: " + this.id + ", name: " + this.name + "}";
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
        return this.syntacticType == Syntactic.MOLECULAR;
    }

	public boolean isBase() {
        return this.syntacticType == Syntactic.BASE;
    }

	public boolean isVariable() {
        return this.syntacticType == Syntactic.VARIABLE;
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

	public int customHash() {  // custom hash function to hash using the free variables
		int hash = 0;
		int factor = 1;
		int[] sorted = new int[freeVariableSet.size()];
		int i = 0;
		for(Node var : freeVariableSet) {
			sorted[i++] = var.getId();
		}
		Arrays.sort(sorted);
		for(int id : sorted) {
			hash += id*factor;
			factor *= 100;
		}
		//assuming ids wont be over 100
		return hash;
	}

	public void setFreeVariableSet(NodeSet freeVariableSet) {
		this.freeVariableSet = freeVariableSet;
		this.freeVariablesHash = customHash();
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

	protected Request establishChannel(ChannelType type, Node targetNode,
			Substitutions switchSubs,
			Substitutions filterSubs, String contextName,
			int attitudeId,
			int matchType, Node requesterNode,Support support) {
		/* BEGIN - Helpful Prints */
		String reporterIdent = targetNode.getName();
		String requesterIdent = requesterNode.getName();
		System.out.println("Trying to establish a channel from " + requesterIdent + " to " + reporterIdent);
		/* END - Helpful Prints */
		Substitutions switchSubstitutions = switchSubs == null ? new Substitutions()
				: switchSubs;
		Substitutions filterSubstitutions = filterSubs == null ? new Substitutions()
				: filterSubs;
		Channel newChannel;
		switch (type) {
			case Matched:
				newChannel = new MatchChannel(switchSubstitutions, filterSubstitutions,
						contextName, attitudeId,
						matchType, requesterNode,support);
				break;
			case AntRule:
				newChannel = new AntecedentToRuleChannel(switchSubstitutions,
						filterSubstitutions, contextName,
						attitudeId,
						requesterNode);
				break;
			case RuleCons:
				newChannel = new RuleToConsequentChannel(switchSubstitutions,
						filterSubstitutions, contextName,
						attitudeId,
						requesterNode);
				break;
			case IfRule:
				newChannel = new IfToRuleChannel(switchSubstitutions,
						filterSubstitutions, contextName,
						attitudeId,
						requesterNode);
				break;
			case WhenRule:
				newChannel = new WhenToRuleChannel(switchSubstitutions,
						filterSubstitutions, contextName,
						attitudeId,
						requesterNode);
				break;
			default:
				newChannel = new ActChannel(switchSubstitutions,
						filterSubstitutions, contextName,
						attitudeId,
						requesterNode);
				break;

		}
		Channel currentChannel;

		currentChannel = ((PropositionNode) targetNode).getOutgoingChannels().getChannel(newChannel);

		if (currentChannel == null) {
			/* BEGIN - Helpful Prints */
			System.out.println("Channel of type " + newChannel.getChannelType()
					+ " is successfully created and used for further operations");
			/* END - Helpful Prints */
			Request newRequest = new Request(newChannel, targetNode);

			((PropositionNode) targetNode).addToOutgoingChannels(newChannel);

			return newRequest;
		}

		/* BEGIN - Helpful Prints */
		System.out.println(
				"Channel of type " + currentChannel.getChannelType()
						+ " was already established and re-enqueued for further operations");
		/* END - Helpful Prints */
		return new Request(currentChannel, targetNode);

	}

	protected void sendRequestsToNodeSet(NodeSet nodeSet, Substitutions filterSubs,
			Substitutions switchSubs, String contextName, int attitudeId,
			ChannelType channelType, Node requesterNode) {
		for (Node sentTo : nodeSet) {
			Request newRequest = establishChannel(channelType, sentTo, switchSubs, filterSubs,
					contextName, attitudeId, -1, requesterNode,null);
			Scheduler.addToLowQueue(newRequest);
		}
	}

	public void processReports() {
		// TODO Auto-generated method stub

	}

	public void processRequests() throws NoSuchTypeException, DirectCycleException {
		// TODO Auto-generated method stub

	}

}
