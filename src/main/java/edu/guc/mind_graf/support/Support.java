package edu.guc.mind_graf.support;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;


public class Support {

	private int nodeID;
	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> justificationSupport;
	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> assumptionSupport;
	private HashMap<Integer, HashMap<Integer, SupportTree>> supportsTree;
	private HashSet<Integer> isHyp;
	private HashMap<Integer, HashSet<Integer>> isTreeCalculatetd;

	public Support(int nodeID){
		// Constructor
		this.nodeID = nodeID;
		justificationSupport = new HashMap<>();
		assumptionSupport = new HashMap<>();
		isHyp = new HashSet<>();
		isTreeCalculatetd = new HashMap<>();
		supportsTree = new HashMap<>();
	}

	public Support(int nodeID, HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> justificationSupport) throws DirectCycleException{
		// Constructor
		setJustificationSupport(justificationSupport);
		this.nodeID = nodeID;
		isHyp = new HashSet<>();
		isTreeCalculatetd = new HashMap<>();
		supportsTree = new HashMap<>();
	}

	public Support(int nodeID, int currentAttitudeID, int level, Pair<PropositionNodeSet,PropositionNodeSet> justificationSupport) throws DirectCycleException{
		// Constructor
		HashMap<Integer,Pair<PropositionNodeSet,PropositionNodeSet>> innerHashMap = new HashMap<>();
		innerHashMap.put(currentAttitudeID, justificationSupport);
		ArrayList< Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> array = new ArrayList<>();
		array.add(new Pair<HashMap<Integer,Pair<PropositionNodeSet,PropositionNodeSet>>, PropositionNodeSet>(innerHashMap,new PropositionNodeSet()));
		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> outerHashMap = new HashMap<>();
		outerHashMap.put(currentAttitudeID, array);
		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> levelHash = new HashMap<>();
		levelHash.put(level, outerHashMap);
		setJustificationSupport(levelHash);
		this.nodeID = nodeID;
		isHyp = new HashSet<>();
		isTreeCalculatetd = new HashMap<>();
		supportsTree = new HashMap<>();
	}


	/**
	 * @return the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	/**
	 * @return the supportsTree
	 */
	public HashMap<Integer, HashMap<Integer, SupportTree>> getSupportsTree() {
		return supportsTree;
	}

	/**
	 * @return the isHyp
	 */
	public HashSet<Integer> getIsHyp() {
		return isHyp;
	}

	/**
	 * @return the isTreeCalculatetd
	 */
	public HashMap<Integer, HashSet<Integer>> getisTreeCalculatetd() {
		return isTreeCalculatetd;
	}

	/**
	 * @return the justificationSupport
	 */
	public HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> getJustificationSupport() {
		return justificationSupport;
	}

	/**
	 * @return the assumptionSupport
	 */
	public HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> getAssumptionSupport() {
		return assumptionSupport;
	}

	/**
	 * @param justificationSupport the justificationSupport to set
	 * @throws DirectCycleException
	 */
	private void setJustificationSupport(
			HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> justificationSupport) throws DirectCycleException {
		for(Integer level : justificationSupport.keySet()) {
			if(hasCycle(justificationSupport.get(level))){
				throw new DirectCycleException("Direct Cycle found");
			}
		}

		for(Integer level : justificationSupport.keySet()) {
			this.assumptionSupport = createAssumptionSupport(justificationSupport.get(level));
		}

		this.justificationSupport = justificationSupport;
	}

