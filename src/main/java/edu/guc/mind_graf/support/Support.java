package edu.guc.mind_graf.support;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;


public class Support implements Cloneable{

	private final int nodeID;
	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> justificationSupport;
	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> assumptionSupport;
	private HashMap<Integer, HashSet<Integer>> isHyp;
	//	private HashMap<Integer, HashMap<Integer, SupportTree>> supportsTree;
	//	private HashMap<Integer, HashSet<Integer>> isTreeCalculated;

	public Support(int nodeID) {
		// Constructor
		this.nodeID = nodeID;
		justificationSupport = new HashMap<>();

		assumptionSupport = new HashMap<>();

		isHyp = new HashMap<>();
	}

	public Support(int nodeID, int currentAttitudeID, int level, HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> justificationSupport, PropositionNodeSet bridgeRules) {
		// Constructor
		ArrayList< Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> array = new ArrayList<>();
		array.add(new Pair<>(justificationSupport, bridgeRules));
		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> outerHashMap = new HashMap<>();
		outerHashMap.put(currentAttitudeID, array);
		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>>> levelHash = new HashMap<>();
		levelHash.put(level, outerHashMap);
		setJustificationSupport(levelHash);
		this.nodeID = nodeID;
		isHyp = new HashMap<>();
	}


	/**
	 * @return the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}


	/**
	 * @return a copy of isHyp
	 */
	public HashMap<Integer, HashSet<Integer>> getIsHyp() {
		HashMap<Integer, HashSet<Integer>> cloneIsHyp = new HashMap<>();
		for(Integer level : isHyp.keySet()){
			cloneIsHyp.put(level, new HashSet<>(this.isHyp.get(level)));
		}
		return cloneIsHyp;
	}

	/**
	 * @return a copy of justificationSupport
	 */
	public HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> getJustificationSupport() {
		return cloneSupport(justificationSupport);
	}

	/**
	 * @return a copy of assumptionSupport
	 */
	public HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> getAssumptionSupport() {
		return cloneSupport(assumptionSupport);
	}

	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> cloneSupport(HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> support){
		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> copy = new HashMap<>();

		for (Map.Entry<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> entry : support.entrySet()) {
			int level = entry.getKey();
			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> attitudeMap = new HashMap<>();

			for (Map.Entry<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> innerEntry : entry.getValue().entrySet()) {
				int innerKey = innerEntry.getKey();
				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> list = new ArrayList<>();

				for (Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> pair : innerEntry.getValue()) {

					HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerAttitudeMap = new HashMap<>();
					for (Map.Entry<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerentry : pair.getFirst().entrySet()) {
						Integer key = innerentry.getKey();

						Pair<PropositionNodeSet,PropositionNodeSet> value = innerentry.getValue();
						PropositionNodeSet originSet = new PropositionNodeSet();

						originSet.putAll(value.getFirst().getValues());
						PropositionNodeSet gradeSet = new PropositionNodeSet();
						gradeSet.putAll(value.getSecond().getValues());

						innerAttitudeMap.put(key, new Pair<>(originSet, gradeSet));
					}

					PropositionNodeSet bridgeRules = new PropositionNodeSet();
					bridgeRules.putAll(pair.getSecond().getValues());

					// Add the Outer Pair object to the list
					list.add(new Pair<>(innerAttitudeMap, bridgeRules));
				}

				// Put the list into the attitudeMap
				attitudeMap.put(innerKey, list);
			}

			// Put the attitudeMap into the copy map
			copy.put(level, attitudeMap);
		}

		return copy;
	}

	/**
	 * @param justificationSupport the justificationSupport to set
	 */
	private void setJustificationSupport(
			HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> justificationSupport) {

		this.justificationSupport = new HashMap<>();
		this.assumptionSupport = new HashMap<>();

		for (Integer level : justificationSupport.keySet()) {
			for (Integer attitude : justificationSupport.get(level).keySet()) {
				addJustificationSupportForAttitude(attitude, level, justificationSupport.get(level).get(attitude));
			}
		}

	}

