package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;

public class CHB {
    String context;
    public CHB(String context){
        this.context = context;
    } 
    public HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> getMaximalHypotheses(){
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> maxHyp =new HashMap<>();
        Pair<PropositionNodeSet, PropositionNodeSet>[] hyps = new Pair[ContextController.getAttitudes().size()];
        for (int i = 0; i < ContextController.getAttitudes().size(); i++) {
            hyps[i] = (new Pair<>(new PropositionNodeSet(), new PropositionNodeSet()));
        }
        Context currentContext = ContextController.getContext(context);
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> contextHyps = currentContext.getHypotheses();
        for(int level : contextHyps.keySet()){
            for(int attitudeID = 0; attitudeID < contextHyps.get(level).length; attitudeID++){
                PropositionNodeSet originSet = contextHyps.get(level)[attitudeID].getFirst();
                PropositionNodeSet gradedSet = contextHyps.get(level)[attitudeID].getSecond();
                for(int hyp : originSet.getProps()){
                    if(isMaximalHyp(hyp, attitudeID, level)){
                        maxHyp.get(level)[attitudeID].getFirst().add(hyp);
                    }
                }
                for(int hyp : gradedSet.getProps()){
                    if(isMaximalHyp(hyp, attitudeID, level)){
                        maxHyp.get(level)[attitudeID].getSecond().add(hyp);
                    }
                }
            }
        }
        return maxHyp;
    }
    public boolean isMaximalHyp(int nodeID, int attitude, int level){
        boolean isMaxHyp = false;
        PropositionNode node =(PropositionNode) Network.getNodeById(nodeID); 
        Context currentContext = ContextController.getContext(context);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = node.getSupport().getAssumptionBasedSupport().get(level).get(attitude);
        for(Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : supports){
            HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> supportNodesInAttitudes = support.getFirst();
            boolean isValidSupport = true; 
            for(int supportingAttitude : supportNodesInAttitudes.keySet()){
                PropositionNodeSet originSet = supportNodesInAttitudes.get(supportingAttitude).getFirst();
                PropositionNodeSet gradedSet = supportNodesInAttitudes.get(supportingAttitude).getSecond();
                if(currentContext.isInvalidSupport(level, supportingAttitude,  originSet) || currentContext.isInvalidSupport(level, supportingAttitude,  gradedSet)){   
                    isValidSupport = false;
                }
                
            }
            boolean isHypSupport = isHypSupport(attitude, nodeID, support);
            if(isValidSupport && isHypSupport)
                isMaxHyp = true;
            if(isValidSupport && !isHypSupport)
                return false;
        }
        return isMaxHyp;
        
    }
    public  static boolean isHypSupport( int attitudeID, int nodeID, Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support){
        if(support.getFirst().keySet().size() != 1 || !support.getSecond().isEmpty())
            return false;
        Pair<PropositionNodeSet, PropositionNodeSet> supportingNodes = support.getFirst().get(attitudeID);
        if(supportingNodes != null){
            if(supportingNodes.getFirst().size() == 1 && supportingNodes.getSecond().isEmpty() && supportingNodes.getFirst().contains(nodeID))
                return true;
        }
        return false;
    } 
}
