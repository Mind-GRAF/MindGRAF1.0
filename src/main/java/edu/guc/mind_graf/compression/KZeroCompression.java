package edu.guc.mind_graf.compression;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.Set;

public class KZeroCompression{
    public void kZeroCompress(){
        HashMap<Integer, Node> propositionNodes = Network.getPropositionNodes();
        HashMap<String, Context> contexts = ContextController.getContextSet().getSet();
        HashMap<String, Integer> attitudes = ContextController.getAttitudes().getSet();
        HashSet<Integer> nodesToBeRemoved = new HashSet<>(); 
        for(int nodeID : propositionNodes.keySet()){
            PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
            boolean isHypothesisInAnyContext = false;
            for(Context context : contexts.values()){
              ArrayList<Integer> levels = context.getLevels();
              for(int level : levels){
                for(int attitude : attitudes.values()){
                   if(context.isHypothesis(level, attitude, node)){
                    isHypothesisInAnyContext = true;
                    break;
                   }
                }
                if(isHypothesisInAnyContext)
                    break;
              }
              if(isHypothesisInAnyContext)
                    break;
            }
            if(!isHypothesisInAnyContext){
                nodesToBeRemoved.add(nodeID);
            }
        }
        //remove these nodes completely wael remove hyp with the lowest grade i think to handle contradiction
        // what about removing the node from the network
        //if hyp in contexts and inferred in others
        //ask about network current level
        //
    } 
}