	/**
	 * Adds the specified supports in the list to the justification based support
	 * and gets their cross products in the assumption based support
	 * @param attitudeID attitude to add justification in
	 * @param level Grade level to add justification in
	 * @param justificationSupport list of supports to add
	 * @throws DirectCycleException
	 */
	public void addJustificatoinSupportForAttitude(int attitudeID, int level,  ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> justificationSupport) throws DirectCycleException {
		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> temp = new HashMap<>();
		temp.put(attitudeID, justificationSupport);
		if(hasCycle(temp)){
			throw new DirectCycleException("Direct Cycle found");
		}
		else {

			HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> assumptionsTemp = createAssumptionSupport(temp);

			for(Integer assumptionlevel : assumptionsTemp.keySet()) {
				if(this.assumptionSupport.containsKey(assumptionlevel)) {
					if(this.assumptionSupport.get(assumptionlevel).containsKey(attitudeID)){
						this.assumptionSupport.get(assumptionlevel).get(attitudeID).addAll(assumptionsTemp.get(assumptionlevel).get(attitudeID));
					}
					else {
						this.assumptionSupport.get(assumptionlevel).put(assumptionlevel, assumptionsTemp.get(assumptionlevel).get(attitudeID));
					}
				}
				else {
					this.assumptionSupport.put(assumptionlevel, assumptionsTemp.get(assumptionlevel));
				}
			}

			if(this.justificationSupport.containsKey(level)) {
				if(this.justificationSupport.get(level).containsKey(attitudeID)){
					this.justificationSupport.get(level).get(attitudeID).addAll(justificationSupport);
				}
				else {
					this.justificationSupport.get(level).put(attitudeID, justificationSupport);
				}
			}
			else {
				this.justificationSupport.put(level, temp);
			}
			isTreeCalculatetd.remove(attitudeID);
		}
	}

	private boolean hasCycle(
			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> justificationSupport) {

		PropositionNodeSet dependents = new PropositionNodeSet();
		for (Integer supportedAttitude : justificationSupport.keySet()) {
			ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> Supports = justificationSupport.get(supportedAttitude);
			for (Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> supportAttitudes : Supports) {
				for (Integer attitude : supportAttitudes.getFirst().keySet()) {
					PropositionNodeSet supportProps1 = supportAttitudes.getFirst().get(attitude).getFirst();
					int[] propsIDs1 = supportProps1.getProps();
					for (int id : propsIDs1) {
						if(attitude == supportedAttitude && id == nodeID) {
							return true;
						}
						dependents.add(id);
					}
				}
			}
		}

		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();
		for(int currProp : dependents.getValues()) {
			PropositionNode depNode = (PropositionNode) networkPropositions.get(currProp);
			if(nodeID != -1) {
				depNode.getJustificationSupportDependents().add(nodeID);
			}
		}
		return false;
	}


	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> createAssumptionSupport(
			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> justificationSupport) {

		PropositionNodeSet dependents = new PropositionNodeSet();
		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> newAssumptionSupport = new HashMap<>();
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();

		//iterate over the supported attitudes of the justification support(Outer HashMap)
		for (Integer supportedAttitude : justificationSupport.keySet()) {
			ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> supportsHashMapArray = justificationSupport.get(supportedAttitude);
			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> newAssumptionsHashMapArray = new HashMap<>();

			//iterate over the Supporting Attitudes of current supported attitude(Outer HashMap)
			for (Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> supportingAttitudes : supportsHashMapArray) {
				ArrayList<HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> temptogetcrossproduct = new ArrayList<>();

				//iterate over the supports of current support to get cross product(ArrayList of HashMap<Integer, PropositionNodeSet> of supports)(Inner HashMap)
				for (Integer supportingAttitude : supportingAttitudes.getFirst().keySet()) {

					Pair<PropositionNodeSet,PropositionNodeSet> supportPropsSets = supportingAttitudes.getFirst().get(supportingAttitude);
					PropositionNodeSet supportPropsSet = supportPropsSets.getFirst();
					supportPropsSet.putAll(supportPropsSets.getSecond().getValues());
					int[] propsIDs = supportPropsSet.getProps();


					temptogetcrossproduct.add(getCrossProductOfNodeSet(propsIDs, networkPropositions, supportingAttitude, supportedAttitude, dependents,supportingAttitudes.getSecond()));
				}
				newAssumptionsHashMapArray = getCrossProductBetweenAttitudes(temptogetcrossproduct);
			}
			newAssumptionSupport.put(supportedAttitude, newAssumptionsHashMapArray);
		}

		for(Integer dep: dependents.getValues()) {
			PropositionNode depNode = (PropositionNode) networkPropositions.get(dep);
			depNode.getAssumptionSupportDependents().add(nodeID);
		}
		return newAssumptionSupport;
	}

