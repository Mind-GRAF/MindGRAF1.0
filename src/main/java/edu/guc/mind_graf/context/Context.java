package edu.guc.mind_graf.context;

import java.util.BitSet;
import java.util.HashMap;

import edu.guc.mind_graf.nodes.PropositionNode;
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


}