	/**
	 * Adds the specified supports in the list to the justification based support
	 * and gets their cross products in the assumption based support
	 * @param attitudeID attitude to add justification in
	 * @param level Grade level to add justification in
	 * @param justificationSupport list of supports to add
	 */
	public void addJustificationSupportForAttitude(int attitudeID, int level, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> justificationSupport) {

		//Clean empty hashmaps and proposition sets and check if justification support to be added is empty and clean duplicates
		ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> justificationTempSupports = new ArrayList<>();
		for(int i = 0; i < justificationSupport.size(); i++){
			if(justificationTempSupports.contains(justificationSupport.get(i))){
				justificationSupport.remove(i);
				i--;
			}
			else {
				justificationTempSupports.add(justificationSupport.get(i));
				ArrayList<Integer> innerAttitudes = new ArrayList<>(justificationSupport.get(i).getFirst().keySet());
				for (Integer innerAttitude : innerAttitudes) {
					if (justificationSupport.get(i).getFirst().get(innerAttitude).getFirst().isEmpty()) {
						justificationSupport.get(i).getFirst().remove(innerAttitude);
					}
				}
				if (justificationSupport.get(i).getFirst().isEmpty()) {
					justificationSupport.remove(i);
					i--;
				}
			}
		}
		if(justificationSupport.isEmpty()){
			return;
		}

		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> temp = new HashMap<>();
		temp.put(attitudeID, justificationSupport);

		//Check for direct cycles
		if(hasCycle(temp)){
			return;
		}

		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> assumptionsTemp = createAssumptionSupport(attitudeID, justificationSupport);

		//Clean empty hashmaps and proposition sets and check if assumptionsTemp support to be added is empty and clean duplicates
		ArrayList<Integer> levels = new ArrayList<>(assumptionsTemp.keySet());
		for(Integer assumptionLevel : levels) {
			if(assumptionsTemp.get(assumptionLevel)!= null) {
				ArrayList<Integer> attitudes = new ArrayList<>(assumptionsTemp.get(assumptionLevel).keySet());
				for (Integer attitude : attitudes) {
					if(assumptionsTemp.get(assumptionLevel).get(attitude)!= null) {
						ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> tempSupports = new ArrayList<>();
						for (int i = 0; i < assumptionsTemp.get(assumptionLevel).get(attitude).size(); i++) {
							if(tempSupports.contains(assumptionsTemp.get(assumptionLevel).get(attitude).get(i))){
								assumptionsTemp.get(assumptionLevel).get(attitude).remove(i);
								i--;
							}
							else {
								tempSupports.add(assumptionsTemp.get(assumptionLevel).get(attitude).get(i));
								ArrayList<Integer> innerAttitudes = new ArrayList<>(assumptionsTemp.get(assumptionLevel).get(attitude).get(i).getFirst().keySet());
								for (Integer innerAttitude : innerAttitudes) {
									if (assumptionsTemp.get(assumptionLevel).get(attitude).get(i).getFirst().get(innerAttitude).getFirst().isEmpty()) {
										assumptionsTemp.get(assumptionLevel).get(attitude).get(i).getFirst().remove(innerAttitude);
									}
								}
								if (assumptionsTemp.get(assumptionLevel).get(attitude).get(i).getFirst().isEmpty()) {
									assumptionsTemp.get(assumptionLevel).get(attitude).remove(i);
									i--;
								}
							}
						}
					}
					else{
						assumptionsTemp.get(assumptionLevel).remove(attitude);
					}
					if (assumptionsTemp.get(assumptionLevel).get(attitude).isEmpty()) {
						assumptionsTemp.get(assumptionLevel).remove(attitude);
					}
				}
			}
			else{
				assumptionsTemp.remove(assumptionLevel);
			}
			if (assumptionsTemp.get(assumptionLevel).isEmpty()) {
				assumptionsTemp.remove(assumptionLevel);
			}
		}

		for(Integer assumptionLevel : assumptionsTemp.keySet()) {
			if(this.assumptionSupport.containsKey(assumptionLevel)) {
				for(Integer attitude : assumptionsTemp.get(assumptionLevel).keySet()) {
					if (this.assumptionSupport.get(assumptionLevel).containsKey(attitude)) {
						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> support : assumptionsTemp.get(assumptionLevel).get(attitude)) {
							if(!this.assumptionSupport.get(assumptionLevel).get(attitude).contains(support)) {
								this.assumptionSupport.get(assumptionLevel).get(attitude).add(support);
							}
						}
					} else if(assumptionsTemp.get(assumptionLevel).get(attitude) != null){
						this.assumptionSupport.get(assumptionLevel).put(attitude, assumptionsTemp.get(assumptionLevel).get(attitude));
					}
				}
			}
			else if(assumptionsTemp.get(assumptionLevel) != null){
				this.assumptionSupport.put(assumptionLevel, assumptionsTemp.get(assumptionLevel));
			}
		}

