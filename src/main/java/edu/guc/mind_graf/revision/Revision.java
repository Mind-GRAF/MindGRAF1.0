package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;

public class Revision {
	
	public static boolean test1(Context context, int attitudeNo, PropositionNode node){
		PropositionNodeSet nodeSet = context.getAllPropositionsInAnAttitude(attitudeNo);
		System.out.println(nodeSet);
		return true;
	}
}
