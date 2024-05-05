package context;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import support.Pair;
import nodes.PropositionNode;
import set.PropositionNodeSet;
import set.Set;

public class Context {

    private final Hashtable<Integer, Pair<PropositionNodeSet,PropositionNodeSet>> attitudes;
    //TODO change this to hashmap
    private final String name;
    private Set<Integer, BitSet> AttitudesBitset;


    public Context(String name, Set<String,Integer> attitudeNames){
        this.name=name;
        this.attitudes = new Hashtable<>();
        for(Map.Entry<String,Integer> entry: attitudeNames.getSet().entrySet()){
            Pair<PropositionNodeSet, PropositionNodeSet> p = new Pair<>(new PropositionNodeSet(),new PropositionNodeSet());
            this.attitudes.put(entry.getValue(), p);
        }
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

//    public PropositionNodeSet getAllPropositionsInAnAttitude(int attitude) {
//        return this.attitudes.get(attitude);
//    }

    public void addHypothesisToContext(int attitudeNumber, PropositionNode node) {
        this.attitudes.get(attitudeNumber).getFirst().add(node);
        node.getSupport().setHyp(attitudeNumber);
    }

    public void removeHypothesisFromContext(int attitudeNumber, PropositionNode node) {
        //TODO: wael handle graded
        this.attitudes.get(attitudeNumber).getSecond().remove(node);
    }

    public boolean isHypothesis(int attitudeNumber, PropositionNode node){
        return this.attitudes.get(attitudeNumber).getFirst().contains(node) || this.attitudes.get(attitudeNumber).getSecond().contains(node);
    }

    public Pair<PropositionNodeSet,PropositionNodeSet> getAttitudeProps(int attitudeID){
        return this.attitudes.get(attitudeID);
    }


}
