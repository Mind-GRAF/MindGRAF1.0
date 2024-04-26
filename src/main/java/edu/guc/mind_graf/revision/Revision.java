package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.nodes.PropositionNode;

import java.util.ArrayList;

public class Revision {
	public static void checkContradiction(Context c, int attitudeNumberOfAddedNode, PropositionNode node) {
		PropositionNode nodeCompliment = (PropositionNode) node.getNegation();
		if(nodeCompliment == null){
			return;
		}
		
		ArrayList<ArrayList<Integer>> filteredConsistentAttitudes = filterAttitudes(ContextController.getConsistentAttitudes(),attitudeNumberOfAddedNode);
		ArrayList<Integer> contradictingNodeAttitudes = new ArrayList<>();
		//TODO: wael add a set for already checked attitudes to increase performance
		for(ArrayList<Integer> entry : filteredConsistentAttitudes){
			for(int attitudeNumber : entry){
//				if(isSupported(c, attitudeNumber, nodeCompliment){
				contradictingNodeAttitudes.add(attitudeNumber);
//				}
			}
		}
		if(!contradictingNodeAttitudes.isEmpty()){
			if(ContextController.isHandleContradictionsManually()){
				handleContradictionsManually();
			}else{
				handleContradictionsAutomatically();
			}
		}

	}
	
	public static void  handleContradictionsManually(){
		   //TODO: wael
	}
	
	public static void  handleContradictionsAutomatically(){
	
	}
	
	private static ArrayList<ArrayList<Integer>> filterAttitudes(ArrayList<ArrayList<Integer>> consistentAttitudes, int attitudeNumber) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		for(ArrayList<Integer> entry : consistentAttitudes){
			if(entry.contains(attitudeNumber)){
				result.add(entry);
			}
		}
		return result;
	}
}
