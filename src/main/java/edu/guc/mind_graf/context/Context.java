package edu.guc.mind_graf.context;

import java.util.*;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.revision.Revision;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;


public class Context {

    private final HashMap<Integer,Pair<PropositionNodeSet,PropositionNodeSet>[]> hypotheses;
    private final String name;
    private Set<Integer, BitSet> AttitudesBitset;


    public Context(String name, Set<String,Integer> attitudeNames){
        this.name=name;
        this.hypotheses = new HashMap<>();
        Pair<PropositionNodeSet, PropositionNodeSet>[] hyps = new Pair[attitudeNames.size()];
        for(int i =0;i<attitudeNames.size();i++){
            hyps[i] = (new Pair<>(new PropositionNodeSet(), new PropositionNodeSet()));
        }
        this.hypotheses.put(0, hyps);
    }
    public Integer getPropositionAttitude(Integer prop) {

        // loop through all the Integer keys of attitudesBitset
        for (Integer key : this.AttitudesBitset.getSet().keySet()) {
            // If the propositionNode is in the attitude then return the name of the
            // attitude
            if (this.AttitudesBitset.get(key).get(prop)) {
                return key;
            }
        }
        throw new RuntimeException("PropositionNode is not in any attitude");
    }

    public String getName() {
        return name;
    }

    public void addHypothesisToContext(int attitudeNumber, PropositionNode node) {
        this.hypotheses.get(0)[attitudeNumber].getFirst().add(node);
        node.getSupport().setHyp(attitudeNumber);
    }

    public void addHypothesisToContext(int attitudeNumber, PropositionNode node, int grade) {
        this.hypotheses.get(grade)[attitudeNumber].getFirst().add(node);
        node.getSupport().setHyp(attitudeNumber);
    }

    public void removeHypothesisFromContext(int attitudeNumber, PropositionNode node) {
        for(Pair<PropositionNodeSet, PropositionNodeSet>[] hypsForThisGrade : this.hypotheses.values()) {
            for (Pair<PropositionNodeSet, PropositionNodeSet> hypsForThisAttitude : hypsForThisGrade) {
                hypsForThisAttitude.getFirst().remove(node);
            }
        }
    }

    public boolean isHypothesis(int attitudeNumber, PropositionNode node){
        for(Pair<PropositionNodeSet, PropositionNodeSet>[] hypsForThisGrade : this.hypotheses.values()) {
            for (Pair<PropositionNodeSet, PropositionNodeSet> hypsForThisAttitude : hypsForThisGrade) {
                if(hypsForThisAttitude.getFirst().contains(node) || hypsForThisAttitude.getSecond().contains(node)){
                    return true;
                }
            }
        }
        return false;
    }

    public Pair<PropositionNodeSet, PropositionNodeSet> getAttitudeProps(int level, int attitudeID){
        return this.hypotheses.get(level)[attitudeID];
    }

    public void completelyRemoveNodeFromContext(int attitudeNumber, PropositionNode node){
        if(this.isHypothesis(attitudeNumber,node)){
            this.removeHypothesisFromContext(attitudeNumber,node);
        }
        this.removeInferredNodeFromContext(attitudeNumber,node);
    }

    public void removeInferredNodeFromContext(int attitudeNumber, PropositionNode node){
//		print("Starting removal of node: "+ node.getId()+" from attitude: "+ ContextController.getAttitudeName(attitudeNumber)+" from Context: "+ c.getName());
        //TODO
//        for(HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> assumptionSupport : node.getSupport().getAssumptionSupport().getFirst().get(attitudeNumber)){
//            for(Map.Entry<Integer,Pair<PropositionNodeSet,PropositionNodeSet>> support: assumptionSupport.entrySet()){
//                if(Revision.supportIsInvalid(this,attitudeNumber,support.getValue().getFirst())) {
//                    continue;
//                }
//                Revision.print("choose node to remove from this support in attitude: "+ support.getKey());
//                ArrayList<Node> supportNodes = new ArrayList<>(support.getValue().getFirst().getNodes());
//                for(int i =0;i<supportNodes.size();i++){
//                    PropositionNode nodeInSupport = (PropositionNode) supportNodes.get(i);
//                    Revision.print((i+1)+". Node: "+ nodeInSupport.toString());
//                }
//                PropositionNode nodeToRemove = (PropositionNode) supportNodes.get(Revision.readInt()-1);
//
//                if(this.isHypothesis(support.getKey(),nodeToRemove)){
//
//                    //TODO: wael change these to remove node completely
//                    this.removeHypothesisFromContext(attitudeNumber,nodeToRemove);
//                }else{
//                    Revision.print("this node is inferred, choose how to remove it:");
//                    this.removeInferredNodeFromContext(attitudeNumber,nodeToRemove);
//                }
//            }
//        }
//		print("successfully removed Node: "+ node+" from attitude: "+ ContextController.getAttitudeName(attitudeNumber)+" from Context: "+ c.getName());
    }
}
