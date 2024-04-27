package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.Set;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Revision {
	public static void checkContradiction(Context c, int attitudeNumberOfAddedNode, PropositionNode node) {
		PropositionNode nodeCompliment = (PropositionNode) node.getNegation();
		if(nodeCompliment == null){
			return;
		}
		
		ArrayList<ArrayList<Integer>> filteredConsistentAttitudes = filterAttitudes(ContextController.getConsistentAttitudes(),attitudeNumberOfAddedNode);
		ArrayList<Contradiction> contradictions = new ArrayList<>();
		for(ArrayList<Integer> entry : filteredConsistentAttitudes){
			for(int attitudeNumber : entry){
//				if(isSupported(c, attitudeNumber, nodeCompliment){
					Contradiction cont = new Contradiction(node);
					cont.getContradictions().add(attitudeNumber,nodeCompliment);
					contradictions.add(cont);
//				}
			}
		}

		if(!contradictions.isEmpty()){
			if(ContextController.isAutomaticHandling()){
				//TODO auto
			}else{
				manualContradictionHandling(c,attitudeNumberOfAddedNode,contradictions);
			}
		}

	}

	public static void manualContradictionHandling(Context c, int attitudeNumber, ArrayList<Contradiction> contradictions){
		print("Contradiction Detected");
		print("Select How to handle this contradiction");
		print("\t1. remove node:"+ contradictions.getFirst().toString());
		print("\t2. remove nodes that support contradicting nodes");
		int decision = readInt();
		if(decision == 1){
			if(c.isHypothesis(attitudeNumber,contradictions.getFirst().getNode())) {
//				if(isGraded){

//				}else{
					c.get
//				}
			}else{
				//TODO
			}
		}else{
			HashSet<Integer> alreadyHandledNodes = new HashSet<>();
			for(Contradiction cont : contradictions){
				for(Map.Entry<Integer,PropositionNode> entry : cont.getContradictions().getSet().entrySet()){

				}
			}
		}

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