	private HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> getCrossProductOfNodeSet(int[]propsIDs, HashMap<Integer, Node> networkPropositions, int supportingAttitude, int supportedAttitude, PropositionNodeSet dependents,PropositionNodeSet bridgeRules) {

		PropositionNode firstSupp = (PropositionNode) networkPropositions.get(propsIDs[0]);
		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> result = new HashMap<>();
		for(Integer level : firstSupp.getSupport().getAssumptionSupport().keySet()) {
			result.put(level, firstSupp.getSupport().getAssumptionSupport().get(level).get(supportingAttitude));
		}

		ArrayList<Integer> keys = new ArrayList<>(result.keySet());
		for(Integer level : keys) {
			for(int i = 0; i < result.get(level).size(); i++) {//handling Indirect Cycles and bridge rules
				Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr = result.get(level).get(i);
				if(curr.getFirst().containsKey(supportedAttitude) && curr.getFirst().get(supportedAttitude).getFirst().contains(nodeID)) {
					result.get(level).get(i).getFirst().remove(supportedAttitude);
				}
				if(curr.getFirst().isEmpty()) {
					result.get(level).remove(i);
					i--;
				}
				else {
					result.get(level).get(i).getSecond().putAll(bridgeRules.getValues());
				}
			}
			if(result.get(level).isEmpty()) {
				result.remove(level);
			}
		}

		for(int i = 1; i < propsIDs.length; i++) {

			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> levelstemp = new HashMap<>();

			PropositionNode currNode = (PropositionNode) networkPropositions.get(propsIDs[i]);
			for(Integer level : currNode.getSupport().getAssumptionSupport().keySet()) {

				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> temp = new ArrayList<>();

				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> currSuppAssumptions = currNode.getSupport().getAssumptionSupport().get(level).get(supportingAttitude);

				if(result.containsKey(level)) {

					for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> currSupport : currSuppAssumptions) {

						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> combination : result.get(level)) {

							Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> newSupport = combination;

							for(Integer currAttitude : currSupport.getFirst().keySet()) {
								if(!(currAttitude == supportedAttitude && currSupport.getFirst().get(currAttitude).getFirst().contains(nodeID))) {//handling Indirect Cycles
									if(combination.getFirst().containsKey(currAttitude)) {
										Pair<PropositionNodeSet,PropositionNodeSet> oldPair = combination.getFirst().get(currAttitude);
										Pair<PropositionNodeSet,PropositionNodeSet> currPair = currSupport.getFirst().get(currAttitude);

										PropositionNodeSet newPropSet1 = oldPair.getFirst();
										PropositionNodeSet newPropSet2 = oldPair.getSecond();

										newPropSet1.putAll(currPair.getFirst().getValues());
										newPropSet2.putAll(currPair.getSecond().getValues());



										// for(int oldProp : oldPropsSet.getProps()) {
										// 	newPropSet.add(oldProp);
										// 	dependents.add(oldProp);
										// }
										// for(int currProp : currPropsSet.getProps()) {
										// 	newPropSet.add(currProp);
										// 	dependents.add(currProp);
										// }

										for(int currProp : newPropSet1.getValues()) {
											PropositionNode depNode = (PropositionNode) networkPropositions.get(currProp);
											if(nodeID != -1) {
												depNode.getAssumptionSupportDependents().add(nodeID);
											}
										}
										HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Inner = new HashMap<>();
										Inner.put(currAttitude, new Pair<PropositionNodeSet,PropositionNodeSet>(newPropSet1,newPropSet2));
										PropositionNodeSet bridge = combination.getSecond();
										bridge.putAll(currSupport.getSecond().getValues());
										newSupport = new Pair<>(Inner, bridge);
									}
									else {
										for(int currProp : currSupport.getFirst().get(currAttitude).getFirst().getProps()) {
											PropositionNode depNode = (PropositionNode) networkPropositions.get(currProp);
											if(nodeID != -1) {
												depNode.getAssumptionSupportDependents().add(nodeID);
											}
										}
										HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Inner = new HashMap<>();
										Inner.put(currAttitude, currSupport.getFirst().get(currAttitude));
										PropositionNodeSet bridge = combination.getSecond();
										bridge.putAll(bridgeRules.getValues());
										bridge.putAll(currSupport.getSecond().getValues());
										newSupport = new Pair<>(Inner, bridge);
									}
								}
							}
							temp.add(newSupport);
						}
					}
				}
				else {
					for(int y = 0; y < currSuppAssumptions.size(); y++) {//handling Indirect Cycles
						Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr = currSuppAssumptions.get(y);
						if(curr.getFirst().containsKey(supportedAttitude) && curr.getFirst().get(supportedAttitude).getFirst().contains(nodeID)) {
							currSuppAssumptions.get(y).getFirst().remove(supportedAttitude);
						}
						if(curr.getFirst().isEmpty()) {
							currSuppAssumptions.remove(y);
							y--;
						}
						else {
							currSuppAssumptions.get(y).getSecond().putAll(bridgeRules.getValues());
						}
					}
					if(!currSuppAssumptions.isEmpty()) {
						temp = currSuppAssumptions;
					}
				}
				if(!temp.isEmpty()) {
					levelstemp.put(level, temp);
				}
			}
			result = levelstemp;
		}
		return result;

	}


	private HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> getCrossProductBetweenAttitudes(ArrayList<HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> input) {

		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> result = new HashMap<>(input.get(0));

		for(int i = 1; i < input.size(); i++) {

			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> levelstemp = new HashMap<>();

			for(Integer level : input.get(i).keySet()) {

				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> temp = new ArrayList<>();

				if(result.containsKey(level)) {

					for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr : input.get(level).get(i)) {

						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> combination : result.get(level)) {

							Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> newSupport = combination;

							for(Integer currAttitude : curr.getFirst().keySet()) {

								if(combination.getFirst().containsKey(currAttitude)) {


									Pair<PropositionNodeSet,PropositionNodeSet> oldPair = combination.getFirst().get(currAttitude);
									Pair<PropositionNodeSet,PropositionNodeSet> currPair = curr.getFirst().get(currAttitude);

									PropositionNodeSet newPropSet1 = oldPair.getFirst();
									PropositionNodeSet newPropSet2 = oldPair.getSecond();

									newPropSet1.putAll(currPair.getFirst().getValues());
									newPropSet2.putAll(currPair.getSecond().getValues());
									HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Inner = new HashMap<>();
									Inner.put(currAttitude, new Pair<PropositionNodeSet,PropositionNodeSet>(newPropSet1,newPropSet2));
									PropositionNodeSet bridge = combination.getSecond();
									bridge.putAll(curr.getSecond().getValues());
									newSupport = new Pair<>(Inner, bridge);

								}
								else {
									HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Inner = new HashMap<>();
									Inner.put(currAttitude, curr.getFirst().get(currAttitude));
									PropositionNodeSet bridge = combination.getSecond();
									bridge.putAll(curr.getSecond().getValues());
									newSupport = new Pair<>(Inner, bridge);
								}
							}
							temp.add(newSupport);
						}
					}
				}
				else {
					temp = input.get(level).get(i);
				}
				if(!temp.isEmpty()) {
					levelstemp.put(level, temp);
				}
			}
			result = levelstemp;
		}
		return result;
	}

	/**
	 * Union two whole supports with each other
	 * @param support support to be unionized with
	 * @throws DirectCycleException
	 */
	public void union(Support support) throws DirectCycleException {
		for(Integer level : support.getJustificationSupport().keySet()){
			for(Integer attitude : support.getJustificationSupport().get(level).keySet()) {
				this.addJustificatoinSupportForAttitude(attitude, level, support.getJustificationSupport().get(level).get(attitude));
				if(this.justificationSupport.containsKey(level)) {
					if(this.justificationSupport.get(level).containsKey(attitude)){
						this.justificationSupport.get(level).get(attitude).addAll(support.getJustificationSupport().get(level).get(attitude));
					}
					else {
						this.justificationSupport.get(level).put(attitude, support.getJustificationSupport().get(level).get(attitude));
					}
				}
				else {
					this.justificationSupport.put(level, support.getJustificationSupport().get(level));
				}
			}
		}

		for(Integer level : support.getJustificationSupport().keySet()){
			for(Integer attitude : support.getJustificationSupport().get(level).keySet()) {
				if(this.assumptionSupport.containsKey(level)) {
					if(this.assumptionSupport.get(level).containsKey(attitude)){
						this.assumptionSupport.get(level).get(attitude).addAll(support.getAssumptionSupport().get(level).get(attitude));
					}
					else {
						this.assumptionSupport.get(level).put(attitude, support.getAssumptionSupport().get(level).get(attitude));
					}
				}
				else {
					this.assumptionSupport.put(level, support.getAssumptionSupport().get(level));
				}
			}
		}
	}

	/**
	 * Adds a Proposition Node to this support
	 * @param attitude attitude to add the node in
	 * @param propositionNode Proposition Node to be added
	 * @throws DirectCycleException
	 */
	public void addNode(int attitude, PropositionNode propositionNode) throws DirectCycleException {

		if(this.nodeID == propositionNode.getId()) {
			throw new DirectCycleException("Direct Cycle found");
		}

		PropositionNodeSet prop = new PropositionNodeSet(propositionNode);
		Pair<PropositionNodeSet, PropositionNodeSet> innerPair = new Pair<>(prop, new PropositionNodeSet());
		HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
		hash.put(attitude, innerPair);
		Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> outerPair = new Pair<>(hash, new PropositionNodeSet());
		ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> list = new ArrayList<>();
		list.add(outerPair);
		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> outerHash = new HashMap<>();
		outerHash.put(attitude, list);

		if(this.justificationSupport.containsKey(0)) {
			if(this.justificationSupport.get(0).containsKey(attitude)){
				this.justificationSupport.get(0).get(attitude).add(outerPair);
			}
			else {
				this.justificationSupport.get(0).put(attitude, list);
			}
		}
		else {
			this.justificationSupport.put(0, outerHash);
		}

		for(Integer nodeLevel : propositionNode.getSupport().getAssumptionSupport().keySet()) {
			if(this.assumptionSupport.containsKey(nodeLevel)) {
				if(this.assumptionSupport.get(nodeLevel).containsKey(attitude)){
					this.assumptionSupport.get(nodeLevel).get(attitude).addAll(propositionNode.getSupport().getAssumptionSupport().get(nodeLevel).get(attitude));
				}
				else {
					this.assumptionSupport.get(nodeLevel).put(attitude, propositionNode.getSupport().getAssumptionSupport().get(nodeLevel).get(attitude));
				}
			}
			else {
				this.assumptionSupport.put(nodeLevel, propositionNode.getSupport().getAssumptionSupport().get(nodeLevel));
			}
		}
		propositionNode.getJustificationSupportDependents().add(this.nodeID);
	}

	/**
	 * Make this support a hypothesis in the specified attitude
	 * @param attitudeID attitude to this support a hypothesis in
	 */
	public void setHyp(int attitudeID) {
		if(isHyp.contains(attitudeID)){
			return;
		}
		//Add itself to its own assumptionSupport
		PropositionNodeSet hypSet = new PropositionNodeSet(this.nodeID);
		Pair<PropositionNodeSet,PropositionNodeSet> hypPair = new Pair<>(hypSet, new PropositionNodeSet());
		HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Hyp = new HashMap<>();
		Hyp.put(attitudeID, hypPair);
		Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> pair = new Pair<>(Hyp, new PropositionNodeSet());
		if (!assumptionSupport.get(0).containsKey(attitudeID)) {
			assumptionSupport.get(0).put(attitudeID, new ArrayList<>());
		}
		this.assumptionSupport.get(0).get(attitudeID).add(pair);
		//Add the attitude to the isHyp
		isHyp.add(attitudeID);
		isTreeCalculatetd.remove(attitudeID);
	}

	/**
	 * Checks if this support has any children in the specified attitude
	 * @param attitudeID attitude to check in
	 */
	public boolean hasChildren(int attitudeID) {

		if(!justificationSupport.containsKey(attitudeID) && ! assumptionSupport.containsKey(attitudeID)) {
			return false;
		}

		boolean empty = true;

		for(Integer level : justificationSupport.keySet()) {
			for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr : justificationSupport.get(level).get(attitudeID)) {
				for(Integer currKey : curr.getFirst().keySet()) {
					if(!curr.getFirst().get(currKey).getFirst().equals(null)) {
						empty = false;
						break;
					}
				}
				if(empty == false) {
					break;
				}
				break;
			}
		}

		for(Integer level : assumptionSupport.keySet()) {
			for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr : assumptionSupport.get(level).get(attitudeID)) {
				for(Integer currKey : curr.getFirst().keySet()) {
					if(!curr.getFirst().get(currKey).getFirst().equals(null)) {
						empty = false;
						break;
					}
				}
				if(empty == false) {
					break;
				}
				break;
			}
		}

		return !empty;
	}


	/**
	 * Removes the specified node from the justification based support
	 * @param id node to be removed
	 */
	public void removeNodeFromJustifications (int id) {
		for(Integer level : justificationSupport.keySet()) {
			removeNodeFromSupportHelper(id, justificationSupport.get(level));
		}
	}


	/**
	 * Removes the specified node from the justification based support
	 * @param id node to be removed
	 */
	public void removeNodeFromAssumptions (int id) {
		for(Integer level : assumptionSupport.keySet()) {
			removeNodeFromSupportHelper(id, assumptionSupport.get(level));
		}
	}

	private void removeNodeFromSupportHelper(int id, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> CurrentSupport) {
		Iterator<Integer> iterator = CurrentSupport.keySet().iterator();
		while (iterator.hasNext()) {
			Integer supportedAttitude = iterator.next();
			ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> Supports = CurrentSupport.get(supportedAttitude);
			Iterator<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> innerIterator = Supports.iterator();
			while (innerIterator.hasNext()) {
				Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> support = innerIterator.next();
				boolean flag = true;
				if (support.getSecond().contains(id)) {
					innerIterator.remove(); // Safely remove the support from the list
					isTreeCalculatetd.remove(supportedAttitude);
					flag = false;

				}
				if(flag) {
					for (Integer supportingAttitude : support.getFirst().keySet()) {
						PropositionNodeSet supportProps1 = support.getFirst().get(supportingAttitude).getFirst();
						PropositionNodeSet supportProps2 = support.getFirst().get(supportingAttitude).getSecond();
						if (supportProps1.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
							isTreeCalculatetd.remove(supportedAttitude);
							break;
						}
						if (supportProps2.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
							isTreeCalculatetd.remove(supportedAttitude);
							break;
						}
					}
				}
			}
			if (Supports.isEmpty()) {
				iterator.remove(); // Safely remove the attitude entry from the map
				CurrentSupport.remove(supportedAttitude);
			}
		}
	}


	/**
	 * Forgets the specified node from the justification based support
	 * and makes it a hypothesis if this forgetting empties its supports
	 * @param id node to be forgotten
	 */
	public void ForgetNodeFromJustifications (int id) {
		for(Integer level : justificationSupport.keySet()) {
			ForgetNodeFromSupportHelper(id, justificationSupport.get(level),1);
		}
	}


	/**
	 * Forgets the specified node from the justification based support
	 * and makes it a hypothesis if this forgetting empties its supports
	 * @param id node to be forgotten
	 */
	public void ForgetNodeFromAssumptions (int id) {
		for(Integer level : assumptionSupport.keySet()) {
			ForgetNodeFromSupportHelper(id, assumptionSupport.get(level),2);
		}
	}


	private void ForgetNodeFromSupportHelper(int id, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> CurrentSupport,int i) {
		Iterator<Integer> iterator = CurrentSupport.keySet().iterator();
		ArrayList<Integer> temp = new ArrayList<>();
		while (iterator.hasNext()) {
			Integer supportedAttitude = iterator.next();
			ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> Supports = CurrentSupport.get(supportedAttitude);
			Iterator<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> innerIterator = Supports.iterator();
			while (innerIterator.hasNext()) {
				Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> support = innerIterator.next();
				boolean flag = true;
				if (support.getSecond().contains(id)) {
					innerIterator.remove(); // Safely remove the support from the list
					isTreeCalculatetd.remove(supportedAttitude);
					flag = false;

				}
				if(flag) {
					for (Integer supportingAttitude : support.getFirst().keySet()) {
						PropositionNodeSet supportProps1 = support.getFirst().get(supportingAttitude).getFirst();
						PropositionNodeSet supportProps2 = support.getFirst().get(supportingAttitude).getSecond();
						if (supportProps1.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
							isTreeCalculatetd.remove(supportedAttitude);
							break;
						}
						if (supportProps2.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
							isTreeCalculatetd.remove(supportedAttitude);
							break;
						}
					}
				}
			}
			if (Supports.isEmpty()) {
				iterator.remove(); // Safely remove the attitude entry from the map
				CurrentSupport.remove(supportedAttitude);
				temp.add(supportedAttitude);
			}
		}
		for(Integer supps : temp){
			setHyp(supps);
		}
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Node").append(nodeID).append(": ").append("\n");
		for(Integer level : justificationSupport.keySet()) {
			sb.append("Level").append(level).append(": ").append("\n");
			sb.append(mapToString(" JustificationSupport", justificationSupport.get(level)));
		}
		for(Integer level : justificationSupport.keySet()) {
			sb.append("Level").append(level).append(": ").append("\n");
			sb.append(mapToString(" AssumptionSupport", assumptionSupport.get(level)));
		}
		return sb.toString();
	}


	private String mapToString(String title, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> map) {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append(":\n");

		// Iterate through the outer HashMap
		for (Map.Entry<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> entry : map.entrySet()) {
			Integer key = entry.getKey();
			ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> valueList = entry.getValue();

			sb.append("   Supported Attitude: ").append(key).append("\n");
			sb.append("   Supports:\n");

			// Iterate through the ArrayList
			for (int i = 0; i < valueList.size(); i++) {
				sb.append("     Support ").append(i+1).append(":").append("\n");

				Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> innerMap = valueList.get(i);
				// Iterate through the inner HashMap
				for (Map.Entry<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerEntry : innerMap.getFirst().entrySet()) {
					Integer innerKey = innerEntry.getKey();
					PropositionNodeSet OriginNodeSet = innerEntry.getValue().getFirst();

					sb.append("       In Attitude ").append(innerKey).append(":").append("\n");
					sb.append("       Origin PropositionNodeSet: ").append(OriginNodeSet.toString()).append("\n");

					PropositionNodeSet GradedNodeSet = innerEntry.getValue().getSecond();

					sb.append("       Graded PropositionNodeSet: ").append(GradedNodeSet.toString()).append("\n");
				}
				sb.append("               Bridge Rules: ").append(innerMap.getSecond().toString()).append("\n");
			}
			sb.append("\n"); // Extra newline for better separation
		}

		return sb.toString();
	}

	public Support clone(){
		return new Support(-1);
	}



	/*
	public void calculateSupportsTree() {

		for(Integer level : justificationSupport.keySet()) {
			if(!isTreeCalculatetd.containsKey(level)) {
				isTreeCalculatetd.put(level, new HashSet<Integer>());
			}
			for(Integer attitude : justificationSupport.get(level).keySet()) {
				if(!isTreeCalculatetd.get(level).contains(attitude)) {
					if(!supportsTree.containsKey(level)) {
						supportsTree.put(level, new HashMap<Integer, SupportTree>());
					}
					supportsTree.get(level).put(attitude, calculateSupportTreeForAttitude(level, attitude));
				}
			}
		}

	}

	public SupportTree calculateSupportTreeForAttitude(int level, int Attitude) {

		ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> supports = justificationSupport.get(level).get(Attitude);
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();

		SupportTree supportTree = new SupportTree(nodeID);
		for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> support : supports) {
			HashMap<Integer, Pair<ArrayList<SupportTree>,ArrayList<SupportTree>>> attitudesSupportTrees = new HashMap<>();
			for(Integer supportingAttitude : support.getFirst().keySet()) {
				Pair<ArrayList<SupportTree>,ArrayList<SupportTree>> supportTrees = new Pair<>(new ArrayList<>(),new ArrayList<>());
				PropositionNodeSet currSupports1 = support.getFirst().get(supportingAttitude).getFirst();
				for(int id : currSupports1.getProps()) {
					PropositionNode currNode = (PropositionNode)networkPropositions.get(id);
					//If the tree is calculated for supporting attitude add it to the supportTrees
					if(currNode.getSupport().isTreeCalculatetd.contains(supportingAttitude)) {
						supportTrees.getFirst().add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
					}
					else {
						//calculate the support tree for supporting attitude and then add it
						currNode.getSupport().calculateSupportTreeForAttitude(supportingAttitude);
						supportTrees.getFirst().add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
					}
				}
				attitudesSupportTrees.put(supportingAttitude, supportTrees);

//				PropositionNodeSet currSupports2 = support.get(supportingAttitude).getSecond();
//				for(int id : currSupports2.getProps()) {
//					PropositionNode currNode = (PropositionNode)networkPropositions.get(id);
//					//If the tree is calculated for supporting attitude add it to the supportTrees
//					if(currNode.getSupport().isTreeCalculatetd.contains(supportingAttitude)) {
//						supportTrees.getSecond().add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
//					}
//					else {
//						//calculate the support tree for supporting attitude and then add it
//						currNode.getSupport().calculateSupportTreeForAttitude(supportingAttitude);
//						supportTrees.getSecond().add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
//					}
//				}
//				attitudesSupportTrees.put(supportingAttitude, supportTrees);

			}
			supportTree.getChildren().add(attitudesSupportTrees);
		}
		isTreeCalculatetd.add(Attitude);
		return supportTree;
	}
	*/
}