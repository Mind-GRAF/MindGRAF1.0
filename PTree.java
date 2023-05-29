package inferenceRules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import nodes.Node;
import set.NodeSet;


public class PTree extends RuisHandler {
	private Hashtable<Integer, NodeSet> patternVariables;//PatternId, Nodes
	private Hashtable<Node, Set<Integer>> variablePatterns;//Node, PatternIds
	private NodeSet notProccessed;
	private HashSet<PSubTree> subTrees;
	private Hashtable<Integer, PSubTree> subTreesMap;

	/**
	 * Constructor for the PTree
	 * @param context
	 */
	public PTree() {
		super();
		patternVariables = new Hashtable<Integer, NodeSet>();
		variablePatterns = new Hashtable<Node, Set<Integer>>();
		notProccessed = new NodeSet();
		subTrees = new HashSet<PSubTree>();
		subTreesMap = new Hashtable<Integer, PSubTree>();
	}

	/**
	 * Builds the PTree by preparing pattern variables set, variable patterns set, pattern sequence and finally constructing the tree bottom up 
	 * @param ants
	 */
	public void buildTree(NodeSet ants){
		fillPVandVP(ants);

		Queue<PTreeNode> patternSequence = getPatternSequence();		

		constructBottomUp(patternSequence);

		patternVariables = new Hashtable<Integer, NodeSet>();
		variablePatterns = new Hashtable<Node, Set<Integer>>();
		notProccessed = new NodeSet();
	}

	/**
	 * Prepares both pattern variables set and variable patterns set
	 * @param ants
	 */
	private void fillPVandVP(NodeSet ants) {
		for(Node pattern : ants.getValues()){
			int id = pattern.getId();

			NodeSet vars = patternVariables.get(id);
			if(vars == null || vars.isEmpty())
				vars = new NodeSet();
			Set<Integer> pats = variablePatterns.get(id);
			if(pats == null || pats.isEmpty())
				pats = new HashSet<Integer>();

			if(pattern.isVariable()){
				vars.add (pattern);
				notProccessed.add( pattern);
			}

			if(pattern.isOpen()){
				NodeSet freeVars = pattern.getFreeVariables();
				vars.addAllTo(freeVars);
				notProccessed.addAllTo(freeVars);
			}
			patternVariables.put(id, vars);	
		}

		Set<Integer> pats = patternVariables.keySet();
		for(int curPat : pats){
			NodeSet vars = patternVariables.get(curPat);
			if(!(vars.isEmpty()) && !(vars == null)){
				for(Node curVar : vars.getValues()){
					Set<Integer> pat = variablePatterns.get(curVar);
					if(pat == null){
						pat = new HashSet<Integer>();
						pat.add(curPat);
						variablePatterns.put(curVar, pat);
						continue;
					}
					if(!pat.contains(curPat)){
						pat.add(curPat);
						variablePatterns.put(curVar, pat);
					}
				}
			}
		}
	}
	/**
	 * Prepares pattern sequence into a sequence of PTreeNodes ready for construction
	 * @return
	 */
	private Queue<PTreeNode> getPatternSequence() {
		LinkedHashSet<Integer> res = new LinkedHashSet<Integer>();
		Queue<PTreeNode> treeNodes = new LinkedList<PTreeNode>();

		for(Node currentVar : notProccessed.getValues()){
			Set<Integer> vPatternsIds = variablePatterns.get(currentVar);

			for(int currentPatId : vPatternsIds)
				if(!res.contains(currentPatId)){
					res.add(currentPatId);

					NodeSet vs = new NodeSet();
					vs.add(currentVar);
					Set<Integer> pats = new HashSet<Integer>();
					pats.add(currentPatId);

					treeNodes.add(new PTreeNode(pats, vs));
				}
		}
		return treeNodes;

	}
	/**
	 * Uses given ordered PTreeNodes to construct the PTree
	 * If no two adjacent nodes in the sequence share variables, a PSubTree is handled 
	 * @param treeNodes
	 */
	private void constructBottomUp(Queue<PTreeNode> treeNodes) {
		PTreeNode head = treeNodes.poll();
		if (treeNodes.isEmpty()) {
			processSubTree(head);
			return;
		}
		Queue<PTreeNode> newTreeNodes = new LinkedList<PTreeNode>();
		boolean sharing = false;
		while (!treeNodes.isEmpty()) {
			PTreeNode second = treeNodes.poll();
			if (sharingVars(head, second)) {
				sharing = true;
				Set<Integer> pats = union(head.getPats(), second.getPats());
				NodeSet vars = head.getVars().union(second.getVars());
				PTreeNode treeNode = new PTreeNode(pats, vars);
				NodeSet intersection = getSharedVars(head, second);
				treeNode.insertLeftAndRight(head, second, intersection);
				newTreeNodes.add(treeNode);
				head = treeNodes.poll();
			} else {
				newTreeNodes.add(head);
				head = second;
			}
		}
		if (head != null)
			newTreeNodes.add(head);
		if (sharing)
			constructBottomUp(newTreeNodes);
		else
			for(PTreeNode subHead : newTreeNodes)
				processSubTree(subHead);
	}
	/**
	 * Creates a PSubTree and inserts it into subTrees and subTreesMap
	 * @param subHead
	 */
	private void processSubTree(PTreeNode subHead) {
		if(subHead != null){
			PSubTree subTree = new PSubTree(subHead);
			subTrees.add(subTree);
			for (int id : subHead.getPats()) {
				if(subTree != null)
					subTreesMap.put(id, subTree);
			}
		}
	}

