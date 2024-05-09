package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Revision {
	public static ArrayList<Contradiction> checkContradiction(Context c, int attitudeNumberOfAddedNode, PropositionNode node) {
		System.out.println("checking contradictions");
		PropositionNode nodeCompliment = (PropositionNode) node.getNegation();
		if(nodeCompliment == null){
			//node complement is not in the network so a contradiction can never happen
			return null;
		}
		System.out.println("found negation"+ nodeCompliment);
		//TODO: wael cache
		ArrayList<ArrayList<Integer>> filteredConsistentAttitudes = filterAttitudes(ContextController.getConsistentAttitudes(),attitudeNumberOfAddedNode);
		ArrayList<Contradiction> contradictions = new ArrayList<>();
		for(ArrayList<Integer> entry : filteredConsistentAttitudes){
			Contradiction cont = new Contradiction(node);
			for(int attitudeNumber : entry){
				//TODO: wael check level
				if(nodeCompliment.supported(c.getName(), attitudeNumber,0)){
					cont.getContradictions().add(attitudeNumber,nodeCompliment);
				}
			}
			if(!cont.getContradictions().isEmpty() && !contradictions.contains(cont)){
				contradictions.add(cont);
			}
		}
		System.out.println("Found Contradictions: "+ contradictions);


		//TODO: wael add a parent method
		return contradictions;
//		if(!contradictions.isEmpty()){
//			if(ContextController.automaticHandlingEnabled()){
//				automaticContradictionHandling(c,attitudeNumberOfAddedNode,contradictions);
//			}else{
//				manualContradictionHandling(c,attitudeNumberOfAddedNode,contradictions);
//			}
//		}

	}

	public static void manualContradictionHandling(Context c, int attitudeNumber, ArrayList<Contradiction> contradictions) {
		print("Contradiction Detected");
		print("Select How to handle this contradiction");
		print("\t1. Remove node:" + contradictions.getFirst().toString());
		print("\t2. Remove contradicting nodes");
		int decision = readInt();
        handleDecision(c,attitudeNumber,contradictions, decision == 1);
		print("completed contradiction handling");
	}

	public static void automaticContradictionHandling(Context c, int attitudeNumber, ArrayList<Contradiction> contradictions) {
		boolean nodeIsHyp = c.isHypothesis(attitudeNumber,contradictions.getFirst().getNode());
		boolean contradictingIsHyp = containsHyp(c,contradictions);

		if(nodeIsHyp && contradictingIsHyp){
			print("Found a contradiction that can't be automatically handled, reverting to manual handling");
			manualContradictionHandling(c,attitudeNumber,contradictions);
		}
		if(nodeIsHyp && !contradictingIsHyp){
			for (Contradiction cont : contradictions) {
				for (Map.Entry<Integer, PropositionNode> entry : cont.getContradictions().getSet().entrySet()) {
					c.removeInferredNodeFromContext(entry.getKey(), entry.getValue());
				}
			}
		}
		if(!nodeIsHyp && contradictingIsHyp){
			c.removeHypothesisFromContext(attitudeNumber, contradictions.getFirst().getNode());
		}else{
			int gradeOfNode = getGradeOfNode(c,attitudeNumber,contradictions.getFirst().getNode());
			int gradeOfContradictions = 0;
			//TODO: wael the way we are merging will half the value if we average, change every thing to a stream and reduce
			for(Contradiction cont: contradictions){
				int gradeForConsistentAttitudes = 0;
				for(Map.Entry<Integer,PropositionNode> entry : cont.getContradictions().getSet().entrySet()){
					//This merges on the level of 2 contradicting nodes in the same consistent attitudes list
					gradeForConsistentAttitudes = ContextController.max(gradeOfContradictions, getGradeOfNode(c,attitudeNumber,entry.getValue()));
				}
				//This merges between grades of consistent attitudes to get the final grade
				gradeOfContradictions = ContextController.max(gradeOfContradictions,gradeForConsistentAttitudes);
			}
			handleDecision(c,attitudeNumber,contradictions, gradeOfNode <= gradeOfContradictions);
			//TODO: wael
		}
	}

	public static void handleDecision(Context c, int attitudeNumber, ArrayList<Contradiction> contradictions, boolean removeNode){
		if (removeNode) {
			c.completelyRemoveNodeFromContext(attitudeNumber, contradictions.getFirst().getNode());
		} else {
			for (Contradiction cont : contradictions) {
				for (Map.Entry<Integer, PropositionNode> entry : cont.getContradictions().getSet().entrySet()) {
					c.completelyRemoveNodeFromContext(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	/**
	 * checks the validity of a node set as a support in a context and an attitude
	 */
	public static boolean supportIsInvalid(Context c, int attitudeNumber, PropositionNodeSet nodes) {
		for(PropositionNode node:nodes){
			//TODO: wael check level
			if(!c.isHypothesis(attitudeNumber,node) && !node.supported(c.getName(),attitudeNumber,0)){
					return true;
			}
		}
	return  false;
	}

	public static ArrayList<ArrayList<Integer>> filterAttitudes(ArrayList<ArrayList<Integer>> consistentAttitudes, int attitudeNumber) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		for(ArrayList<Integer> entry : consistentAttitudes){
			if(entry.contains(attitudeNumber)){
				result.add(entry);
			}
		}
		return result;
	}

	private static boolean containsHyp(Context c, ArrayList<Contradiction> contradictions){
		for(Contradiction contradiction: contradictions){
			for(Map.Entry<Integer, PropositionNode> entry : contradiction.getContradictions().getSet().entrySet()){
				if(c.isHypothesis(entry.getKey(), entry.getValue())){
					return true;
				}
			}
		}
		return false;
	}

	public static int getGradeOfNode(Context c,int attitudeNumber, PropositionNode node){
//		int grade = 0;
//		for(HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> assumptionSupport : node.getSupport().getAssumptionSupport().getFirst().get(attitudeNumber)) {
//			int gradeForThisSupport = 0;
//			for(Map.Entry<Integer,Pair<PropositionNodeSet,PropositionNodeSet>> support: assumptionSupport.entrySet()) {
//				if (supportIsInvalid(c, attitudeNumber, support.getValue().getFirst())) {
//					continue;
//				}
//				gradeForThisSupport = 0;
//				support.getValue().getSecond().getNodes().stream().mapToInt(((Node n)-> (PropositionNode) n).getGradeFromParent())
//				for(PropositionNode supportNode :support.getValue().getSecond()){
//					//This merges grades of nodes in a single support for example: g(g(p,2),3)
//					gradeForThisSupport = ContextController.max(gradeForThisSupport, supportNode.getGradeFromParent());
//				}
//			}
//			//This merges grades between multiple supports
//			grade = ContextController.max(grade,gradeForThisSupport);
//		}
		return 0;
	}

	////////////////////////////////////////////////////

	public static void print(String s){
		System.out.println(s);
	}

	public static int readInt(){
		Scanner sc = new Scanner(System.in);
		int x = sc.nextInt();
		sc.close();
		return x;
	}
}
