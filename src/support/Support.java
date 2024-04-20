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
	private HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> justificationSupport;
	private HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> assumptionSupport;
	private HashMap<Integer, SupportTree> supportsTree;
	private HashSet<Integer> isHyp;
	private HashSet<Integer> isTreeCalculatetd;

	public Support(int nodeID){
		// Constructor
		this.nodeID = nodeID;
		justificationSupport = new HashMap<>();
		assumptionSupport = new HashMap<>();
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
	public HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> getJustificationSupport() {
		return justificationSupport;
	}

	/**
	 * @return the assumptionSupport
	 */
	public HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> getAssumptionSupport() {
		return assumptionSupport;
	}

	/**
	 * @param justificationSupport the justificationSupport to set
	 * @throws Exception 
	 */
	public void setJustificationSupport(
			HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> justificationSupport) throws DirectCycleException {
		if(hasCycle(justificationSupport)){
			throw new DirectCycleException("Direct Cycle found");
		}
		else {
			this.assumptionSupport = createAssumptionSupport(justificationSupport);
			this.justificationSupport = justificationSupport;
		}
	}

	public boolean hasCycle(
			HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> justificationSupport) {
		
		PropositionNodeSet dependents = new PropositionNodeSet();
		for (Integer supportedAttitude : justificationSupport.keySet()) {
			ArrayList<HashMap<Integer, PropositionNodeSet>> Supports = justificationSupport.get(supportedAttitude);
			for (HashMap<Integer, PropositionNodeSet> supportAttitudes : Supports) {
				for (Integer attitude : supportAttitudes.keySet()) {
					PropositionNodeSet supportProps = supportAttitudes.get(attitude);
					int[] propsIDs = supportProps.getProps();
					for (int id : propsIDs) {
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


	private HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> createAssumptionSupport(
			HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> justificationSupport) {

		PropositionNodeSet dependents = new PropositionNodeSet();
		HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> newAssumptionSupport = new HashMap<>();
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes(); 
		
		//iterate over the supported attitudes of the justification support
		for (Integer supportedAttitude : justificationSupport.keySet()) { 
			ArrayList<HashMap<Integer, PropositionNodeSet>> supportsHashMapArray = justificationSupport.get(supportedAttitude);
			ArrayList<HashMap<Integer, PropositionNodeSet>> newAssumptionsHashMapArray = new ArrayList<>();
			
			//iterate over the Supports of current supported attitude
			for (HashMap<Integer, PropositionNodeSet> supportingAttitudes : supportsHashMapArray) {
				ArrayList<ArrayList<HashMap<Integer, PropositionNodeSet>>> temptogetcrossproduct = new ArrayList<>();
				
				//iterate over the supporting attitudes of current support to get cross product(ArrayList of HashMap<Integer, PropositionNodeSet> of supports)
				for (Integer supportingAttitude : supportingAttitudes.keySet()) {
					ArrayList<HashMap<Integer, PropositionNodeSet>> newSupports = new ArrayList<>();
					
					PropositionNodeSet supportPropsSet = supportingAttitudes.get(supportingAttitude);
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

	private ArrayList<HashMap<Integer, PropositionNodeSet>> getCrossProductOfNodeSet(int[]propsIDs, HashMap<Integer, Node> networkPropositions, int supportingAttitude, int supportedAttitude, PropositionNodeSet dependents) {

		PropositionNode firstSupp = (PropositionNode) networkPropositions.get(propsIDs[0]);
		ArrayList<HashMap<Integer, PropositionNodeSet>> result = new ArrayList<>(firstSupp.getSupport().getAssumptionSupport().get(supportingAttitude));

		for(HashMap<Integer, PropositionNodeSet> curr : result) {//handling Indirect Cycles
			if(curr.containsKey(supportedAttitude) && curr.get(supportedAttitude).contains(nodeID)) {
				curr.remove(supportedAttitude);
			}
		}

		for(int i = 1; i < propsIDs.length; i++) {

			ArrayList<HashMap<Integer, PropositionNodeSet>> temp = new ArrayList<>();

			PropositionNode currNode = (PropositionNode) networkPropositions.get(propsIDs[i]);
			ArrayList<HashMap<Integer, PropositionNodeSet>> currSuppAssumptions = currNode.getSupport().getAssumptionSupport().get(supportingAttitude);

			for(HashMap<Integer, PropositionNodeSet> currSupport : currSuppAssumptions) {

				for(HashMap<Integer, PropositionNodeSet> combination : result) {

					HashMap<Integer, PropositionNodeSet> newSupport = new HashMap<>(combination);

					for(Integer currAttitude : currSupport.keySet()) {
						if(!(currAttitude == supportedAttitude && currSupport.get(currAttitude).contains(nodeID))) {//handling Indirect Cycles
							if(combination.containsKey(currAttitude)) {
								PropositionNodeSet oldPropsSet = combination.get(currAttitude);
								PropositionNodeSet newPropSet = new PropositionNodeSet();
								PropositionNodeSet currPropsSet = currSupport.get(currAttitude);
								for(int oldProp : oldPropsSet.getProps()) {
									newPropSet.add(oldProp);
									dependents.add(oldProp);
								}
								for(int currProp : currPropsSet.getProps()) {
									newPropSet.add(currProp);
									dependents.add(currProp);
								}
								newSupport.put(currAttitude, newPropSet);
							}
							else {
								for(int currProp : currSupport.get(currAttitude).getProps()) {
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


	private ArrayList<HashMap<Integer, PropositionNodeSet>> getCrossProductBetweenAttitudes(ArrayList<ArrayList<HashMap<Integer,PropositionNodeSet>>> input) {

		ArrayList<HashMap<Integer, PropositionNodeSet>> result = new ArrayList<>(input.get(0));

		for(int i = 1; i < input.size(); i++) {

			ArrayList<HashMap<Integer, PropositionNodeSet>> temp = new ArrayList<>();

			for(HashMap<Integer, PropositionNodeSet> curr : input.get(i)) {

				for(HashMap<Integer, PropositionNodeSet> combination : result) {

					HashMap<Integer, PropositionNodeSet> newSupport = new HashMap<>(combination);

					for(Integer currAttitude : curr.keySet()) {

						if(combination.containsKey(currAttitude)) {
							

							PropositionNodeSet oldPropsSet = combination.get(currAttitude);
							PropositionNodeSet newPropSet = new PropositionNodeSet();
							PropositionNodeSet currPropsSet = curr.get(currAttitude);

							for(int oldProp : oldPropsSet.getProps()) {
								newPropSet.add(oldProp);
							}
							for(int nextProp : currPropsSet.getProps()) {
								newPropSet.add(nextProp);
							}
							newSupport.put(currAttitude, newPropSet);
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

	/**
	 * @param assumptionSupport the assumptionSupport to set
	 */
	public void setAssumptionSupport(HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> assumptionSupport) {
		this.assumptionSupport = assumptionSupport;
	}

	public void addJustificatoinSupportForAttitude(int attitudeID,  ArrayList<HashMap<Integer, PropositionNodeSet>> justificationSupport) throws DirectCycleException {
		HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> temp = new HashMap<>();
		temp.put(attitudeID, justificationSupport);
		if(hasCycle(temp)){
			throw new DirectCycleException("Direct Cycle found");
		}
		else {
			this.assumptionSupport.get(attitudeID).addAll(createAssumptionSupport(temp).get(attitudeID));
			this.justificationSupport.get(attitudeID).addAll(justificationSupport);
		}
	}

	public void setHyp(int attitudeID) {
		//Add itself to its own assumptionSupport
		PropositionNodeSet hypSet = new PropositionNodeSet(this.nodeID);
		HashMap<Integer, PropositionNodeSet> Hyp = new HashMap<>();
		Hyp.put(attitudeID, hypSet);
		if (!assumptionSupport.containsKey(attitudeID)) {
		    assumptionSupport.put(attitudeID, new ArrayList<>());
		}
		this.assumptionSupport.get(attitudeID).add(Hyp);
		//Add the attitude to the isHyp
		isHyp.add(attitudeID);
	}

	public boolean hasChildren(int attitudeID) {

		if(!justificationSupport.containsKey(attitudeID) && ! assumptionSupport.containsKey(attitudeID)) {
			return false;
		}

		boolean empty = true;

		for(HashMap<Integer, PropositionNodeSet> curr : justificationSupport.get(attitudeID)) {
			for(Integer currKey : curr.keySet()) {
				if(!curr.get(currKey).isEmpty()) {
					empty = false;
					break;
				}
			}
			if(empty == false) {
				break;
			}
			break;
		}

		for(HashMap<Integer, PropositionNodeSet> curr : assumptionSupport.get(attitudeID)) {
			for(Integer currKey : curr.keySet()) {
				if(!curr.get(currKey).isEmpty()) {
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
        sb.append(mapToString(" AssumptionSupport", assumptionSupport));
        return sb.toString();
    }
	
	private String mapToString(String title, HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(":\n");
        
        // Iterate through the outer HashMap
        for (Map.Entry<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> entry : map.entrySet()) {
            Integer key = entry.getKey();
            ArrayList<HashMap<Integer, PropositionNodeSet>> valueList = entry.getValue();

            sb.append("   Supported Attitude: ").append(key).append("\n");
            sb.append("   Supports:\n");
            
            // Iterate through the ArrayList
            for (int i = 0; i < valueList.size(); i++) {
                sb.append("     Support ").append(i+1).append(":").append("\n");

                HashMap<Integer, PropositionNodeSet> innerMap = valueList.get(i);
                // Iterate through the inner HashMap
                for (Map.Entry<Integer, PropositionNodeSet> innerEntry : innerMap.entrySet()) {
                    Integer innerKey = innerEntry.getKey();
                    PropositionNodeSet nodeSet = innerEntry.getValue();

                    sb.append("       In Attitude ").append(innerKey).append(":").append("\n");
                    sb.append("       PropositionNodeSet: ").append(nodeSet.toString()).append("\n");
                }
            }
            sb.append("\n"); // Extra newline for better separation
        }

        return sb.toString();
    }
	
	public void removeNodeFromSupport (int id) {
		removeNodeFromSupportHelper(id, assumptionSupport);
		removeNodeFromSupportHelper(id, justificationSupport);
	}
	
	private void removeNodeFromSupportHelper(int id, HashMap<Integer, ArrayList<HashMap<Integer, PropositionNodeSet>>> CurrentSupport) {
		Iterator<Integer> iterator = CurrentSupport.keySet().iterator();
	    while (iterator.hasNext()) {
	        Integer supportedAttitude = iterator.next();
	        ArrayList<HashMap<Integer, PropositionNodeSet>> Supports = CurrentSupport.get(supportedAttitude);
	        Iterator<HashMap<Integer, PropositionNodeSet>> innerIterator = Supports.iterator();
	        while (innerIterator.hasNext()) {
	            HashMap<Integer, PropositionNodeSet> support = innerIterator.next();
	            for (Integer supportingAttitude : support.keySet()) {
	                PropositionNodeSet supportProps = support.get(supportingAttitude);
	                if (supportProps.contains(id)) {
	                    innerIterator.remove(); // Safely remove the support from the list
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
	
	public void calculateSupportsTree() {
		
		for(Integer Attitude : justificationSupport.keySet()) {
			supportsTree.put(Attitude, calculateSupportTreeForAttitude(Attitude));
		}
		
	}
	
	public SupportTree calculateSupportTreeForAttitude(int Attitude) {
		
		ArrayList<HashMap<Integer, PropositionNodeSet>> supports = justificationSupport.get(Attitude);
		//get Propositions of the network
		HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes(); 
		
		SupportTree supportTree = new SupportTree(nodeID);
		for(HashMap<Integer, PropositionNodeSet> support : supports) {
			HashMap<Integer, ArrayList<SupportTree>> attitudesSupportTrees = new HashMap<>();
			for(Integer supportingAttitude : support.keySet()) {
				ArrayList<SupportTree>supportTrees = new ArrayList<>();
				PropositionNodeSet currSupports = support.get(supportingAttitude);
				for(int id : currSupports.getProps()) {
					PropositionNode currNode = (PropositionNode)networkPropositions.get(id);
					//If the tree is calculated for supporting attitude add it to the supportTrees
					if(currNode.getSupport().isTreeCalculatetd.contains(supportingAttitude)) {
						supportTrees.add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
					}
					else {
						//calculate the support tree for supporting attitude and then add it
						currNode.getSupport().calculateSupportTreeForAttitude(supportingAttitude);
						supportTrees.add(currNode.getSupport().getSupportsTree().get(supportingAttitude));
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