	@Override
	public RuleUseInfoSet insertRUI(RuleUseInfo rui) {
		Stack<RuleUseInfoSet> stack = new Stack<RuleUseInfoSet>();
		FlagNodeSet fns = rui.getFlagNodeSet();
		for(FlagNode node : fns){
			int pattern = node.getNode().getId();

			PSubTree subTree = subTreesMap.get(pattern);
			RuleUseInfoSet returned = new RuleUseInfoSet();
			if(subTree != null)
				returned = subTree.insert(rui);
			for (PSubTree sub : subTrees) {
				if (sub == subTree)
					continue;
				RuleUseInfoSet tSet = sub.getRootRUIS();
				if (tSet == null)
					tSet = new RuleUseInfoSet();
				stack.push(tSet);
			}
			stack.push(returned);
		}
		return multiply(stack);
	}

	private RuleUseInfoSet multiply(Stack<RuleUseInfoSet> infoSets) {
		RuleUseInfoSet first = infoSets.pop();
		while (!infoSets.isEmpty()) {
			RuleUseInfoSet second = infoSets.pop();
			first = first.combine(second);
		}
		return first;
	}

	private boolean sharingVars(PTreeNode head, PTreeNode second) {
		NodeSet smaller = null, bigger = null;
		if (head.getVars().size() > second.getVars().size()) {
			smaller = second.getVars();
			bigger = head.getVars();
		} else {
			bigger = second.getVars();
			smaller = head.getVars();
		}
		for (Node i : smaller.getValues())
			if (bigger.contains(i))
				return true;
		return false;
	}
	private NodeSet getSharedVars(PTreeNode first, PTreeNode second) {
		NodeSet smaller = null, bigger = null, intersection = new NodeSet();
		if (first.getVars().size() > second.getVars().size()) {
			smaller = second.getVars();
			bigger = first.getVars();
		} else {
			bigger = second.getVars();
			smaller = first.getVars();
		}
		for (Node i : smaller.getValues())
			if (bigger.contains(i))
				intersection.add(i);
		return intersection;
	}
	private Set<Integer> union(Set<Integer> vars1, Set<Integer> vars2) {
		Set<Integer> union = new HashSet<Integer>();
		for(int strng : vars1){
			if(vars2.contains(strng))
				union.add(strng);
		}
		return union;
	}

	public HashSet<PSubTree> getSubTrees() {
		return subTrees;
	}
	public void setSubTrees(HashSet<PSubTree> subTrees) {
		this.subTrees = subTrees;
	}
	public Hashtable<Integer, PSubTree> getSubTreesMap() {
		return subTreesMap;
	}

	public void setSubTreesMap(Hashtable<Integer, PSubTree> subTreesMap) {
		this.subTreesMap = subTreesMap;
	}
	public RuleUseInfoSet getAllRootRuis(){
		RuleUseInfoSet res = new RuleUseInfoSet();
		for(PSubTree subtree : subTrees)
			
			res.addset(subtree.getRootRUIS());
		return res;
	}


	
	public class PSubTree {
		private PTreeNode root;

		public PSubTree(PTreeNode rot){
			root = rot;
		}

		public RuleUseInfoSet insert(RuleUseInfo rui) {
			FlagNodeSet fns = rui.getFlagNodeSet();
			RuleUseInfoSet res = new RuleUseInfoSet();
			for(FlagNode node : fns){
				int pattern = node.getNode().getId();
			
				PTreeNode leaf = getLeafPattern(pattern, root);
				res = leaf.insertIntoTree(rui, res);
			}
			return res;
		}

