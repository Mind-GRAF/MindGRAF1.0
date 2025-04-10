package edu.guc.mind_graf.compression;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.ContextSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.context.*;
public class Trim{
    String context;
    public Trim(String context){
        this.context = context;
    }
    public void setContext(String context){
      this.context = context;
    }
    public void contextTrim(){
      ContextSet contextSet = ContextController.getContextSet();
      HashMap<String, Context> contexts = contextSet.getSet();
      Context currentContext = ContextController.getContext(context);
      HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> currentContextHypotheses = currentContext.getHypotheses();
      Set<Integer> levels = currentContextHypotheses.keySet();
      HashMap<Integer, HashMap<Integer,HashSet<Integer>>> result = new HashMap<>();
      for(Integer level : levels){  
        for(int attitudeID=0 ; attitudeID < (currentContextHypotheses.get(level)).length; attitudeID++){
         int [] originHypothesis = currentContextHypotheses.get(level)[attitudeID].getFirst().getProps();
         for(int j = 0 ; j < originHypothesis.length;j++){
         boolean hasDependents = checkDependents(context, attitudeID, level, originHypothesis[j]);
         if(!hasDependents){
          if(result.get(level)!=null){
            if(result.get(level).get(attitudeID)!=null){
              result.get(level).get(attitudeID).add(originHypothesis[j]);
            }
            else{
              HashSet<Integer> nodesToBeRemoved = new HashSet<>();
              nodesToBeRemoved.add(originHypothesis[j]);
              result.get(level).put(attitudeID,nodesToBeRemoved);
            }
          }
          else{
            HashSet<Integer> nodesToBeRemoved = new HashSet<>();
            nodesToBeRemoved.add(originHypothesis[j]);
            HashMap<Integer,HashSet<Integer>>nodesToBeRemovedAtittudes = new HashMap<>() ;
            nodesToBeRemovedAtittudes.put(attitudeID, nodesToBeRemoved);
            result.put(level, nodesToBeRemovedAtittudes);
          }
         } 
         }
        }
     }
     for(int level:result.keySet()){
      for(int attitudeID :result.get(level).keySet()){
        for(int nodeID :result.get(level).get(attitudeID)){
          PropositionNode nodeToBeRemoved = (PropositionNode) Network.getNodeById(nodeID);
          currentContext.removeHypothesisFromContext(level, attitudeID,nodeToBeRemoved);
          boolean isHypothesisInOtherContext = false;
          for(Context context : contexts.values()){
            isHypothesisInOtherContext = context.isHypothesis(level, attitudeID, nodeToBeRemoved);
            if(isHypothesisInOtherContext){
              break;
            }
          }
          if(!isHypothesisInOtherContext){
            HashMap<Integer, HashSet<Integer>> isHyp = nodeToBeRemoved.getSupport().getIsHyp();
            isHyp.get(level).remove(attitudeID);
            if(isHyp.get(level).isEmpty()){
              isHyp.remove(level);
            }
          }
        }
      }
     }
     
    }
    // check if the node has dependents in the same context means it supports another hypotheses in the same context.
    /**
     * @param context
     * @param attitude
     * @param level
     * @param nodeID
     * @return
     */
    public static boolean checkDependents(String context, int SupportingNodeAttitude, int level, int nodeID){
      PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
      int [] assumptionDependentNodes = node.getAssumptionSupportDependents().getProps();
      Collection<Integer> attitudes = ContextController.getAttitudes().getSet().values();
      for (int i=0 ;i < assumptionDependentNodes.length;i++){
        PropositionNode dependentNode = (PropositionNode) Network.getNodeById(assumptionDependentNodes[i]);
        for(Integer attitude : attitudes){
          boolean isSupported = dependentNode.supported(context,attitude,level);
          if(isSupported){
            ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports= dependentNode.getSupport().getAssumptionBasedSupport().get(level).get(attitude);
            for(Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support:supports){
              PropositionNodeSet originSet= support.getFirst().get(SupportingNodeAttitude).getFirst();
              boolean hasDependents = originSet.contains(nodeID);
              if(hasDependents)
               return true;
            }
          }
  
        }
      }  
      int [] justificationDependentNodes = node.getAssumptionSupportDependents().getProps();
      for (int i=0 ;i < justificationDependentNodes.length;i++){
        PropositionNode dependentNode = (PropositionNode) Network.getNodeById(justificationDependentNodes[i]);
        for(Integer attitude : attitudes){
          boolean isSupported = dependentNode.supported(context,attitude,level);
          if(isSupported){
            ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports= dependentNode.getSupport().getAssumptionBasedSupport().get(level).get(attitude);
            for(Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support:supports){
              PropositionNodeSet originSet= support.getFirst().get(SupportingNodeAttitude).getFirst();
              boolean hasDependents = originSet.contains(nodeID);
              if(hasDependents)
               return true;
            }
          }
  
        }
      }  
    return false; 
    }


}