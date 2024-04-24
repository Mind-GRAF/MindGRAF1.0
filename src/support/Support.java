package support;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import exceptions.DirectCycleException;
import set.PropositionNodeSet;
import network.Network;
import nodes.Node;
import nodes.PropositionNode;


public class Support {

	private int nodeID;
	private HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> justificationSupport;
	private Pair<HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>>,PropositionNodeSet> assumptionSupport;
	private HashMap<Integer, SupportTree> supportsTree;
	private HashSet<Integer> isHyp;
	private HashSet<Integer> isTreeCalculatetd;

	public Support(int nodeID){
		// Constructor
		this.nodeID = nodeID;
		justificationSupport = new HashMap<>();
		assumptionSupport = new Pair<>();
		isHyp = new HashSet<>();
		isTreeCalculatetd = new HashSet<>();
		supportsTree = new HashMap<>();
	}
	
	/**
	 * @return the supportsTree
	 */
	public HashMap<Integer, SupportTree> getSupportsTree() {
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
	public HashSet<Integer> getisTreeCalculatetd() {
		return isTreeCalculatetd;
	}

	/**
	 * @return the justificationSupport
	 */
	public HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> getJustificationSupport() {
		return justificationSupport;
	}

	/**
	 * @return the assumptionSupport
	 */
	public Pair<HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>>,PropositionNodeSet> getAssumptionSupport() {
		return assumptionSupport;
	}

	/**
	 * @param justificationSupport the justificationSupport to set
	 * @throws Exception 
	 */
	public void setJustificationSupport(
		HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> justificationSupport) throws DirectCycleException {
		if(hasCycle(justificationSupport)){
			throw new DirectCycleException("Direct Cycle found");
		}
		else {
			this.assumptionSupport.setFirst(createAssumptionSupport(justificationSupport));
			this.justificationSupport = justificationSupport;
		}
	}

	public boolean hasCycle(
		HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> justificationSupport) {
		
		PropositionNodeSet dependents = new PropositionNodeSet();
		for (Integer supportedAttitude : justificationSupport.keySet()) {
			ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> Supports = justificationSupport.get(supportedAttitude);
			for (HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> supportAttitudes : Supports) {
				for (Integer attitude : supportAttitudes.keySet()) {
					PropositionNodeSet supportProps1 = supportAttitudes.get(attitude).getFirst();
					PropositionNodeSet supportProps2 = supportAttitudes.get(attitude).getSecond();
					int[] propsIDs1 = supportProps1.getProps();
					int[] propsIDs2 = supportProps2.getProps();
					for (int id : propsIDs1) {
						if(attitude == supportedAttitude && id == nodeID) {
							return true;
						}
						dependents.add(id);
					}
					for (int id : propsIDs2) {
						if(attitude == supportedAttitude && id == nodeID) {
							return true;
						}
						dependents.add(id);
					}
				}
			}
		}
		
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();
		PropositionNode thisNode = (PropositionNode) networkPropositions.get(nodeID);
		thisNode.getJustificationSupportDependents().putAll(dependents.getValues());
		return false;
	}


	private HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> createAssumptionSupport(
		HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> justificationSupport) {

		PropositionNodeSet dependents = new PropositionNodeSet();
		HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> newAssumptionSupport = new HashMap<>();
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes(); 
		
		//iterate over the supported attitudes of the justification support(Outer HashMap)
		for (Integer supportedAttitude : justificationSupport.keySet()) { 
			ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> supportsHashMapArray = justificationSupport.get(supportedAttitude);
			ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> newAssumptionsHashMapArray = new ArrayList<>();
			
			//iterate over the Supporting Attitudes of current supported attitude(Outer HashMap)
			for (HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> supportingAttitudes : supportsHashMapArray) {
				ArrayList<ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> temptogetcrossproduct = new ArrayList<>();
				
				//iterate over the supports of current support to get cross product(ArrayList of HashMap<Integer, PropositionNodeSet> of supports)(Inner HashMap)
				for (Integer supportingAttitude : supportingAttitudes.keySet()) {
					ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> newSupports = new ArrayList<>();
					
					Pair<PropositionNodeSet,PropositionNodeSet> supportPropsSets = supportingAttitudes.get(supportingAttitude);
					PropositionNodeSet supportPropsSet = supportPropsSets.getFirst();
					supportPropsSet.putAll(supportPropsSets.getSecond().getValues());
					int[] propsIDs = supportPropsSet.getProps();
					
					newSupports.addAll(getCrossProductOfNodeSet(propsIDs, networkPropositions, supportingAttitude, supportedAttitude, dependents));

					temptogetcrossproduct.add(newSupports);
				}
				newAssumptionsHashMapArray.addAll(getCrossProductBetweenAttitudes(temptogetcrossproduct));
			}
			newAssumptionSupport.put(supportedAttitude, newAssumptionsHashMapArray);
		}
		
		PropositionNode thisNode = (PropositionNode) networkPropositions.get(nodeID);
		thisNode.getAssumptionSupportDependents().putAll(dependents.getValues());
		return newAssumptionSupport;
	}

	private ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> getCrossProductOfNodeSet(int[]propsIDs, HashMap<Integer, Node> networkPropositions, int supportingAttitude, int supportedAttitude, PropositionNodeSet dependents) {

		PropositionNode firstSupp = (PropositionNode) networkPropositions.get(propsIDs[0]);
		ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> result = new ArrayList<>(firstSupp.getSupport().getAssumptionSupport().getFirst().get(supportingAttitude));

		for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> curr : result) {//handling Indirect Cycles
			if(curr.containsKey(supportedAttitude) && (curr.get(supportedAttitude).getFirst().contains(nodeID) || curr.get(supportedAttitude).getSecond().contains(nodeID))) {
				curr.remove(supportedAttitude);
			}
		}

		for(int i = 1; i < propsIDs.length; i++) {

			ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> temp = new ArrayList<>();

			PropositionNode currNode = (PropositionNode) networkPropositions.get(propsIDs[i]);
			ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> currSuppAssumptions = currNode.getSupport().getAssumptionSupport().getFirst().get(supportingAttitude);

			for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> currSupport : currSuppAssumptions) {

				for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> combination : result) {

					HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> newSupport = new HashMap<>(combination);

					for(Integer currAttitude : currSupport.keySet()) {
						if(!(currAttitude == supportedAttitude && (currSupport.get(currAttitude).getFirst().contains(nodeID) || currSupport.get(currAttitude).getSecond().contains(nodeID)))) {//handling Indirect Cycles
							if(combination.containsKey(currAttitude)) {
								Pair<PropositionNodeSet,PropositionNodeSet> oldPropsSet = combination.get(currAttitude);
								Pair<PropositionNodeSet,PropositionNodeSet> currPropsSet = currSupport.get(currAttitude);

								PropositionNodeSet newPropSet1 = oldPropsSet.getFirst();
								PropositionNodeSet newPropSet2 = oldPropsSet.getSecond();

								newPropSet1.putAll(currPropsSet.getFirst().getValues());
								newPropSet1.putAll(currPropsSet.getSecond().getValues());
								newPropSet2.putAll(currPropsSet.getSecond().getValues());

								// for(int oldProp : oldPropsSet.getProps()) {
								// 	newPropSet.add(oldProp);
								// 	dependents.add(oldProp);
								// }
								// for(int currProp : currPropsSet.getProps()) {
								// 	newPropSet.add(currProp);
								// 	dependents.add(currProp);
								// }
								newSupport.put(currAttitude, new Pair<PropositionNodeSet,PropositionNodeSet>(newPropSet1,newPropSet2));
							}
							else {
								for(int currProp : currSupport.get(currAttitude).getFirst().getProps()) {
									dependents.add(currProp);
								}
								for(int currProp : currSupport.get(currAttitude).getSecond().getProps()) {
									dependents.add(currProp);
								}
								newSupport.put(currAttitude, currSupport.get(currAttitude));
							}
						}
					}
					temp.add(newSupport);
				}
			}
			result = temp;
		}
		return result;

	}


	private ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> getCrossProductBetweenAttitudes(ArrayList<ArrayList<HashMap<Integer,Pair<PropositionNodeSet,PropositionNodeSet>>>> input) {

		ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> result = new ArrayList<>(input.get(0));

		for(int i = 1; i < input.size(); i++) {

			ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> temp = new ArrayList<>();

			for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> curr : input.get(i)) {

				for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> combination : result) {

					HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> newSupport = new HashMap<>(combination);

					for(Integer currAttitude : curr.keySet()) {

						if(combination.containsKey(currAttitude)) {
							

							Pair<PropositionNodeSet,PropositionNodeSet> oldPropsSet = combination.get(currAttitude);
							Pair<PropositionNodeSet,PropositionNodeSet> currPropsSet = curr.get(currAttitude);

							PropositionNodeSet newPropSet1 = oldPropsSet.getFirst();
							PropositionNodeSet newPropSet2 = oldPropsSet.getSecond();

							newPropSet1.putAll(currPropsSet.getFirst().getValues());
							newPropSet1.putAll(currPropsSet.getSecond().getValues());
							newPropSet2.putAll(currPropsSet.getSecond().getValues());
							// for(int oldProp : oldPropsSet.getProps()) {
							// 	newPropSet.add(oldProp);
							// }
							// for(int nextProp : currPropsSet.getProps()) {
							// 	newPropSet.add(nextProp);
							// }
							newSupport.put(currAttitude, new Pair<PropositionNodeSet,PropositionNodeSet>(newPropSet1,newPropSet2));
							}
						else {
							newSupport.put(currAttitude, curr.get(currAttitude));
						}
					}
					temp.add(newSupport);
				}
			}
			result = temp;
		}
		return result;
	}

	// /**
	//  * @param assumptionSupport the assumptionSupport to set
	//  */
	// public void setAssumptionSupport(Pair<HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>>,PropositionNodeSet> assumptionSupport) {
	// 	this.assumptionSupport = assumptionSupport;
	// }

	public void addJustificatoinSupportForAttitude(int attitudeID,  ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> justificationSupport) throws DirectCycleException {
		HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> temp = new HashMap<>();
		temp.put(attitudeID, justificationSupport);
		if(hasCycle(temp)){
			throw new DirectCycleException("Direct Cycle found");
		}
		else {

			if(this.assumptionSupport.getFirst().containsKey(attitudeID)){
				this.assumptionSupport.getFirst().get(attitudeID).addAll(createAssumptionSupport(temp).get(attitudeID));
			}
			else {
				this.assumptionSupport.getFirst().put(attitudeID, createAssumptionSupport(temp).get(attitudeID));
			}

			if(this.justificationSupport.containsKey(attitudeID)){
				this.justificationSupport.get(attitudeID).addAll(justificationSupport);
			}
			else {
				this.justificationSupport.put(attitudeID, justificationSupport);
			}
			isTreeCalculatetd.remove(attitudeID);
		}
	}

	public void setHyp(int attitudeID) {
		if(isHyp.contains(attitudeID)){
			return;
		}
		//Add itself to its own assumptionSupport
		PropositionNodeSet hypSet = new PropositionNodeSet(this.nodeID);
		Pair<PropositionNodeSet,PropositionNodeSet> hypPair = new Pair<>(new PropositionNodeSet(), hypSet);
		HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> Hyp = new HashMap<>();
		Hyp.put(attitudeID, hypPair);
		if (!assumptionSupport.getFirst().containsKey(attitudeID)) {
		    assumptionSupport.getFirst().put(attitudeID, new ArrayList<>());
		}
		this.assumptionSupport.getFirst().get(attitudeID).add(Hyp);
		//Add the attitude to the isHyp
		isHyp.add(attitudeID);
		isTreeCalculatetd.remove(attitudeID);
	}

	public boolean hasChildren(int attitudeID) {

		if(!justificationSupport.containsKey(attitudeID) && ! assumptionSupport.getFirst().containsKey(attitudeID)) {
			return false;
		}

		boolean empty = true;

		for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> curr : justificationSupport.get(attitudeID)) {
			for(Integer currKey : curr.keySet()) {
				if(!curr.get(currKey).getFirst().equals(null) && !curr.get(currKey).getSecond().equals(null)) {
					empty = false;
					break;
				}
			}
			if(empty == false) {
				break;
			}
			break;
		}

		for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> curr : assumptionSupport.getFirst().get(attitudeID)) {
			for(Integer currKey : curr.keySet()) {
				if(!curr.get(currKey).getFirst().equals(null) && !curr.get(currKey).getSecond().equals(null)) {
					empty = false;
					break;
				}
			}
			if(empty == false) {
				break;
			}
			break;
		}

		return !empty;
	}
	
	
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node").append(nodeID).append(": ").append("\n");
        sb.append(mapToString(" JustificationSupport", justificationSupport));
        sb.append(mapToString(" AssumptionSupport", assumptionSupport.getFirst()));
        return sb.toString();
    }
	
	private String mapToString(String title, HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(":\n");
        
        // Iterate through the outer HashMap
        for (Map.Entry<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> entry : map.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> valueList = entry.getValue();

            sb.append("   Supported Attitude: ").append(key).append("\n");
            sb.append("   Supports:\n");
            
            // Iterate through the ArrayList
            for (int i = 0; i < valueList.size(); i++) {
                sb.append("     Support ").append(i+1).append(":").append("\n");

                HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerMap = valueList.get(i);
                // Iterate through the inner HashMap
                for (Map.Entry<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> innerEntry : innerMap.entrySet()) {
                    Integer innerKey = innerEntry.getKey();
                    PropositionNodeSet WeightnodeSet = innerEntry.getValue().getFirst();

                    sb.append("       In Attitude ").append(innerKey).append(":").append("\n");
                    sb.append("       Graded PropositionNodeSet: ").append(WeightnodeSet.toString()).append("\n");

                    PropositionNodeSet NonWeightnodeSet = innerEntry.getValue().getSecond();

                    sb.append("       Non Graded PropositionNodeSet: ").append(NonWeightnodeSet.toString()).append("\n");
                }
            }
            sb.append("\n"); // Extra newline for better separation
        }

        return sb.toString();
    }
	
	public void removeNodeFromJustifications (int id) {
		removeNodeFromSupportHelper(id, justificationSupport);
	}
	
	public void removeNodeFromAssumptions (int id) {
		removeNodeFromSupportHelper(id, assumptionSupport.getFirst());
		assumptionSupport.getSecond().remove(id);
	}
	

	private void removeNodeFromSupportHelper(int id, HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> CurrentSupport) {
		Iterator<Integer> iterator = CurrentSupport.keySet().iterator();
	    while (iterator.hasNext()) {
	        Integer supportedAttitude = iterator.next();
	        ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> Supports = CurrentSupport.get(supportedAttitude);
	        Iterator<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> innerIterator = Supports.iterator();
	        while (innerIterator.hasNext()) {
	            HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> support = innerIterator.next();
	            for (Integer supportingAttitude : support.keySet()) {
	                PropositionNodeSet supportProps1 = support.get(supportingAttitude).getFirst();
	                PropositionNodeSet supportProps2 = support.get(supportingAttitude).getSecond();
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
	        if (Supports.isEmpty()) {
	            iterator.remove(); // Safely remove the attitude entry from the map
	            CurrentSupport.remove(supportedAttitude);	        
	            }
	    }
	}
	
	public void ForgetNodeFromJustifications (int id) {
		ForgetNodeFromSupportHelper(id, justificationSupport,1);
	}
	
	public void ForgetNodeFromAssumptions (int id) {
		ForgetNodeFromSupportHelper(id, assumptionSupport.getFirst(),2);
		assumptionSupport.getSecond().remove(id);
	}
	
	private void ForgetNodeFromSupportHelper(int id, HashMap<Integer, ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>>> CurrentSupport,int i) {
		Iterator<Integer> iterator = CurrentSupport.keySet().iterator();
		ArrayList<Integer> temp = new ArrayList<>();
	    while (iterator.hasNext()) {
	        Integer supportedAttitude = iterator.next();
	        ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> Supports = CurrentSupport.get(supportedAttitude);
	        Iterator<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> innerIterator = Supports.iterator();
	        while (innerIterator.hasNext()) {
	            HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> support = innerIterator.next();
	            for (Integer supportingAttitude : support.keySet()) {
	                PropositionNodeSet supportProps1 = support.get(supportingAttitude).getFirst();
	                PropositionNodeSet supportProps2 = support.get(supportingAttitude).getSecond();
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

	public void calculateSupportsTree() {
		
		for(Integer Attitude : justificationSupport.keySet()) {
			if(!isTreeCalculatetd.contains(Attitude))
			supportsTree.put(Attitude, calculateSupportTreeForAttitude(Attitude));
		}
		
	}
	
	public SupportTree calculateSupportTreeForAttitude(int Attitude) {
		
		ArrayList<HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>>> supports = justificationSupport.get(Attitude);
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes(); 
		
		SupportTree supportTree = new SupportTree(nodeID);
		for(HashMap<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> support : supports) {
			HashMap<Integer, Pair<ArrayList<SupportTree>,ArrayList<SupportTree>>> attitudesSupportTrees = new HashMap<>();
			for(Integer supportingAttitude : support.keySet()) {
				Pair<ArrayList<SupportTree>,ArrayList<SupportTree>> supportTrees = new Pair<>(new ArrayList<>(),new ArrayList<>());
				PropositionNodeSet currSupports1 = support.get(supportingAttitude).getFirst();
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

				PropositionNodeSet currSupports2 = support.get(supportingAttitude).getSecond();
				for(int id : currSupports2.getProps()) {
					PropositionNode currNode = (PropositionNode)networkPropositions.get(id);
					//If the tree is calculated for supporting attitude add it to the supportTrees
					if(currNode.getSupport().isTreeCalculatetd.contains(supportingAttitude)) {
						supportTrees.getSecond().add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
					}
					else {
						//calculate the support tree for supporting attitude and then add it
						currNode.getSupport().calculateSupportTreeForAttitude(supportingAttitude);
						supportTrees.getSecond().add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
					}
				}
				attitudesSupportTrees.put(supportingAttitude, supportTrees);

			}
			supportTree.getChildren().add(attitudesSupportTrees);
		}
		isTreeCalculatetd.add(Attitude);
		return supportTree;
	}

}