		public PTreeNode getLeafPattern(int pattern, PTreeNode pNode) {
			if (pNode.getLeftChild() == null)
				return pNode;
			PTreeNode left = pNode.getLeftChild(), right = pNode.getRightChild();
			if (left.getPats().contains(pattern))
				return getLeafPattern(pattern, left);
			else
				return getLeafPattern(pattern, right);
		}

		public RuleUseInfoSet getRootRUIS() {
			RuleUseInfoSet ruis = root.getRUIS();
			if(ruis == null)
				return new RuleUseInfoSet();
			return ruis;
		}

		public PTreeNode getRoot(){
			return root;
		}
	}

	/**
	 * @className 
	 * 
	 * @ClassDescription The PTreeNode is the lowest level in the PTree, where RUIs are stored and combined. 
	 * 
	 * @author Amgad Ashraf
	 * 
	 * @version 3.00 31/5/2018
	 */
	public class PTreeNode {
		private PTreeNode parent, sibling;
		private PTreeNode leftChild, rightChild;
		private Hashtable<Integer[], RuleUseInfoSet> ruisMap;//Substitutions node IDs
		private Set<Integer> pats;
		private NodeSet vars;
		private NodeSet siblingIntersection;

		public PTreeNode(){
			ruisMap = new Hashtable<Integer[], RuleUseInfoSet>();
			parent = null;			sibling = null;
			leftChild = null;		rightChild = null;
			pats = null;			vars = null;
		}
		public PTreeNode(Set<Integer> p, NodeSet v){
			this();
			pats = p;				vars = v;
		}

		public RuleUseInfoSet insertIntoTree(RuleUseInfo rui, RuleUseInfoSet ruiSet) {
			Integer[] key = insertRUI(rui);
			if (sibling == null) {
				ruiSet.add(rui);
				return ruiSet;
			}
			RuleUseInfoSet siblingSet = sibling.getRUIS(key);
			if (siblingSet == null)
				return ruiSet;
			for (RuleUseInfo tRui : siblingSet) {
				RuleUseInfo combinedRui = rui.combine(tRui);
				if (combinedRui == null)
					continue;
				parent.insertIntoTree(combinedRui, ruiSet);
			}
			return ruiSet;
		}

		public void insertLeftAndRight(PTreeNode leftNode, PTreeNode rightNode,
				NodeSet intersection) {
			leftNode.parent = this;
			rightNode.parent = this;
			leftNode.sibling = rightNode;
			rightNode.sibling = leftNode;
			leftNode.siblingIntersection = intersection;
			rightNode.siblingIntersection = intersection;
			leftChild = leftNode;
			rightChild = rightNode;
		}

		public Integer[] insertRUI(RuleUseInfo rui) {
			if (sibling == null) {
				RuleUseInfoSet ruiSet = ruisMap.get(new Integer[]{0});
				if (ruiSet == null) {
					ruiSet = new RuleUseInfoSet();
					ruisMap.put(new Integer[]{0}, ruiSet);
				}
				ruiSet.add(rui);
				return new Integer[]{0};
			}

//			Integer[] vs = new Integer[siblingIntersection.size()];
//			int index = 0;
//			for (Node var : siblingIntersection.getValues())
//				vs[index++] = rui.getSub().getBindingByVariable(var).getNode().getId();
//			RuleUseInfoSet ruis = ruisMap.get(vs);
		//	if (ruis == null) {
			RuleUseInfoSet	ruis = new RuleUseInfoSet();
			//	ruisMap.put(vs, ruis);
		//	}
			ruis.add(rui);
			return new Integer[]{0};
			//return vs;
		}

		public RuleUseInfoSet getRUIS(){
			RuleUseInfoSet ruiSet = new RuleUseInfoSet();
			Collection<RuleUseInfoSet> mappedRuis = ruisMap.values();
			for(RuleUseInfoSet singleSet : mappedRuis)
				ruiSet.addset(singleSet);
			return ruiSet;
		}

		public RuleUseInfoSet getRUIS(Integer[] index) {
			return ruisMap.get(index);
		}

		public PTreeNode getLeftChild() {
			return leftChild;
		}
		public PTreeNode getRightChild() {
			return rightChild;
		}
		public Set<Integer> getPats() {
			return pats;
		}
		public NodeSet getVars() {
			return vars;
		}
		@Override
		public String toString(){
			return pats.toString();
		}

	}
}