		if(this.justificationSupport.containsKey(level)) {
			if(this.justificationSupport.get(level).containsKey(attitudeID)){
				for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> support : justificationSupport) {
					if(!this.justificationSupport.get(level).get(attitudeID).contains(support)) {
						this.justificationSupport.get(level).get(attitudeID).add(support);
					}
				}
			}
			else {
				this.justificationSupport.get(level).put(attitudeID, justificationSupport);
			}
		}
		else {
			this.justificationSupport.put(level, temp);
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
						if(Objects.equals(attitude, supportedAttitude) && id == nodeID) {
							return true;
						}
						dependents.add(id);
					}
				}
			}
		}

		if(nodeID >= 1) {
			HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();
			for(int currProp : dependents.getValues()) {
				if(nodeID != currProp && networkPropositions.containsKey(currProp)) {
					PropositionNode depNode = (PropositionNode) networkPropositions.get(currProp);
					depNode.addNodeToJustificationSupportDependents(nodeID);
				}
			}
		}
		return false;
	}


	private HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> createAssumptionSupport(
			int supportedAttitude, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> justificationSupports) {

		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> newAssumptionSupport = new HashMap<>();
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();

		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> newAssumptionsHashMapArray = new HashMap<>();

		//iterate over the supports
		for (Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> support : justificationSupports) {
			ArrayList<HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> tempToGetCrossProduct = new ArrayList<>();

			//iterate over the supporting attitudes in the current support
			for (Integer supportingAttitude : support.getFirst().keySet()) {

				PropositionNodeSet supportPropsSet = support.getFirst().get(supportingAttitude).getFirst();
				supportPropsSet.putAll(support.getFirst().get(supportingAttitude).getSecond().getValues());
				int[] propsIDs = supportPropsSet.getProps();

				//get cross product between the assumption supports of the supporting proposition nodes
				tempToGetCrossProduct.add(getCrossProductOfNodeSet(propsIDs, networkPropositions, supportingAttitude, supportedAttitude,support.getSecond()));
			}
			if(tempToGetCrossProduct.size()>1){
				HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> tempHash = getCrossProductBetweenAttitudes(tempToGetCrossProduct);
				for(Integer level : tempHash.keySet()) {
					if(newAssumptionsHashMapArray.containsKey(level)) {
						newAssumptionsHashMapArray.get(level).addAll(tempHash.get(level));
					}
					else{
						newAssumptionsHashMapArray.put(level, tempHash.get(level));
					}
				}
			}
			else{
				if(!tempToGetCrossProduct.isEmpty() && !tempToGetCrossProduct.get(0).isEmpty()){
					HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> tempHash = getCrossProductBetweenAttitudes(tempToGetCrossProduct);
					for(Integer level : tempHash.keySet()) {
						if(newAssumptionsHashMapArray.containsKey(level)) {
							newAssumptionsHashMapArray.get(level).addAll(tempHash.get(level));
						}
						else{
							newAssumptionsHashMapArray.put(level, tempHash.get(level));
						}
					}
				}
			}
		}
		for(Integer level : newAssumptionsHashMapArray.keySet()){
			if(newAssumptionsHashMapArray.get(level) != null){
				for(int i = 0; i < newAssumptionsHashMapArray.get(level).size(); i++){
					ArrayList<Integer> innerAttitudes = new ArrayList<>(newAssumptionsHashMapArray.get(level).get(i).getFirst().keySet());
					for(Integer innerAttitude : innerAttitudes){
						if(newAssumptionsHashMapArray.get(level).get(i).getFirst().get(innerAttitude).getFirst().isEmpty()){
							newAssumptionsHashMapArray.get(level).get(i).getFirst().remove(innerAttitude);
						}
					}
					if(newAssumptionsHashMapArray.get(level).get(i).getFirst().isEmpty()){
						newAssumptionsHashMapArray.get(level).remove(i);
						i--;
					}
				}
			}
			if(newAssumptionsHashMapArray.get(level) != null && !newAssumptionsHashMapArray.get(level).isEmpty()) {
				HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>>> innerHash = new HashMap<>();
				innerHash.put(supportedAttitude, newAssumptionsHashMapArray.get(level));
				newAssumptionSupport.put(level, innerHash);
			}
		}
		return newAssumptionSupport;
	}

	private HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> getCrossProductOfNodeSet(int[]propsIDs, HashMap<Integer, Node> networkPropositions, int supportingAttitude, int supportedAttitude,PropositionNodeSet bridgeRules) {

		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> result = new HashMap<>();
		PropositionNode firstNode = null;
		int k = 0;
		while(k < propsIDs.length) {
			if(networkPropositions.containsKey(propsIDs[k])) {
				firstNode = (PropositionNode) networkPropositions.get(propsIDs[k]);
				if(!firstNode.getSupport().getAssumptionSupport().isEmpty()){
					break;
				}
			}
			k++;
		}
		if(k == propsIDs.length){
				return result;
		}
		Support firstSupp = firstNode.getSupport();
		for(Integer level : firstSupp.assumptionSupport.keySet()) {
			if(firstSupp.assumptionSupport.get(level).get(supportingAttitude) != null && !firstSupp.assumptionSupport.get(level).get(supportingAttitude).isEmpty()) {
				result.put(level, firstSupp.assumptionSupport.get(level).get(supportingAttitude));
			}
		}

		//handling Indirect Cycles and bridge rules
		ArrayList<Integer> keys = new ArrayList<>(result.keySet());
		for(Integer level : keys) {
			for(int i = 0; i < result.get(level).size(); i++) {
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

		for(int i = k+1; i < propsIDs.length; i++) {

			PropositionNode currNode = null;

			while(i < propsIDs.length) {
				if(networkPropositions.containsKey(propsIDs[i])) {
					currNode = (PropositionNode) networkPropositions.get(propsIDs[i]);
					break;
				}
				else{
					i++;
				}
			}
			if(i == propsIDs.length){
				return result;
			}
			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> levelsTemp = new HashMap<>();

			for(Integer level : currNode.getSupport().assumptionSupport.keySet()) {

				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> temp = new ArrayList<>();

				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> currSuppAssumptions = currNode.getSupport().assumptionSupport.get(level).get(supportingAttitude);

				if(result.containsKey(level)) {

					for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> combination : result.get(level)) {

						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> currSupport : currSuppAssumptions) {


							Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> newSupport = new Pair<>();
							HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerAttitudeMap = new HashMap<>();
							for (Map.Entry<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerentry : combination.getFirst().entrySet()) {
								Integer key = innerentry.getKey();

								Pair<PropositionNodeSet,PropositionNodeSet> value = innerentry.getValue();

								PropositionNodeSet originSet = value.getFirst().clone();

								PropositionNodeSet gradeSet = value.getSecond().clone();

								innerAttitudeMap.put(key, new Pair<>(originSet, gradeSet));
							}
							newSupport.setFirst(innerAttitudeMap);

							PropositionNodeSet bridge = combination.getSecond();
							bridge.putAll(currSupport.getSecond().getValues());
							newSupport.setSecond(bridge);

							for(Integer currAttitude : currSupport.getFirst().keySet()) {
								if(!(currAttitude == supportedAttitude && currSupport.getFirst().get(currAttitude).getFirst().contains(nodeID))) {//handling Indirect Cycles
									if(combination.getFirst().containsKey(currAttitude)) {
										Pair<PropositionNodeSet,PropositionNodeSet> oldPair = new Pair<>(combination.getFirst().get(currAttitude).getFirst().clone(), combination.getFirst().get(currAttitude).getSecond().clone());
										Pair<PropositionNodeSet,PropositionNodeSet> currPair = currSupport.getFirst().get(currAttitude);

										PropositionNodeSet newPropSet1 = oldPair.getFirst();
										PropositionNodeSet newPropSet2 = oldPair.getSecond();

										newPropSet1.putAll(currPair.getFirst().getValues());
										newPropSet2.putAll(currPair.getSecond().getValues());

										for(int currProp : newPropSet1.getValues()) {
											PropositionNode depNode = (PropositionNode) networkPropositions.get(currProp);
											if(nodeID != currProp && nodeID >= 1) {
												depNode.addNodeToAssumptionSupportDependents(nodeID);
											}
										}

										newSupport.getFirst().put(currAttitude, new Pair<>(newPropSet1,newPropSet2));
									}
									else {
										for(int currProp : currSupport.getFirst().get(currAttitude).getFirst().getProps()) {
											PropositionNode depNode = (PropositionNode) networkPropositions.get(currProp);
											if(nodeID != currProp && nodeID >= 1) {
												depNode.addNodeToAssumptionSupportDependents(nodeID);
											}
										}
										newSupport.getFirst().put(currAttitude, currSupport.getFirst().get(currAttitude));
									}
								}
							}
							temp.add(newSupport);
						}
					}
				}
				else {
					if(currSuppAssumptions != null) {
						for (int y = 0; y < currSuppAssumptions.size(); y++) {//handling Indirect Cycles
							Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> curr = currSuppAssumptions.get(y);
							if (curr.getFirst().containsKey(supportedAttitude) && curr.getFirst().get(supportedAttitude).getFirst().contains(nodeID)) {
								currSuppAssumptions.get(y).getFirst().remove(supportedAttitude);
							}
							if (curr.getFirst().isEmpty()) {
								currSuppAssumptions.remove(y);
								y--;
							} else {
								currSuppAssumptions.get(y).getSecond().putAll(bridgeRules.getValues());
							}
						}
						if (!currSuppAssumptions.isEmpty()) {
							temp = currSuppAssumptions;
						}
					}
				}
				if(!temp.isEmpty()) {
					levelsTemp.put(level, temp);
				}
			}
			if(!levelsTemp.isEmpty()) {
				for(Integer level : levelsTemp.keySet()){
					result.put(level, levelsTemp.get(level));
				}
			}
		}
		return result;

	}


	private HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> getCrossProductBetweenAttitudes(ArrayList<HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> input) {

		HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> result = new HashMap<>(input.get(0));

		for(int i = 1; i < input.size(); i++) {

			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> levelsTemp = new HashMap<>();

			for(Integer level : input.get(i).keySet()) {

				ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> temp = new ArrayList<>();

				if(result.containsKey(level)) {

					for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> combination : result.get(level)) {

						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr : input.get(i).get(level)) {


							Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> newSupport = new Pair<>();
							HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerAttitudeMap = new HashMap<>();
							for (Map.Entry<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerentry : combination.getFirst().entrySet()) {
								Integer key = innerentry.getKey();

								Pair<PropositionNodeSet,PropositionNodeSet> value = innerentry.getValue();

								PropositionNodeSet originSet = value.getFirst().clone();

								PropositionNodeSet gradeSet = value.getSecond().clone();

								innerAttitudeMap.put(key, new Pair<>(originSet, gradeSet));
							}
							newSupport.setFirst(innerAttitudeMap);

							PropositionNodeSet bridge = combination.getSecond();
							bridge.putAll(curr.getSecond().getValues());
							newSupport.setSecond(bridge);

							for(Integer currAttitude : curr.getFirst().keySet()) {

								if(combination.getFirst().containsKey(currAttitude)) {


									Pair<PropositionNodeSet,PropositionNodeSet> oldPair = new Pair<>(combination.getFirst().get(currAttitude).getFirst().clone(), combination.getFirst().get(currAttitude).getSecond().clone());
									Pair<PropositionNodeSet,PropositionNodeSet> currPair = curr.getFirst().get(currAttitude);

									PropositionNodeSet newPropSet1 = oldPair.getFirst();
									PropositionNodeSet newPropSet2 = oldPair.getSecond();

									newPropSet1.putAll(currPair.getFirst().getValues());
									newPropSet2.putAll(currPair.getSecond().getValues());

									newSupport.getFirst().put(currAttitude, new Pair<>(newPropSet1,newPropSet2));
								}
								else {
									newSupport.getFirst().put(currAttitude, curr.getFirst().get(currAttitude));
								}
							}
							temp.add(newSupport);
						}
					}
				}
				else {
					temp = input.get(level).get(i);
				}
				levelsTemp.put(level, temp);
			}
			for(Integer level : levelsTemp.keySet()){
				result.put(level, levelsTemp.get(level));
			}
		}
		return result;
	}

	/**
	 * Unions the given support with the current one
	 * @param support support to be unionized with
	 */
	public void union(Support support) {
		Support clonedSupport = support.clone();
		for(Integer level : clonedSupport.justificationSupport.keySet()){
			//Handle direct cycles
			if(hasCycle(clonedSupport.justificationSupport.get(level))){
				return;
			}
			for(Integer attitude : clonedSupport.justificationSupport.get(level).keySet()) {
				if(this.justificationSupport.containsKey(level)) {
					if(this.justificationSupport.get(level).containsKey(attitude)){
						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> supp : clonedSupport.justificationSupport.get(level).get(attitude)) {
							if(!this.justificationSupport.get(level).get(attitude).contains(supp)) {
								this.justificationSupport.get(level).get(attitude).add(supp);
							}
						}
					}
					else if(clonedSupport.justificationSupport.get(level).get(attitude) != null){
						this.justificationSupport.get(level).put(attitude, clonedSupport.justificationSupport.get(level).get(attitude));
					}
				}
				else if(clonedSupport.justificationSupport.get(level) != null){
					this.justificationSupport.put(level, clonedSupport.justificationSupport.get(level));
				}
			}
		}

		//handling Indirect Cycles
		ArrayList<Integer> levels = new ArrayList<>(clonedSupport.assumptionSupport.keySet());
		for(Integer level : levels) {
			ArrayList<Integer> attitudes = new ArrayList<>(clonedSupport.assumptionSupport.get(level).keySet());
			for(Integer supportedAttitude : attitudes) {
				for (int i = 0; i < clonedSupport.assumptionSupport.get(level).get(supportedAttitude).size(); i++) {
					Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> curr = clonedSupport.assumptionSupport.get(level).get(supportedAttitude).get(i);
					if (curr.getFirst().containsKey(supportedAttitude) && curr.getFirst().get(supportedAttitude).getFirst().contains(nodeID)) {
						clonedSupport.assumptionSupport.get(level).get(supportedAttitude).get(i).getFirst().remove(supportedAttitude);
					}
					if (clonedSupport.assumptionSupport.get(level).get(supportedAttitude).get(i).getFirst().isEmpty()) {
						clonedSupport.assumptionSupport.get(level).get(supportedAttitude).remove(i);
						i--;
					}
				}
				if(clonedSupport.assumptionSupport.get(level).get(supportedAttitude).isEmpty()){
					clonedSupport.assumptionSupport.remove(supportedAttitude);
				}
			}
			if(clonedSupport.assumptionSupport.get(level).isEmpty()) {
				clonedSupport.assumptionSupport.remove(level);
			}
		}

		for(Integer level : clonedSupport.assumptionSupport.keySet()){
			for(Integer attitude : clonedSupport.assumptionSupport.get(level).keySet()) {
				if(this.assumptionSupport.containsKey(level)) {
					if(this.assumptionSupport.get(level).containsKey(attitude)){
						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> supp : clonedSupport.assumptionSupport.get(level).get(attitude)) {
							if(!this.assumptionSupport.get(level).get(attitude).contains(supp)) {
								this.assumptionSupport.get(level).get(attitude).add(supp);
							}
						}
					}
					else if(clonedSupport.assumptionSupport.get(level).get(attitude) != null){
						this.assumptionSupport.get(level).put(attitude, clonedSupport.assumptionSupport.get(level).get(attitude));
					}
				}
				else if(clonedSupport.assumptionSupport.get(level) !=null){
					this.assumptionSupport.put(level, clonedSupport.assumptionSupport.get(level));
				}
			}
		}
	}

	/**
	 * Combines the given support into the current one
	 * by getting the cross product or their justification supports
	 * and then getting the new assumption support
	 * @param support support to be combined with
	 * @param attitude attitude to combine in
	 */
	public void combine(int attitude, Support support){
		Support clonedSupport = support.clone();
		HashMap<Integer, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>>> newJustificationSupport = this.getJustificationSupport();

		for(Integer level : clonedSupport.justificationSupport.keySet()){
			if(newJustificationSupport.containsKey(level)) {
				if(newJustificationSupport.get(level).containsKey(attitude)){
					ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> tempList = new ArrayList<>();
					for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> currSupport : clonedSupport.justificationSupport.get(level).get(attitude)){
						for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> thisSupport : newJustificationSupport.get(level).get(attitude)){
							Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> tempSupport = new Pair<>();
							HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerAttitudeMap = new HashMap<>();
							for (Map.Entry<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerentry : currSupport.getFirst().entrySet()) {
								Integer key = innerentry.getKey();

								Pair<PropositionNodeSet,PropositionNodeSet> value = innerentry.getValue();

								PropositionNodeSet originSet = value.getFirst().clone();

								PropositionNodeSet gradeSet = value.getSecond().clone();

								innerAttitudeMap.put(key, new Pair<>(originSet, gradeSet));
							}
							tempSupport.setFirst(innerAttitudeMap);

							PropositionNodeSet bridge = currSupport.getSecond();
							bridge.putAll(thisSupport.getSecond().getValues());
							tempSupport.setSecond(bridge);

							for(Integer innerAttitude : thisSupport.getFirst().keySet()){
								if(tempSupport.getFirst().containsKey(innerAttitude)){
									tempSupport.getFirst().get(innerAttitude).getFirst().putAll(thisSupport.getFirst().get(innerAttitude).getFirst().getValues());
									tempSupport.getFirst().get(innerAttitude).getSecond().putAll(thisSupport.getFirst().get(innerAttitude).getSecond().getValues());
								}
								else {
									tempSupport.getFirst().put(innerAttitude, thisSupport.getFirst().get(innerAttitude));
								}
							}
							tempList.add(tempSupport);
						}
					}
					for(Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> supp : tempList) {
						if(!newJustificationSupport.get(level).get(attitude).contains(supp)) {
							newJustificationSupport.get(level).get(attitude).add(supp);
						}
					}
				}
				else if(clonedSupport.justificationSupport.get(level).get(attitude) != null){
					newJustificationSupport.get(level).put(attitude, clonedSupport.justificationSupport.get(level).get(attitude));
				}
			}
			else if(clonedSupport.justificationSupport.get(level) != null){
				newJustificationSupport.put(level, clonedSupport.justificationSupport.get(level));
			}
		}

		setJustificationSupport(newJustificationSupport);
	}

	/**
	 * Adds a Proposition Node to this support
	 * @param attitude attitude to add the node in
	 * @param propositionNode Proposition Node to be added
	 */
	public void addNode(int attitude, PropositionNode propositionNode) {

		Pair<PropositionNodeSet, PropositionNodeSet> innerPair = new Pair<>(new PropositionNodeSet(propositionNode), new PropositionNodeSet());
		HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> hash = new HashMap<>();
		hash.put(attitude, innerPair);
		Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> outerPair = new Pair<>(hash, new PropositionNodeSet());
		ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> list = new ArrayList<>();
		list.add(outerPair);

		addJustificationSupportForAttitude(attitude, Network.currentLevel, list);

	}


	public void combineNode(int attitude, PropositionNode propositionNode){

		if(propositionNode.getId() == nodeID){
			return;
		}
		int level = Network.currentLevel;
		ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> newJustificationSupport = this.getJustificationSupport().get(level).get(attitude);

		for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> currSupp : newJustificationSupport){
			currSupp.getFirst().get(attitude).getFirst().add(propositionNode.getId());

		}

		this.justificationSupport.remove(level);
		addJustificationSupportForAttitude(attitude, level, newJustificationSupport);
	}


	/**
	 * Make this support a hypothesis in the specified attitude
	 * @param attitudeID attitude to this support a hypothesis in
	 */
	public void setHyp(int attitudeID) {
		int level = Network.currentLevel;
		if(isHyp.containsKey(level)) {
			if (isHyp.get(level).contains(attitudeID)) {
				return;
			}
		}
		//Add itself to its own assumptionSupport
		HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Hyp = new HashMap<>();
		Hyp.put(attitudeID, new Pair<>(new PropositionNodeSet(this.nodeID), new PropositionNodeSet()));
		ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> list = new ArrayList<>();
		list.add(new Pair<>(Hyp, new PropositionNodeSet()));

		if(assumptionSupport.containsKey(level)) {
			if (!assumptionSupport.get(level).containsKey(attitudeID)) {
				assumptionSupport.get(level).put(attitudeID, list);
			}
			else if(!this.assumptionSupport.get(level).get(attitudeID).containsAll(list)) {
				this.assumptionSupport.get(level).get(attitudeID).addAll(list);
			}
		}
		else{
			HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> outerHash = new HashMap<>();
			outerHash.put(attitudeID, list);
			this.assumptionSupport.put(level, outerHash);
		}

		//Add the attitude to the isHyp
		if(isHyp.containsKey(level)){
			isHyp.get(level).add(attitudeID);
		}
		else {
			isHyp.put(level, new HashSet<>(attitudeID));
		}
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
					if(!curr.getFirst().get(currKey).getFirst().isEmpty()) {
						empty = false;
						break;
					}
				}
				if(!empty) {
					break;
				}
				break;
			}
		}

		for(Integer level : assumptionSupport.keySet()) {
			for(Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet> curr : assumptionSupport.get(level).get(attitudeID)) {
				for(Integer currKey : curr.getFirst().keySet()) {
					if(!curr.getFirst().get(currKey).getFirst().isEmpty()) {
						empty = false;
						break;
					}
				}
				if(!empty) {
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
		ArrayList<Integer> levels = new ArrayList<>(justificationSupport.keySet());
		for(Integer level : levels) {
			removeNodeFromSupportHelper(id, justificationSupport.get(level));
			if(justificationSupport.get(level).isEmpty()){
				justificationSupport.remove(level);
			}
			else{
				ArrayList<Integer> attitudes = new ArrayList<>(justificationSupport.get(level).keySet());
				for(Integer attitude : attitudes) {
					ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> tempSupports = new ArrayList<>();
					for(int i = 0; i < justificationSupport.get(level).get(attitude).size(); i++){
						if(tempSupports.contains(justificationSupport.get(level).get(attitude).get(i))){
							justificationSupport.get(level).get(attitude).remove(i);
							i--;
						}
						else{
							tempSupports.add(justificationSupport.get(level).get(attitude).get(i));
						}
					}
					if(justificationSupport.get(level).get(attitude).isEmpty()){
						justificationSupport.get(level).remove(attitude);
					}
				}
				if(justificationSupport.get(level).isEmpty()){
					justificationSupport.remove(level);
				}
			}
		}
	}


	/**
	 * Removes the specified node from the justification based support
	 * @param id node to be removed
	 */
	public void removeNodeFromAssumptions (int id) {
		ArrayList<Integer> levels = new ArrayList<>(assumptionSupport.keySet());
		for(Integer level : levels) {
			removeNodeFromSupportHelper(id, assumptionSupport.get(level));
			if(assumptionSupport.get(level).isEmpty()){
				assumptionSupport.remove(level);
			}
			else{
				ArrayList<Integer> attitudes = new ArrayList<>(assumptionSupport.get(level).keySet());
				for(Integer attitude : attitudes) {
					ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>> tempSupports = new ArrayList<>();
					for(int i = 0; i < assumptionSupport.get(level).get(attitude).size(); i++){
						if(tempSupports.contains(assumptionSupport.get(level).get(attitude).get(i))){
							assumptionSupport.get(level).get(attitude).remove(i);
							i--;
						}
						else{
							tempSupports.add(assumptionSupport.get(level).get(attitude).get(i));
						}
					}
					if(assumptionSupport.get(level).get(attitude).isEmpty()){
						assumptionSupport.get(level).remove(attitude);
					}
				}
				if(assumptionSupport.get(level).isEmpty()){
					assumptionSupport.remove(level);
				}
			}
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
					flag = false;

				}
				if(flag) {
					for (Integer supportingAttitude : support.getFirst().keySet()) {
						PropositionNodeSet supportProps1 = support.getFirst().get(supportingAttitude).getFirst();
						PropositionNodeSet supportProps2 = support.getFirst().get(supportingAttitude).getSecond();
						if (supportProps1.contains(id)) {
							innerIterator.remove();// Safely remove the support from the list
							break;
						}
						if (supportProps2.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
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
					flag = false;

				}
				if(flag) {
					for (Integer supportingAttitude : support.getFirst().keySet()) {
						PropositionNodeSet supportProps1 = support.getFirst().get(supportingAttitude).getFirst();
						PropositionNodeSet supportProps2 = support.getFirst().get(supportingAttitude).getSecond();
						if (supportProps1.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
							break;
						}
						if (supportProps2.contains(id)) {
							innerIterator.remove(); // Safely remove the support from the list
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
		for(Integer supports : temp){
			setHyp(supports);
		}
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Node").append(nodeID).append(": ").append("\n");
		sb.append(" Justification Support: ").append("\n");
		for(Integer level : justificationSupport.keySet()) {
//			sb.append("Level").append(level).append(": ").append("\n");
			sb.append(mapToString("  Level" + level + ": ", justificationSupport.get(level)));
		}
		for(Integer level : assumptionSupport.keySet()) {
			sb.append("Level").append(level).append(": ").append("\n");
			sb.append(mapToString(" AssumptionSupport", assumptionSupport.get(level)));
		}
		return sb.toString();
	}


	private String mapToString(String title, HashMap<Integer, ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>,PropositionNodeSet>>> map) {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append("\n");

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
				sb.append("       Bridge Rules: ").append(innerMap.getSecond().toString()).append("\n");
			}
			sb.append("\n"); // Extra newline for better separation
		}

		return sb.toString();
	}

	@Override
    public Support clone(){
		Support clone = new Support(nodeID);
		clone.justificationSupport = this.getJustificationSupport();
		clone.assumptionSupport = this.getAssumptionSupport();
		clone.isHyp = this.getIsHyp();

		return clone;
	}

	/*
	public void calculateSupportsTree() {

		for(Integer level : justificationSupport.keySet()) {
			if(!isTreeCalculated.containsKey(level)) {
				isTreeCalculated.put(level, new HashSet<Integer>());
			}
			for(Integer attitude : justificationSupport.get(level).keySet()) {
				if(!isTreeCalculated.get(level).contains(attitude)) {
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
					if(currNode.getSupport().isTreeCalculated.contains(supportingAttitude)) {
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
//					if(currNode.getSupport().isTreeCalculated.contains(supportingAttitude)) {
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
		isTreeCalculated.add(Attitude);
		return supportTree;
	}
	*/
}